package sh.komet.gui.control;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.time.*;
import java.util.Optional;

public class PropertySheetItemDateTimeWrapper implements PropertySheet.Item {

    private final String name;
    private final SimpleObjectProperty<LocalDateTime> dateObserver;
    private final LongProperty timeProperty;

    public PropertySheetItemDateTimeWrapper(String name, LongProperty timeProperty) {
        this.name = name;
        this.timeProperty = timeProperty;
        Instant instant;

        if(timeProperty.get() == Long.MAX_VALUE){
            instant = Instant.now();
        }else{
            instant = Instant.ofEpochMilli(timeProperty.get());
        }
        this.dateObserver = new SimpleObjectProperty<>(LocalDateTime.ofInstant(instant,
                (OffsetDateTime.now(ZoneId.systemDefault())).getOffset()));
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
    public Object getValue() {
        return this.dateObserver.get();
    }

    @Override
    public void setValue(Object value) {
        LocalDateTime localDateTime = (LocalDateTime) value;
        this.dateObserver.setValue(localDateTime);
        this.timeProperty.set(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.timeProperty);
    }
}
