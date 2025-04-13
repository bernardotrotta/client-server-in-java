package messages;

import java.io.Serializable;

public interface Message<T> extends Serializable {
    T get();
}
