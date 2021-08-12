package dnd.jon.spellbook;

import androidx.arch.core.util.Function;
import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyAwareLiveData<T extends BaseObservable> extends MutableLiveData<T> {

    private final class PropertyWatcherInfo<P> {
        final LifecycleOwner lifecycleOwner;
        final MutableLiveData<P> liveData;
        final Function<T,P> transform;

        PropertyWatcherInfo(LifecycleOwner lifecycleOwner, MutableLiveData<P> liveData, Function<T,P> transform) {
            this.lifecycleOwner = lifecycleOwner;
            this.transform = transform;
            this.liveData = liveData;
        }

        // We do this update in a method for type safety
        void updateValue(T sourceValue) {
            liveData.setValue(transform.apply(sourceValue));
        }
    }

    private final Map<Integer, List<PropertyWatcherInfo<?>>> callbacks = new HashMap<>();

    // This is the method that the 'client' (i.e. one of our Fragments) calls
    // It returns a LiveData that will be updated when the appropriate property is changed
    <P> LiveData<P> livePropertyWatcher(LifecycleOwner owner, int propertyID, Function<T,P> transform) {
        final MutableLiveData<P> liveData = new MutableLiveData<>(transform.apply(getValue()));
        final PropertyWatcherInfo<P> watcherInfo = new PropertyWatcherInfo<>(owner, liveData, transform);
        this.watchProperty(propertyID, watcherInfo);
        return liveData;
    }

    // This method adds the PropertyWatcherInfo to the internal map of callbacks
    private <P> void watchProperty(int propertyId, PropertyWatcherInfo<P> watcherInfo) {
        final List<PropertyWatcherInfo<?>> entry = callbacks.get(propertyId);
        if (entry != null) {
            entry.add(watcherInfo);
        } else {
            callbacks.put(propertyId, Collections.singletonList(watcherInfo));
        }
    }

    Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {

            // We only want to trigger updates if the sender is
            // the value that we're currently storing
            final T value = getValue();
            if (sender != value) {
                return;
            }

            final List<PropertyWatcherInfo<?>> toExecute = callbacks.get(propertyId);
            if (toExecute == null) { return; }
            for (PropertyWatcherInfo<?> info : toExecute) {
                final Lifecycle.State lifecycleState = info.lifecycleOwner.getLifecycle().getCurrentState();
                if (lifecycleState == Lifecycle.State.DESTROYED) {
                    toExecute.remove(info);
                    continue;
                }
                info.updateValue(value);
            }
        }
    };

    @Override
    public void setValue(T value) {
        super.setValue(value);
        value.addOnPropertyChangedCallback(callback);
    }

}
