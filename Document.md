Data output structure:

```
[OUTPUT_DIR]
├── [START_TIMESTAMP]
│   ├── ide_tracking.xml
│   ├── eye_tracking.xml
│   ├── archives
│   │   ├── [ARCHIVE_TIMESTAMP_1].archive
│   │   ├── [ARCHIVE_TIMESTAMP_2].archive
│   │   ├── ...
│   ├── screen_recording
│   │   ├── video_clip_1.mp4
│   │   ├── video_clip_2.mp4
│   │   ├── ...
│   │   ├── frames.csv
```

Comment:

- `[OUTPUT_DIR]` is the output directory specified in the configuration.
- `[START_TIMESTAMP]` is the timestamp when the tracking starts.
- `[ARCHIVE_TIMESTAMP]` is the timestamp when the archive is triggered.
- `video_clip_[k].mp4` is the video clip of the screen recording from the (k-1)-th pause (0-th pause is start) to the
  k-th pause.
- `frames.csv` records the timestamp and clip number of each frame in the video clip.

All the timestamps used by CodeVision are Unix time in milliseconds, starting from 1970-01-01 00:00:00 UTC.

---

Element: `<ide_tracking>`

Sub-element:

- `<environment>`
- `<archives>`
- `<actions>`
- `<typings>`
- `<files>`
- `<mouses>`
- `<carets>`
- `<selections>`
- `<visible_areas>`

Comment:

- The root element of the `ide_tracking.xml` file.

---

Element: `<environment>`

Attribute:

- ide_name
- ide_version
- os_name
- java_version
- project_name
- project_path
- screen_size
- scale_x
- scale_y

Example:

```xml

<environment ide_name="IntelliJ IDEA" ide_version="2022.2.5" java_version="17.0.6" os_name="Windows 10"
             project_name="HelloWorld" project_path="C:/Users/Lenovo/IdeaProjects/HelloWorld" scale_x="1.25"
             scale_y="1.25" screen_size="(1536,864)"/>
```

Comment:

- `scale_x` and `scale_y` are used to calculate the real screen resolution based on the `screen_size`. In the example
  above, the real screen resolution is (1536\*1.25, 864\*1.25) = (1920, 1080).
- `java_version` will be replaced by `python_version` in PyCharm, etc.
- All `path` attributes in the data start with `/` are relative to `project_path`, otherwise they are absolute paths.

---

Element: `<archives>`

Sub-element:

- `<archive>`

Comment:

- A **real-time archive mechanism** is implemented to track the state of the code file and console output at any
  timestamp during the development process. The **file archive** is triggered under two specific conditions: (1) When a
  file is opened or closed, or its selection changes; (2) When the content of the code in the main editor changes. The
  **console archive** is triggered when the console output changes (e.g., run class).

---

Element: `<archive>`

Attribute:

- id
- timestamp
- path
- remark

Example:

```xml

<archive id="fileArchive" path="/src/Main.java" remark="fileOpened" timestamp="1696203834202"/>
<archive id="fileArchive" path="/1696203101069/ide_tracking.xml" remark="fileOpened | NotCodeFile | Fail"
         timestamp="1696203834208"/>
<archive id="fileArchive" path="/src/Main.java" remark="contentChanged" timestamp="1696203839648"/>
<archive id="consoleArchive" timestamp="1696203842925"/>
```

Comment:

- `id` could be `fileArchive` or `consoleArchive`.
- `path` and `remark` is only used in the file archive.
- `remark` could be `fileOpened`, `fileClosed`, `fileSelectionChanged`, `contentChanged | OldFile`, `contentChanged |
  NewFile`.
- If the file is not a code file, i.e., the file extension is not in the ".java", ".cpp", ".c", ".py", ".rb", ".js",
  or ".md", `NotCodeFile | Fail` would be added to the remark. This is to prevent archiving data file with large size.
- If there are IO errors when archiving the file, `IOException | Fail` would be added to the remark.

---

Element: `<actions>`
Sub-element: `<action>`

- `<action>`