package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class AvailabilityChecker {

    public static boolean checkPythonEnvironment(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                import tobii_research as tr
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

    public static boolean checkEyeTracker(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                import tobii_research as tr
                                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print('Found')
                """;

        String line = runPythonScript(pythonInterpreter, pythonScript);
        return line.equals("Found");
    }

    public static String getEyeTrackerName(String pythonInterpreter) throws IOException, InterruptedException {
        String pythonScript = """
                import tobii_research as tr
                                
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('Not Found')
                else:
                    print(found_eyetrackers[0].device_name)
                """;

        return runPythonScript(pythonInterpreter, pythonScript);
    }

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
