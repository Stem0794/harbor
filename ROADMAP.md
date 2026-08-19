# Harbor Roadmap

This roadmap outlines Harbor's current development priorities. It is directional and may change as Android platform behavior, compatibility requirements, and user feedback evolve.

## Beta

### Cross-profile app access

- Add explicit per-app controls for supported cross-profile communication.
- Use standard Android profile-owner APIs.
- Keep cross-profile access disabled by default.
- Preserve Android's user-consent flow and Harbor's isolation-first model.

### Compatibility hardening

- Improve managed-profile provisioning and recovery across supported Android versions.
- Expand testing across major OEM Android variants.
- Harden behavior across reboots, profile restarts, and application upgrades.

### App-management polish

- Improve freeze and unfreeze reliability.
- Refine app launch, details, uninstall, and shortcut behavior.
- Improve error handling and state reporting for managed apps.

## Later

- Improve secondary-user and multi-workspace support.
- Refine package cloning and installation workflows.
- Expand advanced operations where Android's standard APIs are insufficient.
- Broaden cross-profile compatibility while keeping access explicit and narrowly scoped.

## Principles

Harbor will continue to prefer standard Android APIs, explicit user control, minimal privileges, and conservative cross-profile behavior. Advanced mechanisms should remain optional and narrowly scoped.
