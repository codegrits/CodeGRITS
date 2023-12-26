# Real-time Data API Reference

## Overview
We provide a real-time data API for future plugin developers and researchers to get the real-time data from IDE tracker and eye tracker separately. The API is based on the [IDE Tracker](#ide-tracker) and [Eye Tracker](#eye-tracker).

## Configuration
Before using the API, you first need to add the following dependency to the `intellij` section in `build.gradle.kts` file.

```groovy
intellij {
    plugins.set(file("path-to-CodeGRITS-project\\build\\idea-sandbox\\plugins\\CodeGRITS"))
}
```

## Quick Start
To use the API, simply call the `getInstance()` method to get the instance of the IDE Tracker or Eye Tracker. Then, set the `isRealTimeDataTransmitting` to `true` to enable the real-time data transmitting. After that, set the `ideTrackerDataHandler` or `eyeTrackerDataHandler` to handle the real-time data. Finally, call the `startTracking()` method to start tracking.

```java
IDETracker ideTracker = IDETracker.getInstance();
ideTracker.setIsRealTimeDataTransmitting(true);
ideTracker.setIdeTrackerDataHandler(element->{
    String formattedStr = "Event: "+element.getAttribute("id");
    System.out.println(formattedStr);
});
ideTracker.startTracking(currentProject);
```

## IDE Tracker
#### `IDETracker.getInstance()`

#### `setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting)`

#### `setIdeTrackerDataHandler(Consumer<Element> ideTrackerDataHandler)`
!!!
`Element` object is an XML element which is imported from `org.w3c.dom.Element` package.
!!!

#### `startTracking(Project project)`

## Eye Tracker

#### `EyeTracker.getInstance()`

#### `setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting)`

#### `setEyeTrackerDataHandler(Consumer<Element> eyeTrackerDataHandler)`

#### `startTracking(Project project)`