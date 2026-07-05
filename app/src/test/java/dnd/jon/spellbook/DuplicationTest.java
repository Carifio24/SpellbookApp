package dnd.jon.spellbook;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RunWith(RobolectricTestRunner.class)
public class DuplicationTest {

    private void checkSortFilterEquivalent(SortFilterStatus status1, SortFilterStatus status2) {
        final List<Function<SortFilterStatus,Boolean>> boolGetters = Arrays.asList(
                SortFilterStatus::getConcentrationFilter,
                SortFilterStatus::getRitualFilter,
                SortFilterStatus::getApplyFiltersToLists,
                SortFilterStatus::getFirstSortReverse,
                SortFilterStatus::getSecondSortReverse
        );
        for (Function<SortFilterStatus,Boolean> getter : boolGetters) {
            Truth.assertThat(getter.apply(status1)).isEqualTo(getter.apply(status2));
        }

        Truth.assertThat(status1.getFirstSortField()).isEqualTo(status2.getFirstSortField());
        Truth.assertThat(status1.getSecondSortField()).isEqualTo(status2.getSecondSortField());
    }

    private void checkSpellFilterEquivalent(SpellFilterStatus status1, SpellFilterStatus status2) {
        final Collection<UUID> status1IDs = status1.spellIDsWithOneProperty();
        final Collection<UUID> status2IDs = status2.spellIDsWithOneProperty();

        Truth.assertThat(status1IDs).containsExactlyElementsIn(status2IDs);
        for (UUID id: status1IDs) {
            final SpellStatus ss1 = status1.getStatus(id);
            final SpellStatus ss2 = status2.getStatus(id);
            Truth.assertThat(ss1.favorite).isEqualTo(ss2.favorite);
            Truth.assertThat(ss1.prepared).isEqualTo(ss2.prepared);
            Truth.assertThat(ss1.known).isEqualTo(ss2.known);
        }
    }

    @Test
    @Config(sdk = 34)
    public void checkSortFilterDuplication() {
        final SortFilterStatus original = new SortFilterStatus();
        final SortFilterStatus clone = original.duplicate();
        checkSortFilterEquivalent(original, clone);
    }

    @Test
    @Config(sdk = 34)
    public void checkSpellFilterDuplication() {
        final SpellFilterStatus original = new SpellFilterStatus();
        final SpellFilterStatus clone = original.duplicate();
        checkSpellFilterEquivalent(original, clone);
    }
}
