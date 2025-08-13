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
    static final String LANGUAGE_KEY = "language";
    private static final String TAG = "shortcut_spell_window_activity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SpellWindowBinding binding = SpellWindowBinding.inflate(getLayoutInflater());

        final View rootView = binding.getRoot();
        setContentView(rootView);
        AndroidUtils.applyDefaultWindowInsets(rootView);



        // Since the shortcut isn't associated with any particular character,
        // we set this to true so that we're giving all of the information
        binding.setUseExpanded(true);

        // We don't really offer any way to change these yet, so for now we can just use defaults
        // When we do offer a way to change these in the app settings, we'll want to respect that here
        binding.setTextColor(getColor(AndroidUtils.resourceIDForAttribute(this, R.attr.defaultTextColor)));
        binding.setTextSize(14);

        // Set values from intent
        final Intent intent = getIntent();

        Locale locale = null;
        if (intent.hasExtra(LANGUAGE_KEY)) {
            final String languageCode = intent.getStringExtra(LANGUAGE_KEY);
            if (languageCode != null) {
                locale = new Locale(languageCode);
            }
        }
        if (locale == null) {
            locale = SpellbookUtils.spellsLocale(this);
        }
        final Context context = LocalizationUtils.getLocalizedContext(this, locale);
        binding.setContext(context);

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
                    this.finish();
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
