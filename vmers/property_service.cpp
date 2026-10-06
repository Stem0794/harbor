#include "property_service.hpp"
#include <iostream>
#include <fstream>
#include <sstream>
#include <thread>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/stat.h>

namespace vmers {

PropertyService::PropertyService(const std::string& rootfs_path)
    : rootfs_path_(rootfs_path) {
    socket_path_ = rootfs_path_ + "/dev/socket/property_service";
}

PropertyService::~PropertyService() {
    Stop();
}

bool PropertyService::LoadPropFile(const std::string& filepath) {
    std::ifstream file(filepath);
    if (!file.is_open()) {
        std::cerr << "[Vmers-Property] Failed to open prop file: " << filepath << std::endl;
        return false;
    }

    std::lock_guard<std::mutex> lock(prop_mutex_);
    std::string line;
    while (std::getline(file, line)) {
        if (line.empty() || line[0] == '#') continue;
        size_t eq_pos = line.find('=');
        if (eq_pos != std::string::npos) {
            std::string key = line.substr(0, eq_pos);
            std::string val = line.substr(eq_pos + 1);
            
            // Trim whitespace
            key.erase(0, key.find_first_not_of(" \t\r\n"));
            key.erase(key.find_last_not_of(" \t\r\n") + 1);
            val.erase(0, val.find_first_not_of(" \t\r\n"));
            val.erase(val.find_last_not_of(" \t\r\n") + 1);

            properties_[key] = val;
        }
    }
    std::cout << "[Vmers-Property] Loaded " << properties_.size() << " properties from " << filepath << std::endl;
    return true;
}

void PropertyService::SetProperty(const std::string& key, const std::string& value) {
    std::lock_guard<std::mutex> lock(prop_mutex_);
    properties_[key] = value;
}

std::string PropertyService::GetProperty(const std::string& key, const std::string& default_val) const {
    std::lock_guard<std::mutex> lock(prop_mutex_);
    auto it = properties_.find(key);
    if (it != properties_.end()) {
        return it->second;
    }
    return default_val;
}

bool PropertyService::Start() {
    if (running_) return true;

    // Ensure /dev/socket folder exists
    std::string socket_dir = rootfs_path_ + "/dev/socket";
    mkdir((rootfs_path_ + "/dev").c_str(), 0755);
    mkdir(socket_dir.c_str(), 0755);
    unlink(socket_path_.c_str());

    server_fd_ = socket(AF_UNIX, SOCK_STREAM, 0);
    if (server_fd_ < 0) {
        perror("[Vmers-Property] socket creation failed");
        return false;
    }

    struct sockaddr_un addr{};
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, socket_path_.c_str(), sizeof(addr.sun_path) - 1);

    if (bind(server_fd_, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("[Vmers-Property] bind failed");
        close(server_fd_);
        server_fd_ = -1;
        return false;
    }

    chmod(socket_path_.c_str(), 0666);

    if (listen(server_fd_, 16) < 0) {
        perror("[Vmers-Property] listen failed");
        close(server_fd_);
        server_fd_ = -1;
        return false;
    }

    running_ = true;
    std::thread([this]() { ServerLoop(); }).detach();
    std::cout << "[Vmers-Property] Property Service Daemon active on " << socket_path_ << std::endl;
    return true;
}

void PropertyService::Stop() {
    running_ = false;
    if (server_fd_ >= 0) {
        close(server_fd_);
        server_fd_ = -1;
    }
    unlink(socket_path_.c_str());
}

void PropertyService::ServerLoop() {
    while (running_) {
        int client_fd = accept(server_fd_, nullptr, nullptr);
        if (client_fd < 0) {
            if (!running_) break;
            continue;
        }
        std::thread([this, client_fd]() { HandleClient(client_fd); }).detach();
    }
}

void PropertyService::HandleClient(int client_fd) {
    PropertyMessage msg{};
    ssize_t bytes = read(client_fd, &msg, sizeof(msg));
    if (bytes >= static_cast<ssize_t>(sizeof(msg.cmd))) {
        if (msg.cmd == PROP_MSG_SETPROP) {
            msg.name[sizeof(msg.name) - 1] = '\0';
            msg.value[sizeof(msg.value) - 1] = '\0';
            SetProperty(msg.name, msg.value);
            std::cout << "[Vmers-Property] SET: " << msg.name << " = " << msg.value << std::endl;
            uint32_t result = 0; // Success
            write(client_fd, &result, sizeof(result));
        }
    }
    close(client_fd);
}

} // namespace vmers
