package dnd.jon.spellbook;

import android.view.View;
import android.widget.ImageButton;

class SpellStatusPopup extends CustomPopupWindow {

    private static final int layoutID = R.layout.spell_status_popup;

    private final Spell spell;
    private final ImageButton favoriteIB;
    private final ImageButton preparedIB;
    private final ImageButton knownIB;

    SpellStatusPopup(MainActivity mainActivity, Spell s) {
        super(mainActivity, layoutID);
        spell = s;

        // Get the ImageButtons and set the appropriate images
        favoriteIB = popupView.findViewById(R.id.status_popup_favorite);
        preparedIB = popupView.findViewById(R.id.status_popup_prepared);
        knownIB = popupView.findViewById(R.id.status_popup_known);
        final SpellFilterStatus status = mainActivity.getSpellFilterStatus();
        setFavoriteIcon(status.isFavorite(spell));
        setPreparedIcon(status.isPrepared(spell));
        setKnownIcon(status.isKnown(spell));

        // Set the button listeners
        favoriteIB.setOnClickListener((View v) -> {
            final SpellFilterStatus sfs = mainActivity.getSpellFilterStatus();
            final boolean nowFavorite = !sfs.isFavorite(spell);
            sfs.setFavorite(spell, nowFavorite);
            setFavoriteIcon(nowFavorite);
        });
        preparedIB.setOnClickListener((View v) -> {
            final SpellFilterStatus sfs = mainActivity.getSpellFilterStatus();
            final boolean nowPrepared = !sfs.isPrepared(spell);
            sfs.setPrepared(spell, nowPrepared);
            setPreparedIcon(nowPrepared);
        });
        knownIB.setOnClickListener((View v) -> {
            final SpellFilterStatus sfs = mainActivity.getSpellFilterStatus();
            final boolean nowKnown = !sfs.isKnown(spell);
            sfs.setKnown(spell, nowKnown);
            setKnownIcon(nowKnown);
        });

        // Set the OnDismissListener
        popup.setOnDismissListener(mainActivity::saveCharacterProfile);

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
