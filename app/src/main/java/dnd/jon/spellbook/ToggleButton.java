package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.util.function.Consumer;

public class ToggleButton extends android.support.v7.widget.AppCompatImageButton {

    // Constructors
    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleButton, 0, 0);
        resT = a.getResourceId(R.styleable.ToggleButton_resourceTrue, 0);
        resF = a.getResourceId(R.styleable.ToggleButton_resourceFalse, 0);
        on = a.getBoolean(R.styleable.ToggleButton_set, false);
        set(on);

        // Set what happens when the button is pressed
        setOnClickListener((v) -> {
            callback.run();
            toggle();
        });
}

    ToggleButton(Context context, int imageResF, int imageResT, boolean b) {
        super(context);
        resF = imageResF;
        resT = imageResT;
        on = b;
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

    void setCallback(Runnable r) {
        callback = r;
    }

    void setCallback(Consumer<Void> cv) {
        callback = () -> cv.accept(null);
    }

    // Member values
    int resT;
    int resF;
    boolean on;
    Runnable callback;
}
