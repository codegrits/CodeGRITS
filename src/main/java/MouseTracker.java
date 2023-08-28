import java.awt.event.*;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import org.jetbrains.annotations.NotNull;

public class MouseTracker {
    // TODO: mouse event tracking, including mouse click, mouse move, mouse drag, scroll, etc.
    EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    boolean isTracking = false;

    EditorMouseListener editorMouseListener = new EditorMouseListener() {
        @Override
        public void mousePressed(@NotNull EditorMouseEvent e) {
            MouseEvent mouseEvent = e.getMouseEvent();
            System.out.println("Mouse Pressed");
        }

        @Override
        public void mouseClicked(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Clicked");
        }

        @Override
        public void mouseReleased(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Released");
        }

        @Override
        public void mouseEntered(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Entered");
        }

        @Override
        public void mouseExited(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Exited");
        }
    };

    EditorMouseMotionListener editorMouseMotionListener = new EditorMouseMotionListener() {
        @Override
        public void mouseMoved(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Moved");
        }

        @Override
        public void mouseDragged(@NotNull EditorMouseEvent e) {
            System.out.println("Mouse Dragged");
        }
    };

    public void startTracking() {
        editorEventMulticaster.addEditorMouseListener(editorMouseListener, () -> {
        });
        editorEventMulticaster.addEditorMouseMotionListener(editorMouseMotionListener, () -> {
        });
        isTracking = true;
    }

    public void stopTracking() {
        editorEventMulticaster.removeEditorMouseListener(editorMouseListener);
        editorEventMulticaster.removeEditorMouseMotionListener(editorMouseMotionListener);
        isTracking = false;
    }

}

