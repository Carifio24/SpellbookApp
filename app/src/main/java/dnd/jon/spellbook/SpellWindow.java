package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

import dnd.jon.spellbook.databinding.SpellWindowActivityBinding;

public final class SpellWindow extends AppCompatActivity {

    static final String SPELL_KEY = "spell";
    static final String TEXT_SIZE_KEY = "textSize";
    static final String INDEX_KEY = "index";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";
    static final String USE_EXPANDED_KEY = "use_expanded";

    private static final String FRAGMENT_TAG = "spell_window_fragment";

    private Intent returnIntent;
    private SpellWindowActivityBinding binding;
    private SpellWindowFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = SpellWindowActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set values from intent
        final Intent intent = getIntent();
        final Spell spell = intent.getParcelableExtra(SPELL_KEY);
        final int index = intent.getIntExtra(INDEX_KEY,-1);
        final boolean useExpanded = intent.getBooleanExtra(USE_EXPANDED_KEY, false);
        //final int spellTextSize = intent.getIntExtra(TEXT_SIZE_KEY, Settings.defaultSpellTextSize);
        boolean favorite = intent.getBooleanExtra(FAVORITE_KEY, false);
        boolean prepared = intent.getBooleanExtra(PREPARED_KEY, false);
        boolean known = intent.getBooleanExtra(KNOWN_KEY, false);

        final SpellStatus status = new SpellStatus(favorite, prepared, known);

        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putParcelable(SpellWindowFragment.SPELL_STATUS_KEY, status);
        fragmentArgs.putBoolean(USE_EXPANDED_KEY, useExpanded);

        fragment = new SpellWindowFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.spell_window_fragment_container, fragment, FRAGMENT_TAG)
                .commit();

        // Create the return intent
        returnIntent = new Intent(SpellWindow.this, MainActivity.class);
        returnIntent.putExtra(SPELL_KEY, spell);
        returnIntent.putExtra(FAVORITE_KEY, favorite);
        returnIntent.putExtra(KNOWN_KEY, known);
        returnIntent.putExtra(PREPARED_KEY, prepared);
        returnIntent.putExtra(INDEX_KEY, index);

        // Set buttons from Bundle (if we're coming from a rotation)
        if (savedInstanceState != null) {
            final SpellStatus savedStatus = new SpellStatus();
            if (savedInstanceState.containsKey(FAVORITE_KEY)) {
                favorite = savedInstanceState.getBoolean(FAVORITE_KEY);
                returnIntent.putExtra(FAVORITE_KEY, favorite);
                savedStatus.favorite = favorite;
            }
            if (savedInstanceState.containsKey(PREPARED_KEY)) {
                prepared = savedInstanceState.getBoolean(PREPARED_KEY);
                returnIntent.putExtra(PREPARED_KEY, prepared);
                savedStatus.prepared = prepared;
            }
            if (savedInstanceState.containsKey(KNOWN_KEY)) {
                known = savedInstanceState.getBoolean(KNOWN_KEY);
                returnIntent.putExtra(KNOWN_KEY, known);
                savedStatus.known = known;
            }
        }

        fragment.updateSpell(spell);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAVORITE_KEY, returnIntent.getBooleanExtra(FAVORITE_KEY, false));
        outState.putBoolean(PREPARED_KEY, returnIntent.getBooleanExtra(PREPARED_KEY, false));
        outState.putBoolean(KNOWN_KEY, returnIntent.getBooleanExtra(KNOWN_KEY, false));
    }


    public SpellStatus getSpellStatus(Spell spell) {
        return new SpellStatus(returnIntent.getBooleanExtra(FAVORITE_KEY, false),
                    returnIntent.getBooleanExtra(KNOWN_KEY, false),
                    returnIntent.getBooleanExtra(PREPARED_KEY, false));
    }

    public void updateFavorite(Spell spell, boolean favorite) {
        returnIntent.putExtra(FAVORITE_KEY, favorite);
    }

    public void updateKnown(Spell spell, boolean known) {
        returnIntent.putExtra(KNOWN_KEY, known);
    }

    public void updatePrepared(Spell spell, boolean prepared) {
        returnIntent.putExtra(PREPARED_KEY, prepared);
    }

    public void handleSpellWindowClose() {
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
