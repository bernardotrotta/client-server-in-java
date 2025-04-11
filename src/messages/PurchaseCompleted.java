package messages;

import java.io.Serial;

public class PurchaseCompleted implements Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    public PurchaseCompleted(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
