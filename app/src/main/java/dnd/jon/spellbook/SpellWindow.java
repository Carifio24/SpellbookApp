package dnd.jon.spellbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.content.Intent;
import android.graphics.Color;

import dnd.jon.spellbook.databinding.SpellWindowActivityBinding;
import dnd.jon.spellbook.databinding.SpellWindowBinding;

public final class SpellWindow extends AppCompatActivity {

    static final String SPELL_KEY = "spell";
    static final String TEXT_SIZE_KEY = "textSize";
    static final String INDEX_KEY = "index";
    static final String FAVORITE_KEY = "favorite";
    static final String KNOWN_KEY = "known";
    static final String PREPARED_KEY = "prepared";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final SpellWindowActivityBinding binding = SpellWindowActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dismiss activity on a swipe to the right
        final View rootView = binding.getRoot();
        final Activity thisActivity = this;
        rootView.setOnTouchListener(new OnSwipeTouchListener(thisActivity) {

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
