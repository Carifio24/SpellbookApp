package dnd.jon.spellbook;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class CharacterSelectionWindow {

    private MainActivity main;
    private View popupView;
    private PopupWindow popup;
    private Context popupContext;
    private TableLayout table;
    private View.OnClickListener textListener;
    private View.OnClickListener deleteListener;

    static final String NAME_KEY = "name";
    private static final int layoutID = R.layout.character_selection;
    private static final int requestCode = RequestCodes.CHARACTER_SELECTION_REQUEST;
    private static final int width = 1000;
    private static final int height = 1000;
    private static final boolean focusable = true;
    private static final int nameSize = 25;

    CharacterSelectionWindow(MainActivity m) {
        main = m;
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(layoutID, null);
        popup = new PopupWindow(popupView, width, height, focusable);
        popupContext = popupView.getContext();
        popup.setAnimationStyle(android.R.style.Animation_Dialog);

        table = popupView.findViewById(R.id.selection_table);
        updateTable();

        // The TextView listener
        textListener = (View view) -> {
            TextView tv = (TextView) view;
            String name = tv.getText().toString();
            main.loadCharacterProfile(name);
            popup.dismiss();
        };

        // The delete listener
        deleteListener = (View view) -> {
            ImageButton button = (ImageButton) view;
            String name = button.getTag().toString();
            String title = "Confirm";
            String message = "Are you sure you want to delete " + name + "?";
            YesNoDialog dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putInt(YesNoDialog.REQUEST_KEY, RequestCodes.DELETE_CHARACTER_REQUEST);
            args.putString(YesNoDialog.MESSAGE_KEY, message);
            args.putString(YesNoDialog.TITLE_KEY, title);
            dialog.setArguments(args);
            dialog.show(main.getSupportFragmentManager(), "deleteCharacter");
            updateTable();
        };


    }

    void updateTable() {

        // Clear out all rows, if necessary
        table.removeAllViews();

        // Set up the table of characters
        for (String charName : main.charactersList()) {

            // The character name
            TextView tv = new TextView(popupContext);
            tv.setText(charName);
            tv.setTextSize(nameSize);
            tv.setOnClickListener(textListener);
            tv.setGravity(Gravity.START);

            // The delete icon
            ImageButton deleteButton = new ImageButton(main);
            deleteButton.setImageResource(android.R.drawable.ic_delete);
            deleteButton.setTag(charName);
            deleteButton.setOnClickListener(deleteListener);

            // Put these into a TableRow
            TableRow tr = new TableRow(popupContext);
            tr.addView(tv);
            tr.addView(deleteButton);
            TableLayout.LayoutParams trlp = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
            tr.setLayoutParams(trlp);

            // Add the row to the table
            table.addView(tr);
        }

    }

    void show() {
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    void dismiss() {
        popup.dismiss();
    }



}