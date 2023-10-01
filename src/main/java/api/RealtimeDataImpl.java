package api;

import com.intellij.openapi.project.Project;

public class RealtimeDataImpl implements RealtimeDataInterface{
    private final Project project;
    public RealtimeDataImpl(Project project) {
        this.project = project;
    }
    @Override
    public void transmitRealTimeData() {
        // TODO Auto-generated method stub
        System.out.println("Hello World!");

    }
}
