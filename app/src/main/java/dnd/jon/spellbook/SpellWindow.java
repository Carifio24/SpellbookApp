package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
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

        // Next, the labels
        final TextView schoolLabel = makeLabel("School");
        final TextView levelLabel = makeLabel("Level");
        final TextView rangeLabel = makeLabel("Range");
        final TextView concentrationLabel = makeLabel("Concentration");
        final TextView ritualLabel = makeLabel("Ritual");
        final TextView durationLabel = makeLabel("Duration");
        final TextView componentsLabel = makeLabel("Components");
        final TextView materialsLabel = makeLabel("Material");
        final TextView classesLabel = makeLabel("Classes");
        final TextView subclassesLabel = makeLabel("Subclasses");
        final TextView descriptionLabel = makeLabel("Description");


    }

    TextView makeLabel(String field) {
        TextView label = new TextView(this);
        label.setText(field + ": ");
        label.setTypeface(null, Typeface.BOLD);
        return label;
    }


}
