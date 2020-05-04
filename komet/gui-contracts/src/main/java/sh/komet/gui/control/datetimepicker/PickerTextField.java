package sh.komet.gui.control.datetimepicker;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.skins.JFXTextFieldSkin;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.PaintConverter;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PickerTextField is the material design implementation of a text Field.
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class PickerTextField extends TextField implements IFXLabelFloatControl {

    /**
     * {@inheritDoc}
     */
    public PickerTextField() {
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    public PickerTextField(String text) {
        super(text);
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new JFXTextFieldSkin<>(this);
    }

    private void initialize() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        if ("dalvik".equals(System.getProperty("java.vm.name").toLowerCase())) {
            this.setStyle("-fx-skin: \"com.jfoenix.android.skins.JFXTextFieldSkinAndroid\";");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return USER_AGENT_STYLESHEET;
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    /**
     * wrapper for validation properties / methods
     */
    protected ValidationControl validationControl = new ValidationControl(this);

    @Override
    public ValidatorBase getActiveValidator() {
        return validationControl.getActiveValidator();
    }

    @Override
    public ReadOnlyObjectProperty<ValidatorBase> activeValidatorProperty() {
        return validationControl.activeValidatorProperty();
    }

    @Override
    public ObservableList<ValidatorBase> getValidators() {
        return validationControl.getValidators();
    }

    @Override
    public void setValidators(ValidatorBase... validators) {
        validationControl.setValidators(validators);
    }

    @Override
    public boolean validate() {
        return validationControl.validate();
    }

    @Override
    public void resetValidation() {
        validationControl.resetValidation();
    }

    /***************************************************************************
     *                                                                         *
     * Styleable Properties                                                    *
     *                                                                         *
     **************************************************************************/

    /**
     * Initialize the style class to 'jfx-text-field'.
     * <p>
     * This is the selector class from which CSS can be used to style
     * this control.
     */
    private static final String DEFAULT_STYLE_CLASS = "jfx-text-field";
    private static final String USER_AGENT_STYLESHEET = JFoenixResources.load("css/controls/jfx-text-field.css").toExternalForm();


    /**
     * set true to show a float the prompt text when focusing the field
     */
    private StyleableBooleanProperty labelFloat = new SimpleStyleableBooleanProperty(PickerTextField.StyleableProperties.LABEL_FLOAT,
            PickerTextField.this,
            "lableFloat",
            false);

    @Override
    public final StyleableBooleanProperty labelFloatProperty() {
        return this.labelFloat;
    }

    @Override
    public final boolean isLabelFloat() {
        return this.labelFloatProperty().get();
    }

    @Override
    public final void setLabelFloat(final boolean labelFloat) {
        this.labelFloatProperty().set(labelFloat);
    }

    /**
     * default color used when the field is unfocused
     */
    private StyleableObjectProperty<Paint> unFocusColor = new SimpleStyleableObjectProperty<>(PickerTextField.StyleableProperties.UNFOCUS_COLOR,
            PickerTextField.this,
            "unFocusColor",
            Color.rgb(77,
                    77,
                    77));

    @Override
    public Paint getUnFocusColor() {
        return unFocusColor == null ? Color.rgb(77, 77, 77) : unFocusColor.get();
    }

    @Override
    public StyleableObjectProperty<Paint> unFocusColorProperty() {
        return this.unFocusColor;
    }

    @Override
    public void setUnFocusColor(Paint color) {
        this.unFocusColor.set(color);
    }

    /**
     * default color used when the field is focused
     */
    private StyleableObjectProperty<Paint> focusColor = new SimpleStyleableObjectProperty<>(PickerTextField.StyleableProperties.FOCUS_COLOR,
            PickerTextField.this,
            "focusColor",
            Color.valueOf("#4059A9"));

    @Override
    public Paint getFocusColor() {
        return focusColor == null ? Color.valueOf("#4059A9") : focusColor.get();
    }

    @Override
    public StyleableObjectProperty<Paint> focusColorProperty() {
        return this.focusColor;
    }

    @Override
    public void setFocusColor(Paint color) {
        this.focusColor.set(color);
    }

    /**
     * disable animation on validation
     */
    private StyleableBooleanProperty disableAnimation = new SimpleStyleableBooleanProperty(PickerTextField.StyleableProperties.DISABLE_ANIMATION,
            PickerTextField.this,
            "disableAnimation",
            false);

    @Override
    public final StyleableBooleanProperty disableAnimationProperty() {
        return this.disableAnimation;
    }

    @Override
    public final Boolean isDisableAnimation() {
        return disableAnimation != null && this.disableAnimationProperty().get();
    }

    @Override
    public final void setDisableAnimation(final Boolean disabled) {
        this.disableAnimationProperty().set(disabled);
    }


    private static class StyleableProperties {
        private static final CssMetaData<PickerTextField, Paint> UNFOCUS_COLOR = new CssMetaData<PickerTextField, Paint>(
                "-jfx-unfocus-color",
                PaintConverter.getInstance(),
                Color.valueOf("#A6A6A6")) {
            @Override
            public boolean isSettable(PickerTextField control) {
                return control.unFocusColor == null || !control.unFocusColor.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(PickerTextField control) {
                return control.unFocusColorProperty();
            }
        };
        private static final CssMetaData<PickerTextField, Paint> FOCUS_COLOR = new CssMetaData<PickerTextField, Paint>(
                "-jfx-focus-color",
                PaintConverter.getInstance(),
                Color.valueOf("#3f51b5")) {
            @Override
            public boolean isSettable(PickerTextField control) {
                return control.focusColor == null || !control.focusColor.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(PickerTextField control) {
                return control.focusColorProperty();
            }
        };
        private static final CssMetaData<PickerTextField, Boolean> LABEL_FLOAT = new CssMetaData<PickerTextField, Boolean>(
                "-jfx-label-float",
                BooleanConverter.getInstance(),
                false) {
            @Override
            public boolean isSettable(PickerTextField control) {
                return control.labelFloat == null || !control.labelFloat.isBound();
            }

            @Override
            public StyleableBooleanProperty getStyleableProperty(PickerTextField control) {
                return control.labelFloatProperty();
            }
        };

        private static final CssMetaData<PickerTextField, Boolean> DISABLE_ANIMATION =
                new CssMetaData<PickerTextField, Boolean>("-jfx-disable-animation",
                        BooleanConverter.getInstance(), false) {
                    @Override
                    public boolean isSettable(PickerTextField control) {
                        return control.disableAnimation == null || !control.disableAnimation.isBound();
                    }

                    @Override
                    public StyleableBooleanProperty getStyleableProperty(PickerTextField control) {
                        return control.disableAnimationProperty();
                    }
                };


        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(
                    TextField.getClassCssMetaData());
            Collections.addAll(styleables, UNFOCUS_COLOR, FOCUS_COLOR, LABEL_FLOAT, DISABLE_ANIMATION);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return PickerTextField.StyleableProperties.CHILD_STYLEABLES;
    }
}
