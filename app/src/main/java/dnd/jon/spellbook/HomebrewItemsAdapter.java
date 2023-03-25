package dnd.jon.spellbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dnd.jon.spellbook.databinding.ExpandableSubmenuItemBinding;
import dnd.jon.spellbook.databinding.HomebrewSourceHeaderBinding;

public class HomebrewItemsAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private Map<Source,List<Spell>> items = new HashMap<>();
    private final List<Source> sources = new ArrayList<>();
    private final Comparator<Source> sourceComparator = new Comparator<Source>() {
        final Collator collator = Collator.getInstance(LocalizationUtils.getLocale());
        @Override
        public int compare(Source s1, Source s2) {
            return collator.compare(s1, s2);
        }
    };

    HomebrewItemsAdapter(Context context, Collection<Spell> createdSpells) {
        this.context = context;
        populateItems(createdSpells);
    }

    private void populateItems(Collection<Spell> createdSpells) {
        for (Spell spell : createdSpells) {
            for (Source source : spell.getSourcebooks()) {
                final List<Spell> spells = items.get(source);
                if (spells == null) {
                    items.put(source, Arrays.asList(spell));
                    sources.add(source);
                } else {
                    spells.add(spell);
                }
            }
        }
        sources.sort(sourceComparator);
    }

    void updateSpells(Collection<Spell> createdSpells) {
        populateItems(createdSpells);
        notifyDataSetChanged();
    }

    @Override public int getGroupCount() { return items.size(); }
    @Override public Object getGroup(int groupPosition) { return sources.get(groupPosition); }
    @Override public long getGroupId(int groupPosition) { return groupPosition; }
    @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }
    @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
    @Override public boolean hasStableIds() { return false; }

    @Override
    public int getChildrenCount(int position) {
        final Collection<Spell> spells = items.get(sources.get(position));
        return spells != null ? spells.size() : 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return items.get(sources.get(groupPosition)).get(childPosition);
    }

    void addSpellForSource(Spell spell, Source source) {
        if (!sources.contains(source)) {
            sources.add(source);
            items.put(source, Arrays.asList(spell));
            sources.sort(sourceComparator);
        } else {
            items.get(source).add(spell);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Source source = (Source) getGroup(groupPosition);
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final HomebrewSourceHeaderBinding binding = HomebrewSourceHeaderBinding.inflate(inflater);
            convertView = binding.getRoot();
        }
        final TextView header = convertView.findViewById(R.id.header);
        header.setText(DisplayUtils.getDisplayName(source, context));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Spell spell = (Spell) getChild(groupPosition, childPosition);
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ExpandableSubmenuItemBinding binding = ExpandableSubmenuItemBinding.inflate(inflater);
            convertView = binding.getRoot();
        }
        final TextView childTV = convertView.findViewById(R.id.submenu);
        childTV.setText(spell.getName());
        return convertView;
    }
}