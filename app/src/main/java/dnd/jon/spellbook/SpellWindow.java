package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.content.Intent;
import android.graphics.Typeface;

public final class SpellWindow extends Activity {

    private Spell spell;
    PopupWindow popup;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        spell = intent.getParcelableExtra("spell");

        // Get the window size
        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Create the popup window
        super.onCreate(savedInstanceState);
        popup = new PopupWindow(this);

        // Add the spell text
        // Start with the title
        final TextView title = new TextView(this);
        title.setText(spell.getName());

        TextView schoolLabel = makeTextView("School: ", Spellbook.schoolNames[spell.getSchool().value]);
        TextView levelLabel = makeTextView("Level: ", Integer.toString(spell.getLevel()));


    }

    TextView makeTextView(String label, String text) {
        SpannableStringBuilder str = new SpannableStringBuilder(label + text);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView label = new TextView(this);
        label.setText(str);
        return label;
    }


}
