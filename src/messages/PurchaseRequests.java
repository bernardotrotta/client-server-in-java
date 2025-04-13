package messages;

import java.io.Serial;

public class PurchaseRequests implements Message<Void> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Void get() {
        return null;
    }
}
