# Unexpected Keyboard pong (fork)

Upstream Unexpected Keyboard with a "Grey Console" theme (the bandit-nix shell
palette) and 0xProto labels, as `juloo.keyboard2.pong` next to F-Droid's
`juloo.keyboard2`. Design and plan: `docs/superpowers/`.

- Build: every push to `grey-console` runs `.github/workflows/pong-release.yml`,
  which tests, signs with the `ukpong` key (GitHub secrets; backup in Bitwarden,
  note "Unexpected Keyboard pong signing key") and uploads `uk-pong-release`.
- Update to a new upstream tag: `git fetch upstream --tags`, `git merge <tag>` on
  `grey-console`, push, then install the new artifact over the old app (same key,
  so settings stay).
