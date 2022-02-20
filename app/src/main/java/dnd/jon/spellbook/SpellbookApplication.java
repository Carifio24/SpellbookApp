package dnd.jon.spellbook;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class SpellbookApplication extends Application {

    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

}