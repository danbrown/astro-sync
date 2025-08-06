<p align="center" width="100%">
     <img src="https://github.com/danbrown/astro-sync/assets/42703631/6f71effc-3b39-4c54-a611-6fc897a51618" alt="astro-sync">
</p>

<div align="center">
  <h1>AstroSync Mod</h1>
  <img src="https://img.shields.io/static/v1?label=Version&message=1.20.1&color=green&style=for-the-badge&logo=ghost"/>
</div>

Astro Sync is a Minecraft mod that facilitates automatic updating of your modpack's folders directly from a GitHub repository during the modpack's startup. This mod is ideal for players and modpack developers who want to ensure that all users always have the latest version of the necessary files without having to manually download updates.

## Features

- **Automatic Update:** Automatically checks and downloads updates from a specified GitHub repository during the modpack's startup.
- **File Synchronization:** Ensures that all specified folders and files from the repository are always up to date.
- **Easy Configuration:** Simple configuration through a settings file where you can define the GitHub repository and other synchronization options.
- **Compatibility:** Works with any Minecraft-based modpack.

## How to Use

1. **Installation:**
   - Download the latest version of the Astro Sync mod and place it in the `mods` folder of your modpack.

2. **Configuration:**
   - On the first run, the mod will generate a configuration file (`astrosync-common.toml`) in your modpack's config folder.
   - Edit this file to add the GitHub repository you want to sync. Example:
     ```toml
     ["Common configs for Astro Sync mod"]
     # The owner of the github repo to check for updates
     RepoOwner = ""
     # The name of the github repo to check for updates
     RepoName = ""
     ```
     - `RepoOwner`: The GitHub repository owner's name.
     - `RepoName`: The name of the repository from which the `.zip` will be downloaded.

3. **Initialization:**
   - Start Minecraft with the modpack and Astro Sync installed.
   - The mod will check the specified repository and sync the folders and files as needed.

## Requirements

- Minecraft 1.20.1.
- Forge Mod Loader.
- KotlinForForge 4.11.0.

## Contributions

If you want to contribute to the development of Astro Sync, follow these steps:

1. Fork this repository.
2. Create a new branch for your changes: `git checkout -b my-branch`.
3. Commit your changes: `git commit -m 'Added a new feature'`.
4. Push to the original branch: `git push origin my-branch`.
5. Open a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.

## Support

For support, please open an issue in the repository or contact the project maintainers directly.

---

Developed by Dan Brown and Vitor Batista for the Minecraft community.
