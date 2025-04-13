package messages;

import java.io.Serial;

public class StateMessage implements Message<String> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    public StateMessage(String message) {
        this.message = message;
    }

    @Override
    public String get() {
        return message;
    }
}
