package dnd.jon.spellbook;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class QueryUtilities {

    static void addInCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<String> items) {
        final String placeholders = TextUtils.join(",", Collections.nCopies(items.size(), "?"));
        queryItems.add("(" + fieldName + " IN (" + placeholders + "))");
        queryArgs.addAll(items);
        System.out.println(TextUtils.join(", ", items));
    }

    static <T> Collection<String> names(Collection<T> items, Function<T,String> nameGetter) {
        return items.stream().map(nameGetter).collect(Collectors.toList());
    }

    static <T extends Named> void addInNamesCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<T> items, Function<T,String> nameGetter) {
        addInCheck(queryItems, queryArgs, fieldName, names(items, nameGetter));
    }

    static <T extends Enum<T> & Named> void addInEnumNamesCheck(List<String> queryItems, List<Object> queryArgs, String fieldName, Collection<T> items, Class<T> type, Function<T,String> nameGetter) {

        // We only need to do the check if the set of visible items has smaller size than the number of enum values
        // We assume that all entries in items are unique
        final T[] values = type.getEnumConstants();
        if (values != null && items.size() < values.length) {
            addInNamesCheck(queryItems, queryArgs, fieldName, items, nameGetter);
        }
    }

    static void addFilterCheck(List<String> queryItems, String fieldName, boolean yesVisible, boolean noVisible) {

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

    static void addSpanningRangeCheck(List<String> queryItems, List<Object> queryArgs, String prefix, int minValue, int maxValue) {
        queryItems.add("(" + prefix + "base_value BETWEEN ? AND ?)");
        queryArgs.add(minValue);
        queryArgs.add(maxValue);
    }

   static <T extends Enum<T> & QuantityType> String quantityTypeSort(Class<T> type, String fieldName) {

        final StringBuilder sb = new StringBuilder("(CASE ").append(fieldName).append(" ");
        final T[] values = type.getEnumConstants();
        if (values == null) { return ""; }
        for (T t : values) {
            sb.append(" WHEN ").append(t.getDisplayName()).append(" THEN ").append(t.ordinal());
        }
        sb.append(" END)");

        return sb.toString();
    }

    static String quantitySort(String typeSorter, String prefix, boolean reverse) {
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

    static String sortString(SortField sortField, boolean reverse) {
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

    static String fieldContainsCheck(String fieldName) { return "(" + fieldName + " LIKE '%' || ? || '%')"; }

    static final String durationTypeSort = quantityTypeSort(Duration.DurationType.class, "duration_type");
    static final String castingTimeTypeSort = quantityTypeSort(CastingTime.CastingTimeType.class, "casting_time_type");
    static final String rangeTypeSort = quantityTypeSort(Range.RangeType.class, "range_type");

    // The query that we need is a bit too complicated to do at compile-time
    // In particular, it's the fact that each spell has multiple visible classes
    // So we construct the query dynamically at runtime
    static SimpleSQLiteQuery getVisibleSpellsQuery(CharacterProfile profile, StatusFilterField statusFilter, int minLevel, int maxLevel, boolean ritualVisible, boolean notRitualVisible, boolean concentrationVisible, boolean notConcentrationVisible,
                                           boolean verbalVisible, boolean notVerbalVisible, boolean somaticVisible, boolean notSomaticVisible, boolean materialVisible, boolean notMaterialVisible,
                                           Collection<Source> visibleSources, Collection<CasterClass> visibleCasters, Collection<School> visibleSchools, Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes,
                                           int minCastingTimeValue, int maxCastingTimeValue, Collection<Duration.DurationType> visibleDurationTypes, int minDurationValue, int maxDurationValue,
                                           Collection<Range.RangeType> visibleRangeTypes, int minRangeValue, int maxRangeValue, String filterText, SortField sortField1, SortField sortField2, boolean reverse1, boolean reverse2) {

        final List<String> queryItems = new ArrayList<>();
        final List<Object> queryArgs = new ArrayList<>();

        // First, check if this is excluded by the name filtering text
        if (filterText != null && !filterText.isEmpty()) {
            queryItems.add(fieldContainsCheck("name"));
            queryArgs.add(filterText);
        }

        // Check that the spell's school is visible
        addInCheck(queryItems, queryArgs, "source_id", visibleSources.stream().map(src -> Integer.toString(src.getId())).collect(Collectors.toList()));
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
        final String casterClassCondition = fieldContainsCheck("classes");
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
        final StringBuilder sb = new StringBuilder("SELECT * FROM spells ");

        // If we have a status filter, inner join the spells table with the entries from spell_lists with the desired filter
        if (statusFilter != StatusFilterField.ALL) {
            sb.append("INNER JOIN (SELECT spell_id FROM spell_lists WHERE ").append(statusFilter.getDisplayName()).append(" = 1) ")
                    .append("ON spells.id = spell_lists.spell_id ");
        }

        // Add the sourcebooks check
        //sb.append("INNER JOIN (SELECT source_id FROM sources_lists WHERE character_id = ?) srcs ON spells.source_id = srcs.source_id");
        //queryArgs.add(profile.getId());

        final String filterString = TextUtils.join(" AND ", queryItems);
        sb.append("WHERE ").append(filterString).append(" ORDER BY ").append(sortString(sortField1, reverse1));
        if (sortField1 != sortField2) {
            sb.append(", ").append(sortString(sortField2, reverse2));
        }
        final String queryString = sb.toString();
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString, queryArgs.toArray());
        System.out.println(query.getArgCount());
        System.out.println(query.getSql());

        return query;

    }

}
