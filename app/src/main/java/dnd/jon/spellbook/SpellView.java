package dnd.jon.spellbook;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;

import dnd.jon.spellbook.databinding.SpellRowBinding;


class SpellView extends ConstraintLayout {

    private Context context;
    private View spellView;
    private SpellRowBinding binding;


    SpellView(Context ctx) {
        super(ctx);
        context = ctx;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        spellView = inflater.inflate(R.layout.spell_row, null);
        binding = SpellRowBinding.inflate(inflater);

    }

    public View getView() { return spellView; }
//    public TextView getSpellNameView() { return spellView.findViewById(R.id.spell_name_label); }
//    public TextView getSchoolView() { return spellView.findViewById(R.id.school_label); }
//    public TextView getLevelView() { return spellView.findViewById(R.id.level_label); }

}
