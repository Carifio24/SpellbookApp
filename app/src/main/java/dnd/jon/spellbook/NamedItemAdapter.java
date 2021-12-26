package dnd.jon.spellbook;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Function;

public abstract class NamedItemAdapter<Holder extends NameRowHolder> extends RecyclerView.Adapter<Holder> {

    // Member values
    private List<String> names;
    final FragmentActivity activity;
    final SpellbookViewModel viewModel;

    // Constructor
    @SuppressLint("NotifyDataSetChanged")
    NamedItemAdapter(FragmentActivity activity,
                     Function<SpellbookViewModel,LiveData<List<String>>> nameLDGetter) {
        this.activity = activity;
        this.viewModel = new ViewModelProvider(activity, activity.getDefaultViewModelProviderFactory())
                .get(SpellbookViewModel.class);
        final LiveData<List<String>> namesLiveData = nameLDGetter.apply(viewModel);
        namesLiveData.observe(activity, (names) -> {
            this.names = names;
            notifyDataSetChanged();
        });
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        // Do nothing if the index is out of bounds
        // This shouldn't happen, but it's better than a crash
        if ( (position >= names.size()) || (position < 0) ) { return; }

        // Get the appropriate spell and bind it to the holder
        final String name = names.get(position);
        holder.bind(name);
    }

    public int getItemCount() { return names.size(); }

}


