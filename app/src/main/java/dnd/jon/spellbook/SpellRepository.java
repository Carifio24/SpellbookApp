package dnd.jon.spellbook;

import android.app.Application;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellRepository {

    private SpellDao spellDao;

    SpellRepository(Application application) {
        final SpellRoomDatabase db = SpellRoomDatabase.getDatabase(application);
        spellDao = db.spellDao();
    }

    LiveData<List<Spell>> getAllSpells() { return spellDao.getAllSpells(); }
    List<Spell> getAllSpellsTest() { return spellDao.getAllSpellsTest(); }

    /*
    ("SELECT * from spells where (level >= :minLevel) AND (level <= :maxLevel) AND (school NOT IN  (:hiddenSchoolNames)) AND (sourcebook NOT IN (:hiddenSourcebookNames))"
            // Handle the range checks
            + "AND (range_type = :rangeType) AND (range_base_value >= :minRangeValue) AND (range_base_value <= :maxRangeValue)" +
            // Handle the duration checks
            "AND (duration_type = :durationType) AND (duration_base_value >= :minDurationValue) AND (duration_base_value <= :maxDurationValue) " +
            // Handle the casting time checks
            "AND (casting_time_type = :castingTimeType) AND (casting_time_base_value >= :minCastingTimeValue) AND (casting_time_base_value <= :maxCastingTimeValue)" +
            // Handle the ritual filters
            "AND (NOT (ritual AND (NOT :ritualVisible))) AND ((NOT ritual) AND (NOT :notRitualVisible))" +
            // Handle the concentration filters
            "AND (NOT (concentration AND (NOT :concentrationVisible))) AND ((NOT concentration) AND (NOT :notConcentrationVisible))" +
            // Handle the verbal filters
            "AND (NOT (verbal AND (NOT :verbalVisible))) AND ((NOT verbal) AND (NOT :notVerbalVisible))" +
            // Handle the somatic filters
            "AND (NOT (somatic AND (NOT :somaticVisible))) AND ((NOT somatic) AND (NOT :notSomaticVisible))" +
            // Handle the material filters
            "AND (NOT (material AND (NOT :materialVisible))) AND ((NOT material) AND (NOT :notMaterialVisible))"
    )
     */

    private void addInCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<String> items) {
        final String placeholders = TextUtils.join(",", Collections.nCopies(items.size(), "?"));
        queryItems.add("(" + fieldName + " IN (" + placeholders + "))");
        queryArgs.addAll(items);
        System.out.println(TextUtils.join(", ", items));
    }

    private <T> Collection<String> names(Collection<T> items, Function<T,String> nameGetter) {
        return items.stream().map(nameGetter).collect(Collectors.toList());
    }

    private <T extends Named> void addInNamesCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<T> items, Function<T,String> nameGetter) {
        addInCheck(queryItems, queryArgs, fieldName, names(items, nameGetter));
    }

    private <T extends Enum<T> & Named> void addInEnumNamesCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<T> items, Class<T> type, Function<T,String> nameGetter) {

        // We only need to do the check if the set of visible items has smaller size than the number of enum values
        // We assume that all entries in items are unique
        final T[] values = type.getEnumConstants();
        if (values != null && items.size() < values.length) {
            addInNamesCheck(queryItems, queryArgs, fieldName, items, nameGetter);
        }
    }

    private void addFilterCheck(List<String> queryItems, String fieldName, boolean yesVisible, boolean noVisible) {

        // If both are visible, there's no need to add a filter
        if (yesVisible && noVisible) { return; }

        // Otherwise, add the filter check(s) to the query
        if (!noVisible) {
            queryItems.add("(" + fieldName + ")");
        }
        if (!yesVisible) {
            queryItems.add("(NOT " + fieldName + ")");
        }
    }

    private void addSpanningRangeCheck(List<String> queryItems, List<Object> queryArgs, String prefix, int minValue, int maxValue) {
        queryItems.add("(" + prefix + "base_value BETWEEN ? AND ?)");
        queryArgs.add(minValue);
        queryArgs.add(maxValue);
    }

    private static <T extends Enum<T> & QuantityType> String quantityTypeSort(Class<T> type, String fieldName) {

        final StringBuilder sb = new StringBuilder("(CASE ").append(fieldName).append(" ");
        final T[] values = type.getEnumConstants();
        if (values == null) { return ""; }
        for (T t : values) {
            sb.append(" WHEN ").append(t.getDisplayName()).append(" THEN ").append(t.ordinal());
        }
        sb.append(" END)");

        return sb.toString();
    }

    private static String quantitySort(String typeSorter, String prefix, boolean reverse) {
        final StringBuilder sb = new StringBuilder();
        if (!typeSorter.isEmpty()) {
            sb.append(typeSorter);
            if (reverse) {
                sb.append(" DESC");
            } else {
                sb.append(", ");
            }
        }
        sb.append(prefix).append("base_value");
        if (reverse) { sb.append(" DESC"); }
        return sb.toString();
    }

    private static String sortString(SortField sortField, boolean reverse) {
        switch (sortField) {
            case NAME:
            case SCHOOL:
            case LEVEL:
                final String fieldName = sortField.getDisplayName().toLowerCase();
                return reverse ? fieldName + " DESC" : fieldName;
            case DURATION:
                return quantitySort(durationTypeSort, "duration_", reverse);
            case CASTING_TIME:
                return quantitySort(castingTimeTypeSort, "casting_time_", reverse);
            case RANGE:
                return quantitySort(rangeTypeSort, "range_", reverse);
            default:
                return SortField.NAME.getDisplayName().toLowerCase();
        }
    }

    private static final String durationTypeSort = quantityTypeSort(Duration.DurationType.class, "duration_type");
    private static final String castingTimeTypeSort = quantityTypeSort(CastingTime.CastingTimeType.class, "casting_time_type");
    private static final String rangeTypeSort = quantityTypeSort(Range.RangeType.class, "range_type");
    private static final String casterClassCondition = "(classes LIKE '%' || ? || '%')";

    // The query that we need is a bit too complicated to do at compile-time
    // In particular, it's the fact that each spell has multiple visible classes
    // So we construct the query dynamically at runtime
    LiveData<List<Spell>> getVisibleSpells(Collection<String> filterNames, int minLevel, int maxLevel, boolean ritualVisible, boolean notRitualVisible, boolean concentrationVisible, boolean notConcentrationVisible,
                                           boolean verbalVisible, boolean notVerbalVisible, boolean somaticVisible, boolean notSomaticVisible, boolean materialVisible, boolean notMaterialVisible,
                                           Collection<Sourcebook> visibleSourcebooks, Collection<CasterClass> visibleCasters, Collection<School> visibleSchools, Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes,
                                           int minCastingTimeValue, int maxCastingTimeValue, Collection<Duration.DurationType> visibleDurationTypes, int minDurationValue, int maxDurationValue,
                                           Collection<Range.RangeType> visibleRangeTypes, int minRangeValue, int maxRangeValue, String filterText, SortField sortField1, SortField sortField2, boolean reverse1, boolean reverse2) {

        final List<String> queryItems = new ArrayList<>();
        final List<Object> queryArgs = new ArrayList<>();

        // First, check if this is excluded by the name filtering text
        if (filterText != null && !filterText.isEmpty()) {
            queryItems.add("name LIKE '%?%");
            queryArgs.add(filterText);
        }

        // Next, check if this is excluded by the current profile's status filter
        if (filterNames != null) {
            addInCheck(queryItems, queryArgs, "name", filterNames);
        }

        // Check that the spell's sourcebook and school are visible
        addInEnumNamesCheck(queryItems, queryArgs, "sourcebook", visibleSourcebooks, Sourcebook.class, Sourcebook::getCode);
        addInEnumNamesCheck(queryItems, queryArgs, "school", visibleSchools, School.class, School::getDisplayName);

        // First, add the level checks, if necessary
        if (minLevel > Spellbook.MIN_SPELL_LEVEL) {
            queryItems.add("(level >= ?)");
            queryArgs.add(minLevel);
        }
        if (maxLevel < Spellbook.MAX_SPELL_LEVEL) {
            queryItems.add("(level <= ?)");
            queryArgs.add(maxLevel);
        }

        // Next, the various filters
        addFilterCheck(queryItems, "ritual", ritualVisible, notRitualVisible);
        addFilterCheck(queryItems, "concentration", concentrationVisible, notConcentrationVisible);
        addFilterCheck(queryItems, "verbal", verbalVisible, notVerbalVisible);
        addFilterCheck(queryItems, "somatic", somaticVisible, notSomaticVisible);
        addFilterCheck(queryItems, "material", materialVisible, notMaterialVisible);

        // Now do the quantity type checks
        addInEnumNamesCheck(queryItems, queryArgs, "casting_time_type", visibleCastingTimeTypes, CastingTime.CastingTimeType.class, CastingTime.CastingTimeType::getParseName);
        addInEnumNamesCheck(queryItems, queryArgs, "duration_type", visibleDurationTypes, Duration.DurationType.class, Duration.DurationType::getDisplayName);
        addInEnumNamesCheck(queryItems, queryArgs, "range_type", visibleRangeTypes, Range.RangeType.class, Range.RangeType::getDisplayName);

        // If the spanning type is selected for each quantity, do the spanning range check
        if (visibleCastingTimeTypes.contains(CastingTime.CastingTimeType.spanningType())) {
            addSpanningRangeCheck(queryItems, queryArgs, "casting_time_", minCastingTimeValue, maxCastingTimeValue);
        }

        if (visibleDurationTypes.contains(Duration.DurationType.spanningType())) {
            addSpanningRangeCheck(queryItems, queryArgs, "duration_", minDurationValue, maxDurationValue);
        }

        if (visibleRangeTypes.contains(Range.RangeType.spanningType())) {
            addSpanningRangeCheck(queryItems, queryArgs, "range_", minRangeValue, maxRangeValue);
        }

        // Check caster classes
        if (visibleCasters.size() < CasterClass.values().length) {
            final List<String> casterItems = new ArrayList<>();
            for (CasterClass casterClass : visibleCasters) {
                casterItems.add(casterClassCondition);
                queryArgs.add(casterClass.getDisplayName());
            }
            final String castersCondition = TextUtils.join(" OR ", casterItems);
            queryItems.add("(" + castersCondition + ")");
        }

        // Construct the query object
        final String filterString = TextUtils.join(" AND ", queryItems);
        final StringBuilder sb = new StringBuilder("SELECT * FROM spells WHERE ").append(filterString).append(" ORDER BY ").append(sortString(sortField1, reverse1));
        if (sortField1 != sortField2) {
            sb.append(", ").append(sortString(sortField2, reverse2));
        }
        final String queryString = sb.toString();
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString, queryArgs.toArray());
        System.out.println(query.getArgCount());
        System.out.println(query.getSql());

        //query = new SimpleSQLiteQuery("SELECT * FROM spells");

        // Send the query to the DAO and return the results
        return spellDao.getVisibleSpells(query);

    }
    


}
