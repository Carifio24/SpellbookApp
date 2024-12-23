package dnd.jon.spellbook;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.DisplayMetrics;
import android.util.Log;


import org.javatuples.Pair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

class AndroidUtils {
    private static final String LOGGING_TAG = "android_utils";
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

    static Intent openDocumentIntent(String fileType, String initialURI) {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(fileType);

        if (initialURI != null && OSUtils.getSDKInt() >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialURI);
        }

        return intent;
    }

    static void copyToClipboard(Context context, String text, String label) {
       final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
       final ClipData clipData = ClipData.newPlainText(label, text);
       clipboardManager.setPrimaryClip(clipData);
    }

    static void copyAssetToData(Context context, String assetFilePath, String destinationFilePath) {
        final File dataDir = context.getDataDir();
        final File destination = new File(dataDir, destinationFilePath);
        try {
            if (!destination.exists()) {
                System.out.println("The destination is " + destination);
                final boolean created = destination.createNewFile();
                Log.d("AndroidUtils", "Created file at " + destination.getAbsolutePath() + ": " + created);
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
            System.out.println("Exception encountered!");
            Log.e(LOGGING_TAG, SpellbookUtils.stackTrace(e));
        }

    }

}
