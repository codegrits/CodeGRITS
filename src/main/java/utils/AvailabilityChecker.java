package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class is used to check the availability of the python environment and the eye-tracking device, and to get the eye tracker name and the available frequencies.
 */
public class AvailabilityChecker {

    /**
     * Check the availability of the python environment, i.e., whether the required python packages are installed.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return {@code true} if the python environment is available, {@code false} otherwise.
     */
    public static boolean checkPythonEnvironment(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                from screeninfo import get_monitors
                import pyautogui
                import time
                import sys
                import math
                
                print('OK')
                """;

        String line = runPythonScript(pythonInterpreter, pythonScript);
        return line.equals("OK");
    }

    /**
     * Check the availability of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return {@code true} if the eye-tracking device is available, {@code false} otherwise.
     */
    public static boolean checkEyeTracker(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                try:
                    import tobii_research as tr
                
                    found_eyetrackers = tr.find_all_eyetrackers()
                    if found_eyetrackers == ():
                        print('Not Found')
                    else:
                        print('Found')
                
                except ImportError:
                    print('Not Found')
                """;

        String line = runPythonScript(pythonInterpreter, pythonScript);
        return line.equals("Found");
    }

    /**
     * Get the name of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return The name of the eye tracker.
     */
    public static String getEyeTrackerName(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                try:
                    import tobii_research as tr
                
                    found_eyetrackers = tr.find_all_eyetrackers()
                    if found_eyetrackers == ():
                        print('Not Found')
                    else:
                        print(found_eyetrackers[0].device_name)
                
                except ImportError:
                    print('Not Found')
                """;

        return runPythonScript(pythonInterpreter, pythonScript);
    }

    /**
     * Get the available frequencies of the eye-tracking device.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @return The available frequencies of the eye tracker.
     */
    public static List<String> getFrequencies(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                import tobii_research as tr
                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print(found_eyetrackers[0].get_all_gaze_output_frequencies())
                """;
        String resultTuple = runPythonScript(pythonInterpreter, pythonScript); //(30.0, 60.0, 90.0)

        return List.of(resultTuple.substring(1, resultTuple.length() - 1).split(", "));
    }

    /**
     * Run a python script with {@code ProcessBuilder} and use {@code BufferedReader} to get the first line of the output.
     *
     * @param pythonInterpreter The path of the python interpreter.
     * @param pythonScript      The python script to run.
     * @return The first line of the output.
     */
    private static String runPythonScript(String pythonInterpreter, String pythonScript) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true);
        Process p;
        p = pb.start();

        InputStream stdout = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        String line = reader.readLine();
        p.waitFor();
        return line;
    }
}
