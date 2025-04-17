package dnd.jon.spellbook;

import androidx.databinding.BindingAdapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.widget.TextView;

public class BindingAdapterUtils {

    @BindingAdapter({"promptText", "otherText"})
    public static void promptFormat(TextView tv, String promptText, String otherText) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(promptText + ": " + otherText);
        ssb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, promptText.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb);
    }

    public static String schoolLevelText(Context context, int level, String schoolName) {
        String text;
        if (level == 0) {
            text = context.getString(R.string.school_cantrip, schoolName);
            text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        } else {
            final String ordinal = level + DisplayUtils.ordinalString(context, level);
            text = context.getString(R.string.ordinal_school, ordinal, schoolName.toLowerCase());
        }
        return text;
    }

    @BindingAdapter({"context", "level", "schoolName", "ritual"})
    public static void schoolLevelRitualText(TextView tv, Context context, int level, String schoolName, boolean ritual) {

        String text = schoolLevelText(context, level, schoolName);
        if (ritual) {
            final String ritualString = context.getString(R.string.ritual).toLowerCase();
            text += String.format(" (%s)", ritualString);
        }
        tv.setText(text);
    }

    @BindingAdapter({"context", "level", "schoolName", "ritual", "concentration"})
    public static void schoolLevelRitualConcentrationText(TextView tv, Context context, int level, String schoolName, boolean ritual, boolean concentration) {

        String text = schoolLevelText(context, level, schoolName);

        if (ritual || concentration) {
            final StringBuilder builder = new StringBuilder(text);
            builder.append(" (");
            if (ritual) {
                builder.append(context.getString(R.string.ritual).toLowerCase());
            }
            if (ritual && concentration) {
                builder.append(", ");
            }
            if (concentration) {
                builder.append(context.getString(R.string.concentration_abbr));
            }
            builder.append(")");
            text = builder.toString();
        }
        tv.setText(text);
    }

    @BindingAdapter("set")
    public static void setToggleButton(ToggleButton button, boolean set) {
        button.set(set);
    }

    @BindingAdapter({"context", "slotsRowLevel"})
    public static void setSlotsRowText(TextView tv, Context context, int level) {
        tv.setText(context.getString(R.string.prompt, context.getString(R.string.level_number, level), ""));
    }

    @BindingAdapter({"context", "slotAdjustTotalLevel"})
    public static void setSlotAdjustTotalText(TextView tv, Context context, int level) {
        tv.setText(context.getString(R.string.level_number, level));
    }

    @BindingAdapter("android:textSize")
    public static void setTextSize(TextView tv, float size) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }
}
