package sh.isaac.model.observable.override;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import sh.isaac.api.observable.coordinate.PropertyWithOverride;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

import java.util.Collection;
import java.util.function.Predicate;

public class SetPropertyWithOverride <T> extends SimpleEqualityBasedSetProperty<T>
    implements PropertyWithOverride<ObservableSet<T>> {

    private final SetProperty<T> overriddenProperty;
    private boolean overridden = false;


    public SetPropertyWithOverride(SetProperty<T> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
        this.bind(overriddenProperty);
    }



    @Override
    public boolean isOverridden() {
        return overridden;
    }

    @Override
    public void removeOverride() {
        this.set(null);
    }

    @Override
    public void set(ObservableSet<T> newValue) {
        if (!overridden) {
            overridden = true;
            this.unbind();
        }
        if (newValue == null) {
            overridden = false;
            this.bind(overriddenProperty);
        } else {
            super.set(newValue);
        }
    }

    @Override
    public boolean setAll(T... elements) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet());
        }
        return super.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet());
        }
        return super.setAll(elements);
    }

    @Override
    public void setValue(ObservableSet<T> v) {
        if (!overridden) {
            overridden = true;
            this.unbind();
        }
        if (v == null) {
            overridden = false;
            this.bind(this.overriddenProperty);
        } else {
            super.setValue(v);
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.remove(obj);
    }

    @Override
    public boolean add(T element) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.addAll(elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.retainAll(objects);
    }

    @Override
    public void clear() {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet());
        }
        super.clear();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (!overridden) {
            overridden = true;
            this.unbind();
            super.set(FXCollections.observableSet(get()));
        }
        return super.removeIf(filter);
    }
    @Override
    public void bindBidirectional(Property<ObservableSet<T>> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContentBidirectional(ObservableSet<T> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContent(ObservableSet<T> list) {
        throw new UnsupportedOperationException();
    }
}
