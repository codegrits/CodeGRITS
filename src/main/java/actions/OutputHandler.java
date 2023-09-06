package actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class OutputHandler {
    private Consumer<String> outputHandler;
    private Process p;
    private Thread outputThread;

    public void addOutputHandler(Consumer<String> outputHandler) {
       this.outputHandler = outputHandler;
    }

    protected void handleOutput(String line) {
        outputHandler.accept(line);
    }

    protected Consumer<String> getOutputHandler() {
        return outputHandler;
    }


    public void checkTracker() throws IOException, InterruptedException {
        String pythonInterpreter = "python";
        String pythonScript = """
                import tobii_research as tr
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('No eye tracker found')
                else:
                    print('Found eye tracker')
                """
                ;

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        p = pb.start();
        // Get the process's standard output stream
        InputStream stdout = p.getInputStream();

        // Create a reader to read the output stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

        // Read the output and print it to the console
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // Get the process output (optional)
        int exitCode = p.waitFor();
        System.out.println("Python script exited with code: " + exitCode);
    }

    public void startTracking() throws IOException {
        System.out.println("Starting tracking");
        String pythonInterpreter = "python";
        String pythonScript;
        pythonScript = """
                import tobii_research as tr
                import time
                found_eyetrackers = tr.find_all_eyetrackers()
                my_eyetracker = found_eyetrackers[0]
                def gaze_data_callback(gaze_data):
                    print(gaze_data)
                my_eyetracker.subscribe_to(tr.EYETRACKER_GAZE_DATA, gaze_data_callback, as_dictionary=True)
                print('Subscribed to gaze data')
                time.sleep(100)
                """
        ;

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        p = pb.start();
        System.out.println("Started tracking");

        outputThread = new Thread(() -> {
            try (InputStream inputStream = p.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                System.out.println("Reading output");
                while ((line = bufferedReader.readLine()) != null) {
                    if (getOutputHandler() != null) {
                        System.out.println("Handling output");
                        handleOutput(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();
    }

    public void stopTracking() {
        p.destroy();
    }
}
