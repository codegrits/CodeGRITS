---
id: Usage Guide
icon: book
order: 90
---

# Usage Guide

## Environment Requirements

### Eye-tracking Device

CodeGRITS is built on top
of [Tobii Pro SDK](https://www.tobii.com/products/software/applications-and-developer-kits/tobii-pro-sdk/), and is
expected to be compatible with all [Tobii eye-tracking devices](https://www.tobii.com/)
(see [Supported Eye Trackers](https://developer.tobiipro.com/tobiiprosdk/supportedeyetrackers.html) for more details).
However, we have only tested CodeGRITS
with [Tobii Pro Fusion](https://www.tobii.com/products/eye-trackers/screen-based/tobii-pro-fusion) since we do not have
access to other eye-tracking devices. If you want to further develop CodeGRITS for your own eye-tracking device,
please refer to the [Developer Guide](developer.md) for more details, and also feel free to contact us if you need
further assistance.

You could also use CodeGRITS without an eye-tracking device. Since CodeGRITS provides mouse simulation as a substitute
for eye gaze. You could also uncheck the `Eye Tracking` option in the configuration window to disable eye tracking.

### IDE Compatibility

CodeGRITS is expected to be compatible with the entire family of [JetBrains IDEs](https://www.jetbrains.com/), including
IntelliJ IDEA, PyCharm, Clion, etc. Due to the limited time, we did not specifically test CodeGRITS thoroughly on all
of them. We provide a list of JetBrains IDEs that we have tested CodeGRITS on.

{.compact}
| JetBrains IDEs | Version |
|:--------------:|:---------------:|
| IntelliJ IDEA | 2022.2 - 2023.3 |
| PyCharm | 2022.2 - 2023.3 |
| Clion | 2022.2 - 2023.3 |
| PhpStorm | 2022.2 - 2023.3 |

Since CodeGRITS is still in its early developmental stage, even though we believe it would work well, some minor
issues may still exist. If you encounter any of them, please feel free to contact us or create
a [GitHub Issue](https://github.com/codegrits/CodeGRITS/issues).

### Python Environment

Since [Tobii Pro SDK](https://developer.tobiipro.com/tobiiprosdk.html) did not provide a Java API, we have to use
the [Python API](https://developer.tobiipro.com/python.html) to collect eye gaze data. Thus, it is
necessary to install the following packages in your Python environment to run this plugin (minor version differences
should be fine).

```
python==3.8.8
tobii-research==1.10.1
pyautogui==0.9.53
screeninfo==0.8
```

Refer to [Supported platforms and languages](https://developer.tobiipro.com/tobiiprosdk/platform-and-language.html),
Tobii Pro SDK only supports Python 3.8 and Python 3.10. Regarding operating systems, Tobii Pro SDK supports Windows
10 and 11 (64-bit), macOS 10.15 and 12, and Ubuntu 20.04 LTS.

## Installation

### Get the Plugin

#### Direct Download

We provide the direct download link of the plugin zip file for the following JetBrains IDEs for convenience.

{.compact}
| JetBrains IDEs | Version | Download Link |
|:--------------:|:---------------:|:-------------:|
| IntelliJ IDEA | 2022.2 - 2023.3 | [Download](https://drive.google.com/file/d/1Ineo7c6UEjJVMJD_cyk5YkpxnW3CjRRB/view?usp=drive_link)  |
| PyCharm | 2022.2 - 2023.3 | [Download](https://drive.google.com/file/d/1IEkFM9S8YsN_Uo7No6mFYZ-DkCqnQRly/view?usp=drive_link)  |

#### Build from Source

We also encourage you to build the plugin from the source code, especially for the IDEs that are not listed above.

1. Clone the [repository](https://github.com/codegrits/CodeGRITS) to your local machine.
2. Set the IDE type and version in `build.gradle.kts` file. For example, if you want to build the plugin for IntelliJ
   Community Edition between 2022.2 and
   2023.3, you should set the following in `build.gradle.kts` file.
   ```groovy #2,6-7
   intellij {
       type = "IC"
   }
   tasks {
       patchPluginXml {
           sinceBuild.set("222")
           untilBuild.set("233.*")
       }
   }
   ```
   Please refer to
   the [Gradle IntelliJ Plugin - Configuration](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#configuration)
   and [Plugins Targeting IntelliJ Platform-Based IDEs](https://plugins.jetbrains.com/docs/intellij/dev-alternate-products.html)
   for more details.
3. Open the command line and run `./gradlew build` in the root folder to build the plugin.
4. Find the plugin zip file in the `build/distributions` folder.

### Install the Plugin

1. Open the JetBrains IDE, click `File` - `Settings` - `Plugins` - `Install Plugin from Disk...` to install the plugin
   zip file.
2. Restart IntelliJ IDEA to enable the plugin, then all CodeGRITS features are available in the `Tools` dropdown menu,
   including `Start/Stop Tracking`, `Pause/Resume Tracking`, and `Configuration`.

<div style="text-align: center;">
    <img src="../imgs/toolbar.png" style="width: 16%;">
</div>

## Usage

### Configuration

Before starting tracking, you should first configure the plugin. Click `Tools` - `Configuration` to open
the configuration window. The configuration settings are stored in the `config.json` file in the `bin/` folder of
your installed JetBrains IDE.

<div style="text-align: center;">
    <img src="../imgs/config.png" style="width: 40%;">
</div>

#### Functionalities

You can select the trackers which you want to use, including IDE Tracker, Eye Tracker, and Screen Recorder. If a
compatible eye-tracking device is not available, CodeGRITS would use the mouse cursor as a substitute for eye gaze data.

#### Settings

You can configure the following settings:

- The Python interpreter path that is used for Eye Tracker;
- The output directory for the collected data, defaults to the root folder of your project;
- The sample frequency of Eye Tracker. The range depends on the eye-tracking device;
- The eye-tracking device to use. The mouse is also available as a substitute.

To enable eye tracking, you need to have the necessary Python packages installed in your
Python environment. The plugin automatically checks if the required packages are installed.

#### Preset Labels

You are able to pre-set some labels here which could be used to mark the developers' semantic activities that cannot
be captured by explicit IDE interactions. You can add the labels by right-clicking during tracking. The label is also
recorded in the output data via IDE Tracker.

<div style="text-align: center;">
    <img src="../imgs/add-label.png" style="width: 40%;">
</div><br>

!!!info Buttons
After configuring the plugin, you can start tracking by clicking `Tools` - `Start Tracking`. You can also pause,
or resume tracking by clicking `Tools` - `Pause Tracking` or `Tools` - `Resume Tracking`. CodeGRITS will not collect
any data when it is paused. You can stop tracking by clicking `Tools` - `Stop Tracking`. The plugin will export data
in XML format to the configured folder.
!!!

### Trackers

#### IDE Tracker

IDE Tracker could track a wide range of IDE interactions including all but not limited to the following.

<div style="text-align: center;">
    <img src="../imgs/ide-tracker.png" style="width: 50%;"><br><br>
</div>

A **real-time archive mechanism** is also implemented to archive the whole code files when they
are changed, and the console output during the development process. Below is one example of data collected by IDE
Tracker. See [Data Format](data.md) for more details.

<div style="text-align: center;">
    <img src="../imgs/ide-data.png" style="width: 70%;"><br><br>
</div>

#### Eye Tracker

The workflow of Eye Tracker is divided into three steps:

(1) Connect to the eye-tracking device and receive raw data,
which includes the coordinates of the eye gaze points, pupil diameters of both eyes and their validity;
If a compatible eye-tracking device is not available, CodeGRITS will use the mouse cursor as a substitute.

(2) Map the coordinates of raw gazes within the text editor to specific locations in the code (i.e., file path, line and
column number);
<div style="text-align: center;">
    <img src="../imgs/eye-tracker.png" style="width: 50%;"><br><br>
</div>
(3) Infer the source code tokens that each gaze point is focusing on, as well as perform a bottom-up
   process to traverse the AST structures of the tokens.
<div style="text-align: center;">
    <img src="../imgs/ast.png" style="width: 50%;"><br><br>
</div>

Below is one example of the data collected by Eye Tracker. See [Data Format](data.md) for more details.
<div style="text-align: center;">
    <img src="../imgs/eye-data.png" style="width: 70%;"><br><br>
</div>

!!!
CodeGRITS does not provide calibration for eye tracking.
We highly recommend
using [Tobii Pro Eye Tracker Manager](https://www.tobii.com/products/software/applications-and-developer-kits/tobii-pro-eye-tracker-manager#downloads)
to conduct calibration before using eye tracking.
!!!

#### Screen Recorder

Screen Recorder captures everything on the screen and saves the capture to a video. It also records the timestamp of
each frame, which can be used to synchronize the screen recording with other tracking data to facilitate analysis.
See [Data Format](data.md) for more details.

!!!
When using CodeGRITS's Screen Recorder, it is recommended to limit the setup to a single monitor. In cases where dual
monitors are used, please position the IDE on the primary screen.
!!!