package dnd.jon.spellbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Pair;
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

import dnd.jon.spellbook.databinding.HomebrewSourceHeaderBinding;
import dnd.jon.spellbook.databinding.HomebrewSpellItemBinding;

public class HomebrewItemsAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final Map<Source,List<Spell>> items = new HashMap<>();
    private final List<Source> sources = new ArrayList<>();

    // It's possible to have a spell without a source - we don't allow creating one,
    // but one could delete the spell's only source later
    private final List<Spell> spellsWithoutSources = new ArrayList<>();
    private final Comparator<Spell> spellComparator;
    private final Comparator<Source> sourceComparator = new Comparator<>() {
        final Collator collator = Collator.getInstance(LocalizationUtils.getLocale());
        @Override
        public int compare(Source s1, Source s2) {
            return collator.compare(s1, s2);
        }
    };

    HomebrewItemsAdapter(Context context, Collection<Spell> createdSpells) {
        this.context = context;
        this.spellComparator = new SpellComparator(context, List.of(new Pair<>(SortField.NAME, false)));
        populateItems(createdSpells);
    }

    private void populateItems(Collection<Spell> createdSpells) {
        reset();
        for (Spell spell : createdSpells) {
            for (Source source : spell.getSourcebooks()) {
                final List<Spell> spells = items.get(source);
                if (spells == null) {
                    final List<Spell> spellsList = new ArrayList<>() {{ add(spell); }};
                    items.put(source, spellsList);
                    sources.add(source);
                } else {
                    spells.add(spell);
                }
            }
            if (spell.getLocations().size() == 0) {
                spellsWithoutSources.add(spell);
            }
        }
        sources.sort(sourceComparator);
        spellsWithoutSources.sort(spellComparator);
    }

    void reset() {
        items.clear();
        sources.clear();
        spellsWithoutSources.clear();
    }

    void updateSpells(Collection<Spell> createdSpells) {
        populateItems(createdSpells);
        notifyDataSetChanged();
    }

    @Override public int getGroupCount() {
        int size = items.size();
        if (spellsWithoutSources.size() > 0) {
            size += 1;
        }
        return size;
    }
    @Override public Object getGroup(int groupPosition) {
        if (groupPosition < sources.size()) {
            return sources.get(groupPosition);
        } else {
            return null;
        }
    }
    @Override public long getGroupId(int groupPosition) { return groupPosition; }
    @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }
    @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
    @Override public boolean hasStableIds() { return false; }

    @Override
    public int getChildrenCount(int position) {
        if (position < sources.size()) {
            final Collection<Spell> spells = items.get(sources.get(position));
            return spells != null ? spells.size() : 0;
        } else {
            return spellsWithoutSources.size();
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition < sources.size()) {
            return items.get(sources.get(groupPosition)).get(childPosition);
        } else {
            return spellsWithoutSources.get(childPosition);
        }
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
        if (source != null) {
            header.setText(DisplayUtils.getDisplayName(source, context));
        } else {
            header.setText(R.string.spells_without_sources);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Spell spell = (Spell) getChild(groupPosition, childPosition);
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final HomebrewSpellItemBinding binding = HomebrewSpellItemBinding.inflate(inflater);
            convertView = binding.getRoot();
        }
        final TextView childTV = convertView.findViewById(R.id.submenu);
        childTV.setText(spell.getName());
        return convertView;
    }
}