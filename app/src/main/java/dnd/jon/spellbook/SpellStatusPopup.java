package dnd.jon.spellbook;

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
        CharacterProfile cp = main.getCharacterProfile();
        setFavoriteIcon(cp.isFavorite(spell));
        setPreparedIcon(cp.isPrepared(spell));
        setKnownIcon(cp.isKnown(spell));

        // Set the button listeners
        favoriteIB.setOnClickListener((View v) -> {
            CharacterProfile profile = main.getCharacterProfile();
            boolean nowFavorite = !profile.isFavorite(spell);
            profile.setFavorite(spell, nowFavorite);
            setFavoriteIcon(nowFavorite);
            main.filterIfStatusSet();
        });
        preparedIB.setOnClickListener((View v) -> {
            CharacterProfile profile = main.getCharacterProfile();
            boolean nowPrepared = !profile.isPrepared(spell);
            profile.setPrepared(spell, nowPrepared);
            setPreparedIcon(nowPrepared);
            main.filterIfStatusSet();
        });
        knownIB.setOnClickListener((View v) -> {
            CharacterProfile profile = main.getCharacterProfile();
            boolean nowKnown = !profile.isKnown(spell);
            profile.setKnown(spell, nowKnown);
            setKnownIcon(nowKnown);
            main.filterIfStatusSet();
        });

        // Set the OnDismissListener
        popup.setOnDismissListener(main::saveCharacterProfile);

        // Set the elevation
        popup.setElevation(10);

    }

    private void setImageFromBoolean(boolean tf, ImageButton ib, int trueId, int falseId) {
        int resId = tf ? trueId : falseId;
        ib.setImageResource(resId);
    }

    private void setFavoriteIcon(boolean tf) {
        setImageFromBoolean(tf, favoriteIB, R.drawable.star_filled, R.drawable.star_empty);
    }

    private void setPreparedIcon(boolean tf) {
        setImageFromBoolean(tf, preparedIB, R.drawable.wand_filled, R.drawable.wand_empty);
    }

    private void setKnownIcon(boolean tf) {
        setImageFromBoolean(tf, knownIB, R.drawable.book_filled, R.drawable.book_empty);
    }

}
