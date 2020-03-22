package dnd.jon.spellbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.content.Intent;
import android.graphics.Color;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public final class SpellWindow extends AppCompatActivity {

    private Intent returnIntent;
    private ToggleButton favButton;
    private ToggleButton knownButton;
    private ToggleButton preparedButton;

    static final String SPELL_KEY = "spell";
    static final String TEXT_SIZE_KEY = "textSize";
    static final String INDEX_KEY = "index";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.spell_window);
        SpellWindowBinding binding = DataBindingUtil.setContentView(this, R.layout.spell_window);

        // Set values from intent
        final Intent intent = getIntent();
        final Spell spell = intent.getParcelableExtra(SPELL_KEY);
        final int index = intent.getIntExtra(INDEX_KEY,-1);
        //final int spellTextSize = intent.getIntExtra(TEXT_SIZE_KEY, Settings.defaultSpellTextSize);
        boolean favorite = intent.getBooleanExtra(FAVORITE_KEY, false);
        boolean prepared = intent.getBooleanExtra(PREPARED_KEY, false);
        boolean known = intent.getBooleanExtra(KNOWN_KEY, false);
        binding.setSpell(spell);

        // Create the return intent
        returnIntent = new Intent(SpellWindow.this, MainActivity.class);
        returnIntent.putExtra(SPELL_KEY, spell);
        returnIntent.putExtra(FAVORITE_KEY, favorite);
        returnIntent.putExtra(KNOWN_KEY, known);
        returnIntent.putExtra(PREPARED_KEY, prepared);
        returnIntent.putExtra(INDEX_KEY, index);

        // Set the button actions
        // The favorites button
        favButton = this.findViewById(R.id.favorite_button);
        favButton.setOnClickListener( (v) -> returnIntent.putExtra(FAVORITE_KEY, favButton.isSet()));
        favButton.set(favorite);

        // The known button
        knownButton = this.findViewById(R.id.known_button);
        knownButton.setOnClickListener( (v) -> returnIntent.putExtra(KNOWN_KEY, knownButton.isSet()) );
        knownButton.set(known);

        // The prepared button
        preparedButton = this.findViewById(R.id.prepared_button);
        preparedButton.setBackgroundColor(Color.TRANSPARENT);
        preparedButton.setOnClickListener( (v) -> returnIntent.putExtra(PREPARED_KEY, preparedButton.isSet()) );
        preparedButton.set(prepared);

        // Set buttons from Bundle (if we're coming from a rotation)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FAVORITE_KEY)) {
                favorite = savedInstanceState.getBoolean(FAVORITE_KEY);
                returnIntent.putExtra(FAVORITE_KEY, favorite);
                favButton.set(favorite);
            }
            if (savedInstanceState.containsKey(PREPARED_KEY)) {
                prepared = savedInstanceState.getBoolean(PREPARED_KEY);
                returnIntent.putExtra(PREPARED_KEY, prepared);
                preparedButton.set(prepared);
            }
            if (savedInstanceState.containsKey(KNOWN_KEY)) {
                known = savedInstanceState.getBoolean(KNOWN_KEY);
                returnIntent.putExtra(KNOWN_KEY, known);
                knownButton.set(known);
            }
        }

        ScrollView scroll = this.findViewById(R.id.spell_window_scroll);
        final Activity thisActivity = this;
        scroll.setOnTouchListener(new OnSwipeTouchListener(thisActivity) {

            @Override
            public void onSwipeRight() {
                setResult(Activity.RESULT_OK, returnIntent);
                thisActivity.finish();
            }
        });


    }

    private void setImageResourceBoolean(ImageButton ib, boolean b, int imageResT, int imageResF) {
        int imageRes = b ? imageResT : imageResF;
        ib.setImageResource(imageRes);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, R.anim.left_to_right_exit);
    }


    // Necessary for handling rotations (phone only, since we don't ever use this activity on a tablet)
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAVORITE_KEY, favButton.isSet());
        outState.putBoolean(PREPARED_KEY, preparedButton.isSet());
        outState.putBoolean(KNOWN_KEY, knownButton.isSet());
    }


}
