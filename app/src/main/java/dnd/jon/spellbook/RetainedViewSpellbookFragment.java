package dnd.jon.spellbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

// I'm not a huge fan of this!
// but there are some fragments that we only ever need one of
// and that we can reuse throughout the application.
// Navigation by default will destroy/recreate the fragment & view
// on each navigation (which is noticeable particularly for the
// non-animated transitions). So we do this instead.
public abstract class RetainedViewSpellbookFragment<VB extends ViewBinding> extends SpellbookFragment<VB> {

    protected static ViewBinding retainedBinding = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (retainedBinding != null && retainedBinding.getRoot().getContext() == context) {
            binding = (VB) retainedBinding;
            return binding.getRoot();
        }
        super.onCreateView(inflater, container, savedInstanceState);
    }

}
