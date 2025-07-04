// File: dev/vtvinh24/ezquiz/util/SingleEvent.java
package dev.vtvinh24.ezquiz.util;

public class SingleEvent<T> {
    private T content;
    private boolean hasBeenHandled = false;

    public SingleEvent(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }
}