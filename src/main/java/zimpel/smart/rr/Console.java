package zimpel.smart.rr;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;


public class Console extends OutputStream {

    private final JTextArea terminalWindow;
    private final StringBuilder sb = new StringBuilder();
    private String prompt;

    public Console(JTextArea terminalWindow, String prompt) {
        this.terminalWindow = terminalWindow;
        this.prompt = prompt;
        sb.append(prompt);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\r')
            return;

        if (b == '\n') {
            final String text = sb.toString() + "\n";
            SwingUtilities.invokeLater(() -> terminalWindow.append(text));
            sb.setLength(0);
            sb.append(prompt).append(" ");
            return;
        }

        sb.append((char) b);
    }
}
