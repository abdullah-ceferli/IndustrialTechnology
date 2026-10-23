# GitHub upload

Use the contents of **github-ready/** as your GitHub repository root.

Start with `github-ready/README.md` and `github-ready/PUBLISHING.md`.

The folder includes the patched source, portable build/test scripts, original Angelica credits and licenses, and a .gitignore. Local logs, settings and backups stay outside the public project.

Prepared release assets are in `github-ready/dist/`: the patched JAR, matching source ZIP, and SHA256SUMS.txt. Upload those as release attachments if you want to share a binary. The source ZIP is also a clean copy of the source project.

This is an unofficial experimental compatibility patch. The exact original factory flicker has not been confirmed fixed in a controlled comparison.
