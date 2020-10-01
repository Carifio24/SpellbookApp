package dnd.jon.spellbook;

import android.text.TextUtils;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class QueryUtilities {

    static String databaseField(String dbName, String field) { return dbName + "." + field; }

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
    static SimpleSQLiteQuery getVisibleSpellsQuery(CharacterProfile profile, String filterText) {

        final List<String> filterItems = new ArrayList<>();
        final List<Object> queryArgs = new ArrayList<>();

        // Get the character ID
        final long characterID = profile.getId();

        // The base query takes care of the classes, sources, and schools that are visible for this character
        final String baseQuery = "SELECT * FROM spells INNER JOIN (SELECT spell_id FROM spell_classes INNER JOIN (SELECT class_id FROM character_classes WHERE character_id = ?) AS scci ON scci.class_id = spell_classes.class_id GROUP BY spell_id) AS from_classes ON spells.id = from_classes.spell_id INNER JOIN (SELECT id from spells INNER JOIN (SELECT school_id FROM character_schools WHERE character_id = ?) AS cs on spells.school_id = cs.school_id) AS from_schools ON from_classes.spell_id = from_schools.id INNER JOIN (SELECT id FROM spells INNER JOIN (SELECT source_id FROM character_sources WHERE character_id = ?) AS cs ON spells.source_id = cs.source_id) AS from_sources ON from_classes.spell_id = from_sources.id";
        final StringBuilder sb = new StringBuilder(baseQuery);
        queryArgs.add(characterID); // We use the character ID three times in the base query
        queryArgs.add(characterID);
        queryArgs.add(characterID);

        // Add the spell list check (favorites, known, prepared) if necessary
        final StatusFilterField statusFilter = profile.getStatusFilter();
        if (statusFilter != StatusFilterField.ALL) {
            sb.append("INNER JOIN (SELECT spell_id FROM character_spells WHERE character_id = ? AND ").append(statusFilter.getDisplayName()).append(" = 1) AS from_character ON spells.id = from_character.spell_id)");
            queryArgs.add(characterID);
        }

        // First, check if the spell is excluded by the name filtering text
        if (filterText != null && !filterText.isEmpty()) {
            filterItems.add(fieldContainsCheck("name"));
            queryArgs.add(filterText);
        }

        // First, add the level checks, if necessary
        final int minLevel = profile.getMinLevel();
        if (minLevel > Spellbook.MIN_SPELL_LEVEL) {
            filterItems.add("(level >= ?)");
            queryArgs.add(minLevel);
        }
        final int maxLevel = profile.getMaxLevel();
        if (maxLevel < Spellbook.MAX_SPELL_LEVEL) {
            filterItems.add("(level <= ?)");
            queryArgs.add(maxLevel);
        }

        // Next, the various filters
        addFilterCheck(filterItems, "ritual", profile.getRitualFilter(), profile.getNotRitualFilter());
        addFilterCheck(filterItems, "concentration", profile.getConcentrationFilter(), profile.getNotConcentrationFilter());
        addFilterCheck(filterItems, "verbal", profile.getVerbalFilter(), profile.getNotVerbalFilter());
        addFilterCheck(filterItems, "somatic", profile.getSomaticFilter(), profile.getNotSomaticFilter());
        addFilterCheck(filterItems, "material", profile.getMaterialFilter(), profile.getNotMaterialFilter());

        // Now do the quantity type checks
        final Collection<CastingTime.CastingTimeType> visibleCastingTimeTypes = profile.getVisibleCastingTimeTypes();
        final Collection<Duration.DurationType> visibleDurationTypes = profile.getVisibleDurationTypes();
        final Collection<Range.RangeType> visibleRangeTypes = profile.getVisibleRangeTypes();
        addInEnumNamesCheck(filterItems, queryArgs, "casting_time_type", visibleCastingTimeTypes, CastingTime.CastingTimeType.class, CastingTime.CastingTimeType::getParseName);
        addInEnumNamesCheck(filterItems, queryArgs, "duration_type", visibleDurationTypes, Duration.DurationType.class, Duration.DurationType::getDisplayName);
        addInEnumNamesCheck(filterItems, queryArgs, "range_type", visibleRangeTypes, Range.RangeType.class, Range.RangeType::getDisplayName);

        // If the spanning type is selected for each quantity, do the spanning range check
        if (visibleCastingTimeTypes.contains(CastingTime.CastingTimeType.spanningType())) {
            addSpanningRangeCheck(filterItems, queryArgs, "casting_time_", profile.getMinCastingTime().baseValue, profile.getMaxCastingTime().baseValue);
        }

        if (visibleDurationTypes.contains(Duration.DurationType.spanningType())) {
            addSpanningRangeCheck(filterItems, queryArgs, "duration_", profile.getMinDuration().baseValue, profile.getMaxDuration().baseValue);
        }

        if (visibleRangeTypes.contains(Range.RangeType.spanningType())) {
            addSpanningRangeCheck(filterItems, queryArgs, "range_", profile.getMinRange().baseValue, profile.getMaxRange().baseValue);
        }

        final String filterString = TextUtils.join(" AND ", filterItems);
        final SortField sortField1 = profile.getFirstSortField();
        final SortField sortField2 = profile.getSecondSortField();
        final boolean reverse1 = profile.getFirstSortReverse();
        final boolean reverse2 = profile.getSecondSortReverse();
        sb.append(" WHERE ").append(filterString).append(" ORDER BY ").append(sortString(sortField1, reverse1));
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
