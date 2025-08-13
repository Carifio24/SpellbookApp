package dnd.jon.spellbook;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class SpellShortcutPopup extends CustomPopupWindow {
    private static final int layoutID = R.layout.spell_shortcut_popup;

    SpellShortcutPopup(Context activity, Spell spell) {
        super(activity, layoutID, true);
        final Button button = popupView.findViewById(R.id.create_spell_shortcut_button);
        button.setOnClickListener((View view) -> {
            SpellbookUtils.createShortcut(activity, spell);
            final Context context = view.getContext();
            Toast.makeText(context, context.getString(R.string.shortcut_created_toast, spell.getName()), Toast.LENGTH_SHORT).show();
            this.dismiss();
        });
    }
}
