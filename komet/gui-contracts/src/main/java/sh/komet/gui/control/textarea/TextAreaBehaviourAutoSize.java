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
package sh.komet.gui.control.textarea;

import com.sun.javafx.PlatformUtil;
import static com.sun.javafx.PlatformUtil.isMac;
import static com.sun.javafx.PlatformUtil.isWindows;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import com.sun.javafx.scene.text.HitInfo;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.KP_DOWN;
import static javafx.scene.input.KeyCode.KP_LEFT;
import static javafx.scene.input.KeyCode.KP_RIGHT;
import static javafx.scene.input.KeyCode.KP_UP;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.PAGE_DOWN;
import static javafx.scene.input.KeyCode.PAGE_UP;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 *
 * @author kec
 */
public class TextAreaBehaviourAutoSize extends TextInputControlBehavior<TextArea> {
    /**************************************************************************
     *                          Setup KeyBindings                             *
     *************************************************************************/
    protected static final List<KeyBinding> TEXT_AREA_BINDINGS = new ArrayList<KeyBinding>();
    static {
        TEXT_AREA_BINDINGS.add(new KeyBinding(HOME, KEY_PRESSED, "LineStart")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(END, KEY_PRESSED, "LineEnd")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "PreviousLine")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "PreviousLine")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "NextLine")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "NextLine")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(PAGE_UP, KEY_PRESSED, "PreviousPage")); // new
        TEXT_AREA_BINDINGS.add(new KeyBinding(PAGE_DOWN, KEY_PRESSED, "NextPage")); // new
        TEXT_AREA_BINDINGS.add(new KeyBinding(ENTER, KEY_PRESSED, "InsertNewLine")); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(TAB, KEY_PRESSED, "TraverseOrInsertTab")); // changed

        TEXT_AREA_BINDINGS.add(new KeyBinding(HOME, KEY_PRESSED, "SelectLineStart").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(END, KEY_PRESSED, "SelectLineEnd").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "SelectPreviousLine").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "SelectPreviousLine").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "SelectNextLine").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "SelectNextLine").shift()); // changed
        TEXT_AREA_BINDINGS.add(new KeyBinding(PAGE_UP, KEY_PRESSED, "SelectPreviousPage").shift()); // new
        TEXT_AREA_BINDINGS.add(new KeyBinding(PAGE_DOWN, KEY_PRESSED, "SelectNextPage").shift()); // new
        // Platform specific settings
        if (isMac()) {
            TEXT_AREA_BINDINGS.add(new KeyBinding(LEFT, KEY_PRESSED, "LineStart").shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_LEFT, KEY_PRESSED, "LineStart").shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(RIGHT, KEY_PRESSED, "LineEnd").shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_RIGHT, KEY_PRESSED, "LineEnd").shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "Home").shortcut());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "Home").shortcut());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "End").shortcut());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "End").shortcut());

            TEXT_AREA_BINDINGS.add(new KeyBinding(LEFT, KEY_PRESSED, "SelectLineStartExtend").shift().shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_LEFT, KEY_PRESSED, "SelectLineStartExtend").shift().shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(RIGHT, KEY_PRESSED, "SelectLineEndExtend").shift().shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_RIGHT, KEY_PRESSED, "SelectLineEndExtend").shift().shortcut()); // changed
            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "SelectHomeExtend").shortcut().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "SelectHomeExtend").shortcut().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "SelectEndExtend").shortcut().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "SelectEndExtend").shortcut().shift());

            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "ParagraphStart").alt());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "ParagraphStart").alt());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "ParagraphEnd").alt());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "ParagraphEnd").alt());

            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "SelectParagraphStart").alt().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "SelectParagraphStart").alt().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "SelectParagraphEnd").alt().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "SelectParagraphEnd").alt().shift());
        } else {
            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "ParagraphStart").ctrl());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "ParagraphStart").ctrl());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "ParagraphEnd").ctrl());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "ParagraphEnd").ctrl());
            TEXT_AREA_BINDINGS.add(new KeyBinding(UP, KEY_PRESSED, "SelectParagraphStart").ctrl().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_UP, KEY_PRESSED, "SelectParagraphStart").ctrl().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(DOWN, KEY_PRESSED, "SelectParagraphEnd").ctrl().shift());
            TEXT_AREA_BINDINGS.add(new KeyBinding(KP_DOWN, KEY_PRESSED, "SelectParagraphEnd").ctrl().shift());
        }
        // However, we want to consume other key press / release events too, for
        // things that would have been handled by the InputCharacter normally
        TEXT_AREA_BINDINGS.add(new KeyBinding(null, KEY_PRESSED, "Consume"));
    }

    private TextAreaAutoSizeSkin skin;
    private ContextMenu contextMenu;
    private TwoLevelFocusBehavior tlFocus;

    /**************************************************************************
     * Constructors                                                           *
     *************************************************************************/

    public TextAreaBehaviourAutoSize(final TextArea textArea) {
        super(textArea, TEXT_AREA_BINDINGS);

        contextMenu = new ContextMenu();
        if (IS_TOUCH_SUPPORTED) {
            contextMenu.getStyleClass().add("text-input-context-menu");
        }

        // Register for change events
        textArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // NOTE: The code in this method is *almost* and exact copy of what is in TextFieldBehavior.
                // The only real difference is that TextFieldBehavior selects all the text when the control
                // receives focus (when not gained by mouse click), whereas TextArea doesn't, and also the
                // TextArea doesn't lose selection on focus lost, whereas the TextField does.
                final TextArea textArea = getControl();
                if (textArea.isFocused()) {
                    if (!focusGainedByMouseClick) {
                        setCaretAnimating(true);
                    }
                } else {
//                    skin.hideCaret();
                    if (PlatformUtil.isIOS() && textArea.getScene() != null) {
                        // releasing the focus => we need to hide the native component and also native keyboard
                        textArea.getScene().getWindow().impl_getPeer().releaseInput();
                    }
                    focusGainedByMouseClick = false;
                    setCaretAnimating(false);
                }
            }
        });

        // Only add this if we're on an embedded platform that supports 5-button navigation
        if (com.sun.javafx.scene.control.skin.Utils.isTwoLevelFocus()) {
            tlFocus = new TwoLevelFocusBehavior(textArea); // needs to be last.
        }
    }

    @Override public void dispose() {
        if (tlFocus != null) tlFocus.dispose();
        super.dispose();
    }

    // An unholy back-reference!
    public void setTextAreaSkin(TextAreaAutoSizeSkin skin) {
        this.skin = skin;
    }

    /**************************************************************************
     * Key handling implementation                                            *
     *************************************************************************/

    @Override public void callAction(String name) {
        final TextArea textInputControl = getControl();

        boolean done = false;

        if (textInputControl.isEditable()) {
//            fnCaretAnim(false);
//            setCaretOpacity(1.0);
            setEditing(true);
            done = true;
            if ("InsertNewLine".equals(name)) insertNewLine();
            else if ("TraverseOrInsertTab".equals(name)) insertTab();
            else {
                done = false;
            }
            setEditing(false);
        }

        if (!done) {
            done = true;
            if ("LineStart".equals(name)) lineStart(false, false);
            else if ("LineEnd".equals(name)) lineEnd(false, false);
            else if ("SelectLineStart".equals(name)) lineStart(true, false);
            else if ("SelectLineStartExtend".equals(name)) lineStart(true, true);
            else if ("SelectLineEnd".equals(name)) lineEnd(true, false);
            else if ("SelectLineEndExtend".equals(name)) lineEnd(true, true);
            else if ("PreviousLine".equals(name)) skin.previousLine(false);
            else if ("NextLine".equals(name)) skin.nextLine(false);
            else if ("SelectPreviousLine".equals(name)) skin.previousLine(true);
            else if ("SelectNextLine".equals(name)) skin.nextLine(true);

            else if ("ParagraphStart".equals(name)) skin.paragraphStart(true, false);
            else if ("ParagraphEnd".equals(name)) skin.paragraphEnd(true, isWindows(), false);
            else if ("SelectParagraphStart".equals(name)) skin.paragraphStart(true, true);
            else if ("SelectParagraphEnd".equals(name)) skin.paragraphEnd(true, isWindows(), true);

            else if ("PreviousPage".equals(name)) skin.previousPage(false);
            else if ("NextPage".equals(name)) skin.nextPage(false);
            else if ("SelectPreviousPage".equals(name)) skin.previousPage(true);
            else if ("SelectNextPage".equals(name)) skin.nextPage(true);
            else if ("TraverseOrInsertTab".equals(name)) {
                // RT-40312: Non-editabe mode means traverse instead of insert.
                name = "TraverseNext";
                done = false;
            } else {
                done = false;
            }
        }
//            fnCaretAnim(true);

        if (!done) {
            super.callAction(name);
        }
    }

    private void insertNewLine() {
        TextArea textArea = getControl();
        textArea.replaceSelection("\n");
    }

    private void insertTab() {
        TextArea textArea = getControl();
        textArea.replaceSelection("\t");
    }

    @Override protected void deleteChar(boolean previous) {
        skin.deleteChar(previous);
    }

    @Override protected void deleteFromLineStart() {
        TextArea textArea = getControl();
        int end = textArea.getCaretPosition();

        if (end > 0) {
            lineStart(false, false);
            int start = textArea.getCaretPosition();
            if (end > start) {
                replaceText(start, end, "");
            }
        }
    }

    private void lineStart(boolean select, boolean extendSelection) {
        skin.lineStart(select, extendSelection);
    }

    private void lineEnd(boolean select, boolean extendSelection) {
        skin.lineEnd(select, extendSelection);
    }

    protected void scrollCharacterToVisible(int index) {
        // TODO this method should be removed when TextAreaSkin
        // TODO is refactored to no longer need it.
        skin.scrollCharacterToVisible(index);
    }

    @Override protected void replaceText(int start, int end, String txt) {
        getControl().replaceText(start, end, txt);
    }

    /**
     * If the focus is gained via response to a mouse click, then we don't
     * want to select all the text even if selectOnFocus is true.
     */
    private boolean focusGainedByMouseClick = false; // TODO!!
    private boolean shiftDown = false;
    private boolean deferClick = false;

    @Override public void mousePressed(MouseEvent e) {
        TextArea textArea = getControl();
        super.mousePressed(e);
        // We never respond to events if disabled
        if (!textArea.isDisabled()) {
            // If the text field doesn't have focus, then we'll attempt to set
            // the focus and we'll indicate that we gained focus by a mouse
            // click, TODO which will then NOT honor the selectOnFocus variable
            // of the textInputControl
            if (!textArea.isFocused()) {
                focusGainedByMouseClick = true;
                textArea.requestFocus();
            }

            // stop the caret animation
            setCaretAnimating(false);
            // only if there is no selection should we see the caret
//            setCaretOpacity(if (textInputControl.dot == textInputControl.mark) then 1.0 else 0.0);

            // if the primary button was pressed
            if (e.getButton() == MouseButton.PRIMARY && !(e.isMiddleButtonDown() || e.isSecondaryButtonDown())) {
                HitInfo hit = skin.getIndex(e.getX(), e.getY());
                int i = com.sun.javafx.scene.control.skin.Utils.getHitInsertionIndex(hit, textArea.textProperty().getValueSafe());
//                 int i = skin.getInsertionPoint(e.getX(), e.getY());
                final int anchor = textArea.getAnchor();
                final int caretPosition = textArea.getCaretPosition();
                if (e.getClickCount() < 2 &&
                    (e.isSynthesized() ||
                     (anchor != caretPosition &&
                      ((i > anchor && i < caretPosition) || (i < anchor && i > caretPosition))))) {
                    // if there is a selection, then we will NOT handle the
                    // press now, but will defer until the release. If you
                    // select some text and then press down, we change the
                    // caret and wait to allow you to drag the text (TODO).
                    // When the drag concludes, then we handle the click

                    deferClick = true;
                    // TODO start a timer such that after some millis we
                    // switch into text dragging mode, change the cursor
                    // to indicate the text can be dragged, etc.
                } else if (!(e.isControlDown() || e.isAltDown() || e.isShiftDown() || e.isMetaDown() || e.isShortcutDown())) {
                    switch (e.getClickCount()) {
                        case 1: skin.positionCaret(hit, false, false); break;
                        case 2: mouseDoubleClick(hit); break;
                        case 3: mouseTripleClick(hit); break;
                        default: // no-op
                    }
                } else if (e.isShiftDown() && !(e.isControlDown() || e.isAltDown() || e.isMetaDown() || e.isShortcutDown()) && e.getClickCount() == 1) {
                    // didn't click inside the selection, so select
                    shiftDown = true;
                    // if we are on mac os, then we will accumulate the
                    // selection instead of just moving the dot. This happens
                    // by figuring out past which (dot/mark) are extending the
                    // selection, and set the mark to be the other side and
                    // the dot to be the new position.
                    // everywhere else we just move the dot.
                    if (isMac()) {
                        textArea.extendSelection(i);
                    } else {
                        skin.positionCaret(hit, true, false);
                    }
                }
//                 skin.setForwardBias(hit.isLeading());
//                if (textInputControl.editable)
//                    displaySoftwareKeyboard(true);
            }
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e) {
        final TextArea textArea = getControl();
        // we never respond to events if disabled, but we do notify any onXXX
        // event listeners on the control
        if (!textArea.isDisabled() && !e.isSynthesized()) {
            if (e.getButton() == MouseButton.PRIMARY &&
                    !(e.isMiddleButtonDown() || e.isSecondaryButtonDown() ||
                            e.isControlDown() || e.isAltDown() || e.isShiftDown() || e.isMetaDown())) {
                skin.positionCaret(skin.getIndex(e.getX(), e.getY()), true, false);
            }
        }
        deferClick = false;
    }

    @Override public void mouseReleased(final MouseEvent e) {
        final TextArea textArea = getControl();
        super.mouseReleased(e);
        // we never respond to events if disabled, but we do notify any onXXX
        // event listeners on the control
        if (!textArea.isDisabled()) {
            setCaretAnimating(false);
            if (deferClick) {
                deferClick = false;
                skin.positionCaret(skin.getIndex(e.getX(), e.getY()), shiftDown, false);
                shiftDown = false;
            }
            setCaretAnimating(true);
        }
    }

    @Override public void contextMenuRequested(ContextMenuEvent e) {
        final TextArea textArea = getControl();

        if (contextMenu.isShowing()) {
            contextMenu.hide();
        } else if (textArea.getContextMenu() == null) {
            double screenX = e.getScreenX();
            double screenY = e.getScreenY();
            double sceneX = e.getSceneX();

            if (IS_TOUCH_SUPPORTED) {
                Point2D menuPos;
                if (textArea.getSelection().getLength() == 0) {
                    skin.positionCaret(skin.getIndex(e.getX(), e.getY()), false, false);
                    menuPos = skin.getMenuPosition();
                } else {
                    menuPos = skin.getMenuPosition();
                    if (menuPos != null && (menuPos.getX() <= 0 || menuPos.getY() <= 0)) {
                        skin.positionCaret(skin.getIndex(e.getX(), e.getY()), false, false);
                        menuPos = skin.getMenuPosition();
                    }
                }

                if (menuPos != null) {
                    Point2D p = getControl().localToScene(menuPos);
                    Scene scene = getControl().getScene();
                    Window window = scene.getWindow();
                    Point2D location = new Point2D(window.getX() + scene.getX() + p.getX(),
                                                   window.getY() + scene.getY() + p.getY());
                    screenX = location.getX();
                    sceneX = p.getX();
                    screenY = location.getY();
                }
            }

            skin.populateContextMenu(contextMenu);
            double menuWidth = contextMenu.prefWidth(-1);
            double menuX = screenX - (IS_TOUCH_SUPPORTED ? (menuWidth / 2) : 0);
            Screen currentScreen = com.sun.javafx.util.Utils.getScreenForPoint(screenX, 0);
            Rectangle2D bounds = currentScreen.getBounds();

            if (menuX < bounds.getMinX()) {
                getControl().getProperties().put("CONTEXT_MENU_SCREEN_X", screenX);
                getControl().getProperties().put("CONTEXT_MENU_SCENE_X", sceneX);
                contextMenu.show(getControl(), bounds.getMinX(), screenY);
            } else if (screenX + menuWidth > bounds.getMaxX()) {
                double leftOver = menuWidth - ( bounds.getMaxX() - screenX);
                getControl().getProperties().put("CONTEXT_MENU_SCREEN_X", screenX);
                getControl().getProperties().put("CONTEXT_MENU_SCENE_X", sceneX);
                contextMenu.show(getControl(), screenX - leftOver, screenY);
            } else {
                getControl().getProperties().put("CONTEXT_MENU_SCREEN_X", 0);
                getControl().getProperties().put("CONTEXT_MENU_SCENE_X", 0);
                contextMenu.show(getControl(), menuX, screenY);
            }
        }

        e.consume();
    }

    @Override protected void setCaretAnimating(boolean play) {
        skin.setCaretAnimating(play);
    }

    protected void mouseDoubleClick(HitInfo hit) {
        final TextArea textArea = getControl();
        textArea.previousWord();
        if (isWindows()) {
            textArea.selectNextWord();
        } else {
            textArea.selectEndOfNextWord();
        }
    }

    protected void mouseTripleClick(HitInfo hit) {
        // select the line
        skin.paragraphStart(false, false);
        skin.paragraphEnd(false, isWindows(), true);
    }

    //    public function mouseWheelMove(e:MouseEvent):Void {
//        def textBox = bind skin.control as TextBox;
//        // we never respond to events if disabled, but we do notify any onXXX
//        // event listeners on the control
//        if (not textBox.disabled) {
//            var rot = Math.abs(e.wheelRotation);
//            while (rot > 0) {
//                rot--;
//                scrollText(e.wheelRotation > 0);
//            }
//        }
//    }

}
