# Jirani-Project

Monorepo for the Jirani Android app and optional Rust remote server.

## Projects

- `jirani/` - Android app imported from `https://github.com/ewanyonyi/jirani.git`
- `jirani-rust/` - Optional Rust backend imported from `https://github.com/ewanyonyi/jirani-rust.git`

Both projects were imported with `git subtree` so their commit histories are preserved inside this repository.

## Working With History

View Android app history:

```bash
git log -- jirani
```

View Rust backend history:

```bash
git log -- jirani-rust
```
