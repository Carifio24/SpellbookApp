package dnd.jon.spellbook;

import android.app.Application;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.InvocationTargetException;

public class SpellbookViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    SpellbookViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) throws RuntimeException {
        if (modelClass == SpellbookViewModel.class) {
            return (T) new SpellbookViewModel(application);
        } else {
            throw new RuntimeException("Cannot create an instance of " + modelClass);
        }
    }

}
