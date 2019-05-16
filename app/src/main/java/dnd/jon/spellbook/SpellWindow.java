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
    Bitmap fav_filled;
    Bitmap fav_empty;
    Bitmap known_filled;
    Bitmap known_empty;
    Bitmap prepared_filled;
    Bitmap prepared_empty;
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

    private float scale;

    private static double imageDPfrac = 0.07;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SpellWindowBinding binding = DataBindingUtil.setContentView(this, R.layout.spell_window);

        // Get the scale
        scale = this.getResources().getDisplayMetrics().density;

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

        // Get the window size
        // Get the height and width of the display
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Adjust for margins
        Configuration config = this.getResources().getConfiguration();
        int dpWidth = config.screenWidthDp;
        int margin_left = 10;
        int margin_right = 10;
        int margin_top = 5;
        int margin_bottom = 20;
        int margin_horizontal = margin_left + margin_right;
        int margin_vertical = margin_top + margin_bottom;
        width = width - Math.round(width*margin_horizontal/dpWidth);
        //height = height - Math.round(height*margin_vertical/dpWidth);

        // Set the width for the title and the button
        int titleWidth = (int) Math.round(width*0.9);
        int buttonWidth = width - titleWidth;

        // Make the bitmaps for the buttons
        int dpHeight = config.screenHeightDp - margin_vertical;
        int bitmapSize = (int) Math.round(imageDPfrac * dpHeight);
        int bitmapDim = (bitmapSize > buttonWidth ? buttonWidth : bitmapSize);
        int bitmapWidth = bitmapDim;
        int bitmapHeight = bitmapDim;
//        System.out.println("height is " + height);
//        System.out.println("bitmapSize is " + bitmapSize);
//        System.out.println("bitmapDim is " + bitmapDim);
//        System.out.println("bitmapWidth is " + bitmapWidth);
//        System.out.println("bitmapHeight is " + bitmapHeight);
//        System.out.println("dpHeight is " + dpHeight);
        fav_filled = createBitmap(R.mipmap.star_filled, bitmapWidth, bitmapHeight);
        fav_empty = createBitmap(R.mipmap.star_empty, bitmapWidth, bitmapHeight);
        known_filled = createBitmap(R.mipmap.book_filled, bitmapWidth, bitmapHeight);
        known_empty = createBitmap(R.mipmap.book_empty, bitmapWidth, bitmapHeight);
        prepared_filled = createBitmap(R.mipmap.wand_filled, bitmapWidth, bitmapHeight);
        prepared_empty = createBitmap(R.mipmap.wand_empty, bitmapWidth, bitmapHeight);
        int imageHeight = fav_filled.getHeight();
        int imageWidth = fav_filled.getWidth();

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

    Bitmap createBitmap(int imageID, int bitmapWidth, int bitmapHeight) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), imageID);
        int w = Math.round(bitmapWidth * scale);
        int h = Math.round(bitmapHeight * scale);
        bmp = Bitmap.createScaledBitmap(bmp, w, h, true);
        return bmp;
    }

    void updateFavButton() {
        if (favorite) {
            favButton.setImageBitmap(fav_filled);
            //favButton.setImageResource(R.mipmap.star_filled);
            //favButton.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            favButton.setImageBitmap(fav_empty);
            //favButton.setImageResource(R.mipmap.star_empty);
            //favButton.setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    void updateKnownButton() {
        if (known) {
            knownButton.setImageBitmap(known_filled);
        } else {
            knownButton.setImageBitmap(known_empty);
        }
    }

    void updatePreparedButton() {
        if (prepared) {
            preparedButton.setImageBitmap(prepared_filled);
        } else {
            preparedButton.setImageBitmap(prepared_empty);
        }
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
