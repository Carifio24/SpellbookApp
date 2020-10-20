package dnd.jon.spellbook;

import android.content.Context;
import android.text.TextUtils;

import java.util.Collection;

public class DisplayNameUtils {

    public static <T extends NameDisplayable> String getDisplayName(T item, Context context) {
        return context.getString(item.getDisplayNameID());
    }

    public static String classesString(Spell spell, Context context) {
        final Collection<CasterClass> classes = spell.getClasses();
        final String[] classStrings = new String[classes.size()];
        int i = 0;
        for (CasterClass cc : classes) {
            classStrings[i++] = getDisplayName(cc, context);
        }
        return TextUtils.join(", ", classStrings);
    }

}
