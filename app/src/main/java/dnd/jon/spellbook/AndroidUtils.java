package dnd.jon.spellbook;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import org.javatuples.Pair;

class AndroidUtils {
    static Pair<Integer,Integer> screenDimensions(Activity activity) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new Pair<>(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static void addOnBackPressedCallback(AppCompatActivity activity, Runnable callback, int priority) {
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                callback.run();
            }
        });
    }

    public static void addOnBackPressedCallback(AppCompatActivity activity, Runnable callback) {
        addOnBackPressedCallback(activity, callback, OnBackInvokedDispatcher.PRIORITY_DEFAULT);
    }
}
