package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.util.function.Consumer;

public class ToggleButton extends androidx.appcompat.widget.AppCompatImageButton {

    // Constructors
    // First constructor is public so that it can be used via XML
    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleButton, 0, 0);
        resT = a.getResourceId(R.styleable.ToggleButton_resourceTrue, 0);
        resF = a.getResourceId(R.styleable.ToggleButton_resourceFalse, 0);
        on = false;
        initialSetup();
        a.recycle();
    }

    ToggleButton(Context context, int imageResF, int imageResT, boolean b) {
        super(context);
        resF = imageResF;
        resT = imageResT;
        on = b;
        initialSetup();
    }

    // For use in constructor
    private void initialSetup() {

        // Set the button to the correct status
        set(on);

        // Set what happens when the button is pressed
        setOnClickListener((v) -> {
            toggle();
            callback.run();
        });

        setOnLongClickListener((v) -> {
            longPressCallback.run();
            return true;
        });

    }

    // Set the button to a specified state
    void set(boolean b) {
        on = b;
        final int toSet = on ? resT : resF;
        setImageResource(toSet);
    }

    // Toggle the button
    void toggle() { set(!on); }

    boolean isSet() { return on; }

    void setCallback(Runnable r) {
        callback = r;
    }

    void setCallback(Consumer<ToggleButton> cv) {
        callback = () -> cv.accept(this);
    }

    void setLongPressCallback(Runnable r) { longPressCallback = r; }
    void setLongPressCallback(Consumer<ToggleButton> cv) { longPressCallback = () -> cv.accept(this); }

    // Member values
    private final int resT;
    private final int resF;
    private boolean on;
    private Runnable callback = () -> {}; // At creation, callback does nothing
    private Runnable longPressCallback = () -> {}; // At creation, does nothing
}
