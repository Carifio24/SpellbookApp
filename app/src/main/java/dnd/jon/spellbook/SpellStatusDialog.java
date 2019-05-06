package dnd.jon.spellbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

public class SpellStatusDialog extends DialogFragment {

    private View view;
    private Spell spell;
    private  MainActivity main;
    private ImageButton favoriteIB;
    private ImageButton preparedIB;
    private ImageButton knownIB;
    static final String spellKey = "Spell";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The main activity
        main = (MainActivity) getActivity();

        // Create the dialog builder
        AlertDialog.Builder b = new AlertDialog.Builder(main);

        // Get the spell
        spell = getArguments().getParcelable(spellKey);

        // Inflate the view and set the builder to use this view
        LayoutInflater inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.spell_status_popup, null);
        b.setView(view);

        // Get the ImageButtons and set the appropriate images
        favoriteIB = view.findViewById(R.id.status_popup_favorite);
        preparedIB = view.findViewById(R.id.status_popup_prepared);
        knownIB = view.findViewById(R.id.status_popup_known);


        AlertDialog alert = b.create();
        return alert;
    }

    private void setImageFromBoolean(boolean tf, ImageButton ib, int trueId, int falseId) {
        int resId = tf ? trueId : falseId;
        ib.setImageResource(resId);
    }

    private void setFavoriteIcon(boolean tf) {
        setImageFromBoolean(tf, favoriteIB, R.mipmap.star_filled, R.mipmap.star_empty);
    }

    private void setPreparedIcon(boolean tf) {
        setImageFromBoolean(tf, preparedIB, R.mipmap.wand_filled, R.mipmap.wand_empty);
    }

    private void setKnownIcon(boolean tf) {
        setImageFromBoolean(tf, knownIB, R.mipmap.book_filled, R.mipmap.book_empty);
    }

}
