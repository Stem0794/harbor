#include <iostream>
#include <csignal>
#include <unistd.h>
#include "vmlink_loader.hpp"

std::atomic<bool> g_shutdown{false};

void signal_handler(int sig) {
    std::cout << "\n[Vmers] Caught signal " << sig << ", initiating graceful shutdown..." << std::endl;
    g_shutdown = true;
}

int main(int argc, char* argv[]) {
    std::cout << "==================================================" << std::endl;
    std::cout << "        Vmers Virtual Container Engine v1.0       " << std::endl;
    std::cout << "      Target OS: AOSP Android 15 (ARM64-v8a)      " << std::endl;
    std::cout << "==================================================" << std::endl;

    signal(SIGINT, signal_handler);
    signal(SIGTERM, signal_handler);

    vmers::VmConfig config;
    if (argc > 1) {
        config.rootfs_dir = argv[1];
    } else {
        config.rootfs_dir = "/data/data/com.vmers.app/vm/vm0/fs";
    }

    std::cout << "[Vmers] Initializing VM with rootfs: " << config.rootfs_dir << std::endl;

    vmers::VmContainerLoader loader(config);
    if (!loader.StartContainer()) {
        std::cerr << "[Vmers] Failed to boot virtual container." << std::endl;
        return 1;
    }

    std::cout << "[Vmers] VM Container is online! Press Ctrl+C to stop." << std::endl;

    while (!g_shutdown) {
        sleep(1);
    }

    loader.StopContainer();
    std::cout << "[Vmers] VM Container halted successfully." << std::endl;
    return 0;
}
