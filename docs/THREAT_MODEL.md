# Threat model

## Protected assets

- Work-profile isolation and user data.
- Profile-owner authority.
- Package inventory and user topology.
- Availability of Settings, launcher, installer, permission controller, and Harbor itself.

## Trust boundaries

- Normal Harbor process: application UID, local profile only.
- Device Policy Manager: profile-owner operations in the managed profile.
- Shizuku UserService: ADB shell or root identity, opt-in and independently revocable.
- Android/OEM provisioning, Settings, launcher, and package manager.

## Controls

- Core behavior never requires Shizuku.
- No network permission or automated diagnostic upload.
- Harbor exports its launcher activity and permission-guarded DPC receiver. The Shizuku provider is exported with `INTERACT_ACROSS_USERS_FULL` as its caller permission, and AndroidX's generated profile-installer receiver is guarded by `DUMP`.
- The DPC receiver requires `BIND_DEVICE_ADMIN`.
- Package and user identifiers are typed and validated.
- Privileged commands use fixed executables and argument arrays, never a shell command string.
- Output is bounded and errors are returned to the user.
- Harbor and recovery-critical system packages cannot be frozen; system-app freezing is disabled in the MVP UI.
- Full-user creation requires explicit confirmation. Full-user deletion is not implemented.
- Root-backed Shizuku receives the same operation allowlist as ADB-backed Shizuku.

## Residual risks

- OEM package-manager commands can behave differently or expose undocumented failures.
- A compromised Shizuku service already has privileges outside Harbor's control.
- `QUERY_ALL_PACKAGES` exposes local package inventory to Harbor; the inventory remains on-device.
- Removing a work profile or full Android user outside Harbor is destructive.
