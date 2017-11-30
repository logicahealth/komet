package sh.komet.gui.search.control;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.query.clauses.*;
import sh.isaac.komet.gui.treeview.MultiParentTreeCell;
import sh.komet.gui.search.flowr.QueryClause;
import sh.komet.gui.search.flowr.QueryClauseParameter;

import java.util.HashMap;

public class WhereParameterCell extends TreeTableCell<QueryClause, Object>{

    public enum ParamterTypes{
        ASSEMBLAGE, CONCEPT, STRING
    }

    private Node controlForListener;
    private Node additionalControlForListener;

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if(this.getTreeTableRow().getTreeItem() != null) {
                Class clauseClass = getClauseClass();
                if (isText(clauseClass))
                    setupTextField((QueryClauseParameter<String>)item);
                else if (isOneSpec(clauseClass))
                    setupOneSpec((QueryClauseParameter<ConceptChronology>)item);
                else if (isTwoSpec(clauseClass))
                    setupTwoSpec((QueryClauseParameter<HashMap<ParamterTypes, ConceptChronology>>)item);
                else if (isOneSpecOneText(clauseClass))
                    setupOneSpecOneText((QueryClauseParameter<HashMap<ParamterTypes, Object>>)item);

            }
        }
    }

    private Class getClauseClass(){
        return this.getTreeTableRow().getTreeItem().getValue().getClause().getClass();
    }

    private boolean isText(Class c){
        return c.equals(DescriptionActiveLuceneMatch.class)
                || c.equals(DescriptionActiveRegexMatch.class)
                || c.equals(DescriptionLuceneMatch.class)
                || c.equals(DescriptionRegexMatch.class);
    }

    private boolean isOneSpec(Class c){
        return c.equals(ConceptIs.class)
                || c.equals(ConceptIsChildOf.class)
                || c.equals(ConceptIsDescendentOf.class)
                || c.equals(ConceptIsKindOf.class);
    }

    private boolean isTwoSpec(Class c){
        return c.equals(AssemblageContainsConcept.class)
                || c.equals(AssemblageContainsKindOfConcept.class);
    }

    private boolean isOneSpecOneText(Class c){
        return c.equals(AssemblageContainsString.class)
                || c.equals(AssemblageLuceneMatch.class);
    }


    private void setupTextField(QueryClauseParameter<String> queryClauseParameter){
        TextField textField;

        if(queryClauseParameter.isEmpty()) {
            textField = new TextField();
        }else {
            textField = new TextField(queryClauseParameter.getParameter());
        }

        this.controlForListener = textField;
        ((TextField)this.controlForListener).textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    queryClauseParameter.setParameter(newValue);
                });

        this.setGraphic(textField);
    }

    private void setupOneSpec(QueryClauseParameter<ConceptChronology> queryClauseParameter){
        Label label;

        if(queryClauseParameter.isEmpty()){
            label = new Label();
        }else{
            label = new Label(queryClauseParameter.toString());
        }

        this.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        this.setOnDragDropped(event -> {
            ConceptChronology droppedChronology = ((MultiParentTreeCell)event.getGestureSource()).getTreeItem().getValue();
            queryClauseParameter.setParameter(droppedChronology);
            this.setText(queryClauseParameter.toString());
        });

        this.setGraphic(label);
    }

    private void setupTwoSpec(QueryClauseParameter<HashMap<ParamterTypes, ConceptChronology>> queryClauseParameter){
        GridPane gridPane = new GridPane();
        Label assemblageListenLabel = new Label("<Assemblage>  ");
        Label assemblageLabel;
        Label conceptListenLabel = new Label("<Concept>     ");
        Label conceptLabel;

        if(queryClauseParameter.isEmpty()){
            conceptLabel = new Label();
            assemblageLabel = new Label();
        }else{
            if(queryClauseParameter.getParameter().containsKey(ParamterTypes.ASSEMBLAGE))
                assemblageLabel = new Label(queryClauseParameter
                        .getParameter().get(ParamterTypes.ASSEMBLAGE).getFullySpecifiedConceptDescriptionText());
            else
                assemblageLabel = new Label();

            if(queryClauseParameter.getParameter().containsKey(ParamterTypes.CONCEPT))
                conceptLabel = new Label(queryClauseParameter.getParameter()
                        .get(ParamterTypes.CONCEPT).getFullySpecifiedConceptDescriptionText());
            else
                conceptLabel = new Label();
        }

        gridPane.add(assemblageListenLabel, 0,0);
        gridPane.add(assemblageLabel, 1,0);
        gridPane.add(conceptListenLabel, 0,1);
        gridPane.add(conceptLabel, 1,1);


        this.controlForListener = conceptListenLabel;
        this.additionalControlForListener = assemblageListenLabel;

        this.controlForListener.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        this.additionalControlForListener.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        this.controlForListener.setOnDragDropped(event -> {
            HashMap<ParamterTypes, ConceptChronology> hashMap;
            ConceptChronology droppedChronology = ((MultiParentTreeCell)event.getGestureSource()).getTreeItem().getValue();
            conceptLabel.setText(droppedChronology.getFullySpecifiedConceptDescriptionText());

            if(queryClauseParameter.isEmpty())
                hashMap = new HashMap<>();
            else
                hashMap = queryClauseParameter.getParameter();
            hashMap.put(ParamterTypes.CONCEPT, droppedChronology);
            queryClauseParameter.setParameter(hashMap);
        });
        this.additionalControlForListener.setOnDragDropped(event -> {
            HashMap<ParamterTypes, ConceptChronology> hashMap;
            ConceptChronology droppedChronology = ((MultiParentTreeCell)event.getGestureSource()).getTreeItem().getValue();
            assemblageLabel.setText(droppedChronology.getFullySpecifiedConceptDescriptionText());

            if(queryClauseParameter.isEmpty())
                hashMap = new HashMap<>();
            else
                hashMap = queryClauseParameter.getParameter();
            hashMap.put(ParamterTypes.ASSEMBLAGE, droppedChronology);
            queryClauseParameter.setParameter(hashMap);
        });

        this.setGraphic(gridPane);
    }

    private void setupOneSpecOneText(QueryClauseParameter<HashMap<ParamterTypes, Object>> queryClauseParameter){
        GridPane gridPane = new GridPane();
        Label assemblageListenLabel = new Label("<Assemblage>  ");
        Label assemblageLabel;
        Label stringlabel = new Label("<String>    ");
        TextField stringTextField;

        if(queryClauseParameter.isEmpty()){
            assemblageLabel = new Label();
            stringTextField = new TextField();
        }else{
            if(queryClauseParameter.getParameter().containsKey(ParamterTypes.ASSEMBLAGE))
                assemblageLabel = new Label(((ConceptChronology)queryClauseParameter
                        .getParameter().get(ParamterTypes.ASSEMBLAGE)).getFullySpecifiedConceptDescriptionText());
            else
                assemblageLabel = new Label();

            if(queryClauseParameter.getParameter().containsKey(ParamterTypes.STRING))
                stringTextField = new TextField(((String) queryClauseParameter
                        .getParameter().get(ParamterTypes.STRING)).toString());
            else
                stringTextField = new TextField();
        }

        gridPane.add(assemblageListenLabel, 0,0);
        gridPane.add(assemblageLabel, 1,0);
        gridPane.add(stringlabel, 0,1);
        gridPane.add(stringTextField, 1,1);


        this.controlForListener = assemblageListenLabel;
        this.controlForListener.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        this.controlForListener.setOnDragDropped(event -> {
            HashMap<ParamterTypes, Object> hashMap;
            ConceptChronology droppedChronology = ((MultiParentTreeCell)event.getGestureSource()).getTreeItem().getValue();
            assemblageLabel.setText(droppedChronology.getFullySpecifiedConceptDescriptionText());

            if(queryClauseParameter.isEmpty())
                hashMap = new HashMap<>();
            else
                hashMap = queryClauseParameter.getParameter();
            hashMap.put(ParamterTypes.ASSEMBLAGE, droppedChronology);
            queryClauseParameter.setParameter(hashMap);
        });

        this.additionalControlForListener = stringTextField;
        ((TextField)this.additionalControlForListener).textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    HashMap<ParamterTypes, Object> hashMap;
                    if(queryClauseParameter.isEmpty())
                        hashMap = new HashMap<>();
                    else
                        hashMap = queryClauseParameter.getParameter();
                    hashMap.put(ParamterTypes.STRING, newValue);
                    queryClauseParameter.setParameter(hashMap);
                });

        this.setGraphic(gridPane);
    }
}
