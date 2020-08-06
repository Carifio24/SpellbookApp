package dnd.jon.spellbook;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import dnd.jon.spellbook.CastingTime.CastingTimeType;
import dnd.jon.spellbook.Duration.DurationType;
import dnd.jon.spellbook.Range.RangeType;

@Entity(tableName = "characters", indices = {@Index(value = {"id"}, unique = true), @Index(value = {"name"}, unique = true)})
public class CharacterProfile {

    // A key for database indexing
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    // Member values
    @NonNull @ColumnInfo(name = "name") private final String name;

    @ColumnInfo(name = "spell_statuses") private Map<String,SpellStatus> spellStatuses;
    @ColumnInfo(name = "first_sort_field") private SortField firstSortField;
    @ColumnInfo(name = "second_sort_field") private SortField secondSortField;
    @ColumnInfo(name = "first_sort_reverse") private boolean firstSortReverse;
    @ColumnInfo(name = "second_sort_reverse") private boolean secondSortReverse;
    @ColumnInfo(name = "status_filter") private StatusFilterField statusFilter;
    @ColumnInfo(name = "min_level") private int minLevel;
    @ColumnInfo(name = "max_level") private int maxLevel;
    @ColumnInfo(name = "ritual_filter") private boolean ritualFilter;
    @ColumnInfo(name = "not_ritual_filter") private boolean notRitualFilter;
    @ColumnInfo(name = "concentration_filter") private boolean concentrationFilter;
    @ColumnInfo(name = "not_concentration_filter") private boolean notConcentrationFilter;
    @ColumnInfo(name = "verbal_filter") private boolean verbalFilter;
    @ColumnInfo(name = "not_verbal_filter") private boolean notVerbalFilter;
    @ColumnInfo(name = "somatic_filter") private boolean somaticFilter;
    @ColumnInfo(name = "not_somatic_filter") private boolean notSomaticFilter;
    @ColumnInfo(name = "material_filter") private boolean materialFilter;
    @ColumnInfo(name = "not_material_filter") private boolean notMaterialFilter;
    @ColumnInfo(name = "visible_sourcebooks") private Set<Source> visibleSources;
    @ColumnInfo(name = "visible_schools") private EnumSet<School> visibleSchools;
    @ColumnInfo(name = "visible_classes") private EnumSet<CasterClass> visibleClasses;
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

    public CharacterProfile(int id, @NonNull String name, Map<String, SpellStatus> spellStatuses, SortField firstSortField, SortField secondSortField,
                            Set<Source> visibleSources, EnumSet<School> visibleSchools, EnumSet<CasterClass> visibleClasses,
                            EnumSet<CastingTimeType> visibleCastingTimeTypes, EnumSet<DurationType> visibleDurationTypes, EnumSet<RangeType> visibleRangeTypes,
                            boolean firstSortReverse, boolean secondSortReverse, StatusFilterField statusFilter, boolean ritualFilter, boolean notRitualFilter,
                            boolean concentrationFilter, boolean notConcentrationFilter, boolean verbalFilter, boolean notVerbalFilter, boolean somaticFilter,
                            boolean notSomaticFilter, boolean materialFilter, boolean notMaterialFilter, int minLevel, int maxLevel,
                            CastingTime minCastingTime, CastingTime maxCastingTime, Duration minDuration, Duration maxDuration, Range minRange, Range maxRange) {
        this.id = id;
        this.name = name;
        this.spellStatuses = spellStatuses;
        this.firstSortField = firstSortField;
        this.secondSortField = secondSortField;
        this.visibleSources = visibleSources;
        this.visibleSchools = visibleSchools;
        this.visibleClasses = visibleClasses;
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
    private CharacterProfile(String name, Set<Source> visibleSources, Map<String, SpellStatus> spellStatuses) {
        this(0, name, spellStatuses, SortField.NAME, SortField.NAME, visibleSources, EnumSet.allOf(School.class), EnumSet.allOf(CasterClass.class), EnumSet.allOf(CastingTimeType.class),
                EnumSet.allOf(DurationType.class), EnumSet.allOf(RangeType.class), false, false, StatusFilterField.ALL, true, true,
                true, true, true, true, true, true, true, true,
                Spellbook.MIN_SPELL_LEVEL, Spellbook.MAX_SPELL_LEVEL, defaultMinCastingTime, defaultMaxCastingTime, defaultMinDuration, defaultMaxDuration, defaultMinRange, defaultMaxRange);
    }

    @Ignore
    CharacterProfile(String name, Set<Source> visibleSources) { this(name, visibleSources, new HashMap<>()); }

    // Basic getters
    public int getId() { return id; }
    @NonNull public String getName() { return name; }
    public Map<String, SpellStatus> getSpellStatuses() { return spellStatuses; }
    public SortField getFirstSortField() { return firstSortField; }
    public SortField getSecondSortField() { return secondSortField; }
    public boolean getFirstSortReverse() { return firstSortReverse; }
    public boolean getSecondSortReverse() { return secondSortReverse; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
    public StatusFilterField getStatusFilter() { return statusFilter; }
    public boolean getRitualFilter() { return ritualFilter; }
    public boolean getConcentrationFilter() { return concentrationFilter; }
    public boolean getNotRitualFilter() { return notRitualFilter; }
    public boolean getNotConcentrationFilter() { return notConcentrationFilter; }
    public boolean getVerbalFilter() { return verbalFilter; }
    public boolean getSomaticFilter() { return somaticFilter; }
    public boolean getMaterialFilter() { return materialFilter; }
    public boolean getNotVerbalFilter() { return notVerbalFilter; }
    public boolean getNotSomaticFilter() { return notSomaticFilter; }
    public boolean getNotMaterialFilter() { return notMaterialFilter; }
    public Set<Source> getVisibleSources() { return visibleSources; }
    public EnumSet<School> getVisibleSchools() { return visibleSchools; }
    public EnumSet<CasterClass> getVisibleClasses() { return visibleClasses; }
    public EnumSet<CastingTimeType> getVisibleCastingTimeTypes() { return visibleCastingTimeTypes; }
    public EnumSet<DurationType> getVisibleDurationTypes() { return visibleDurationTypes; }
    public EnumSet<RangeType> getVisibleRangeTypes() { return visibleRangeTypes; }
    public Duration getMinDuration() { return minDuration; }
    public Duration getMaxDuration() { return maxDuration; }
    public CastingTime getMinCastingTime() { return minCastingTime; }
    public CastingTime getMaxCastingTime() { return maxCastingTime; }
    public Range getMinRange() { return minRange; }
    public Range getMaxRange() { return maxRange; }


    // Setters
    public void setFirstSortField(SortField firstSortField) { this.firstSortField = firstSortField; }
    public void setSecondSortField(SortField secondSortField) { this.secondSortField = secondSortField; }
    public void setFirstSortReverse(boolean firstSortReverse) { this.firstSortReverse = firstSortReverse; }
    public void setSecondSortReverse(boolean secondSortReverse) { this.secondSortReverse = secondSortReverse; }
    public void setSpellStatuses(Map<String,SpellStatus> spellStatuses) { this.spellStatuses = spellStatuses; }
    public void setStatusFilter(StatusFilterField statusFilter) { this.statusFilter = statusFilter; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public void setRitualFilter(boolean ritualFilter) { this.ritualFilter = ritualFilter; }
    public void setNotRitualFilter(boolean notRitualFilter) { this.notRitualFilter = notRitualFilter; }
    public void setConcentrationFilter(boolean concentrationFilter) { this.concentrationFilter = concentrationFilter; }
    public void setNotConcentrationFilter(boolean notConcentrationFilter) { this.notConcentrationFilter = notConcentrationFilter; }
    public void setVerbalFilter(boolean verbalFilter) { this.verbalFilter = verbalFilter; }
    public void setNotVerbalFilter(boolean notVerbalFilter) { this.notVerbalFilter = notVerbalFilter; }
    public void setSomaticFilter(boolean somaticFilter) { this.somaticFilter = somaticFilter; }
    public void setNotSomaticFilter(boolean notSomaticFilter) { this.notSomaticFilter = notSomaticFilter; }
    public void setMaterialFilter(boolean materialFilter) { this.materialFilter = materialFilter; }
    public void setNotMaterialFilter(boolean notMaterialFilter) { this.notMaterialFilter = notMaterialFilter; }
    public void setVisibleSchools(EnumSet<School> visibleSchools) { this.visibleSchools = visibleSchools; }
    public void setVisibleSources(Set<Source> visibleSources) { this.visibleSources = visibleSources; }
    public void setVisibleClasses(EnumSet<CasterClass> visibleClasses) { this.visibleClasses = visibleClasses; }
    public void setVisibleCastingTimeTypes(EnumSet<CastingTimeType> visibleCastingTimeTypes) { this.visibleCastingTimeTypes = visibleCastingTimeTypes; }
    public void setVisibleDurationTypes(EnumSet<DurationType> visibleDurationTypes) { this.visibleDurationTypes = visibleDurationTypes; }
    public void setVisibleRangeTypes(EnumSet<RangeType> visibleRangeTypes) { this.visibleRangeTypes = visibleRangeTypes; }
    public void setMinDuration(Duration minDuration) { this.minDuration = minDuration; }
    public void setMaxDuration(Duration maxDuration) { this.maxDuration = maxDuration; }
    public void setMinCastingTime(CastingTime minCastingTime) { this.minCastingTime = minCastingTime; }
    public void setMaxCastingTime(CastingTime maxCastingTime) { this.maxCastingTime = maxCastingTime; }
    public void setMinRange(Range minRange) { this.minRange = minRange; }
    public void setMaxRange(Range maxRange) { this.maxRange = maxRange; }

}
