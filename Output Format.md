# Output Format

## Output Directory Structure

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

**Comment**:

- `[OUTPUT_DIR]` is the output directory specified in the configuration.
- `[START_TIMESTAMP]` is the timestamp when the tracking starts.
- `[ARCHIVE_TIMESTAMP]` is the timestamp when the archive is triggered.
- `video_clip_[k].mp4` is the video clip of the screen recording from the (k-1)-th pause (0-th pause is start) to the
  k-th pause.
- `frames.csv` records the timestamp and clip number of each frame in the video clip.

All the timestamps used by CodeGRITS are Unix time in milliseconds, starting from 1970-01-01 00:00:00 UTC.

The [editor coordinate system](https://plugins.jetbrains.com/docs/intellij/coordinates-system.html#editor-coordinate-systems)
(e.g., line, column) of IntelliJ Platform is start from 0.

## IDE Tracking

```
[OUTPUT_DIR]
├── [START_TIMESTAMP]
│   ├── ide_tracking.xml
│   ├── archives
│   │   ├── [ARCHIVE_TIMESTAMP_1].archive
│   │   ├── [ARCHIVE_TIMESTAMP_2].archive
│   │   ├── ...
```

### XML Element Tree

<style>
    .tree {
        color: #1956AF;
        border-radius: 10px;
        border: 1px solid #1956AF;
        padding-top: 20px;
        margin-bottom: 20px;
    }
</style>
:::tree
- **`<ide_tracking>`**
  - [`<environment>`](#environment)
  - [`<archives>`](#archives)
    - [`<archive>`](#archive)
  - [`<actions>`](#actions)
    - [`<action>`](#action)
  - [`<typings>`](#typings)
    - [`<typing>`](#typing)
  - [`<files>`](#files)
    - [`<file>`](#file)
  - [`<mouses>`](#mouses)
    - [`<mouse>`](#mouse)
  - [`<carets>`](#carets)
    - [`<caret>`](#caret)
  - [`<selections>`](#selections)
    - [`<selection>`](#selection)
  - [`<visible_areas>`](#vis_areas)
    - [`<visible_area>`](#vis_area)

:::

---

**Element**: `<ide_tracking>`

**Sub-element**:

- `<environment>`
- `<archives>`
- `<actions>`
- `<typings>`
- `<files>`
- `<mouses>`
- `<carets>`
- `<selections>`
- `<visible_areas>`

**Comment**:

- The root element of the `ide_tracking.xml` file.

---
<a name="environment"></a>
**Element**: `<environment>`

**Attribute**:

- ide_name
- ide_version
- os_name
- java_version
- project_name
- project_path
- screen_size
- scale_x
- scale_y

**Example**:

```xml

<environment ide_name="IntelliJ IDEA" ide_version="2022.2.5" java_version="17.0.6" os_name="Windows 10"
             project_name="HelloWorld" project_path="C:/Users/Lenovo/IdeaProjects/HelloWorld" scale_x="1.25"
             scale_y="1.25" screen_size="(1536,864)"/>
```

**Comment**:

- `scale_x` and `scale_y` are used to calculate the real screen resolution based on the `screen_size`. In the example
  above, the real screen resolution is (1536\*1.25, 864\*1.25) = (1920, 1080).
- `java_version` will be replaced by `python_version` in PyCharm, etc.
- All `path` attributes in the data start with `/` are relative to `project_path`, otherwise they are absolute paths.
  Sometimes the path is empty, which means the data is irrelevant to any file or not successfully tracked.

---
<a name="archives"></a>
**Element**: `<archives>`

**Sub-element**: `<archive>`

**Comment**:

- A **real-time archive mechanism** is implemented to track the state of the code file and console output at any
  timestamp during the development process. The **file archive** is triggered under two specific conditions: (1) When a
  file is opened or closed, or its selection changes; (2) When the content of the code in the main editor changes. The
  **console archive** is triggered when the console output changes (e.g., run class).
- The archived data is stored in the `archives` directory, with the name `[ARCHIVE_TIMESTAMP].archive`, where
  `[ARCHIVE_TIMESTAMP]` is the timestamp when the archive is triggered. Relevant information is stored in the
  `<archive>` element, including the timestamp, the path of the file, and the remark.
- Thus, if you want to know the state of the code file at a specific timestamp, you can find the archive file with the
  largest timestamp that is smaller than the target timestamp.

---
<a name="archive"></a>
**Element**: `<archive>`

**Attribute**:

- id
- timestamp
- path: only used in `fileArchive`
- remark: only used in `fileArchive`

**Example**:

```xml

<archive id="fileArchive" path="/src/Main.java" remark="fileOpened" timestamp="1696203834202"/>
<archive id="fileArchive" path="/1696203101069/ide_tracking.xml" remark="fileOpened | NotCodeFile | Fail"
         timestamp="1696203834208"/>
<archive id="fileArchive" path="/src/Main.java" remark="contentChanged" timestamp="1696203839648"/>
<archive id="consoleArchive" timestamp="1696203842925"/>
```

**Comment**:

- `id` could be `fileArchive` or `consoleArchive`.
- `remark` could be `fileOpened`, `fileClosed`, `fileSelectionChanged`, `contentChanged | OldFile`, `contentChanged |
  NewFile`.
- If the file is not a code file, i.e., the file extension is not in the ".java", ".cpp", ".c", ".py", ".rb", ".js",
  or ".md", `NotCodeFile | Fail` would be added to the remark. This is to prevent archiving data file with large size.
- If there are IO errors when archiving the file, `IOException | Fail` would be added to the remark.

---
<a name="actions"></a>
**Element**: `<actions>`

**Sub-element**: `<action>`

**Comment**:

- The elements in `<actions>` are all the IDE-specific features, technically are all objects that implement
  the `AnAction` abstract class in IntelliJ IDEA. The range is diverse, from the basic editing features
  like `EditorEnter`, `EditorBackSpace`, clipboard features like `EditorPaste`, `EditorCut`, run features
  like `RunClass`, `Stop`, `ToggleLineBreakpoint`, `Debug`, navigating features
  like `GotoDeclaration`, `Find`, `ShowIntentionActions`, advanced IDE features
  like `CompareTwoFiles`, `ReformatCode`, to many others that cannot be fully listed here.

---
<a name="action"></a>
**Element**: `<action>`

**Attribute**:

- id
- timestamp
- path

**Example**:

```xml

<action id="ReformatCode" path="/src/Main.java" timestamp="1696214487353"/>
<action id="SaveAll" path="/src/Main.java" timestamp="1696214490354"/>
<action id="RunClass" path="/src/Main.java" timestamp="1696214496053"/>
<action id="ToggleLineBreakpoint" path="/src/Main.java" timestamp="1696214500296"/>
<action id="EditorEnter" path="/src/Main.java" timestamp="1696214504846"/>
<action id="EditorBackSpace" path="/src/Main.java" timestamp="1696214505280"/>
<action id="SaveAll" path="/src/Main.java" timestamp="1696214506877"/>
<action id="GotoDeclaration" path="/src/Main.java" timestamp="1696214513473"/>
<action id="CodeGRITS.StartStopTracking"
        path="C:/Program Files/Java/jdk-16.0.2/lib/src.zip!/java.base/java/io/PrintStream.java"
        timestamp="1696214517658"/>
<action id="EditorCopy" path="/src/Main.java" timestamp="1696216114539"/>
<action id="$Paste" path="/src/Main.java" timestamp="1696216116839"/>
<action id="$Undo" path="/src/Main.java" timestamp="1696216117569"/>
<action id="Debug" path="/src/Main.java" timestamp="1696216129173"/>
<action id="NewClass" path="/src" timestamp="1696217116236"/>
<action id="RenameElement" path="/src/ABC.java" timestamp="1696217122074"/>
```

**Comment**:

- CodeGRITS-related actions are also implemented as `AnAction` objects, and their `id` is prefixed with `CodeGRITS`,
  such as `CodeGRITS.StartStopTracking`, `CodeGRITS.PauseResumeTracking`, etc.
- The "add label" action is also tracked here, with `id` as `"CodeGRITS.AddLabel.[LABEL_NAME]"`, where label name
  is pre-set in the configuration.
- Other IntelliJ plugins may also implement their own `AnAction` objects, which will also be tracked here. For example,
  the `copilot.applyInlays` in the GitHub Copilot plugin.

---
<a name="typings"></a>
**Element**: `<typings>`

**Sub-element**: `<typing>`

**Comment**:

- The `<typings>` element records the typing action of the user in the code editor. The data including the character,
  the timestamp, the path of the file, the line number, and the column number.

---
<a name="typing"></a>
**Element**: `<typing>`

**Attribute**:

- character
- timestamp
- path
- line
- column

**Example**:

```xml

<typing character="S" column="8" line="3" path="/src/Main.java" timestamp="1696216429855"/>
<typing character="y" column="9" line="3" path="/src/Main.java" timestamp="1696216430111"/>
<typing character="s" column="10" line="3" path="/src/Main.java" timestamp="1696216430233"/>
```

---
<a name="files"></a>
**Element**: `<files>`

**Sub-element**: `<file>`

**Comment**:

- The `<files>` element records the file-related actions including opening, closing, and selection change. The data
  including the timestamp and the path of the file.

---
<a name="file"></a>
**Element**: `<file>`

**Attribute**:

- id
- timestamp
- path: only used in `fileOpened`/`fileClosed`
- old_path: only used in `selectionChanged`
- new_path: only used in `selectionChanged`

**Example**:

```xml

<file id="fileClosed" path="/src/Main.java" timestamp="1696216679318"/>
<file id="selectionChanged" new_path="/src/ABC.java" old_path="/src/Main.java"
      timestamp="1696216679330"/>
<file id="fileOpened" path="/src/ABC.java" timestamp="1696216679338"/>
```

**Comment**:

- `id` could be `fileOpened`, `fileClosed`, or `selectionChanged`.

---
<a name="mouses"></a>
**Element**: `<mouses>`

**Sub-element**: `<mouse>`

**Comment**:

- The `<mouses>` element records the mouse-related actions including pressing, releasing, clicking, moving, and
  dragging. The data including the timestamp, the path of the file, the x-coordinate, and the y-coordinate.

---

<a name="mouse"></a>
**Element**: `<mouse>`

**Attribute**:

- id
- timestamp
- path
- x
- y

**Example**:

```xml

<mouse id="mousePressed" path="/src/DEF.java" timestamp="1696217839651" x="642" y="120"/>
<mouse id="mouseReleased" path="/src/DEF.java" timestamp="1696217840187" x="642" y="120"/>
<mouse id="mouseClicked" path="/src/DEF.java" timestamp="1696217840188" x="642" y="120"/>
<mouse id="mousePressed" path="/src/DEF.java" timestamp="1696217843026" x="642" y="120"/>
<mouse id="mouseDragged" path="/src/DEF.java" timestamp="1696217843026" x="634" y="118"/>
<mouse id="mouseReleased" path="/src/DEF.java" timestamp="1696217843830" x="535" y="117"/>
<mouse id="mouseMoved" path="/src/DEF.java" timestamp="1696217843901" x="536" y="117"/>
<mouse id="mouseMoved" path="/src/DEF.java" timestamp="1696217843908" x="537" y="117"/>
```

**Comment**:

- `id` could be `mousePressed`, `mouseReleased`, `mouseClicked`, `mouseMoved`, or `mouseDragged`.
- `x` and `y` are the coordinates relative to the `screen_size` in the `environment`, not the actual screen resolution.

---
<a name="carets"></a>
**Element**: `<carets>`

**Sub-element**: `<caret>`

**Comment**:

- Caret is the cursor in the code editor. The `<carets>` element records the change of the caret position in the code
  editor. The data including the timestamp, the path of the file, the line number, and the column number.

---
<a name="caret"></a>
**Element**: `<caret>`

**Attribute**:

- id
- timestamp
- path
- line
- column

**Example**:

```xml

<caret column="18" id="caretPositionChanged" line="0" path="/src/DEF.java" timestamp="1696217839651"/>
```

**Comment**:

- `id` could only be `caretPositionChanged`.

---
<a name="selections"></a>
**Element**: `<selections>`

**Sub-element**: `<selection>`

**Comment**:

- The `<selections>` element records data when the user selects a piece of code in the code editor. The data including
  the timestamp, the path of the file, the start position, the end position, and the selected text.

---
<a name="selection"></a>
**Element**: `<selection>`

**Attribute**:

- id
- timestamp
- path
- start_position: line:column
- end_position: line:column
- selected_text

**Example**:

```xml

<selection end_position="0:18" id="selectionChanged" path="/src/DEF.java" selected_text="F {" start_position="0:15"
           timestamp="1696219345156"/>
<selection end_position="0:18" id="selectionChanged" path="/src/DEF.java" selected_text="EF {" start_position="0:14"
           timestamp="1696219345169"/>
```

**Comment**:

- `id` could only be `selectionChanged`.

---
<a name="vis_areas"></a>
**Element**: `<visible_areas>`

**Sub-element**: `<visible_area>`

**Comment**:

- The `<visible_areas>` element records the visible area of the code editor.

---
<a name="vis_area"></a>
**Element**: `<visible_area>`
**Attribute**:

- id
- timestamp
- path
- x
- y
- width
- height

```xml

<visible_area height="277" id="visibleAreaChanged" path="/src/DEF.java" timestamp="1696219585893" width="883" x="0"
              y="198"/>
<visible_area height="275" id="visibleAreaChanged" path="/src/DEF.java" timestamp="1696219585921" width="883" x="0"
              y="198"/>
```

**Comment**:

- `id` could only be `visibleAreaChanged`.
- `x` and `y` are the coordinates of the **left-top corner of the visible area** in code editor, relative to the
  **left-top corner of the code editor** including the invisible part (i.e., the line 0 and column 0). The unit
  of `x`, `y`, `width`, and `height` is measured by `screen_size` in the `environment`, not the actual screen
  resolution.
- The change of `x` and `y` is usually caused by **scrolling** code editor, which could be used to track the
  horizontal and vertical scrolling respectively. The change of `width` and `height` is usually caused by **resizing**
  code editor, which could be used to track the horizontal and vertical resizing respectively.

## Eye Tracking

```
[OUTPUT_DIR]
├── [START_TIMESTAMP]
│   ├── eye_tracking.xml
```

---

**Element**: `<eye_tracking>`

**Sub-element**:

- `<setting>`
- `<gazes>`

**Comment**:

- The root element of the `eye_tracking.xml` file. CodeGRITS support both Mouse simulation and Tobii Pro eye tracker
  devices.
- Since [Tobii Pro SDK](https://developer.tobiipro.com/index.html) not support Java, we use Python
  library `tobii-research` to collect eye tracking data and use Java ProcessBuilder to call the Python script to collect
  data. The python interpreter is specified in the configuration.

---

**Element**: `<setting>`

**Attribute**:

- eye_tracker
- sampling_rate

**Example**:

```xml

<setting eye_tracker="Tobii Pro Fusion" sample_frequency="30"/>
```

**Comment**:

- `eye_tracker` could be `Mouse` for simulation, or real Tobii Pro eye tracker device name (e.g., `Tobii Pro Fusion`),
  which is got from `eyetracker.model` in the `tobii-research` library.
- `sampling_rate` is the sampling rate of the eye tracker in Hz, which is pre-set in the configuration and whose range
  could be in `eyetracker.get_all_gaze_output_frequencies()` called in the `tobii-research` library.

---

**Element**: `<gazes>`

**Sub-element**: `<gaze>`

**Comment**:

- Collection of all gaze data.

---

**Element**: `<gaze>`

**Sub-element**:

- `<left_eye>`
- `<right_eye>`
- `<location>`: only used when the gaze point can be mapped to location in the code editor
- `<ast_structure>`: only used when the gaze point cannot be mapped to location in the code editor, and the code file is
  java.

**Attribute**:

- timestamp
- remark: only used when the gaze point cannot be mapped to location in the code editor

**Example**:

```xml

<gaze timestamp="1696224370377">
    <left_eye gaze_point_x="0.5338541666666666" gaze_point_y="0.17407407407407408" gaze_validity="1.0"
              pupil_diameter="2.4835662841796875" pupil_validity="1.0"/>
    <right_eye gaze_point_x="0.5338541666666666" gaze_point_y="0.17407407407407408" gaze_validity="1.0"
               pupil_diameter="2.7188568115234375" pupil_validity="1.0"/>
    <location column="25" line="2" path="/src/Main.java" x="820" y="150"/>
    <ast_structure token="println" type="IDENTIFIER">
        <level end="2:26" start="2:19" tag="PsiIdentifier:println"/>
        <level end="2:26" start="2:8" tag="PsiReferenceExpression:System.out.println"/>
        <level end="2:42" start="2:8" tag="PsiMethodCallExpression:System.out.println(&quot;Hello world!&quot;)"/>
        <level end="2:43" start="2:8" tag="PsiExpressionStatement"/>
        <level end="3:5" start="1:43" tag="PsiCodeBlock"/>
        <level end="3:5" start="1:4" tag="PsiMethod:main"/>
        <level end="4:1" start="0:0" tag="PsiClass:Main"/>
    </ast_structure>
</gaze>
```

**Comment**:

When the gaze point cannot be mapped to location in the code editor in the following 3 cases, the `remark` attribute is
used:

1. The raw gaze point from the eye tracker is invalid. (i.e., nan). In this case, the `remark`
   is `Fail | Invalid Gaze Point`.
2. The code editor is not founded. In this case, the `remark` is `Fail | No Editor`.
3. The code editor is founded, but the gaze point is out of the code editor. In this case, the `remark`
   is `Fail | Out of Text Editor`.

---

**Element**: `<left_eye>`

**Attribute**:

- gaze_point_x
- gaze_point_y
- gaze_validity
- pupil_diameter
- pupil_validity

**Example**:

```xml

<left_eye gaze_point_x="0.5338541666666666" gaze_point_y="0.17407407407407408" gaze_validity="1.0"
          pupil_diameter="2.4835662841796875" pupil_validity="1.0"/>
```

**Comment**:

- `gaze_point_x` and `gaze_point_y` are the location on the screen, ranging from 0 to 1, where (0, 0) is the top-left
  corner of the screen, and (1, 1) is the bottom-right corner of the screen.
- `gaze_validity` and `pupil_validity` are the validity of the gaze point and pupil diameter, which is binary, 0 for
  invalid, 1 for valid. When using Mouse to simulate eye tracker, `gaze_validity` is always 1.0, and `pupil_validity` is
  always 0.0.
- `pupil_diameter` is the diameter of the pupil in mm, when using Mouse to simulate eye tracker, `pupil_diameter` is
  always 0.

---

**Element**: `<right_eye>`

**Attribute**:

- gaze_point_x
- gaze_point_y
- gaze_validity
- pupil_diameter
- pupil_validity

**Example**:

```xml

<right_eye gaze_point_x="0.5338541666666666" gaze_point_y="0.17407407407407408" gaze_validity="1.0"
           pupil_diameter="2.7188568115234375" pupil_validity="1.0"/>
```

**Comment**:

- `gaze_point_x` and `gaze_point_y` are the location on the screen, ranging from 0 to 1, where (0, 0) is the top-left
  corner of the screen, and (1, 1) is the bottom-right corner of the screen.
- `gaze_validity` and `pupil_validity` are the validity of the gaze point and pupil diameter, which is binary, 0 for
  invalid, 1 for valid. When using Mouse to simulate eye tracker, `gaze_validity` is always 1.0, and `pupil_validity` is
  always 0.0.
- `pupil_diameter` is the diameter of the pupil in mm, when using Mouse to simulate eye tracker, `pupil_diameter` is
  always 0.

---

**Element**: `<location>`

**Attribute**:

- path
- line
- column
- x
- y

**Example**:

```xml

<location column="25" line="2" path="/src/Main.java" x="820" y="150"/>
```

**Comment**:

- `x` and `y` are the coordinates of the gaze relative to the top-left corner of the visible code editor, whose unit is
  same to the `screen_size`'s in `environment`, not the actual screen resolution.
- `line` and `column` are the line number and column number of the gaze point in the code editor, which is calculated
  by `xyToLogicalPosition(@NotNull Point p)` method of `Editor` interface in IntelliJ Platform.

---

**Element**: `<ast_structure>`

**Sub-element**: `<level>`: only used when the current token is different from the previous token

**Attribute**:

- token
- type
- remark: only used when the current token is same to the previous token

**Example**:

```xml

<ast_structure token="println" type="IDENTIFIER">
  <level end="2:26" start="2:19" tag="PsiIdentifier:println"/>
  <level end="2:26" start="2:8" tag="PsiReferenceExpression:System.out.println"/>
  <level end="2:42" start="2:8" tag="PsiMethodCallExpression:System.out.println(&quot;Hello world!&quot;)"/>
  <level end="2:43" start="2:8" tag="PsiExpressionStatement"/>
  <level end="3:5" start="1:43" tag="PsiCodeBlock"/>
  <level end="3:5" start="1:4" tag="PsiMethod:main"/>
  <level end="4:1" start="0:0" tag="PsiClass:Main"/>
</ast_structure>
```

```xml

<ast_structure remark="Same (Last Successful AST)" token="println" type="IDENTIFIER"/>
```

**Comment**:

- The abstract syntax tree (AST) of the code file is recorded in the `<ast_structure>` element. The AST is calculated by
  [program structure interface (PSI)](https://plugins.jetbrains.com/docs/intellij/psi-elements.html) of IntelliJ
  Platform.
- `token` is text of the leaf node in the AST of current gaze point, which is calculated by `psiElement.getText()`.
- `type` is the type of the leaf node, which is calculated by `psiElement.getNode().getElementType()`.
- `remark` is used when the current token is same to the previous token, which means the gaze point is still in the same
  leaf node. In this case, the `remark` is `Same (Last Successful AST)`. We designed this mechanism to
  avoid `eye_tracking.xml` to be too large.
- We calculate the parent nodes of the leaf node by `psiElement.getParent()` until the file-level (i.e. `PsiFile`), and
  save them in the `<level>` element. In the previous example, the leaf node is `PsiIdentifier:println`, and its parent
  nodes are `PsiReferenceExpression:System.out.println` => `PsiMethodCallExpression:System.out.println("Hello world!")`
  => `PsiExpressionStatement` => `PsiCodeBlock` => `PsiMethod:main` => `PsiClass:Main`. The original code text is
  ```java
  public class Main {
      public static void main(String[] args) {
          System.out.println("Hello world!");
      }
  }
  ```

---

**Element**: `<level>`

**Attribute**:

- start: line:column
- end: line:column
- tag

**Example**:

```xml

<level end="3:5" start="1:4" tag="PsiMethod:main"/>
```

**Comment**:

- `start` and `end` are the start and end position of the AST node level in the code file, which is calculated by
  `psiElement.getTextRange()`.
- `tag` is the type of the AST node level, which is calculated by `psiElement.toString()`.

## Screen Recording

```
[OUTPUT_DIR]
├── [START_TIMESTAMP]
│   ├── screen_recording
│   │   ├── video_clip_1.mp4
│   │   ├── video_clip_2.mp4
│   │   ├── ...
│   │   ├── frames.csv
```

---

`video_clip_[k].mp4`

**Comment**:

- The video clip of the screen recording from the (k-1)-th pause (0-th pause is start) to the k-th pause. We designed
  this mechanism to avoid the video file in the memory being too large especially when pausing the tracking for a long
  time.

---

`frames.csv`

Column:

- timestamp
- frame_number
- clip_number

**Example**:

```csv
timestamp,frame_number,clip_number
1696224360619,1,1
1696224360991,2,1
```

**Comment**:

- `frame_number` is the frame number of the frame in its video clip.
- `clip_number` is the number of the video clip to which the frame belongs.