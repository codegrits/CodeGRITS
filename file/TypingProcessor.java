package tracker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TypingProcessor {

    Document document;

    public TypingProcessor(Document document) {
        this.document = document;
    }

    // make a combination of process char typing and editor typing
    public void process() {
        processTyping();
        processEditorAction();
    }

    // post-process char typing in the dom document that combine subsequent char typing events
    // e.g., typing "a", "b" and "c" subsequently in same file will be combined to "abc"
    public void processTyping() {
        StringBuilder str = new StringBuilder();
        String timestamp = "", path = "", line = "", column = "", duration = "";
        Element behaviors = (Element) document.getElementsByTagName("typings").item(0);
        Element processedBehaviors = document.createElement("typings");
        document.getElementsByTagName("ide_tracking").item(0).appendChild(processedBehaviors);
        NodeList behaviorsChildren = behaviors.getChildNodes();
        for (int i = 0; i < behaviorsChildren.getLength(); i++) {
            Node node = behaviorsChildren.item(i);
            Element element;
            if (node.getNodeType() == Node.ELEMENT_NODE) element = (Element) node;
            else continue;
            String tagName = element.getTagName();
            if (!tagName.equals("typing") && str.length() == 0) {
                processedBehaviors.appendChild(element.cloneNode(true));
            } else if (!tagName.equals("typing")) {
                getTypingElement(str.toString(), timestamp, path, line, column, duration, processedBehaviors);
                str = new StringBuilder();
                processedBehaviors.appendChild(element.cloneNode(true));
            } else if (str.length() == 0) {
                str.append(element.getAttribute("character"));
                path = element.getAttribute("path");
                timestamp = element.getAttribute("timestamp");
                line = element.getAttribute("line");
                column = element.getAttribute("column");
                duration = "0";
            } else {
                String currentPath = element.getAttribute("path");
                String currentLine = element.getAttribute("line");
                String currentColumn = element.getAttribute("column");
                if (currentPath.equals(path) && currentLine.equals(line) &&
                        Integer.parseInt(currentColumn) == Integer.parseInt(column) + str.length()) {
                    str.append(element.getAttribute("character"));
                    duration = String.valueOf(Long.parseLong(element.getAttribute("timestamp")) -
                            Long.parseLong(timestamp));
                } else {
                    getTypingElement(str.toString(), timestamp, path, line, column, duration, processedBehaviors);
                    str = new StringBuilder();
                    str.append(element.getAttribute("character"));
                    path = element.getAttribute("path");
                    timestamp = element.getAttribute("timestamp");
                    line = element.getAttribute("line");
                    column = element.getAttribute("column");
                    duration = "0";
                }
            }
        }
        if (str.length() != 0)
            getTypingElement(str.toString(), timestamp, path, line, column, duration, processedBehaviors);
        document.getElementsByTagName("ide_tracking").item(0).removeChild(behaviors);
    }

    // post-process editor typing action in the dom document that combine subsequent editor typing events
    // e.g., 3 subsequent "EditorBackspace" or "EditorEnter" records in one file will be combined as one record with count = 3
    public void processEditorAction() {
        Element behaviors = (Element) document.getElementsByTagName("actions").item(0);
        Element processedBehaviors = document.createElement("actions");
        document.getElementsByTagName("ide_tracking").item(0).appendChild(processedBehaviors);
        NodeList behaviorsChildren = behaviors.getChildNodes();
        Element lastElement = null;
        int cnt = 1;
        long currentDuration = 0;
        for (int i = 0; i < behaviorsChildren.getLength(); i++) {
            Node node = behaviorsChildren.item(i);
            Element element;
            if (node.getNodeType() == Node.ELEMENT_NODE) element = (Element) node;
            else continue;
            if (lastElement != null && lastElement.getTagName().equals("action") && element.getTagName().equals("action")
                    && lastElement.getAttribute("id").equals(element.getAttribute("id"))
                    && lastElement.getAttribute("id").startsWith("Editor")
                    && lastElement.getAttribute("path").equals(element.getAttribute("path"))) {
                cnt += 1;
                currentDuration = Long.parseLong(element.getAttribute("timestamp"))
                        - Long.parseLong(lastElement.getAttribute("timestamp"));
            } else {
                if (cnt == 1) {
                    if (lastElement != null) processedBehaviors.appendChild(lastElement.cloneNode(true));
                } else {
                    lastElement.setAttribute("count", String.valueOf(cnt));
                    lastElement.setAttribute("duration", String.valueOf(currentDuration));
                    processedBehaviors.appendChild(lastElement.cloneNode(true));
                    cnt = 1;
                }
                lastElement = element;
            }
        }
        if (cnt != 1) {
            lastElement.setAttribute("count", String.valueOf(cnt));
            lastElement.setAttribute("duration", String.valueOf(currentDuration));
        }
        if (lastElement != null) {
            processedBehaviors.appendChild(lastElement.cloneNode(true));
        }
        document.getElementsByTagName("ide_tracking").item(0).removeChild(behaviors);
    }

    private void getTypingElement(String str, String timestamp, String path, String line, String column, String duration, Element processedBehaviors) {
        Element processedTyping = document.createElement("typing");
        processedTyping.setAttribute("string", str);
        processedTyping.setAttribute("timestamp", timestamp);
        processedTyping.setAttribute("path", path);
        processedTyping.setAttribute("line", line);
        processedTyping.setAttribute("column", column);
        processedTyping.setAttribute("length", String.valueOf(str.length()));
        processedTyping.setAttribute("duration", duration);
        processedBehaviors.appendChild(processedTyping);
    }

}
