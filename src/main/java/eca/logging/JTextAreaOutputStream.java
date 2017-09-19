package eca.logging;

import org.springframework.util.Assert;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Roman Batygin
 */

public class JTextAreaOutputStream extends OutputStream {

    private JTextArea textArea;

    public JTextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        Assert.notNull(b, "Buffer is not Specified!");
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        String text = new String(b, off, len);
        textArea.append(text);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
