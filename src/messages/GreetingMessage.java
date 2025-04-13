package messages;

import java.io.Serial;

public class GreetingMessage implements Message<String> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;
    private final int sessionId;

    public GreetingMessage(String message, int sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    @Override
    public String get() {
        return message;
    }

    public int getSessionId() {
        return sessionId;
    }
}
