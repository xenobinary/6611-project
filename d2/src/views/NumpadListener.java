/**
 * Listener interface for numpad input events.
 * Implemented by panels that need to respond to numeric keypad presses,
 * dot entry, clear/delete, and OK/cancel actions.
 */
public interface NumpadListener {
    /**
     * Called when a number key is pressed.
     *
     * @param n the digit (0-9)
     */
    void onNumber(int n);

    /** Called when the dot key is pressed. */
    void onDot();

    /** Called when the clear key is pressed. */
    void onClear();

    /** Called when the delete/backspace key is pressed. */
    void onDelete();

    /** Called when the OK/enter key is pressed. */
    void onOk();

    /** Called when the cancel key is pressed. */
    void onCancel();
}
