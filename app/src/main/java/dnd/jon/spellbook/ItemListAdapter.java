package dnd.jon.spellbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ItemListAdapter<Item, Binding extends ViewDataBinding> extends RecyclerView.Adapter<ItemViewHolder> {

    private final Object sharedLock = new Object();

    private final LayoutInflater inflater;
    private final BiConsumer<Binding, Item> binder;
    private final Function<LayoutInflater, Binding> inflation;
    List<Item> items; // Cached copy of items

    ItemListAdapter(Context context, Function<LayoutInflater,Binding> inflation, BiConsumer<Binding,Item> binder) {
        inflater = LayoutInflater.from(context);
        this.binder = binder;
        this.inflation = inflation;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Binding binding = inflation.apply(inflater);
        return new ItemViewHolder<>(binding, binder);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Do nothing if the index is out of bounds
        // This shouldn't happen, but it's better than a crash
        if ( (position >= items.size()) || (position < 0) ) { return; }

        // Get the appropriate spell and bind it to the holder
        final Item item = items.get(position);
        holder.bind(item);
    }


    @Override
    public int getItemCount() {
        synchronized (sharedLock) {
            return (items != null) ? items.size() : 0;
        }
    }

    void setItems(List<Item> items) {
        synchronized (sharedLock) {
            this.items = items;
            notifyDataSetChanged();
        }
    }


}
