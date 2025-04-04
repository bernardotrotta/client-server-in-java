package messages;

import java.io.Serial;

public class Price implements Message {
    @Serial
    private static final long serialVersionUID = 1L;
    private final double price;

    public Price(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}
