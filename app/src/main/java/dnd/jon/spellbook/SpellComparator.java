package dnd.jon.spellbook;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToIntBiFunction;
import java.util.Comparator;

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

    private static final ArrayList<ToIntBiFunction<Spell,Spell>> sortFieldComparators = new ArrayList<ToIntBiFunction<Spell,Spell>>(Collections.nCopies(SortField.values().length, null)) {{
        set(SortField.Name.getIndex(), compareProperty(Spell::getName));
        set(SortField.School.getIndex(), compareIntProperty( (Spell s1) -> s1.getSchool().value));
        set(SortField.Level.getIndex(), compareIntProperty(Spell::getLevel));
        set(SortField.Range.getIndex(), compareProperty(Spell::getRange));
        set(SortField.Duration.getIndex(), compareProperty(Spell::getDuration));
    }};

    // Member values
    // The list of tri-comparators
    private final ArrayList<Pair<ToIntBiFunction<Spell,Spell>,Boolean>> comparators;

    // Constructor
    // The ArrayList contains pairs of SortFields from which tri-comparators are obtained, and booleans indicating whether or not the comparison should be reversed
    SpellComparator(ArrayList<Pair<SortField,Boolean>> sortParameters) {
        comparators = new ArrayList<>();
        for (Pair<SortField,Boolean> sortParam : sortParameters) {
            ToIntBiFunction<Spell,Spell> triComparator = sortFieldComparators.get(sortParam.first.getIndex());
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
