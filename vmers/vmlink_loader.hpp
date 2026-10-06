#pragma once

#include <string>
#include <vector>
#include "property_service.hpp"

namespace vmers {

struct VmConfig {
    std::string vm_id = "vm0";
    std::string rootfs_dir;
    std::string data_dir;
    int display_width = 1080;
    int display_height = 2400;
    int display_dpi = 420;
    bool enable_root = true;
    bool enable_gles_hw = true;
};

class VmContainerLoader {
public:
    VmContainerLoader(const VmConfig& config);
    ~VmContainerLoader();

    bool InitializeFileSystem();
    bool StartContainer();
    void StopContainer();

    int GetGuestPid() const { return guest_pid_; }

private:
    void BuildEnvironment(std::vector<std::string>& env_vars);
    bool SpawnZygote(const std::vector<std::string>& env_vars);

    VmConfig config_;
    PropertyService prop_service_;
    int guest_pid_ = -1;
    bool is_running_ = false;
};

} // namespace vmers
