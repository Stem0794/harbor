#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdarg.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>

static const char* g_rootfs = NULL;
static size_t g_rootfs_len = 0;

__attribute__((constructor))
static void init_vmlink_shim() {
    g_rootfs = getenv("VMERS_ROOTFS");
    if (g_rootfs) {
        g_rootfs_len = strlen(g_rootfs);
    }
}

// Helper to translate guest path to sandboxed rootfs path
static int translate_path(const char* orig_path, char* out_buf, size_t out_size) {
    if (!g_rootfs || !orig_path || orig_path[0] != '/') {
        return 0; // No translation needed
    }

    // If already prefixed with rootfs, do nothing
    if (strncmp(orig_path, g_rootfs, g_rootfs_len) == 0) {
        return 0;
    }

    // Intercept standard Android system root paths
    if (strncmp(orig_path, "/system", 7) == 0 ||
        strncmp(orig_path, "/apex", 5) == 0 ||
        strncmp(orig_path, "/vendor", 7) == 0 ||
        strncmp(orig_path, "/product", 8) == 0 ||
        strncmp(orig_path, "/system_ext", 11) == 0 ||
        strncmp(orig_path, "/data", 5) == 0 ||
        strncmp(orig_path, "/dev/socket", 11) == 0) {

        snprintf(out_buf, out_size, "%s%s", g_rootfs, orig_path);
        return 1;
    }

    return 0;
}

// -------------------------------------------------------------
// Hooked Syscalls: open, openat, access, stat, readlink
// -------------------------------------------------------------

typedef int (*real_open_t)(const char*, int, ...);
typedef int (*real_openat_t)(int, const char*, int, ...);
typedef int (*real_access_t)(const char*, int);
typedef int (*real_stat_t)(const char*, struct stat*);
typedef int (*real_lstat_t)(const char*, struct stat*);
typedef int (*real_connect_t)(int, const struct sockaddr*, socklen_t);

extern "C" {

int open(const char* pathname, int flags, ...) {
    static real_open_t real_open = NULL;
    if (!real_open) real_open = (real_open_t)dlsym(RTLD_NEXT, "open");

    char new_path[4096];
    const char* target = pathname;
    if (translate_path(pathname, new_path, sizeof(new_path))) {
        target = new_path;
    }

    va_list args;
    va_start(args, flags);
    mode_t mode = (flags & O_CREAT) ? (mode_t)va_arg(args, int) : 0;
    va_end(args);

    return real_open(target, flags, mode);
}

int openat(int dirfd, const char* pathname, int flags, ...) {
    static real_openat_t real_openat = NULL;
    if (!real_openat) real_openat = (real_openat_t)dlsym(RTLD_NEXT, "openat");

    char new_path[4096];
    const char* target = pathname;
    if (translate_path(pathname, new_path, sizeof(new_path))) {
        target = new_path;
        dirfd = AT_FDCWD;
    }

    va_list args;
    va_start(args, flags);
    mode_t mode = (flags & O_CREAT) ? (mode_t)va_arg(args, int) : 0;
    va_end(args);

    return real_openat(dirfd, target, flags, mode);
}

int access(const char* pathname, int mode) {
    static real_access_t real_access = NULL;
    if (!real_access) real_access = (real_access_t)dlsym(RTLD_NEXT, "access");

    char new_path[4096];
    const char* target = pathname;
    if (translate_path(pathname, new_path, sizeof(new_path))) {
        target = new_path;
    }
    return real_access(target, mode);
}

int stat(const char* pathname, struct stat* statbuf) {
    static real_stat_t real_stat = NULL;
    if (!real_stat) real_stat = (real_stat_t)dlsym(RTLD_NEXT, "stat");

    char new_path[4096];
    const char* target = pathname;
    if (translate_path(pathname, new_path, sizeof(new_path))) {
        target = new_path;
    }
    return real_stat(target, statbuf);
}

int lstat(const char* pathname, struct stat* statbuf) {
    static real_lstat_t real_lstat = NULL;
    if (!real_lstat) real_lstat = (real_lstat_t)dlsym(RTLD_NEXT, "lstat");

    char new_path[4096];
    const char* target = pathname;
    if (translate_path(pathname, new_path, sizeof(new_path))) {
        target = new_path;
    }
    return real_lstat(target, statbuf);
}

// Redirect Unix Domain Sockets (e.g. /dev/socket/property_service)
int connect(int sockfd, const struct sockaddr* addr, socklen_t addrlen) {
    static real_connect_t real_connect = NULL;
    if (!real_connect) real_connect = (real_connect_t)dlsym(RTLD_NEXT, "connect");

    if (addr && addr->sa_family == AF_UNIX) {
        struct sockaddr_un* un = (struct sockaddr_un*)addr;
        char new_path[sizeof(un->sun_path)];
        if (translate_path(un->sun_path, new_path, sizeof(new_path))) {
            struct sockaddr_un redirected;
            memset(&redirected, 0, sizeof(redirected));
            redirected.sun_family = AF_UNIX;
            strncpy(redirected.sun_path, new_path, sizeof(redirected.sun_path) - 1);
            return real_connect(sockfd, (struct sockaddr*)&redirected, sizeof(redirected));
        }
    }

    return real_connect(sockfd, addr, addrlen);
}

} // extern "C"
