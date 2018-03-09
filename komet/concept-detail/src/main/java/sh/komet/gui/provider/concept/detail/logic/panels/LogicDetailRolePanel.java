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
package sh.komet.gui.provider.concept.detail.logic.panels;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.komet.gui.control.concept.ConceptLabel;
import sh.komet.gui.control.titled.TitledToolbarPane;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class LogicDetailRolePanel extends LogicDetailPanel {
    
    //RoleGroup Icon: MaterialDesignIcon.FORMAT_LIST_BULLETED_TYPE
    //Role icon: 
    
    private final RoleNodeSomeWithNids someRoleNode;
    private SimpleStringProperty restrictionText = new SimpleStringProperty();
    private SimpleStringProperty labelText = new SimpleStringProperty();

    private SimpleStringProperty typeTextProperty = new SimpleStringProperty();
    private ArrayList<ReadOnlyStringProperty> labelPartPropertes = new ArrayList<>();
    private ArrayList<LogicDetailRolePanel> rolesInGroup = new ArrayList<>();

    public LogicDetailRolePanel(RoleNodeSomeWithNids someRoleNode,
                        PremiseType premiseType, LogicalExpression logicalExpression, Manifold manifold) {
        super(premiseType, logicalExpression, manifold);

        this.someRoleNode = someRoleNode;
        

        if (someRoleNode.getTypeConceptNid() == TermAux.ROLE_GROUP.getNid()) {
            VBox setBox = new VBox();
            setBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
            typeTextProperty.set("");
            panel.setContent(setBox);
            panel.setLeftGraphic2(Iconography.ROLE_GROUP.getIconographic());
            panel.setExpanded(false);
            panel.getStyleClass()
                .add(StyleClasses.DEF_ROLE_GROUP.toString());        
           // should be 1 child, an AND node. 
            for (LogicNode andNode : someRoleNode.getChildren()) {
                List<LogicDetailPanel> childNodes = new ArrayList<>();
                // Add restrictions to the title of the role group. 
                for (LogicNode andNodeChild : andNode.getChildren()) {
                    switch (andNodeChild.getNodeSemantic()) {
                        case ROLE_SOME:
                            LogicDetailRolePanel rolePanel = new LogicDetailRolePanel((RoleNodeSomeWithNids) andNodeChild, getPremiseType(), logicalExpression, manifold);
                            VBox.setMargin(rolePanel.getPanelNode(), new Insets(0));
                            setBox.getChildren().add(rolePanel.getPanelNode());
                            labelPartPropertes.add(rolePanel.restrictionText);
                            rolesInGroup.add(rolePanel);
                            break;
                        case CONCEPT:
                            LogicDetailConceptPanel conceptPanel = new LogicDetailConceptPanel((ConceptNodeWithNids) andNodeChild, getPremiseType(), logicalExpression, manifold);
                            VBox.setMargin(conceptPanel.getPanelNode(), new Insets(0));
                            setBox.getChildren().add(conceptPanel.getPanelNode());
                            break;
                        default:
                            throw new IllegalStateException("Can't handle node: " + andNodeChild);
                    }
                }
                childNodes.sort(new LogicDetailPanelComparator());
                for (LogicDetailPanel childPanel : childNodes) {
                    setBox.getChildren().add(childPanel.getPanelNode());
                    if (childPanel instanceof LogicDetailRolePanel) {
                        labelPartPropertes.add(((LogicDetailRolePanel) childPanel).restrictionText);
                    } else if(childPanel instanceof LogicDetailConceptPanel) {
                        labelPartPropertes.add(((LogicDetailConceptPanel) childPanel).getConceptText());
                    } else {
                        throw new UnsupportedOperationException("Can't handle: " + childPanel.getClass().getName());
                    }
                }
                
            }

        } else {
            HBox panelBox = new HBox();
            panelBox.paddingProperty().set(new Insets(0, 0, 0, leftInset));
            panel.setContent(panelBox);
            panel.setLeftGraphic2(new Text("∃"));
            panel.setExpanded(false);
            ConceptLabel conceptLabel = new ConceptLabel(manifold,
                    ConceptLabel::setPreferredText, (label) -> new ArrayList<>());
            conceptLabel.setConceptChronology(Get.concept(someRoleNode.getTypeConceptNid()));
            panelBox.getChildren().add(conceptLabel);
            for (LogicNode childNode : someRoleNode.getChildren()) {
                // concept or feature. 
                switch (childNode.getNodeSemantic()) {
                    case AND:
                        // a post coordinated expression. 
                        throw new UnsupportedOperationException();
                    case CONCEPT:
                        LogicDetailConceptPanel conceptPanel = new LogicDetailConceptPanel((ConceptNodeWithNids) childNode, getPremiseType(), logicalExpression, manifold);
                        HBox.setMargin(conceptPanel.getPanelNode(), new Insets(0));
                        panelBox.getChildren().add(conceptPanel.getPanelNode());
                        labelPartPropertes.add(conceptPanel.getConceptText());
                        break;
                }
            }
            panel.getStyleClass()
                .add(StyleClasses.DEF_ROLE.toString());        
        }
        for (ReadOnlyStringProperty property: labelPartPropertes) {
            property.addListener(this::updateDescription);
        }
        panel.textProperty().bind(labelText);
        updateDescription();
         setPseudoClasses(panel);
       
    }

    private void updateDescription(ObservableValue<? extends String> observable,
            String oldSpec,
            String newSpec) {
        updateDescription();

    }

    private void updateDescription() {
        StringBuilder builder = new StringBuilder();
        if (someRoleNode.getTypeConceptNid() == TermAux.ROLE_GROUP.getNid()) {
            for (LogicDetailRolePanel rolePanel: rolesInGroup) {
                builder.append("[");
                builder.append(rolePanel.restrictionText.get());
                builder.append("] ");
            }
        } else {
            restrictionText.set(manifold.getPreferredDescriptionText(((ConceptNodeWithNids) someRoleNode.getChildren()[0]).getConceptNid()));
            typeTextProperty.set(manifold.getPreferredDescriptionText(someRoleNode.getTypeConceptNid()));
            builder.append("(");
            builder.append(typeTextProperty.get());
            builder.append(")➞[");
            builder.append(restrictionText.get());
            builder.append("]");
        }
        labelText.set(builder.toString());
    }
    
    public ReadOnlyStringProperty restrictionText() {
        return restrictionText;
    }
    public ReadOnlyStringProperty labelText() {
        return labelText;
    }

    @Override
    public Node getPanelNode() {
        return panel;
    }

    @Override
    String getLabelText() {
        return labelText.get();
    }

}
