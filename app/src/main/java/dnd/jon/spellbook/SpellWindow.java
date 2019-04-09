package dnd.jon.spellbook;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Color;

public final class SpellWindow extends Activity {

    private Spell spell;
    TableLayout swTable;
    TableLayout swHeader;
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

    private static double imageDPfrac = 0.05;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Get the scale
        scale = this.getResources().getDisplayMetrics().density;

        Intent intent = getIntent();
        spell = intent.getParcelableExtra(SPELL_KEY);
        int index = intent.getIntExtra(INDEX_KEY,-1);
        spellTextSize = intent.getIntExtra(TEXT_SIZE_KEY, Settings.defaultSpellTextSize);
        favorite = intent.getBooleanExtra(FAVORITE_KEY, false);
        prepared = intent.getBooleanExtra(PREPARED_KEY, false);
        known = intent.getBooleanExtra(KNOWN_KEY, false);

        returnIntent = new Intent(SpellWindow.this, MainActivity.class);
        returnIntent.putExtra(INDEX_KEY, index);
        returnIntent.putExtra(FAVORITE_KEY, favorite);
        returnIntent.putExtra(KNOWN_KEY, known);
        returnIntent.putExtra(PREPARED_KEY, prepared);

        //System.out.println(spell.getName() + "'s favorite status is: " + spell.isFavorite() + " " + spell.isKnown() + " " + spell.isPrepared());

        setContentView(R.layout.spell_window);
        swTable = this.findViewById(R.id.swTable);
        swHeader = this.findViewById(R.id.swHeader);

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
        System.out.println("dpWidth: " + dpWidth);

        // Add the spell text
        // Start with the title
        final TextView title = this.findViewById(R.id.spellName);
        title.setText(spell.getName());
        //title.setTypeface(null, Typeface.BOLD);
        //title.setTextSize(30);
        //title.setTypeface(ResourcesCompat.getFont(this, R.font.cloister_black));
        //title.setTextSize(45);

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
        favButton = this.findViewById(R.id.favButton);
        favButton.setBackgroundColor(Color.TRANSPARENT);
        favButton.setOnClickListener((v) -> {
            switchFavorite();
            returnIntent.putExtra(FAVORITE_KEY, favorite);
            updateFavButton();
        });
        updateFavButton();

        // The known button
        knownButton = this.findViewById(R.id.knownButton);
        knownButton.setBackgroundColor(Color.TRANSPARENT);
        knownButton.setOnClickListener((v) -> {
            switchKnown();
            returnIntent.putExtra(KNOWN_KEY, known);
            updateKnownButton();
        });
        updateKnownButton();

        // The prepared button
        preparedButton = this.findViewById(R.id.preparedButton);
        preparedButton.setBackgroundColor(Color.TRANSPARENT);
        preparedButton.setOnClickListener((v) -> {
            switchPrepared();
            returnIntent.putExtra(PREPARED_KEY, prepared);
            updatePreparedButton();
        });
        updatePreparedButton();

        TextView schoolTV = makeTextView("School: ", Spellbook.schoolNames[spell.getSchool().value]);
        TextView levelTV = makeTextView("Level: ", Integer.toString(spell.getLevel()));
        TextView castingTimeTV = makeTextView("Casting time: ", spell.getCastingTime());
        TextView durationTV = makeTextView("Duration: ", spell.getDuration());
        TextView pageTV = makeTextView("Location: ", Spellbook.sourcebookCodes[spell.getSourcebook().value] + " " + Integer.toString(spell.getPage()));
        TextView materialsTV = makeTextView("Materials: ", spell.getMaterial());
        TextView componentsTV = makeTextView("Components: ", spell.componentsString());
        TextView rangeTV = makeTextView("Range: ", spell.getRange());
        TextView ritualTV = makeTextView("Ritual: ", Util.bool_to_yn(spell.getRitual()));
        TextView concentrationTV = makeTextView("Concentration: ", Util.bool_to_yn(spell.getConcentration()));
        TextView classesTV = makeTextView("Classes: ", spell.classesString());
        TextView descriptionTV = makeTextView("Description:\n", spell.getDescription());
        TextView higherTV = makeTextView("Higher level:\n", spell.getHigherLevelDesc());

        TableRow tr = new TableRow(this);

        // Layout configuration for the title
        TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        tlp.width = titleWidth;
        title.setLayoutParams(tlp);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        title.setWidth(titleWidth);

        // Layout configuration for the buttons
        TableLayout.LayoutParams blp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        blp.width = imageHeight;
        blp.height = imageWidth;
        favButton.setLayoutParams(blp);
        knownButton.setLayoutParams(blp);
        preparedButton.setLayoutParams(blp);
        favButton.setVisibility(View.VISIBLE);
        knownButton.setVisibility(View.VISIBLE);
        preparedButton.setVisibility(View.VISIBLE);

        // Layout configuration for the first row
        TableLayout.LayoutParams trlp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        trlp.width = width;
        tr.setLayoutParams(trlp);

        addRow(swHeader, pageTV);
        addRow(swHeader, schoolTV);
        addRow(swHeader, levelTV);
        addRow(swHeader, ritualTV);
        addRow(swHeader, concentrationTV);
        addRow(swHeader, durationTV);
        addRow(swTable, castingTimeTV);
        addRow(swTable, componentsTV);
        if (spell.getComponents()[2]) {
            addRow(swTable, materialsTV);
        }
        addRow(swTable, rangeTV);
        addRow(swTable, classesTV);
        addRow(swTable, descriptionTV);
        if (!spell.getHigherLevelDesc().equals("")) {
            addRow(swTable, higherTV);
        }

        ScrollView scroll = this.findViewById(R.id.spellscroll);
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


    TextView makeTextView(String label, String text) {
        SpannableStringBuilder str = new SpannableStringBuilder(label + text);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView tv = new TextView(this);
        tv.setText(str);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, spellTextSize);
        return tv;
    }

    void addRow(TableLayout tbl, TextView tv) {
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tr.addView(tv);
        tbl.addView(tr);
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
