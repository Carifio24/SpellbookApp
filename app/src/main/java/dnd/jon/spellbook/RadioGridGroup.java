package dnd.jon.spellbook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RadioButton;

class RadioGridGroup extends GridLayout implements View.OnClickListener {

    private RadioButton activeButton;

    RadioGridGroup(Context context) {
        super(context);
    }

    RadioGridGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if (activeButton != null) {
            activeButton.setChecked(false);
        }
        rb.setChecked(true);
        activeButton = rb;
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof RadioButton)) { return; }
        super.addView(child);
        child.setOnClickListener(this);
    }

    RadioButton getActiveButton() { return activeButton; }

}
