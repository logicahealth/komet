/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.dbConfigBuilder.fx.fxUtil;

import java.util.function.Function;
import org.codehaus.plexus.util.StringUtils;
import javafx.beans.value.ObservableStringValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * {@link ErrorMarkerUtils}
 *
 * Convenience methods to wrap a control (textfield, combobox, etc) in a stack pane
 * that has an error/info marker icon, and a tooltip that explains the reason why the marker is there.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ErrorMarkerUtils
{
	
	/**
	 * Setup an 'EXCLAMATION' error marker on the component. Automatically displays anytime that the reasonWhyControlInvalid value
	 * is false. Hides when the isControlCurrentlyValid is true.
	 * @param initialNode the node to be setup
	 * @param notValidFunction a function which gets passed the text of the initialNode whenever it changes.  If the text is valid for the field, 
	 * return an empty string, or null.  If not valid, return the reason why it isn't valid.
	 * @param swapInContainer if true, and the initialNode is contained in a GridPane, HBox or VBox, it will automatically swap the node in place.
	 * @return a stackPane with the initial node inside it. 
	 */
	public static ValidBooleanBinding setupErrorMarker(final TextInputControl initialNode, Function<String, String> notValidFunction, boolean swapInContainer)
	{
		final ValidBooleanBinding validBooleanBinding = new ValidBooleanBinding()
		{
			{
				bind(initialNode.textProperty());
				setComputeOnInvalidate(true);
			}

			@Override
			protected boolean computeValue()
			{
				String notValidReason = notValidFunction.apply(initialNode.getText());
				if (StringUtils.isNotBlank(notValidReason))
				{
					setInvalidReason(notValidReason);
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		
		setupErrorMarker(initialNode, validBooleanBinding, swapInContainer);
		return validBooleanBinding;
		
	}
	/**
	 * Setup an 'EXCLAMATION' error marker on the component. Automatically displays anytime that the reasonWhyControlInvalid value
	 * is false. Hides when the isControlCurrentlyValid is true.
	 * @param initialNode the node to be setup
	 * @param isNodeCurrentlyValid  logic for valid or not
	 * @param swapInContainer if true, and the initialNode is contained in a GridPane, HBox or VBox, it will automatically swap the node in place.
	 * @return a stackPane with the initial node inside it. 
	 */
	public static StackPane setupErrorMarker(Node initialNode, ValidBooleanBinding isNodeCurrentlyValid, boolean swapInContainer)
	{
		ImageView exclamation = Images.EXCLAMATION.createImageView();
		
		StackPane stackPane = new StackPane();
		
		if (swapInContainer)
		{
			Node parent = initialNode.getParent();
			while(true)
			{
				if (parent == null)
				{
					throw new RuntimeException("Couldn't find a supported parent container to swap " + initialNode);
				}
				else if (parent instanceof GridPane)
				{
					swapGridPaneComponents(initialNode, stackPane, (GridPane)parent);
					break;
				}
				else if (parent instanceof HBox)
				{
					swapHBoxComponents(initialNode, stackPane, (HBox)parent);
					break;
				}
				else if (parent instanceof VBox)
				{
					swapVBoxComponents(initialNode, stackPane, (VBox)parent);
					break;
				}
				parent = parent.getParent();
			}
		}

		exclamation.visibleProperty().bind(isNodeCurrentlyValid.not());
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(isNodeCurrentlyValid.getReasonWhyInvalid());
		Tooltip.install(exclamation, tooltip);
		tooltip.setAutoHide(true);
		
		exclamation.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				tooltip.show(exclamation, event.getScreenX(), event.getScreenY());
				
			}
			
		});

		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.getChildren().add(initialNode);
		StackPane.setAlignment(initialNode, Pos.CENTER_LEFT);
		stackPane.getChildren().add(exclamation);
		StackPane.setAlignment(exclamation, Pos.CENTER_RIGHT);
		double insetFromRight;
		if (initialNode instanceof ComboBox)
		{
			insetFromRight = 30.0;
		}
		else if (initialNode instanceof ChoiceBox)
		{
			insetFromRight = 25.0;
		}
		else
		{
			insetFromRight = 5.0;
		}
		StackPane.setMargin(exclamation, new Insets(0.0, insetFromRight, 0.0, 0.0));
		return stackPane;
	}

	/**
	 * Setup an 'INFORMATION' info marker on the component. Automatically displays anytime that the initialControl is disabled.
	 * Put the initial control in the provided stack pane
	 * @param initialControl the node to be setup
	 * @param reasonWhyControlDisabled  the explanation as to if/why the node is disabled
	 * @param swapInContainer if true, and the initialNode is contained in a GridPane, HBox or VBox, it will automatically swap the node in place.
	 * @return a stack pane that contains the initialControl
	 */
	public static StackPane setupDisabledInfoMarker(Control initialControl, ObservableStringValue reasonWhyControlDisabled, boolean swapInContainer)
	{
		ImageView information = Images.INFORMATION.createImageView();

		information.visibleProperty().bind(initialControl.disabledProperty());
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(reasonWhyControlDisabled);
		Tooltip.install(information, tooltip);
		tooltip.setAutoHide(true);
		
		information.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				tooltip.show(information, event.getScreenX(), event.getScreenY());
				
			}
			
		});
		
		StackPane stackPane = new StackPane();
		
		if (swapInContainer)
		{
			Node parent = initialControl.getParent();
			while(true)
			{
				if (parent == null)
				{
					throw new RuntimeException("Couldn't find a supported parent container to swap " + initialControl);
				}
				else if (parent instanceof GridPane)
				{
					swapGridPaneComponents(initialControl, stackPane, (GridPane)parent);
					break;
				}
				else if (parent instanceof HBox)
				{
					swapHBoxComponents(initialControl, stackPane, (HBox)parent);
					break;
				}
				else if (parent instanceof VBox)
				{
					swapVBoxComponents(initialControl, stackPane, (VBox)parent);
					break;
				}
				parent = parent.getParent();
			}
		}

		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.getChildren().add(initialControl);
		StackPane.setAlignment(initialControl, Pos.CENTER_LEFT);
		stackPane.getChildren().add(information);
		if (initialControl instanceof Button)
		{
			StackPane.setAlignment(information, Pos.CENTER);
		}
		else if (initialControl instanceof CheckBox)
		{
			StackPane.setAlignment(information, Pos.CENTER_LEFT);
			StackPane.setMargin(information, new Insets(0, 0, 0, 1));
		}
		else
		{
			StackPane.setAlignment(information, Pos.CENTER_RIGHT);
			double insetFromRight = (initialControl instanceof ComboBox ? 30.0 : 5.0);
			StackPane.setMargin(information, new Insets(0.0, insetFromRight, 0.0, 0.0));
		}
		return stackPane;
	}

	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * GridPane properties, but this implementation copies all properties, which may cause unintended side effects
	 * @param placedNode the node already placed by the fxml file
	 * @param replacementNode the node to replace it with
	 * @param gp the grid pane that contains the placedNode
	 * 
	 * @return the replacementNode
	 */
	private static Node swapGridPaneComponents(Node placedNode, Node replacementNode, GridPane gp)
	{
		int index = gp.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the grid pane");
		}

		gp.getChildren().remove(index);
		gp.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}

	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * VBox properties, but this implementation copies all properties, which may cause unintended side effects
	 * @param placedNode  The node that was placed by the fxml file
	 * @param replacementNode the node to put in its place
	 * @param vb the vbox that contains the placedNode
	 * @return replacementNode
	 */
	private static StackPane swapVBoxComponents(Node placedNode, StackPane replacementNode, VBox vb)
	{
		int index = vb.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the vbox");
		}

		vb.getChildren().remove(index);
		vb.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}
	
	/**
	 * Useful when taking a node already placed by a fxml file, for example, and wrapping it
	 * in a stack pane
	 * WARNING - the mechanism of moving the properties isn't currently very smart - it should only target
	 * HBox properties, but this implementation copies all properties, which may cause unintended side effects 
	 * on untested node types
	 * @param placedNode The node that was placed by the fxml file
	 * @param replacementNode the node to put in its place
	 * @param hb optional - the hbox that the placeNode is inside of - if not provided, recursively gets the parent of the placedNode until it finds an HBox
	 * @return replacementNode 
	 */
	private static StackPane swapHBoxComponents(Node placedNode, StackPane replacementNode, HBox hb)
	{
		int index = hb.getChildren().indexOf(placedNode);
		if (index < 0)
		{
			throw new RuntimeException("Placed Node is not in the hbox");
		}

		hb.getChildren().remove(index);
		hb.getChildren().add(index, replacementNode);

		//this transfers the node specific constraints
		replacementNode.getProperties().putAll(placedNode.getProperties());
		return replacementNode;
	}
}
