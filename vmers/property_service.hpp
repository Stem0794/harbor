#pragma once

#include <string>
#include <unordered_map>
#include <mutex>
#include <atomic>
#include <vector>

namespace vmers {

struct PropertyMessage {
    uint32_t cmd;
    char name[32];
    char value[92];
};

constexpr uint32_t PROP_MSG_SETPROP = 1;

class PropertyService {
public:
    PropertyService(const std::string& rootfs_path);
    ~PropertyService();

    bool Start();
    void Stop();

    bool LoadPropFile(const std::string& filepath);
    void SetProperty(const std::string& key, const std::string& value);
    std::string GetProperty(const std::string& key, const std::string& default_val = "") const;

private:
    void ServerLoop();
    void HandleClient(int client_fd);

    std::string rootfs_path_;
    std::string socket_path_;
    int server_fd_ = -1;
    std::atomic<bool> running_{false};
    mutable std::mutex prop_mutex_;
    std::unordered_map<std::string, std::string> properties_;
};

} // namespace vmers
