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
- Managed-profile setup is fail-closed: Harbor checks Android's public provisioning capability before showing setup as available and re-checks immediately before launching the system provisioning flow.
- Full-user creation requires explicit confirmation. Full-user deletion is not implemented.
- Root-backed Shizuku receives the same operation allowlist as ADB-backed Shizuku.
- Disabling Advanced tools clears the local opt-in and releases Harbor's Shizuku UserService without changing global Shizuku permission.
- APK sideloading remains Android-mediated: Harbor only clears its profile-local unknown-source restriction after the user requests it, while Android asks the source app for separate consent. Harbor does not silently approve any installer.
- Cross-profile file sharing is directional: Harbor clears `DISALLOW_SHARE_INTO_MANAGED_PROFILE` and installs parent-to-managed `ACTION_SEND`/`ACTION_SEND_MULTIPLE` filters. File selection occurs in Personal Harbor or another personal app; the work-profile Harbor share receiver is disabled in Personal, accepts only granted `content://` URIs in Harbor's owned work profile, and copies them into `Downloads/Harbor`. Because the filters are generic Android share filters, other compatible work-profile apps may also be offered as recipients; choosing one of those apps sends it the selected file, and Harbor warns the user before enabling this flow. It never deletes the personal original or exposes a work-to-personal export target.
- Pinned launch shortcuts are created by the Harbor instance inside the work profile. Each shortcut stores an opaque UUID mapped to a validated local package name and signer fingerprint set; the exported entry activity accepts no package or command arguments and refuses to unhide a package whose signing identity changed.
- Workspace aliases and icon choices are local Harbor metadata. They are reconciled against a fresh user ID/name pair, and stale records are not silently applied to a reused Android user ID.

## Residual risks

- OEM package-manager commands can behave differently or expose undocumented failures.
- Android or an OEM can refuse managed-profile provisioning because of an existing device-management state; Harbor reports that state but does not bypass it.
- OEM user-list formats that Harbor cannot fully parse disable privileged user/profile operations until explicitly supported.
- Harbor's conservative clone resolver can reject a legitimate work profile when any additional profile is visible because supported APIs do not expose a reliable parent mapping to the ordinary app.
- A compromised Shizuku service already has privileges outside Harbor's control.
- Users who enable personal-to-work file sharing intentionally weaken the isolation boundary for the files they send; the Harbor receiver copies only explicitly shared, granted content URIs into the work profile. Android/OEM file managers can still reject or reinterpret their proprietary “move” operation, so Harbor describes the supported Share flow instead.
- `QUERY_ALL_PACKAGES` exposes local package inventory to Harbor; the inventory remains on-device.
- Removing a work profile or full Android user outside Harbor is destructive.
- A launcher may retain a stale pinned shortcut after an app is removed or a work profile is paused; Harbor invalidates the UUID mapping or reports the local policy failure and does not launch a package whose signer no longer matches the pinned identity.
