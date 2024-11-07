package dnd.jon.spellbook;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.javatuples.Pair;

import java.io.File;

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

}
