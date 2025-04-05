package messages;

import java.io.Serial;

public class GreetingMessage implements Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;
    private int sessionId;

    public GreetingMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }
}
