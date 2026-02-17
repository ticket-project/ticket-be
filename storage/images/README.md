# Image Storage

`storage/images` is the filesystem root served by `/images/**`.

- Upload process: admin manually places files under this directory.
- URL rule: a DB value like `shows/poster-1.jpg` is exposed as `/images/shows/poster-1.jpg`.
- Absolute URLs (`https://...`) are returned as-is.
- Invalid traversal paths (e.g. `../secret.txt`) are ignored and resolved to `null`.
