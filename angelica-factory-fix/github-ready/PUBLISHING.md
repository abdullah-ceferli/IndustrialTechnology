# Uploading to GitHub

Suggested repository name: `Angelica-HBM-Render-Fix`.

Suggested description: `Unofficial experimental HBM factory visibility workaround for Angelica 2.2.36 / Minecraft 1.7.10. Original Angelica by GTNH and its contributors.`

Upload the contents of this clean source folder as the repository root. With GitHub Desktop, create a new local repository from this folder and publish it to your account. Alternatively, create an empty GitHub repository and run the following here, replacing the URL with your own:

```text
git init -b main
git add .
git status --short
git commit -m "Add experimental Angelica HBM render compatibility patch"
git remote add origin https://github.com/YOUR-USERNAME/Angelica-HBM-Render-Fix.git
git push -u origin main
```

If you have already initialized the repository, skip `git init`. Keep the original license notices and credits. Do not describe the patch as an official Angelica release or a confirmed fix for every FPS problem.

## Optional binary release

After uploading the source, create a GitHub release named **Angelica 2.2.36 — unofficial HBM patch 3 (experimental)** and mark it **Pre-release**. Use the text in [hbm-patch/RELEASE_NOTES.md](hbm-patch/RELEASE_NOTES.md).

Attach these prepared local files from `dist/`:

- `angelica-2.2.36-personal-hbm3.jar`
- `Angelica-HBM-Render-Fix-source.zip`
- `SHA256SUMS.txt`

The source archive contains this source tree, notices, and build instructions. Make the matching source available alongside the binary and keep the upstream attribution. GitHub's automatically generated source archive will also contain the committed source.

The `.gitignore` excludes `.deps/`, `build/`, `dist/`, personal installation records, game settings, saves, logs, backups, and compiled output. Release assets belong in GitHub Releases; there is no need to commit the binary JARs. Generated folders are also absent from the prepared source ZIP.
