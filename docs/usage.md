---
id: Usage Guide
icon: book
order: 90
---

# Usage Guide

### Python Environment

It is necessary to install the following packages in your python environment to run this plugin.

```
python==3.8.8
tobii-research==1.10.1
pyautogui==0.9.53
screeninfo==0.8
```

### Installation

1. Clone the [repository](https://github.com/codegrits/CodeGRITS) to your local machine.
2. Open command line and run `./gradlew build` in the root folder to build the plugin.
3. Open IntelliJ IDEA and click `File` - `Settings` - `Plugins` - `Install Plugin from Disk...` to install the plugin
   zip file in `build/distributions` folder.
4. Restart IntelliJ IDEA to enable the plugin.
5. All CodeGRITS features are available in `Tools` dropdown menu, including `Start Tracking`, `Pause Tracking`,
   and `Configuration`.

### Usage

!!!
Before starting tracking, you should first configure the plugin. Click `Tools` - `CodeGRITS` - `Configuration` to open
the configuration window. To enable eye tracking,
you need to have the necessary Python packages installed in your Python environment. The plugin automatically checks the
Python environment.

We highly recommend
using [Tobii Pro Eye Tracker Manager](https://www.tobii.com/products/software/applications-and-developer-kits/tobii-pro-eye-tracker-manager#downloads)
to conduct calibration before using eye tracking.
!!!

1. Click `Tools` - `CodeGRITS` - `Start Tracking` to start tracking.
2. Click `Tools` - `CodeGRITS` - `Pause Tracking` to pause tracking.
3. Click `Tools` - `CodeGRITS` - `Resume Tracking` to resume tracking.
4. Click `Tools` - `CodeGRITS` - `Stop Tracking` to stop tracking. The plugin will export data in XML format to the
   configured folder.

## Further Development

1. Click `Run` - `Run Plugin` to run the plugin and test it.
2. Open command line and run `./gradlew build` to build the plugin.
3. Get the plugin zip file in `build/distributions` folder.

## Example Output

Here is one example of part of the output data, i.e., the IDE interactions and eye gaze data of a developer.
[Output Format](Output%20Format.md) page provides a complete description of the output data.

<div style="text-align: center;">
    <img src="/docs/imgs/imgs/ide-tracking.png" style="width: 80%;"><br><br>
    <img src="/docs/imgs/imgs/eye-tracking.png" style="width: 80%;"><br><br>
</div>

The source code of CodeGRITS is
also publicly available on [GitHub](https://github.com/codegrits/CodeGRITS).