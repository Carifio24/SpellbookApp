package dnd.jon.spellbook;

import android.os.Build;
import android.os.LocaleList;

import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocalizationUtils {

    static String getCurrentLanguage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return LocaleList.getDefault().get(0).getLanguage();
        } else{
            return Locale.getDefault().getLanguage();
        }
    }

}
