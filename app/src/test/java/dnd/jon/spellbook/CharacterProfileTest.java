package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.google.common.truth.Truth;

@RunWith(RobolectricTestRunner.class)
public class CharacterProfileTest {

    @Test
    public void CorrectParseTest_v2_10() {
        final String jsonString = "{\"CharacterName\":\"2B\",\"Spells\":[{\"SpellID\":178,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":434,\"Favorite\":true,\"Prepared\":true,\"Known\":true},{\"SpellID\":197,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":199,\"Favorite\":false,\"Prepared\":true,\"Known\":false},{\"SpellID\":254,\"Favorite\":false,\"Prepared\":true,\"Known\":false}],\"SortField1\":\"Duration\",\"SortField2\":\"Casting Time\",\"Reverse1\":false,\"Reverse2\":true,\"HiddenCastingTimeTypes\":[\"time\"],\"HiddenSourcebooks\":[],\"HiddenRangeTypes\":[],\"HiddenSchools\":[\"Abjuration\",\"Divination\"],\"HiddenDurationTypes\":[],\"HiddenCasters\":[\"Artificer\",\"Bard\"],\"QuantityRanges\":{\"CastingTimeFilters\":{\"MinUnit\":\"second\",\"MaxUnit\":\"hour\",\"MinText\":\"0\",\"MaxText\":\"24\"},\"RangeFilters\":{\"MinUnit\":\"foot\",\"MaxUnit\":\"mile\",\"MinText\":\"0\",\"MaxText\":\"1\"},\"DurationFilters\":{\"MinUnit\":\"minute\",\"MaxUnit\":\"hour\",\"MinText\":\"2\",\"MaxText\":\"3\"}},\"StatusFilter\":\"All\",\"Ritual\":true,\"NotRitual\":true,\"Concentration\":true,\"NotConcentration\":true,\"ComponentsFilters\":[true,true,true],\"NotComponentsFilters\":[true,true,true],\"MinSpellLevel\":1,\"MaxSpellLevel\":8,\"ApplyFiltersToSpellLists\":false,\"ApplyFiltersToSearch\":false,\"UseTCEExpandedLists\":false,\"VersionCode\":\"2.10.0\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final CharacterProfile cp = CharacterProfile.fromJSON(json);
            Truth.assertThat(cp.getName()).isEqualTo("2B");
            Truth.assertThat(cp.getStatusFilter()).isEqualTo(StatusFilterField.ALL);
            Truth.assertThat(cp.getFirstSortField()).isEqualTo(SortField.DURATION);
            Truth.assertThat(cp.getSecondSortField()).isEqualTo(SortField.CASTING_TIME);
            Truth.assertThat(cp.getFirstSortReverse()).isFalse();
            Truth.assertThat(cp.getSecondSortReverse()).isTrue();
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

}
