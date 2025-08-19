package dnd.jon.spellbook;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class SpellShortcutPopup extends CustomPopupWindow {
    private static final int layoutID = R.layout.spell_shortcut_popup;

    SpellShortcutPopup(Context context, Spell spell) {
        super(context, layoutID, true);
        final Button button = popupView.findViewById(R.id.create_spell_shortcut_button);

        button.setOnClickListener((View view) -> {
            SpellbookUtils.createShortcut(context, spell);
            final Context ctx = view.getContext();
            Toast.makeText(ctx, context.getString(R.string.shortcut_created_toast, spell.getName()), Toast.LENGTH_SHORT).show();
            this.dismiss();
        });
    }

    @Override
    public void showUnderView(View view) {
        super.showUnderView(view);

        final View container = (View) popup.getContentView().getParent();
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) container.getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.3f;
        wm.updateViewLayout(container, params);
    }
}
