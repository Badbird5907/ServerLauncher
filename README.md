# ServerLauncher
A launch wrapper for minecraft servers (or any java application, really) that automatically keeps them up-to-date. Now with jenkins and teamcity support!

Download: [Here](https://ci.badbird5907.net/job/ServerLauncher/)

# Usage
Launch this jar as you would launch a normal MC server.

On first launch, it will create a directory: `ServerLauncher`, and config.json inside.
Edit the config as needed, and start the server again.
(on default settings) it should download and launch the paper jar as `server.jar`

Default Config:
```json
{
  "distro": "PAPER",
  "buildNumber": "AUTO",
  "extraLaunchProperties": {},
  "extraLaunchArgs": [],
  "version": "1.19.2",
  "downloadedFileName": "server.jar"
}
```
## WARNING!
Do not set downloadedFileName to the same name as ServerLauncher jar, it will overwrite itself.

# Plugin Downloader
Downloads plugins from either a direct URL, github releases, Jenkins or TeamCity.

## Usage
Open (or create) the file `ServerLauncher/plugin_config.json`.

Default: `[]`

Example: [Here](https://github.com/Badbird5907/ServerLauncher/blob/master/plugin_config.example.json)
