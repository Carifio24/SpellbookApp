package dnd.jon.spellbook;

import android.widget.TextView;

class SpellcastingInfoPopup extends CustomPopupWindow {

    private static int layoutID = R.layout.spellcasting_info_layout;

    SpellcastingInfoPopup(MainActivity m, String title, int textID, boolean focusable) {
        super(m, layoutID, focusable);
        TextView titleTV = popupView.findViewById(R.id.spellcasting_info_title);
        titleTV.setText(title);
        TextView textTV = popupView.findViewById(R.id.spellcasting_info_text);
        textTV.setText(textID);
    }

}
