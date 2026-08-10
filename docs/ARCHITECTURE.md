# Architecture

## Decisions

Harbor uses one APK in the personal and managed profiles. The personal instance provisions and reports status; the profile-owner instance performs policy operations locally. The core does not perform cross-profile RPC.

The DPC identity consists of the production application ID and `HarborDeviceAdminReceiver` component. Both are release compatibility contracts and must not change after public beta.

The `core:policy` module contains only public Android policy APIs. Optional elevated behavior is isolated in `privileged:shizuku`, whose narrow AIDL interface exposes diagnostics, current-user and user-list queries, package installation for a known user, full-user creation, Harbor installation, and user switching. It exposes no arbitrary command execution. User targets are checked against a fresh privileged listing before each privileged operation, and the UserService is removed when Advanced tools close, the user disables them, or permission is no longer available.

Public-API topology deliberately carries no numeric Android user IDs. Numeric IDs used by privileged commands originate only in Shizuku command output. Package cloning is available only when a fresh listing identifies the current switchable full user and exactly one active managed profile. Harbor cannot obtain a supported parent-ID mapping from the ordinary application APIs, so additional profiles make the relationship ambiguous and cloning fails closed.

## Multiple workspaces

Android-compatible devices guarantee one managed profile per parent user. Harbor therefore models multiple workspaces as multiple full Android users, each of which may provision one standard work profile. The primary instance can list and switch users through Shizuku, but cannot claim knowledge of remote profile-owner state.

## State

Application inventory is queried locally and is not persisted or transmitted. DataStore contains only presentation choices, currently whether the user acknowledged and enabled Advanced tools.

## Rejected Island mechanisms

- `sharedUserId`
- Firebase, analytics, crash upload, or remote configuration
- root setup and direct `su`
- Shizuku hidden-API Binder proxies
- protected cross-user permissions requested by Harbor
- app-ops reflection
- serialized closures or URI-grant cross-profile RPC
- automated `dpm set-profile-owner`
- modification of system user/profile configuration
