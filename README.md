# Programmer Behavior

This is a IntelliJ IDEA plugin that perform eye tracking and IDE tracking of programmers.

## Requirements

```
IntelliJ IDEA 2022.1.4
python==3.8.8
tobii-research==1.10.1
pyautogui 0.9.53
opencv-python 4.6.0.66
numpy 1.23.5
```

## Usage

**Step 1.** Use eye trackers manager to perform calibration.

**Step 2.** Run python scripts in `python` folder.

```
python python/tobii_pro.py
python python/screen_recorder.py
```

**Step 3.** Click `Tools` - `Start Behavior Detecting` to start recording.

**Step 4.** Click `Tools` - `Stop Behavior Detecting` to stop recording.

## Development

1. Click `Run` - `Run Plugin` to run the plugin and test it.
2. Open command line and run `./gradlew build` to build the plugin.
3. Get the plugin zip file in `build/distributions` folder.

## Data

1. Recording_[timestamp].avi
2. Recording_[timestamp].xml
3. eyeTracker_[timestamp].xml
4. iDEBehaviors_[timestamp].xml
