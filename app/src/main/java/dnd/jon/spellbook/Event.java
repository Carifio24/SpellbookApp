package dnd.jon.spellbook;

class Event<T> {

    private final T content;

    private boolean handled = false;

    Event(T content) {
        this.content = content;
    }

    boolean hasBeenHandled() {
        return handled;
    }

    T getContentIfHandled() {
        if (handled) {
            return null;
        } else {
            handled = true;
            return content;
        }
    }

    T peekContent() {
        return content;
    }

}
