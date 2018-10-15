package sh.komet.gui.control;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class PropertySheetItemDateTimeWrapper implements PropertySheet.Item {

    private final String name;
    private final SimpleObjectProperty<LocalDateTime> dateObserver;
    private final LongProperty timeProperty;

    public PropertySheetItemDateTimeWrapper(String name, LongProperty timeProperty) {
        this.name = name;
        this.timeProperty = timeProperty;

        if (timeProperty.get() == Long.MAX_VALUE) {
            this.dateObserver = new SimpleObjectProperty<>(LocalDateTime.MAX);
        } else {
            this.dateObserver = new SimpleObjectProperty<>(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeProperty.get()),
                (OffsetDateTime.now(ZoneId.systemDefault())).getOffset()));
       }
    }

    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Select the a time value...";
    }

    @Override
    public LocalDateTime getValue() {
        return this.dateObserver.get();
    }

    @Override
    public void setValue(Object value) {
        LocalDateTime localDateTime = (LocalDateTime) value;
        if (value == null) {
            localDateTime = LocalDateTime.now();
        }
        this.dateObserver.setValue(localDateTime);
        if (localDateTime.isEqual(LocalDateTime.MAX)) {
            this.timeProperty.set(Long.MAX_VALUE);
        } else {
            this.timeProperty.set(localDateTime.toEpochSecond(ZoneOffset.UTC));
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.timeProperty);
    }
}
