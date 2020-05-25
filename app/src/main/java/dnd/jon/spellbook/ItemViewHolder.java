package dnd.jon.spellbook;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.BiConsumer;

class ItemViewHolder<Item, Binding extends ViewDataBinding> extends RecyclerView.ViewHolder {

    // The binding
    final Binding binding;

    // The binding function
    final BiConsumer<Binding, Item> binder;

    // The item
    Item item = null;

    // Constructor
    ItemViewHolder(Binding binding, BiConsumer<Binding, Item> binder) {
        super(binding.getRoot());
        this.binding = binding;
        this.binder = binder;
    }

    public void bind(Item item) {
        this.item = item;
        binder.accept(binding, item);
        binding.executePendingBindings();
    }

    public Item getItem() { return item; }

}
