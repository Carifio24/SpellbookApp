package dnd.jon.spellbook;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class SpellbookApplication extends Application {

    public void onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        final int theme = SpellbookUtils.themeForContext(this);
        setTheme(theme);

        super.onCreate();
    }

}