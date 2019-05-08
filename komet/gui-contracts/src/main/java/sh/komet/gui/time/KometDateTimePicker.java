/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/**
 *
 * @author kec
 * Modified from tornadofx.control.DateTimePicker
 */
public class KometDateTimePicker extends DatePicker {
	public static final String DefaultFormat = "yyyy-MM-dd HH:mm";

	private DateTimeFormatter formatter;
	private ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(LocalDateTime.now());
	private ObjectProperty<String> format = new SimpleObjectProperty<String>() {
		public void set(String newValue) {
			super.set(newValue);
			formatter = DateTimeFormatter.ofPattern(newValue);
		}
	};

	public void alignColumnCountWithFormat() {
		getEditor().setPrefColumnCount(getFormat().length());
	}

	public KometDateTimePicker() {
		getStyleClass().add("datetime-picker");
		setFormat(DefaultFormat);
		setConverter(new InternalConverter());
        alignColumnCountWithFormat();

		// Synchronize changes to the underlying date value back to the dateTimeValue
		valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				dateTimeValue.set(null);
			} else {
				if (dateTimeValue.get() == null) {
					dateTimeValue.set(LocalDateTime.of(newValue, LocalTime.now()));
				} else {
					LocalTime time = dateTimeValue.get().toLocalTime();
					dateTimeValue.set(LocalDateTime.of(newValue, time));
				}
			}
		});

		// Syncronize changes to dateTimeValue back to the underlying date value
		dateTimeValue.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                LocalDate dateValue = newValue.toLocalDate();
                boolean forceUpdate = dateValue.equals(valueProperty().get());
                // Make sure the display is updated even when the date itself wasn't changed
                setValue(dateValue);
                if (forceUpdate) setConverter(new InternalConverter());
            } else {
                setValue(null);
            }

        });

		// Persist changes onblur
		getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				simulateEnterPressed();
		});

	}

	private void simulateEnterPressed() {
		getEditor().commitValue();
	}

	public LocalDateTime getDateTimeValue() {
		return dateTimeValue.get();
	}

	public void setDateTimeValue(LocalDateTime dateTimeValue) {
		this.dateTimeValue.set(dateTimeValue);
	}

	public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
		return dateTimeValue;
	}

	public String getFormat() {
		return format.get();
	}

	public ObjectProperty<String> formatProperty() {
		return format;
	}

	public void setFormat(String format) {
		this.format.set(format);
		alignColumnCountWithFormat();
	}

	class InternalConverter extends StringConverter<LocalDate> {
                @Override
		public String toString(LocalDate object) {
			LocalDateTime value = getDateTimeValue();
                        if (value != null) {
                            if (value.equals(LocalDateTime.MAX)) {
                                return "latest";
                            }
                        }
			return (value != null) ? value.format(formatter) : "";
		}

                @Override
		public LocalDate fromString(String value) {
			if (value == null || value.isEmpty()) {
				dateTimeValue.set(null);
				return null;
			}
                        if (value.equalsIgnoreCase("latest")) {
                            dateTimeValue.set(LocalDateTime.MAX);
                            return dateTimeValue.get().toLocalDate();
                        }

			dateTimeValue.set(LocalDateTime.parse(value, formatter));
			return dateTimeValue.get().toLocalDate();
		}
	}
}