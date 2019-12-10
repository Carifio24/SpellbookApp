package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.util.function.Consumer;

public class ToggleButton extends android.support.v7.widget.AppCompatImageButton {

    // Constructors
    // First constructor is public so that it can be used via XML
    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleButton, 0, 0);
        resT = a.getResourceId(R.styleable.ToggleButton_resourceTrue, 0);
        resF = a.getResourceId(R.styleable.ToggleButton_resourceFalse, 0);
        on = a.getBoolean(R.styleable.ToggleButton_set, false);
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

    }

    // Set the button to a specified state
    void set(boolean b) {
        on = b;
        int toSet = on ? resT : resF;
        setImageResource(toSet);
    }

    // Toggle the button
    private void toggle() {
        on = !on;
        set(on);
    }

    boolean isSet() { return on; }

    void setCallback(Runnable r) {
        callback = r;
    }

    void setCallback(Consumer<Void> cv) {
        callback = () -> cv.accept(null);
    }

    // Member values
    private int resT;
    private int resF;
    private boolean on;
    private Runnable callback = () -> {}; // At creation, callback does nothing
}
