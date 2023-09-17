package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckAvailable {

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
