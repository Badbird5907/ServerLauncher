# ServerLauncher
A launch wrapper for minecraft servers that automatically keeps them up-to-date. Now with jenkins and teamcity support!

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
Do not set downloadedFileName to the same name as ServerLauncher, it will cause overwrite itself.
