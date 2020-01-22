package dnd.jon.spellbook;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

public class BindingAdapterUtils {

    @BindingAdapter({"promptText", "otherText"})
    public static void promptFormat(TextView tv, String promptText, String otherText) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(promptText + ": " + otherText);
        ssb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, promptText.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb);
    }

    @BindingAdapter({"level", "schoolName", "ritual"})
    public static void schoolLevelText(TextView tv, int level, String schoolName, boolean ritual) {
        String ordinal;
        switch (level) {
            case 0:
                String text = schoolName + " cantrip";
                tv.setText(text); return;
            case 1:
                ordinal = level + "st-level "; break;
            case 2:
                ordinal = level + "nd-level "; break;
            case 3:
                ordinal = level + "rd-level "; break;
            default:
                ordinal = level + "th-level ";
        }
        String text = ordinal + schoolName.toLowerCase();
        if (ritual) {
            text = text + " (ritual)";
        }
        tv.setText(text);
    }

    @BindingAdapter("set")
    public static void setToggleButton(ToggleButton button, boolean set) {
        button.set(set);
    }

}
