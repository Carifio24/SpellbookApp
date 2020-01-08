package dnd.jon.spellbook;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.ScrollView;
import android.widget.TextView;

public final class SpellcastingInfoWindow extends Activity {

    static final String TITLE_KEY = "title";
    static final String INFO_KEY = "info";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.spellcasting_info_activity_layout);

        // Get values from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra(TITLE_KEY);
        int infoResourceID = intent.getIntExtra(INFO_KEY, -1);

        // Get the info and title resources, and set them to the labels
        TextView titleView = findViewById(R.id.spellcasting_info_title);
        TextView infoView = findViewById(R.id.spellcasting_info_text);
        titleView.setText(title);
        infoView.setText(infoResourceID);

        // We want to close the window on a swipe to the right
        ScrollView scroll = this.findViewById(R.id.spellcasting_info_scroll);
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
        overridePendingTransition(R.anim.left_to_right_enter, R.anim.left_to_right_exit);
    }

}
