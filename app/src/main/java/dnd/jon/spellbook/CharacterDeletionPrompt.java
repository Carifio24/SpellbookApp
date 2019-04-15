package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

class CharacterDeletionPrompt extends CustomPopupWindow {

    private TextView titleView;
    private TextView messageView;
    private Button yesButton;
    private Button noButton;
    private String name;

    private static final int layoutID = R.layout.yes_no;
    private static final int width = 1000;
    private static final int height = 1000;
    private static final boolean focusable = true;
    private static final int titleSize = 25;

    CharacterDeletionPrompt(MainActivity m, String charName) {
        super(m, layoutID);
        name = charName;

        titleView = popupView.findViewById(R.id.yes_no_title);
        titleView.setText(R.string.confirm);
        titleView.setTextSize(titleSize);
        messageView = popupView.findViewById(R.id.yes_no_message);
        String s = "Are you sure you want to delete " + name + "?";
        messageView.setText(s);

        // Set up the buttons
        yesButton = popupView.findViewById(R.id.yes_button);
        noButton = popupView.findViewById(R.id.no_button);

        yesButton.setOnClickListener( (View view) -> {
            main.deleteCharacterProfile(name);
        });

    }

}