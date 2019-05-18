package dnd.jon.spellbook;

import android.databinding.BindingAdapter;
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

}
