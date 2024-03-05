---
label: Welcome
icon: home
order: 100
---

# Welcome to CodeGRITS

!!! :zap: NEWS! :zap:
We would present CodeGRITS at
[ICSE 2024 Demo Track](https://conf.researchr.org/track/icse-2024/icse-2024-demonstrations?#program).
Welcome to join us and discuss with us about it!
!!!

CodeGRITS stands for **G**aze **R**ecording & **I**DE **T**racking **S**ystem. It's a plugin developed by
the [SaNDwich Lab](https://toby.li/) and is specially designed for empirical software engineering researchers.
CodeGRITS is built on top
of [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html), with wide compatibility with the
entire family of JetBrains IDEs and [Tobii eye-tracking devices](https://www.tobii.com/),
to track developers’ IDE interactions and eye gaze data. The source code is available
on [GitHub](https://github.com/codegrits/CodeGRITS) with [Javadoc](docs/index.html) documentation.

<div style="text-align: center;">
    <img src="static/visualization.png" style="width: 100%; max-width: 900px; height: auto;"><br><br>
</div>

The data collected by CodeGRITS can be used by empirical SE researchers to understand the behaviors of developers,
especially those related to eye gaze. CodeGRITS also provides a [real-time data API](developer.md)
for future plugin developers and researchers to design context-aware programming support tools.

!!!
CodeGRITS is still in its developmental stage as a research tool. Our goal is to make it mature enough and beneficial
for the community, particularly for those involved in empirical software engineering and eye tracking research. We
encourage the community to contribute through [GitHub Issue](https://github.com/codegrits/CodeGRITS/issues) for any
suggestions or issues, aiding in its improvement.

For any inquiries, please email us at ntang@nd.edu or jan2@nd.edu. If you're interested in
using CodeGRITS in your research, don't hesitate to email us for setup support. We are delighted to provide
tailored assistance based on your specific OS and JetBrains IDE environment.
!!!

### Cross-platform and Multilingual Support

- [x] CodeGRITS provides cross-platform support for Windows, macOS,
  and Linux, and is expected to be compatible with the entire family of JetBrains IDEs, including IntelliJ IDEA,
  PyCharm, WebStorm, etc.
- [x] CodeGRITS could extract the abstract syntax tree (AST) structure of eye gazes on multiple
  programming languages, as long as the IDE supports them, including Java, Python, C/C++, JavaScript, etc.

!!!warning 🚨 macOS Support 🚨
CodeGRITS has been primarily developed and tested on Windows and Linux, with only partial testing on macOS.
There may be some unnoticed bugs on macOS. We have created a
[`mac` branch](https://github.com/codegrits/CodeGRITS/tree/mac) for this and have fixed some known issues.
If you encounter additional issues on macOS, please feel free to report them to us.
!!!

## Key Features

- :mag: **IDE Tracking**: CodeGRITS tracks developers’ IDE interactions, including mouse clicks, keyboard inputs, etc.
- :eye: **Eye Tracking**: CodeGRITS tracks developers’ eye gaze data
  from [Tobii eye-tracking devices](https://www.tobii.com/), and maps them to corresponding source code elements.
- :computer: **Screen Recording**: CodeGRITS simultaneously records developers’ screen for visualizing their behaviors.
- 🔨 **Research Toolkit**: CodeGRITS provides a set of extra features for empirical SE
  researchers, including dynamic configuration, activity labeling, real-time data API, etc.
- 🗃️ **Data Export**: CodeGRITS exports data in XML format for further data analysis. See [Data Format](data.md)
  for more details.

<div style="text-align: center;">
    <img src="static/overview.png" style="width: 100%; max-width: 600px; height: auto;">
</div>

## Citation

The paper of CodeGRITS has been accepted
by [ICSE 2024 Demonstrations Track](https://conf.researchr.org/track/icse-2024/icse-2024-demonstrations).
The PDF version is available [here](static/paper.pdf).
Please cite the following if you use CodeGRITS in your research.

```bibtex
@inproceedings{tang2024codegrits,
  title={CodeGRITS: A Research Toolkit for Developer Behavior and Eye Tracking in IDE},
  author={Tang, Ningzhi and An, Junwen and Chen, Meng and Bansal, Aakash and Huang, Yu and McMillan, Collin and Li, Toby Jia-Jun},
  booktitle={46th International Conference on Software Engineering Companion (ICSE-Companion '24)},
  year={2024},
  organization={ACM}
}
```