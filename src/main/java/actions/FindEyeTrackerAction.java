package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FindEyeTrackerAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            callPythonScript();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void callPythonScript() throws IOException, InterruptedException {
        String pythonInterpreter = "python";

        // May have path issues, so write code directly here
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
        Process p = pb.start();

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
}
