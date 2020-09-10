package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import dnd.jon.spellbook.CastingTime.CastingTimeType;
import dnd.jon.spellbook.Duration.DurationType;
import dnd.jon.spellbook.Range.RangeType;

@Entity(tableName = SpellbookRoomDatabase.CHARACTERS_TABLE, indices = {@Index(name = "index_characters_id", value = {"id"}, unique = true), @Index(name = "index_characters_name", value = {"name"}, unique = true)})
public class CharacterProfile extends BaseObservable {

    // A key for database indexing
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    // Member values
    @NonNull @ColumnInfo(name = "name") private final String name;

    @ColumnInfo(name = "first_sort_field") private SortField firstSortField;
    @ColumnInfo(name = "second_sort_field") private SortField secondSortField;
    @ColumnInfo(name = "first_sort_reverse", defaultValue = "0") private boolean firstSortReverse;
    @ColumnInfo(name = "second_sort_reverse", defaultValue = "0") private boolean secondSortReverse;
    @ColumnInfo(name = "status_filter") private StatusFilterField statusFilter;
    @ColumnInfo(name = "min_level", defaultValue = "0") private int minLevel;
    @ColumnInfo(name = "max_level", defaultValue = "9") private int maxLevel;
    @ColumnInfo(name = "ritual_filter", defaultValue = "1") private boolean ritualFilter;
    @ColumnInfo(name = "not_ritual_filter", defaultValue = "1") private boolean notRitualFilter;
    @ColumnInfo(name = "concentration_filter", defaultValue = "1") private boolean concentrationFilter;
    @ColumnInfo(name = "not_concentration_filter", defaultValue = "1") private boolean notConcentrationFilter;
    @ColumnInfo(name = "verbal_filter", defaultValue = "1") private boolean verbalFilter;
    @ColumnInfo(name = "not_verbal_filter", defaultValue = "1") private boolean notVerbalFilter;
    @ColumnInfo(name = "somatic_filter", defaultValue = "1") private boolean somaticFilter;
    @ColumnInfo(name = "not_somatic_filter", defaultValue = "1") private boolean notSomaticFilter;
    @ColumnInfo(name = "material_filter", defaultValue = "1") private boolean materialFilter;
    @ColumnInfo(name = "not_material_filter", defaultValue = "1") private boolean notMaterialFilter;
    @ColumnInfo(name = "visible_casting_time_types") private EnumSet<CastingTimeType> visibleCastingTimeTypes;
    @ColumnInfo(name = "visible_duration_types") private EnumSet<DurationType> visibleDurationTypes;
    @ColumnInfo(name = "visible_range_types") private EnumSet<RangeType> visibleRangeTypes;
    @Embedded(prefix = "min_duration_") private Duration minDuration;
    @Embedded(prefix = "max_duration_") private Duration maxDuration;
    @Embedded(prefix = "min_casting_time_") private CastingTime minCastingTime;
    @Embedded(prefix = "max_casting_time_") private CastingTime maxCastingTime;
    @Embedded(prefix = "min_range_") private Range minRange;
    @Embedded(prefix = "max_range_") private Range maxRange;

    // Default values
    static final CastingTime defaultMinCastingTime = new CastingTime(0, TimeUnit.SECOND);
    static final CastingTime defaultMaxCastingTime = new CastingTime(24, TimeUnit.HOUR);
    static final Duration defaultMinDuration = new Duration(0, TimeUnit.SECOND);
    static final Duration defaultMaxDuration = new Duration(30, TimeUnit.DAY);
    static final Range defaultMinRange = new Range(0, LengthUnit.FOOT);
    static final Range defaultMaxRange = new Range(1, LengthUnit.MILE);

    public CharacterProfile(int id, @NonNull String name,
                            SortField firstSortField, SortField secondSortField,
                            EnumSet<CastingTimeType> visibleCastingTimeTypes, EnumSet<DurationType> visibleDurationTypes, EnumSet<RangeType> visibleRangeTypes,
                            boolean firstSortReverse, boolean secondSortReverse, StatusFilterField statusFilter, boolean ritualFilter, boolean notRitualFilter,
                            boolean concentrationFilter, boolean notConcentrationFilter, boolean verbalFilter, boolean notVerbalFilter, boolean somaticFilter,
                            boolean notSomaticFilter, boolean materialFilter, boolean notMaterialFilter, int minLevel, int maxLevel,
                            CastingTime minCastingTime, CastingTime maxCastingTime, Duration minDuration, Duration maxDuration, Range minRange, Range maxRange) {
        this.id = id;
        this.name = name;
        this.firstSortField = firstSortField;
        this.secondSortField = secondSortField;
        this.visibleCastingTimeTypes = visibleCastingTimeTypes;
        this.visibleDurationTypes = visibleDurationTypes;
        this.visibleRangeTypes = visibleRangeTypes;
        this.firstSortReverse = firstSortReverse;
        this.secondSortReverse = secondSortReverse;
        this.statusFilter = statusFilter;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.ritualFilter = ritualFilter;
        this.notRitualFilter = notRitualFilter;
        this.concentrationFilter = concentrationFilter;
        this.notConcentrationFilter = notConcentrationFilter;
        this.verbalFilter = verbalFilter;
        this.notVerbalFilter = notVerbalFilter;
        this.somaticFilter = somaticFilter;
        this.notSomaticFilter = notSomaticFilter;
        this.materialFilter = materialFilter;
        this.notMaterialFilter = notMaterialFilter;
        this.minCastingTime = minCastingTime;
        this.maxCastingTime = maxCastingTime;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    @Ignore
    CharacterProfile(String name) {
        this(0, name, SortField.NAME, SortField.NAME, EnumSet.allOf(CastingTimeType.class),
                EnumSet.allOf(DurationType.class), EnumSet.allOf(RangeType.class), false, false, StatusFilterField.ALL, true, true,
                true, true, true, true, true, true, true, true,
                Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL, defaultMinCastingTime, defaultMaxCastingTime, defaultMinDuration, defaultMaxDuration, defaultMinRange, defaultMaxRange);
    }

    // Basic getters
    @Bindable public int getId() { return id; }
    @Bindable @NonNull public String getName() { return name; }
    @Bindable public SortField getFirstSortField() { return firstSortField; }
    @Bindable public SortField getSecondSortField() { return secondSortField; }
    @Bindable public boolean getFirstSortReverse() { return firstSortReverse; }
    @Bindable public boolean getSecondSortReverse() { return secondSortReverse; }
    @Bindable public int getMinLevel() { return minLevel; }
    @Bindable public int getMaxLevel() { return maxLevel; }
    @Bindable public StatusFilterField getStatusFilter() { return statusFilter; }
    @Bindable public boolean getRitualFilter() { return ritualFilter; }
    @Bindable public boolean getConcentrationFilter() { return concentrationFilter; }
    @Bindable public boolean getNotRitualFilter() { return notRitualFilter; }
    @Bindable public boolean getNotConcentrationFilter() { return notConcentrationFilter; }
    @Bindable public boolean getVerbalFilter() { return verbalFilter; }
    @Bindable public boolean getSomaticFilter() { return somaticFilter; }
    @Bindable public boolean getMaterialFilter() { return materialFilter; }
    @Bindable public boolean getNotVerbalFilter() { return notVerbalFilter; }
    @Bindable public boolean getNotSomaticFilter() { return notSomaticFilter; }
    @Bindable public boolean getNotMaterialFilter() { return notMaterialFilter; }
    @Bindable public EnumSet<CastingTimeType> getVisibleCastingTimeTypes() { return visibleCastingTimeTypes; }
    @Bindable public EnumSet<DurationType> getVisibleDurationTypes() { return visibleDurationTypes; }
    @Bindable public EnumSet<RangeType> getVisibleRangeTypes() { return visibleRangeTypes; }
    @Bindable public Duration getMinDuration() { return minDuration; }
    @Bindable public Duration getMaxDuration() { return maxDuration; }
    @Bindable public CastingTime getMinCastingTime() { return minCastingTime; }
    @Bindable public CastingTime getMaxCastingTime() { return maxCastingTime; }
    @Bindable public Range getMinRange() { return minRange; }
    @Bindable public Range getMaxRange() { return maxRange; }


    // Setters
    public void setFirstSortField(SortField firstSortField) { this.firstSortField = firstSortField; notifyPropertyChanged(BR.firstSortField); }
    public void setSecondSortField(SortField secondSortField) { this.secondSortField = secondSortField; notifyPropertyChanged(BR.secondSortField); }
    public void setFirstSortReverse(boolean firstSortReverse) { this.firstSortReverse = firstSortReverse; notifyPropertyChanged(BR.firstSortReverse); }
    public void setSecondSortReverse(boolean secondSortReverse) { this.secondSortReverse = secondSortReverse; notifyPropertyChanged(BR.secondSortReverse); }
    public void setStatusFilter(StatusFilterField statusFilter) { this.statusFilter = statusFilter; notifyPropertyChanged(BR.statusFilter); }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; notifyPropertyChanged(BR.minLevel); }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; notifyPropertyChanged(BR.maxLevel); }
    public void setRitualFilter(boolean ritualFilter) { this.ritualFilter = ritualFilter; notifyPropertyChanged(BR.ritualFilter); }
    public void setNotRitualFilter(boolean notRitualFilter) { this.notRitualFilter = notRitualFilter; notifyPropertyChanged(BR.notRitualFilter); }
    public void setConcentrationFilter(boolean concentrationFilter) { this.concentrationFilter = concentrationFilter; notifyPropertyChanged(BR.concentrationFilter); }
    public void setNotConcentrationFilter(boolean notConcentrationFilter) { this.notConcentrationFilter = notConcentrationFilter; notifyPropertyChanged(BR.notConcentrationFilter); }
    public void setVerbalFilter(boolean verbalFilter) { this.verbalFilter = verbalFilter; notifyPropertyChanged(BR.verbalFilter); }
    public void setNotVerbalFilter(boolean notVerbalFilter) { this.notVerbalFilter = notVerbalFilter; notifyPropertyChanged(BR.notVerbalFilter); }
    public void setSomaticFilter(boolean somaticFilter) { this.somaticFilter = somaticFilter; notifyPropertyChanged(BR.somaticFilter); }
    public void setNotSomaticFilter(boolean notSomaticFilter) { this.notSomaticFilter = notSomaticFilter; notifyPropertyChanged(BR.notSomaticFilter); }
    public void setMaterialFilter(boolean materialFilter) { this.materialFilter = materialFilter; notifyPropertyChanged(BR.materialFilter); }
    public void setNotMaterialFilter(boolean notMaterialFilter) { this.notMaterialFilter = notMaterialFilter; notifyPropertyChanged(BR.notMaterialFilter); }
    public void setVisibleCastingTimeTypes(EnumSet<CastingTimeType> visibleCastingTimeTypes) { this.visibleCastingTimeTypes = visibleCastingTimeTypes; notifyPropertyChanged(BR.visibleCastingTimeTypes); }
    public void setVisibleDurationTypes(EnumSet<DurationType> visibleDurationTypes) { this.visibleDurationTypes = visibleDurationTypes; notifyPropertyChanged(BR.visibleDurationTypes); }
    public void setVisibleRangeTypes(EnumSet<RangeType> visibleRangeTypes) { this.visibleRangeTypes = visibleRangeTypes; notifyPropertyChanged(BR.visibleRangeTypes); }
    public void setMinDuration(Duration minDuration) { this.minDuration = minDuration; notifyPropertyChanged(BR.minDuration); }
    public void setMaxDuration(Duration maxDuration) { this.maxDuration = maxDuration; notifyPropertyChanged(BR.maxDuration); }
    public void setMinCastingTime(CastingTime minCastingTime) { this.minCastingTime = minCastingTime; notifyPropertyChanged(BR.minCastingTime); }
    public void setMaxCastingTime(CastingTime maxCastingTime) { this.maxCastingTime = maxCastingTime; notifyPropertyChanged(BR.maxCastingTime); }
    public void setMinRange(Range minRange) { this.minRange = minRange; notifyPropertyChanged(BR.minRange); }
    public void setMaxRange(Range maxRange) { this.maxRange = maxRange; notifyPropertyChanged(BR.maxRange); }

    <T extends QuantityType> boolean getVisibility(T t) {
        if (t instanceof CastingTimeType) { return visibleCastingTimeTypes.contains(t); }
        else if (t instanceof DurationType) { return visibleDurationTypes.contains(t); }
        else if (t instanceof RangeType) { return visibleRangeTypes.contains(t); }
        else { return false; }
    }

    private <E extends Enum<E> & QuantityType> void setVisibility(E e, EnumSet<E> items, boolean visibility) {
        final boolean in = items.contains(e);
        if (visibility && !in) {
            items.add(e);
        } else if (!visibility && in) {
            items.remove(e);
        }
    }

    <E extends Enum<E> & QuantityType> void setVisibility(E e, boolean visibility) {
        if (e instanceof CastingTimeType) { setVisibility((CastingTimeType) e, visibleCastingTimeTypes, visibility); }
        else if (e instanceof DurationType) { setVisibility((DurationType) e, visibleDurationTypes, visibility); }
        else if (e instanceof RangeType) { setVisibility((RangeType) e, visibleRangeTypes, visibility); }
    }

}
