---
label: Developer Guide
icon: gear
order: 70
---

# Developer Guide

We have provided a [Google Doc](https://docs.google.com/document/d/1_F2sq6U0riRSHU5WK0StQeAnJHSHCKZR9L_EZUeT6RE/edit?usp=sharing) outlining several directions worth exploring for the future development of CodeGRITS.
We encourage the community to contribute to CodeGRITS by submitting GitHub issues and pull requests to develop new features.
Please refer to the document for more details, and feel free to leave comments or suggestions.

For technical details, please refer to [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html).
We also provide the [JavaDoc](docs/index.html) of the source code.

## Supporting New Eye Trackers

If you want to integrate other eye-tracking devices except for Tobii eye-tracking devices, you need to reimplement all
Python scripts in the source code to get the right eye-tracking device information and eye gaze data using your eye
tracker API.

{.compact}
| Location | Method |
|:------------------------------------------:|:------------------------:|
| `/trackers/EyeTracker.java` | `setPythonScriptTobii()` |
| `/utils/AvailabilityChecker.java` | `checkPythonEnvironment(String pythonInterpreter)` |
| `/utils/AvailabilityChecker.java` | `checkEyeTracker(String pythonInterpreter)` |
| `/utils/AvailabilityChecker.java` | `getEyeTrackerName(String pythonInterpreter)` |
| `/utils/AvailabilityChecker.java` | `getFrequencies(String pythonInterpreter)` |

## Real-time Data API

### Overview

We provide a real-time data API for future JetBrains plugin developers and researchers to get real-time data from
IDE tracker and eye tracker separately. The API is based on the [IDE Tracker](#ide-tracker)
and [Eye Tracker](#eye-tracker).

!!! Example Project
We provide an example project [DataStreamReceiver](https://github.com/codegrits/DataStreamReceiver)
that builds on top of the real-time data API. It is designed to receive the IDE and eye tracking data and directly 
visualize them in two separate windows. You could refer to its source code to learn how to use the API.
!!!

### Configuration

Before using the API, you first need to build CodeGRITS from source
(See [Build from Source](usage.md#build-from-source)). Then, find the folder `./build/idea-sandbox/plugins/CodeGRITS`
in the CodeGRITS project, which is the dependency of the API. You need to add it to the `intellij` section
in `build.gradle.kts` file of your plugin project.

```groovy
intellij {
    // the dependency path is like ./build/idea-sandbox/plugins/CodeGRITS
    plugins.set(file("path-to-CodeGRITS-dependency"))
}
```

You also need to add the following to `./src/main/resources/META-INF/plugin.xml`.

```xml
<depends>io.github.codegrits</depends>
```

### Quick Start

To use the API, simply call the `getInstance()` method to get the instance of the IDE Tracker or Eye Tracker. Then, set
the `isRealTimeDataTransmitting` to `true` to enable real-time data transmitting. After that, set
the `ideTrackerDataHandler` or `eyeTrackerDataHandler` to handle the real-time data. Finally, call the `startTracking()`
method to start tracking.

```java
IDETracker ideTracker = IDETracker.getInstance();
ideTracker.setIsRealTimeDataTransmitting(true);
ideTracker.setIdeTrackerDataHandler(element -> {
    String formattedStr = "Event: " + element.getAttribute("id");
    System.out.println(formattedStr);
});
ideTracker.startTracking(currentProject);
```

!!!
`Element` object is an XML element that is imported from `org.w3c.dom.Element` package.
!!!

### IDE Tracker

- `IDETracker.getInstance()`
- `setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting)`
- `setIdeTrackerDataHandler(Consumer<Element> ideTrackerDataHandler)`
- `startTracking(Project project)`

### Eye Tracker

- `EyeTracker.getInstance()`
- `setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting)`
- `setEyeTrackerDataHandler(Consumer<Element> eyeTrackerDataHandler)`
- `startTracking(Project project)`