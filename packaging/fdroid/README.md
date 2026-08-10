# F-Droid packaging

The production package is `io.github.theodorekonikowski.harbor` and is intended to be built and signed by the official F-Droid repository.

Before submission:

1. Publish the repository at its permanent URL and replace the placeholder in `io.github.theodorekonikowski.harbor.yml`.
2. Tag the audited source revision.
3. Run `fdroid scanner` and `fdroid build --server` against the candidate recipe.
4. Ask F-Droid reviewers whether the optional external Shizuku integration requires `NonFreeDep`; do not conceal or remove the integration from metadata.
5. Verify the update path on an active profile-owner installation.
