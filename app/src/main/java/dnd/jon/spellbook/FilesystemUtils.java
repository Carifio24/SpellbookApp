package dnd.jon.spellbook;

import android.content.Context;
import android.util.Log;

import java.io.File;

class FilesystemUtils {

    private static final String LOGGING_TAG = "filesystem_utils";

    static File createFileDirectory(Context context, String directoryName) {
        final File directory = new File(context.getApplicationContext().getFilesDir(), directoryName);
        if ( !(directory.exists() && directory.isDirectory()) ) {
            final boolean success = directory.mkdir();
            if (!success) {
                Log.v(LOGGING_TAG, "Error creating directory: " + directory); // Add something real here eventually
            }
        }
        return directory;
    }
}
