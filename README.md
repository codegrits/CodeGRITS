# CodeGRITS

[[Website]](https://codegrits.github.io/CodeGRITS/) [[Paper]](https://arxiv.org/abs/xxxx.yyyyy) [[Demo Video]](https://www.youtube.com/watch?v=d-YsJfW2NMI)

[CodeGRITS](https://codegrits.github.io/CodeGRITS/) stands for **G**aze **R**ecording & **I**DE **T**racking **S**ystem,
which is a plugin specifically designed
for software engineering (SE) researchers, which is developed by the [SaNDwich Lab](https://toby.li/) at the
[University of Notre Dame](https://www.nd.edu/). CodeGRITS is built on top
of [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html), with wide compatibility with the
entire family of [JetBrains IDEs](https://www.jetbrains.com/) and [Tobii eye-tracking devices](https://www.tobii.com/),
to track developers’ IDE interactions and eye gaze data.

<p align="center">
    <img src="./docs/imgs/overview.png" width="600">
</p>

The data collected by CodeGRITS can be used by empirical SE researchers to understand the behaviors of developers,
especially those related to eye gaze. CodeGRITS also provides a [real-time data API](developer.md)
for future plugin developers and researchers to design context-aware programming support tools.

## Key Features

- 🔍 **IDE Tracking**: CodeGRITS tracks developers’ IDE interactions, including mouse clicks, keyboard inputs, etc.
- 👁️ **Eye Tracking**: CodeGRITS tracks developers’ eye gaze data
  from [Tobii eye-tracking devices](https://www.tobii.com/), and mapping them to corresponding source code elements.
- 💻 **Screen Recording**: CodeGRITS simultaneously records developers’ screen for visualizing their behaviors.
- 🔨 **Research Toolkit**: CodeGRITS provides a set of extra features for empirical SE
  researchers, including dynamic configuration, activity labeling, real-time data API, etc.
- 🗃️ **Data Export**: CodeGRITS exports data in XML format for further data analysis. See [Data Format](data.md)
  for more details.

### Cross-platform and Multilingual Support

- [x] CodeGRITS provides cross-platform support for Windows, macOS,
  and Linux, and is expected to be compatible with the entire family of JetBrains IDEs, including IntelliJ IDEA,
  PyCharm, WebStorm, etc.
- [x] CodeGRITS could extract the abstract syntax tree (AST) structure of eye gazes on multiple
  programming languages, as long as the IDE supports them, including Java, Python, C/C++, JavaScript, etc.

## Usage Guide

Please see the [CodeGRITS website](https://codegrits.github.io/CodeGRITS/) for more details.

## Citation

[//]: # (TODO: Update the citation and PDF link after the paper is published.)

The paper of CodeGRITS has been accepted
by [ICSE 2024 Demonstrations Track](https://conf.researchr.org/track/icse-2024/icse-2024-demonstrations).
The PDF version is available [here](https://arxiv.org/abs/xxxx.yyyyy).
The [video demonstration](https://www.youtube.com/watch?v=d-YsJfW2NMI) is available on YouTube.

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

## Contact us

Please feel free to contact Ningzhi Tang at ntang@nd.edu or Junwen An at jan2@nd.edu
if you have any questions or suggestions.