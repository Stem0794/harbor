CXX = g++
CXX_ARM64 = aarch64-linux-gnu-g++
CXXFLAGS = -std=c++20 -O2 -Wall -pthread

SOURCES = main.cpp property_service.cpp vmlink_loader.cpp
SHIM_SOURCES = vmlink_shim.cpp
TARGET_HOST = vmers_engine_host
TARGET_ARM64 = vmers_engine_arm64
SHIM_HOST = libvmlink_shim_host.so
SHIM_ARM64 = libvmlink_shim_arm64.so

all: host arm64

host: $(SOURCES) $(SHIM_SOURCES)
	$(CXX) $(CXXFLAGS) $(SOURCES) -o $(TARGET_HOST)
	$(CXX) $(CXXFLAGS) -fPIC -shared $(SHIM_SOURCES) -ldl -o $(SHIM_HOST)
	@echo "[+] Built Host Engine & Shim Library"

arm64: $(SOURCES) $(SHIM_SOURCES)
	$(CXX_ARM64) $(CXXFLAGS) -static-libstdc++ -static-libgcc $(SOURCES) -o $(TARGET_ARM64)
	$(CXX_ARM64) $(CXXFLAGS) -fPIC -shared $(SHIM_SOURCES) -ldl -o $(SHIM_ARM64)
	@echo "[+] Built ARM64 Engine & Shim Library"

clean:
	rm -f $(TARGET_HOST) $(TARGET_ARM64) $(SHIM_HOST) $(SHIM_ARM64)
