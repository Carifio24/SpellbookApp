package dnd.jon.spellbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ItemListAdapter<Item, Binding extends ViewDataBinding, VH extends ItemViewHolder<Item,Binding>> extends RecyclerView.Adapter<VH> {

    private final LayoutInflater inflater;
    //private final BiConsumer<Binding, Item> binder;
    private final Function<LayoutInflater, Binding> inflation;
    private final Function<Binding,VH> vhCreator;

    ItemListAdapter(Context context, Function<LayoutInflater,Binding> inflation, Function<Binding,VH> vhCreator) {
        inflater = LayoutInflater.from(context);
        this.inflation = inflation;
        this.vhCreator = vhCreator;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Binding binding = inflation.apply(inflater);
        return vhCreator.apply(binding);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
//        // Do nothing if the index is out of bounds
//        // This shouldn't happen, but it's better than a crash
//        if ( (position >= items.size()) || (position < 0) ) { return; }
//
//        // Get the appropriate spell and bind it to the holder
//        final Item item = items.get(position);
//        holder.bind(item);
//    }

//
//    @Override
//    public int getItemCount() {
//        synchronized (sharedLock) {
//            return (items != null) ? items.size() : 0;
//        }
//    }


}
