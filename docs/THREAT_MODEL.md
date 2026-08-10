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
- Package and user identifiers are typed and validated. Public-API profile discovery does not manufacture numeric user IDs; privileged targets come from fresh Shizuku system output.
- Privileged commands use fixed executables and argument arrays, never a shell command string.
- Command output is always drained to EOF so child processes cannot block after Harbor reaches its capture limit; only a bounded prefix is retained.
- Partially recognized user-list output cannot authorize a privileged operation.
- Package cloning requires an unambiguous current full-user/active managed-profile pair. Additional profiles disable cloning instead of being guessed as a parent or target.
- Harbor and recovery-critical system packages cannot be frozen; system-app freezing is disabled in the MVP UI.
- Full-user creation requires explicit confirmation. Full-user deletion is not implemented.
- Root-backed Shizuku receives the same operation allowlist as ADB-backed Shizuku.
- Disabling Advanced tools clears the local opt-in and releases Harbor's Shizuku UserService without changing global Shizuku permission.
- APK sideloading remains Android-mediated: Harbor only clears its profile-local unknown-source restriction after the user requests it, while Android asks the source app for separate consent. Harbor does not silently approve any installer.
- Cross-profile file sharing is directional: Harbor clears `DISALLOW_SHARE_INTO_MANAGED_PROFILE` and installs only parent-to-managed `ACTION_SEND`/`ACTION_SEND_MULTIPLE` filters. Work-to-personal sharing remains under Android's default isolation policy.
- Pinned launch shortcuts are created by the Harbor instance inside the work profile. Each shortcut stores an opaque UUID mapped to a validated local package name; the exported entry activity accepts no package or command arguments.
- Workspace aliases and icon choices are local Harbor metadata. They are reconciled against a fresh user ID/name pair, and stale records are not silently applied to a reused Android user ID.

## Residual risks

- OEM package-manager commands can behave differently or expose undocumented failures.
- OEM user-list formats that Harbor cannot fully parse disable privileged user/profile operations until explicitly supported.
- Harbor's conservative clone resolver can reject a legitimate work profile when any additional profile is visible because supported APIs do not expose a reliable parent mapping to the ordinary app.
- A compromised Shizuku service already has privileges outside Harbor's control.
- Users who enable personal-to-work file sharing intentionally weaken the isolation boundary for the files they send; Android/OEM file managers can still reject or reinterpret the move operation.
- `QUERY_ALL_PACKAGES` exposes local package inventory to Harbor; the inventory remains on-device.
- Removing a work profile or full Android user outside Harbor is destructive.
- A launcher may retain a stale pinned shortcut after an app is removed or a work profile is paused; Harbor invalidates the UUID mapping or reports the local policy failure and does not launch an unverified package.
