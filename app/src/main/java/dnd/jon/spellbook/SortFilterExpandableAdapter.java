package dnd.jon.spellbook;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import androidx.databinding.ViewDataBinding;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.Function;

import dnd.jon.spellbook.databinding.ActivityMainBinding;
import dnd.jon.spellbook.databinding.FilterBlockRangeLayoutBinding;
import dnd.jon.spellbook.databinding.SortFilterLayoutBinding;

class SortFilterExpandableAdapter extends BaseExpandableListAdapter {

    private static final HashMap<Class<? extends NameDisplayable>, Quartet<Boolean, Function<SortFilterLayoutBinding, ? extends ViewDataBinding>, Integer, Integer>> filterBlockInfo = new HashMap<Class<? extends NameDisplayable>, Quartet<Boolean, Function<SortFilterLayoutBinding, ? extends ViewDataBinding>, Integer, Integer>>() {{
        put(Sourcebook.class, new Quartet<>(false, (b) -> b.sourcebookFilterBlock, R.string.sourcebook_filter_title, R.integer.sourcebook_filter_columns));
        put(CasterClass.class, new Quartet<>(false, (b) -> b.casterFilterBlock, R.string.caster_filter_title, R.integer.caster_filter_columns));
        put(School.class, new Quartet<>(false, (b) -> b.schoolFilterBlock, R.string.school_filter_title, R.integer.school_filter_columns));
        put(CastingTime.CastingTimeType.class, new Quartet<>(true, (b) -> b.castingTimeFilterRange, R.string.casting_time_type_filter_title, R.integer.casting_time_type_filter_columns));
        put(Duration.DurationType.class, new Quartet<>(true, (b) -> b.durationFilterRange, R.string.duration_type_filter_title, R.integer.duration_type_filter_columns));
        put(Range.RangeType.class, new Quartet<>(true, (b) -> b.rangeFilterRange, R.string.range_type_filter_title, R.integer.range_type_filter_columns));
    }};

    // The Quartets consist of
    // Superclass, Filter/Range view ID, min text, max text, max entry length
    private static final HashMap<Class<? extends QuantityType>, Quartet<Class<? extends Unit>, Function<SortFilterLayoutBinding, FilterBlockRangeLayoutBinding> ,Integer, Integer>> rangeViewInfo = new HashMap<Class<? extends QuantityType>, Quartet<Class<? extends Unit>, Function<SortFilterLayoutBinding, FilterBlockRangeLayoutBinding>, Integer, Integer>>()  {{
        put(CastingTime.CastingTimeType.class, new Quartet<>(TimeUnit.class, (b) -> b.castingTimeFilterRange, R.string.casting_time_range_text, R.integer.casting_time_max_length));
        put(Duration.DurationType.class, new Quartet<>(TimeUnit.class, (b) -> b.durationFilterRange, R.string.duration_range_text, R.integer.duration_max_length));
        put(Range.RangeType.class, new Quartet<>(LengthUnit.class, (b) -> b.rangeFilterRange, R.string.range_range_text, R.integer.range_max_length));
    }};

    private final MainActivity main;
    private final SortFilterLayoutBinding sortBinding;
    private final List<Pair<View,String>> viewsAndTitles = new ArrayList<>();

    SortFilterExpandableAdapter(MainActivity main) {
        this.main = main;
        this.sortBinding = main.getBinding().sortFilterWindow;
    }

    void addViewWithTitle(View view, String title) {
        viewsAndTitles.add(new Pair<>(view, title));
    }

    @Override
    public int getGroupCount() { return viewsAndTitles.size(); }

    @Override
    public int getChildrenCount(int groupPosition) { return 1; }

    @Override
    public Object getGroup(int groupPosition) {
        return viewsAndTitles.get(groupPosition).getValue1();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return viewsAndTitles.get(groupPosition).getValue0();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
}
