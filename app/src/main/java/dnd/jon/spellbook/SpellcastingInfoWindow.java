package dnd.jon.spellbook;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.webkit.WebView;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import org.sufficientlysecure.htmltextview.HtmlTextView;
import org.w3c.dom.Text;

import dnd.jon.spellbook.databinding.SpellWindowBinding;
import dnd.jon.spellbook.databinding.SpellcastingInfoActivityLayoutBinding;

public final class SpellcastingInfoWindow extends Activity {

    static final String TITLE_KEY = "title";
    static final String INFO_KEY = "info";
    static final String TABLE_KEY = "table";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final SpellcastingInfoActivityLayoutBinding binding = SpellcastingInfoActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get values from intent
        final Intent intent = getIntent();
        final String title = intent.getStringExtra(TITLE_KEY);
        int infoResourceID = intent.getIntExtra(INFO_KEY, -1);
        int tableID = intent.getIntExtra(TABLE_KEY, -1);

        // Get the info and title resources, and set them to the labels
        final TextView titleView = binding.spellcastingInfoTitle;
        final TextView infoView = binding.spellcastingInfoText;
        titleView.setText(title);
        infoView.setText(infoResourceID);

        // Add the table, if there is one
        if (tableID != -1) {
            final LinearLayout linearLayout = binding.spellcastingInfoLinearLayout;
            final TextView textView = new TextView(this);
            textView.setText(R.string.spellcasting_table);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(3, 35, 3, 3);
            linearLayout.addView(textView);

            ///// Old
            final View view = getLayoutInflater().inflate(tableID, null, false);
            linearLayout.addView(view);

//            final HorizontalScrollView tableScroll = new HorizontalScrollView(this);
//            final WebView tableView = new WebView(this);
//            tableView.loadDataWithBaseURL(null, getString(tableID), "text/html", "utf-8", null);
//            tableView.setBackgroundColor(getColor(android.R.color.transparent));
//            tableScroll.addView(tableView);
//            linearLayout.addView(tableScroll);
        }

        // We want to close the window on a swipe to the right
        final ScrollView scroll = binding.spellcastingInfoScroll;
        final Activity thisActivity = this;
        scroll.setOnTouchListener(new OnSwipeTouchListener(thisActivity) {

            @Override
            public void onSwipeRight() {
                thisActivity.finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.identity, R.anim.left_to_right_exit);
    }

}
