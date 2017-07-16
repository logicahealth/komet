/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.isaac.komet.gui.fx;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;

/**
 * Helper class encapsulating various Java FX helper utilities.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class FxUtils {

   // TODO should this be replaced with fx css? 
    public static final DropShadow RED_DROP_SHADOW = new DropShadow();
    public static final DropShadow GREEN_DROP_SHADOW = new DropShadow();
    public static final DropShadow LIGHT_GREEN_DROP_SHADOW = new DropShadow();
    static
    {
        RED_DROP_SHADOW.setColor(Color.RED);
        GREEN_DROP_SHADOW.setColor(Color.GREEN);
        LIGHT_GREEN_DROP_SHADOW.setColor(Color.LIGHTGREEN);
    }
    
    // Do not instantiate.
    private FxUtils() {};

    /**
     * Makes sure thread is NOT the FX application thread.
     */
    public static void checkBackgroundThread() {
        // Throw exception if on FX user thread
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on background thread; currentThread = "
                    + Thread.currentThread().getName());
        }
    }

    /**
     * Call this after adding all label content to a column, to prevent it from shrinking smaller than the labels
     * returns the calculated width, for convenience
    * @param gp
    * @param colNumber
    * @return 
     */
    public static double preventColCollapse(GridPane gp, int colNumber)
    {
        double largestWidth = 0;
        FontLoader fl = Toolkit.getToolkit().getFontLoader();
        for (Node node : gp.getChildrenUnmodifiable())
        {
            Integer colIndex = GridPane.getColumnIndex(node);
            if (colIndex != null && colIndex == colNumber)
            {
                String textValue = "";
                double extraWidth = 0;
                Font font = null;
                if (node instanceof Label)
                {
                    textValue = ((Label) node).getText();
                    font = ((Label) node).getFont();
                    extraWidth = ((Label)node).getInsets().getLeft() + ((Label)node).getInsets().getRight(); 
                }
                else if (node instanceof CheckBox)
                {
                    textValue = ((CheckBox) node).getText();
                    font = ((CheckBox) node).getFont();
                    extraWidth = 25;
                }
                else if (node instanceof RadioButton)
                {
                    textValue = ((RadioButton) node).getText();
                    font = ((RadioButton) node).getFont();
                    extraWidth = 25;
                }
                if (font != null)
                {
                    double width = fl.computeStringWidth(textValue, font) + extraWidth;
                    if (width > largestWidth)
                    {
                        largestWidth = width;
                    }
                }
            }
        }
        // don't let the column shrink less than the labels

        if (gp.getColumnConstraints().isEmpty())
        {
            if (colNumber > 0)
            {
                throw new RuntimeException("Sorry, not handled");
            }
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(largestWidth);
            cc.setPrefWidth(largestWidth);
            gp.getColumnConstraints().add(cc);
        }
        else
        {
            ColumnConstraints cc = gp.getColumnConstraints().get(colNumber);
            cc.setMinWidth(largestWidth);
            cc.setPrefWidth(largestWidth);
        }
        return largestWidth;
    }
    
    public static void expandAll(TreeItem<?> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<?> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }

    public static void expandParents(TreeItem<?> ti)
    {
        TreeItem<?> parent = ti.getParent();
        if (parent != null)
        {
            ti.getParent().setExpanded(true);
            expandParents(parent);
        }
    }
    
    public static double calculateNecessaryWidthOfBoldLabel(Label l)
    {
        Font f = new Font("System Bold", 13.0);
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(l.getText(), f);
    }
    
    public static double calculateNecessaryWidthOfLabel(Label l)
    {
        Font f = new Font("System", 13.0);
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(l.getText(), f);
    }
    
	public static void assignImageToButton(ButtonBase button, ImageView imageView) {
		FxUtils.assignImageToButton(button, imageView, "");
	}

	public static void assignImageToButton(ButtonBase button, ImageView imageView, String tooltip) {
        button.setText("");
        button.setGraphic(imageView);
        button.setTooltip(new Tooltip(tooltip));
	}

}