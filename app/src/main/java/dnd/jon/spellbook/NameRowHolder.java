package dnd.jon.spellbook;

import dnd.jon.spellbook.databinding.NameRowBinding;

// The row holder class
public class NameRowHolder extends ItemViewHolder<String, NameRowBinding> {

    public NameRowHolder(NameRowBinding binding) {
        super(binding, NameRowBinding::setName);
        itemView.setTag(this);
    }
}
