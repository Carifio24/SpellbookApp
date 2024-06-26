package dnd.jon.spellbook;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;

import dnd.jon.spellbook.databinding.SpellRowBinding;


class SpellView extends ConstraintLayout {

    private final View spellView;

    SpellView(Context context) {
        super(context);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        spellView = inflater.inflate(R.layout.spell_row, null);
        final SpellRowBinding binding = SpellRowBinding.inflate(inflater);
    }

    public View getView() { return spellView; }
//    public TextView getSpellNameView() { return spellView.findViewById(R.id.spell_name_label); }
//    public TextView getSchoolView() { return spellView.findViewById(R.id.school_label); }
//    public TextView getLevelView() { return spellView.findViewById(R.id.level_label); }

}
