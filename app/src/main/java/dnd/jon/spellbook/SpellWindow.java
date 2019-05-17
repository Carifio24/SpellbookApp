package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public final class SpellWindow extends Activity {

    private Spell spell;
    Intent returnIntent;
    ImageButton favButton;
    ImageButton knownButton;
    ImageButton preparedButton;
    int spellTextSize;

    boolean favorite;
    boolean known;
    boolean prepared;

    static final String SPELL_KEY = "spell";
    static final String TEXT_SIZE_KEY = "textSize";
    static final String INDEX_KEY = "index";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";

    private static final int favorite_filled = R.mipmap.star_filled;
    private static final int favorite_empty = R.mipmap.star_empty;
    private static final int prepared_filled = R.mipmap.wand_filled;
    private static final int prepared_empty = R.mipmap.wand_empty;
    private static final int known_filled = R.mipmap.book_filled;
    private static final int known_empty = R.mipmap.book_empty;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.spell_window);
        SpellWindowBinding binding = DataBindingUtil.setContentView(this, R.layout.spell_window);

        Intent intent = getIntent();
        spell = intent.getParcelableExtra(SPELL_KEY);
        int index = intent.getIntExtra(INDEX_KEY,-1);
        spellTextSize = intent.getIntExtra(TEXT_SIZE_KEY, Settings.defaultSpellTextSize);
        favorite = intent.getBooleanExtra(FAVORITE_KEY, false);
        prepared = intent.getBooleanExtra(PREPARED_KEY, false);
        known = intent.getBooleanExtra(KNOWN_KEY, false);
        binding.setSpell(spell);

        returnIntent = new Intent(SpellWindow.this, MainActivity.class);
        returnIntent.putExtra(INDEX_KEY, index);
        returnIntent.putExtra(FAVORITE_KEY, favorite);
        returnIntent.putExtra(KNOWN_KEY, known);
        returnIntent.putExtra(PREPARED_KEY, prepared);

        // Set the button actions
        // The favorites button
        favButton = this.findViewById(R.id.favorite_button);
        favButton.setOnClickListener((v) -> {
            switchFavorite();
            returnIntent.putExtra(FAVORITE_KEY, favorite);
            updateFavButton();
        });
        updateFavButton();

        // The known button
        knownButton = this.findViewById(R.id.known_button);
        knownButton.setOnClickListener((v) -> {
            switchKnown();
            returnIntent.putExtra(KNOWN_KEY, known);
            updateKnownButton();
        });
        updateKnownButton();

        // The prepared button
        preparedButton = this.findViewById(R.id.prepared_button);
        preparedButton.setBackgroundColor(Color.TRANSPARENT);
        preparedButton.setOnClickListener((v) -> {
            switchPrepared();
            returnIntent.putExtra(PREPARED_KEY, prepared);
            updatePreparedButton();
        });
        updatePreparedButton();

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

    void updateFavButton() {
        setImageResourceBoolean(favButton, favorite, favorite_filled, favorite_empty);
    }

    void updateKnownButton() {
        setImageResourceBoolean(knownButton, known, known_filled, known_empty);
    }

    void updatePreparedButton() {
        setImageResourceBoolean(preparedButton, prepared, prepared_filled, prepared_empty);
    }

    void switchFavorite() {
        favorite = !favorite;
    }

    void switchPrepared() {
        prepared = !prepared;
    }

    void switchKnown() {
        known = !known;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }


}
