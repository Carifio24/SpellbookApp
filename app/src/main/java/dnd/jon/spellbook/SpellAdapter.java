package dnd.jon.spellbook;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

public class SpellAdapter extends RecyclerView.Adapter<SpellAdapter.SpellViewHolder> {

    public static class SpellViewHolder extends RecyclerView.ViewHolder {
        public TextView spellNameView;
        public TextView schoolView;
        public TextView levelView;
        public SpellViewHolder(TextView snView, TextView scView, TextView lView) {
            super(snView, scView, lView);

        }
    }
}
