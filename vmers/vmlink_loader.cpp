#include "vmlink_loader.hpp"
#include <iostream>
#include <vector>
#include <cstring>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <fcntl.h>

namespace vmers {

VmContainerLoader::VmContainerLoader(const VmConfig& config)
    : config_(config), prop_service_(config.rootfs_dir) {
}

VmContainerLoader::~VmContainerLoader() {
    StopContainer();
}

bool VmContainerLoader::InitializeFileSystem() {
    std::cout << "[Vmers-Loader] Preparing Guest Rootfs at: " << config_.rootfs_dir << std::endl;
    
    // Create essential guest runtime directories
    const std::vector<std::string> dirs = {
        "/dev",
        "/dev/socket",
        "/dev/pts",
        "/proc",
        "/sys",
        "/tmp",
        "/data",
        "/data/app",
        "/data/data",
        "/data/system",
        "/data/user/0",
        "/data/dalvik-cache",
        "/data/dalvik-cache/arm64",
        "/data/local/tmp",
        "/storage",
        "/storage/emulated/0"
    };

    for (const auto& d : dirs) {
        std::string full_path = config_.rootfs_dir + d;
        mkdir(full_path.c_str(), 0777);
        chmod(full_path.c_str(), 0777);
    }

    // Load build.prop
    std::string prop_file = config_.rootfs_dir + "/system/build.prop";
    prop_service_.LoadPropFile(prop_file);
    
    // Set dynamic runtime properties
    prop_service_.SetProperty("ro.sf.lcd_density", std::to_string(config_.display_dpi));
    prop_service_.SetProperty("persist.sys.timezone", "Asia/Jakarta");
    prop_service_.SetProperty("ro.vmers.running", "1");
    prop_service_.SetProperty("ro.vmers.root", config_.enable_root ? "1" : "0");

    return true;
}

void VmContainerLoader::BuildEnvironment(std::vector<std::string>& env_vars) {
    std::string root = config_.rootfs_dir;
    
    env_vars.push_back("ANDROID_ROOT=/system");
    env_vars.push_back("ANDROID_DATA=/data");
    env_vars.push_back("ANDROID_STORAGE=/storage");
    env_vars.push_back("ANDROID_ART_ROOT=/apex/com.android.art");
    env_vars.push_back("ANDROID_I18N_ROOT=/apex/com.android.i18n");
    env_vars.push_back("ANDROID_TZDATA_ROOT=/apex/com.android.tzdata");
    env_vars.push_back("PATH=/system/bin:/system/xbin:/apex/com.android.art/bin");
    env_vars.push_back("LD_LIBRARY_PATH=/apex/com.android.art/lib64:/system/lib64:/vendor/lib64");
    
    // Modern Android 15 Bootclasspath
    std::string bcp = "BOOTCLASSPATH="
                      "/apex/com.android.art/javalib/core-oj.jar:"
                      "/apex/com.android.art/javalib/core-libart.jar:"
                      "/apex/com.android.art/javalib/okhttp.jar:"
                      "/apex/com.android.art/javalib/bouncycastle.jar:"
                      "/apex/com.android.art/javalib/apache-xml.jar:"
                      "/apex/com.android.i18n/javalib/core-icu4j.jar:"
                      "/system/framework/framework.jar:"
                      "/system/framework/framework-graphics.jar:"
                      "/system/framework/ext.jar:"
                      "/system/framework/telephony-common.jar:"
                      "/system/framework/voip-common.jar:"
                      "/system/framework/ims-common.jar:"
                      "/system/framework/services.jar";
    env_vars.push_back(bcp);

    env_vars.push_back("SYSTEMSERVERCLASSPATH=/system/framework/services.jar:/system/framework/ethernet-service.jar");
}

bool VmContainerLoader::SpawnZygote(const std::vector<std::string>& env_vars) {
    std::string app_process = config_.rootfs_dir + "/system/bin/app_process64";
    if (access(app_process.c_str(), X_OK) != 0) {
        std::cerr << "[Vmers-Loader] app_process64 not found or not executable at: " << app_process << std::endl;
        return false;
    }

    std::cout << "[Vmers-Loader] Forking container process..." << std::endl;
    pid_t pid = fork();
    if (pid < 0) {
        perror("[Vmers-Loader] Fork failed");
        return false;
    }

    if (pid == 0) {
        // Child Process (Container Sandbox)
        // Convert env_vars to char* array
        std::vector<char*> envp;
        for (const auto& e : env_vars) {
            envp.push_back(const_cast<char*>(e.c_str()));
        }
        envp.push_back(nullptr);

        // Change directory to rootfs
        chdir(config_.rootfs_dir.c_str());

        // Arguments for app_process64:
        // app_process64 /system/bin --zygote --start-system-server
        char* argv[] = {
            const_cast<char*>("app_process64"),
            const_cast<char*>("/system/bin"),
            const_cast<char*>("--zygote"),
            const_cast<char*>("--start-system-server"),
            nullptr
        };

        std::cout << "[Vmers-Loader] Executing Zygote & SystemServer via app_process64..." << std::endl;
        execve(app_process.c_str(), argv, envp.data());

        // If execve fails
        perror("[Vmers-Loader] execve failed");
        _exit(127);
    } else {
        // Parent Process (VM Manager monitor)
        guest_pid_ = pid;
        is_running_ = true;
        std::cout << "[Vmers-Loader] Guest Android 15 Container running with PID: " << guest_pid_ << std::endl;
        return true;
    }
}

bool VmContainerLoader::StartContainer() {
    if (is_running_) return true;

    if (!InitializeFileSystem()) {
        std::cerr << "[Vmers-Loader] Filesystem initialization failed." << std::endl;
        return false;
    }

    if (!prop_service_.Start()) {
        std::cerr << "[Vmers-Loader] Property Service failed to start." << std::endl;
        return false;
    }

    std::vector<std::string> env;
    BuildEnvironment(env);

    return SpawnZygote(env);
}

void VmContainerLoader::StopContainer() {
    if (is_running_ && guest_pid_ > 0) {
        std::cout << "[Vmers-Loader] Stopping container PID " << guest_pid_ << "..." << std::endl;
        kill(guest_pid_, SIGTERM);
        usleep(200000);
        kill(guest_pid_, SIGKILL);
        waitpid(guest_pid_, nullptr, WNOHANG);
        guest_pid_ = -1;
    }
    prop_service_.Stop();
    is_running_ = false;
}

} // namespace vmers
