package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(AndroidJUnit4.class)
public class HomebrewSpellListsTest {

    @Before
    public void setup() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final File filesDir = context.getFilesDir();
        SpellbookUtils.deleteDirectory(filesDir);
    }

    @Test
    public void HomebrewSpellListTest() {
        final int N_SPELLS = 6;
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final SpellBuilder builder = new SpellBuilder(context);
        final SpellCodec codec = new SpellCodec(context);
        final List<Spell> spells = new ArrayList<>();
        final Function<Integer, Integer> idForIndex = id -> 100_000 + id;
        for (int i = 0; i < N_SPELLS; i++) {
            builder.setLevel(i);
            builder.setDescription(String.format("Spell number %d", i));
            final Spell spell = builder.buildAndReset();
            spells.add(spell);
            Spellbook.setUUIDForInt(idForIndex.apply(i), spell.getID());
        }
        final List<JSONObject> spellJSONs = spells.stream()
                .map(spell -> {
                    try {
                        return codec.toJSON(spell);
                    } catch (JSONException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        final int[] index = {0};
        spellJSONs.forEach(json -> {
            final int id = idForIndex.apply(index[0]);
            try {
                json.put(SpellCodec.ID_KEY, id);
            } catch (JSONException e) {
                Assert.fail(e.getMessage());
            }
            index[0]++;
        });

        final Set<Integer> favoriteIDs = Set.of(1, 2, 4).stream().map(idForIndex).collect(Collectors.toSet());
        final Set<Integer> knownIDs = Set.of(0, 1).stream().map(idForIndex).collect(Collectors.toSet());
        final Set<Integer> preparedIDs = Set.of(3, 5).stream().map(idForIndex).collect(Collectors.toSet());
        final Map<UUID, SpellStatus> spellStatuses = new HashMap<>();
        for (int i = 0; i < N_SPELLS; i++) {
            final int id = idForIndex.apply(i);
            final boolean isFavorite = favoriteIDs.contains(id);
            final boolean isPrepared = preparedIDs.contains(id);
            final boolean isKnown = knownIDs.contains(id);
            if (isFavorite || isPrepared || isKnown) {
               final SpellStatus spellStatus = new SpellStatus(isFavorite, isPrepared, isKnown);
               spellStatuses.put(spells.get(i).getID(), spellStatus);
            }
        }

        SpellFilterStatus spellFilterStatus = new SpellFilterStatus(spellStatuses);
        CharacterProfile profile = new CharacterProfile("Test", spellFilterStatus);
        JSONObject characterJSON;
        try {
            characterJSON = profile.toJSON();
            profile = CharacterProfile.fromJSON(characterJSON);
        } catch (JSONException e) {
            Assert.fail();
        }

        spellFilterStatus = profile.getSpellFilterStatus();
        final Collection<UUID> finalFavoriteIDs = spellFilterStatus.favoriteSpellIDs();
        final Collection<UUID> finalKnownIDs = spellFilterStatus.knownSpellIDs();
        final Collection<UUID> finalPreparedIDs = spellFilterStatus.preparedSpellIDs();
        assertEquals(3, finalFavoriteIDs.size());
    }
}
