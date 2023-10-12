# Real-time Data API Reference

## Overview
We provide a real-time data API for future plug-in developers and researchers to get the real-time data from IDE tracker and eye tracker separately. The API is based on the [IDE Tracker](#ide-tracker) and [Eye Tracker](#eye-tracker).

## Configuration
add dependency stuff

## Quick Start
describe how to use the API

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