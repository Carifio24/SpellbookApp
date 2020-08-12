package dnd.jon.spellbook;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

class AndroidUtils {

    private static final String LOGGING_TAG = "android_utils";

    static String stringFromID(Context context, int stringID) { return context.getResources().getString(stringID); }
    static int integerFromID(Context context, int integerID) { return context.getResources().getInteger(integerID); }
    static float dimensionFromID(Context context, int dimensionID) { return context.getResources().getDimension(dimensionID); }

    static void setNumberText(TextView tv, int number) {
        tv.setText(String.format(Locale.US, "%d", number));
    }

    static <T> void setSpinnerByItem(Spinner spinner, T item) {
        try {
            final ArrayAdapter<T> adapter = (ArrayAdapter<T>) spinner.getAdapter();
            spinner.setSelection(adapter.getPosition(item));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    static void copyAssetToData(Context context, String assetFilePath, String destinationFilePath) {
        final File dataDir = context.getDataDir();
        final File destination = new File(dataDir, destinationFilePath);
        try {
            if (!destination.exists()) {
                destination.createNewFile();
            }
        } catch (Exception e) {
            Log.e(LOGGING_TAG, SpellbookUtils.stackTrace(e));
        }
        try (InputStream in = context.getAssets().open(assetFilePath);
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination))
        ) {
            byte[] buffer = new byte[in.available()];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.e(LOGGING_TAG, SpellbookUtils.stackTrace(e));
        }

    }

    // Get the value of a LiveData, with a default value if the value is null
    static <T> T getValueWithDefault(LiveData<T> liveData, T defaultValue) {
        final T value = liveData.getValue();
        return (value != null) ? value : defaultValue;
    }



}