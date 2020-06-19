package dnd.jon.spellbook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToIntBiFunction;
import java.util.Comparator;
import java.util.EnumMap;

import android.util.Pair;

class SpellComparator implements Comparator<Spell> {

    private static int boolSign(boolean b) { return b ? -1 : 1; }

    private static ToIntBiFunction<Spell,Spell> compareIntProperty(ToIntFunction<Spell> property) {
        return (Spell s1, Spell s2) ->  property.applyAsInt(s1) - property.applyAsInt(s2);
    }

    private static <T extends Comparable<T>> ToIntBiFunction<Spell,Spell> compareProperty(Function<Spell,T> property) {
        return (Spell s1, Spell s2) -> property.apply(s1).compareTo(property.apply(s2));
    }

    private static ToIntBiFunction<Spell,Spell> defaultComparator = compareProperty(Spell::getName);
    static void setDefaultComparator(ToIntBiFunction<Spell,Spell> triComp) { defaultComparator = triComp; }

    private static final EnumMap<SortField,ToIntBiFunction<Spell,Spell>> sortFieldComparators = new EnumMap<SortField,ToIntBiFunction<Spell,Spell>>(SortField.class) {{
        put(SortField.NAME, compareProperty(Spell::getName));
        put(SortField.SCHOOL, compareIntProperty( (Spell s1) -> s1.getSchool().getValue()));
        put(SortField.LEVEL, compareIntProperty(Spell::getLevel));
        put(SortField.RANGE, compareProperty(Spell::getRange));
        put(SortField.DURATION, compareProperty(Spell::getDuration));
        put(SortField.CASTING_TIME, compareProperty(Spell::getCastingTime));
    }};

    // Member values
    // The list of tri-comparators
    private final List<Pair<ToIntBiFunction<Spell,Spell>,Boolean>> comparators;

    // Constructor
    // The ArrayList contains pairs of SortFields from which tri-comparators are obtained, and booleans indicating whether or not the comparison should be reversed
    SpellComparator(List<Pair<SortField,Boolean>> sortParameters) {
        comparators = new ArrayList<>();
        for (Pair<SortField,Boolean> sortParam : sortParameters) {
            ToIntBiFunction<Spell,Spell> triComparator = sortFieldComparators.get(sortParam.first);
            comparators.add(new Pair<>(triComparator, sortParam.second));
        }
    }

    // The comparison routine
    // The ArrayList contains pairs of tri-comparators, and booleans indicating whether or not the comparison should be reversed
    // The fields are compared in order. If there is still no difference, the default comparator (initially set to name) is used
    public int compare(Spell s1, Spell s2) {
        int r;
        for (Pair<ToIntBiFunction<Spell,Spell>,Boolean> comparator : comparators) {
            if ( (r = comparator.first.applyAsInt(s1, s2)) != 0) {
                return boolSign(comparator.second) * r;
            }
        }
        return defaultComparator.applyAsInt(s1, s2);
    }

}
