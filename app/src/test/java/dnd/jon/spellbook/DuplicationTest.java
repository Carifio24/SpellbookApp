package dnd.jon.spellbook;

import com.google.common.truth.Truth;

import org.junit.Test;

public class DuplicationTest {

    @Test
    public void checkSortFilterDuplication() {
        final SortFilterStatus original = new SortFilterStatus();
        final SortFilterStatus clone = original.duplicate();
        Truth.assertThat(clone.getConcentrationFilter()).isEqualTo(true);
    }
}
