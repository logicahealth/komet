package sh.isaac.model.observable.override;

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.api.observable.coordinate.PropertyWithOverride;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ListPropertyWithOverride<T> extends SimpleEqualityBasedListProperty<T> implements PropertyWithOverride<ObservableList<T>> {

    private final ListProperty<T> overriddenProperty;
    private boolean overridden = false;

    public ListPropertyWithOverride(ListProperty<T> overriddenProperty, Object bean) {
        super(bean, overriddenProperty.getName());
        this.overriddenProperty = overriddenProperty;
        this.set(overriddenProperty.getValue());
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
    public void set(ObservableList<T> newValue) {
        if (!overridden) {
            overridden = true;
        }
        if (newValue == null) {
            overridden = false;
            super.set(this.overriddenProperty.getValue());
        } else {
            super.set(newValue);
        }
    }

    @Override
    public boolean setAll(T... elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList());
        }
        return super.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList());
        }
        return super.setAll(elements);
    }

    @Override
    public void setValue(ObservableList<T> v) {
        if (!overridden) {
            overridden = true;
        }
        if (v == null) {
            overridden = false;
            super.setValue(this.overriddenProperty.getValue());
        } else {
            super.setValue(v);
        }

    }

    @Override
    public boolean add(T element) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.addAll(i, elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.retainAll(objects);
    }

    @Override
    public void clear() {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList());
        }
        super.clear();
    }

    @Override
    public T set(int i, T element) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.set(i, element);
    }

    @Override
    public void add(int i, T element) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        super.add(i, element);
    }

    @Override
    public T remove(int i) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.remove(i);
    }

    @Override
    public boolean addAll(T... elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.addAll(elements);
    }

    @Override
    public boolean removeAll(T... elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.removeAll(elements);
    }

    @Override
    public boolean retainAll(T... elements) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.retainAll(elements);
    }

    @Override
    public void remove(int from, int to) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        super.remove(from, to);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        super.replaceAll(operator);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        if (!overridden) {
            overridden = true;
            super.set(FXCollections.observableArrayList(get()));
        }
        return super.removeIf(filter);
    }

    @Override
    public void bind(ObservableValue<? extends ObservableList<T>> newObservable) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void bindBidirectional(Property<ObservableList<T>> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContentBidirectional(ObservableList<T> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindContent(ObservableList<T> list) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<T> subList(int from, int to) {
        throw new UnsupportedOperationException();
    }

}
