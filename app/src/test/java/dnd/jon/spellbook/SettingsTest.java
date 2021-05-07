package dnd.jon.spellbook;

import com.google.common.truth.Truth;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SettingsTest {

    @Test
    public void Settings_CorrectParse_v2_10_n1() {
        final String jsonString = "{\"TableTextSize\":16,\"TableNRows\":10,\"SpellTextSize\":15,\"HeaderTextSize\":18,\"Character\":\"2B\"}";
        try {
            final JSONObject json = new JSONObject(jsonString);
            final Settings settings = new Settings(json);

            Truth.assertThat(settings.characterName()).isEqualTo("2B");
            Truth.assertThat(settings.headerTextSize()).isEqualTo(18);
            Truth.assertThat(settings.spellTextSize()).isEqualTo(15);
            Truth.assertThat(settings.nTableRows()).isEqualTo(10);
            Truth.assertThat(settings.tableTextSize()).isEqualTo(16);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void Settings_CorrectParse_v2_10_n2() {
        final String jsonString = "{\"TableTextSize\":16,\"TableNRows\":10,\"SpellTextSize\":15,\"HeaderTextSize\":18,\"Character\":\"Test2\"}";
        try {

            final JSONObject json = new JSONObject(jsonString);
            final Settings settings = new Settings(json);

            Truth.assertThat(settings.characterName()).isEqualTo("Test2");
            Truth.assertThat(settings.headerTextSize()).isEqualTo(18);
            Truth.assertThat(settings.spellTextSize()).isEqualTo(15);
            Truth.assertThat(settings.nTableRows()).isEqualTo(10);
            Truth.assertThat(settings.tableTextSize()).isEqualTo(16);
        } catch (JSONException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


}
