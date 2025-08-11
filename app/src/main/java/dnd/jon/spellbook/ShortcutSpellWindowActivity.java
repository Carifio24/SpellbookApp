package dnd.jon.spellbook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import dnd.jon.spellbook.databinding.SpellWindowBinding;

public class ShortcutSpellWindowActivity extends SpellbookActivity {

    private boolean closeOnFinish = false;

    static final String SPELL_JSON_KEY = "spell_json";
    static final String CLOSE_ON_FINISH_KEY = "exit_on_close";
    private static final String TAG = "shortcut_spell_window_activity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SpellWindowBinding binding = SpellWindowBinding.inflate(getLayoutInflater());

        final View rootView = binding.getRoot();
        setContentView(rootView);
        AndroidUtils.applyDefaultWindowInsets(rootView);

        final Locale locale = SpellbookUtils.spellsLocale(this);
        final Context context = LocalizationUtils.getLocalizedContext(this, locale);
        binding.setContext(context);

        // TODO: Make these respect current settings, in some form
        binding.setUseExpanded(true);
        binding.setTextColor(getColor(AndroidUtils.resourceIDForAttribute(this, R.attr.defaultTextColor)));
        binding.setTextSize(14);

        // Set values from intent
        final Intent intent = getIntent();
        if (intent.hasExtra(SPELL_JSON_KEY)) {
            final String spellJsonString = intent.getStringExtra(SPELL_JSON_KEY);
            if (spellJsonString != null) {
                final SpellCodec codec = new SpellCodec(this);
                try {
                    final JSONObject json = new JSONObject(spellJsonString);
                    final SpellBuilder builder = new SpellBuilder(this);
                    final Spell spell = codec.parseSpell(json, builder, false);
                    binding.setSpell(spell);
                    binding.spellWindowButtonGroup.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }

        closeOnFinish = intent.getBooleanExtra(CLOSE_ON_FINISH_KEY, false);
    }

    @Override
    public void finish() {
        super.finish();
        if (closeOnFinish) {
            finishAffinity();
        }
    }
}
