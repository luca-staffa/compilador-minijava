package sourcemanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;

public class SourceManagerEficiente implements SourceManager {
    private PushbackReader reader;
    private int lineNumber = 1;
    private boolean pendingNewLineIncrement;

    public SourceManagerEficiente() {
        pendingNewLineIncrement = false;
    }

    @Override
    public void open(String filePath) throws FileNotFoundException {
        reader = new PushbackReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8),
                1
        );
        lineNumber = 1;
        pendingNewLineIncrement = false;
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    @Override
    public char getNextChar() throws IOException {
        if (pendingNewLineIncrement) {
            lineNumber++;
            pendingNewLineIncrement = false;
        }

        int c = reader.read();

        if (c == '\r') {
            int next = reader.read();
            if (next != -1 && next != '\n') {
                reader.unread(next);
            }
            pendingNewLineIncrement = true;
            return '\n';
        }

        if (c == '\n') {
            pendingNewLineIncrement = true;
            return '\n';
        }

        if (c == -1) {
            return END_OF_FILE;
        }

        return (char) c;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }
}
