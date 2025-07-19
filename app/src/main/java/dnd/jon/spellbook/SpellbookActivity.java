package dnd.jon.spellbook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

public class SpellbookActivity extends AppCompatActivity {

    // ID for the current theme
    private int currentTheme = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int theme = SpellbookUtils.themeForPreferences(this, prefs);
        // No need to recreate the theme here, as the content view hasn't been set up
        // In fact, if we do recreate, we end up in an endless loop
        updateTheme(theme, false);

        // Apply edge-to-edge for the app content
        // This is default on Android 15, so it seems easiest to enforce this everywhere
        // and not have to maintain two layouts
        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
    }

    protected void updateTheme(int theme, boolean recreate) {
        getApplication().setTheme(theme);
        setTheme(theme);
        if (theme != currentTheme) {
            currentTheme = theme;
            if (recreate) {
                this.recreate();
            }
        }
    }

    protected void updateTheme(int theme) {
        updateTheme(theme, true);
    }

}
