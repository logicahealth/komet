package sh.komet.gui.control.datetimepicker;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.AccessibleAttribute;

/**
 * PickerTextField used in pickers {@link JFXDatePicker}, {@link TimePicker}
 * <p>
 * Created by sshahine on 6/8/2017.
 */
final class FakeFocusPickerTextField extends PickerTextField {
    @Override
    public void requestFocus() {
        if (getParent() != null) {
            getParent().requestFocus();
        }
    }

    public void setFakeFocus(boolean b) {
        setFocused(b);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case FOCUS_ITEM:
                // keep focus on parent control
                return getParent();
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    public ReadOnlyObjectWrapper<ValidatorBase> activeValidatorWritableProperty() {
        return validationControl.activeValidatorWritableProperty();
    }
}
