---
label: FAQ
icon: question
order: 0
---

# Frequently Asked Questions (FAQ)

#### Q1. What is the difference between CodeGRITS and iTrace?

[iTrace](https://www.i-trace.org/) is a similar tool to CodeGRITS for collecting developers' eye gaze data in several
IDEs, including Eclipse, Visual Studio, and Atom. However, CodeGRITS is built for JetBrains IDEs, which have increased
popularity in the industry and academia.

CodeGRITS also provides a set of extra functionalities, notably IDE tracking and screen recording, for empirical SE
researchers. See [Trackers](usage.md#trackers) for more details.

#### Q2. Can I use CodeGRITS without an eye-tracking device?

Yes. CodeGRITS provides mouse simulation as a substitute for eye gaze. You could also uncheck the `Eye Tracking` option
in the configuration window to disable the eye tracker to only use the IDE tracker and screen recorder.

#### Q3. How to integrate other eye-tracking devices with CodeGRITS?

See [Accommodating New Eye Trackers](developer.md#accommodating-new-eye-trackers).

#### Q4. How to use CodeGRITS in other JetBrains IDEs?

See [Accommodating New IDEs](developer.md#accommodating-new-ides).

#### Q5. How efficient is the processing of raw eye gaze data?

In CodeGRITS, the efficiency of the processing of gaze data is negligible. For each gaze, we calculate the average time
from the timestamp in the raw data to the timestamp after all processing is complete. This processing primarily involves
location mapping and upward traversal of the AST. The average delay is 4.32 ms, equating to delays of approximately
12.98% for 30Hz, 25.96% for 60Hz, and 51.92% for 120Hz eye gaze data. With such
a high sampling frequency, meaningful changes in the content of the code editor's page are extremely rare within this
short time frame, which ensures the accuracy of gaze processing. Moreover, compared
to [iTrace](https://www.i-trace.org/), our method of receiving data from the
eye-tracking device is more efficient, ensuring that all sampled gazes are mapped without any loss.

#### Q6. How much storage space does CodeGRITS require?

Generally speaking, a high sample frequency generates a large amount of gaze data. To conserve storage space, we
only perform upward traversal of the AST of the first gaze and record the hierarchy structure, and mark the rest
as `same`. This approach significantly reduces storage space. In a previous debugging study, we set the eye-tracking
device's sample frequency to 60Hz, and during the 20-minute experiment, the eye-tracking data amounted to only about
40MB.
