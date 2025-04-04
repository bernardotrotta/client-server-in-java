package messages;

import java.io.Serial;

public class StateMessage implements Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    public StateMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
