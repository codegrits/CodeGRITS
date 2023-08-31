package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StartEyeAction extends AnAction {

    private boolean isTracking = false;
    private Process p;
    private Thread outputThread;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            callPythonScript();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void callPythonScript() throws IOException, InterruptedException {
        String pythonInterpreter = "python";
        String pythonScript;
        if(!isTracking){
            isTracking = true;
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

        }else{
            isTracking = false;
            pythonScript = """
                my_eyetracker.unsubscribe_from(tr.EYETRACKER_GAZE_DATA, gaze_data_callback)
                print('Unsubscribed from gaze data')
                """
                    ;
            p.destroy();
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        p = pb.start();

        outputThread = new Thread(() -> {
            try (InputStream inputStream = p.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();


    }

}
