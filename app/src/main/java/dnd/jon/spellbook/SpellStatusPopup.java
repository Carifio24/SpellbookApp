package dnd.jon.spellbook;

import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

class SpellStatusPopup extends CustomPopupWindow {

    private static final int layoutID = R.layout.spell_status_popup;

    private Spell spell;
    private ImageButton favoriteIB;
    private ImageButton preparedIB;
    private ImageButton knownIB;

    SpellStatusPopup(MainActivity mainActivity, Spell s) {
        super(mainActivity, layoutID);
        spell = s;

        // Get the ImageButtons and set the appropriate images
        favoriteIB = popupView.findViewById(R.id.status_popup_favorite);
        preparedIB = popupView.findViewById(R.id.status_popup_prepared);
        knownIB = popupView.findViewById(R.id.status_popup_known);
        setFavoriteIcon(main.characterProfile.isFavorite(spell));
        setPreparedIcon(main.characterProfile.isPrepared(spell));
        setKnownIcon(main.characterProfile.isKnown(spell));

        // Set the button listeners
        favoriteIB.setOnClickListener((View v) -> {
            boolean nowFavorite = !main.characterProfile.isFavorite(spell);
            main.characterProfile.setFavorite(spell, nowFavorite);
            setFavoriteIcon(nowFavorite);
            main.filterIfStatusSet();
        });
        preparedIB.setOnClickListener((View v) -> {
            boolean nowPrepared = !main.characterProfile.isPrepared(spell);
            main.characterProfile.setPrepared(spell, nowPrepared);
            setPreparedIcon(nowPrepared);
            main.filterIfStatusSet();
        });
        knownIB.setOnClickListener((View v) -> {
            boolean nowKnown = !main.characterProfile.isKnown(spell);
            main.characterProfile.setKnown(spell, nowKnown);
            setKnownIcon(nowKnown);
            main.filterIfStatusSet();
        });


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
