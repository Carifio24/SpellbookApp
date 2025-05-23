package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

import android.view.View;

public class ToggleButton extends androidx.appcompat.widget.AppCompatImageButton {

    // Constructors
    // First constructor is public so that it can be used via XML
    public ToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleButton, 0, 0);
        resT = attributes.getResourceId(R.styleable.ToggleButton_resourceTrue, 0);
        resF = attributes.getResourceId(R.styleable.ToggleButton_resourceFalse, 0);
        on = false;
        initialSetup();
        attributes.recycle();
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
        setOnClickListener(v -> toggle());

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

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener((v) -> {
            toggle();
            listener.onClick(v);
        });
    }

    // Member values
    private final int resT;
    private final int resF;
    private boolean on;
}
