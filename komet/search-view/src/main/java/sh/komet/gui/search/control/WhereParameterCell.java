package sh.komet.gui.search.control;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.GridPane;
import sh.isaac.api.query.clauses.*;
import sh.komet.gui.search.QueryClause;

public class WhereParameterCell extends TreeTableCell<QueryClause, Object>{

    private Node controlToDisplay;


    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if(this.getTreeTableRow().getTreeItem() != null) {

                Class clauseClass = this.getTreeTableRow().getTreeItem().getValue().getClause().getClass();

                if (isText(clauseClass)) {

                    if(this.controlToDisplay == null)
                        setupText();
                    else{
                        
                    }


                } else if (isOneSpec(clauseClass)) {

                    if(this.controlToDisplay == null)
                        setupOneSpec();
                    else{

                    }

                } else if (isTwoSpec(clauseClass)) {

                    if(this.controlToDisplay == null)
                        setupTwoSpec();
                    else{

                    }

                }
            }
        }
    }

    private boolean isText(Class c){
        return c.equals(DescriptionActiveLuceneMatch.class)
                || c.equals(DescriptionActiveRegexMatch.class)
                || c.equals(DescriptionLuceneMatch.class)
                || c.equals(DescriptionRegexMatch.class)? true : false;
    }

    private boolean isOneSpec(Class c){
        return c.equals(ConceptIs.class)
                || c.equals(ConceptIsChildOf.class)
                || c.equals(ConceptIsDescendentOf.class)
                || c.equals(ConceptIsKindOf.class)? true : false;
    }

    private boolean isTwoSpec(Class c){
        return c.equals(AssemblageContainsConcept.class)
                || c.equals(AssemblageContainsKindOfConcept.class)
                || c.equals(AssemblageContainsString.class)
                || c.equals(AssemblageLuceneMatch.class)? true : false;
    }

    private void setupText(){
        this.controlToDisplay = new TextField();
        //No Drag and Drop
        this.setGraphic(this.controlToDisplay);
    }

    private void setupOneSpec(){
        this.controlToDisplay = new Label("");
        //Set up Drag and Drop
        this.setGraphic(this.controlToDisplay);
    }

    private void setupTwoSpec(){
        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Concept?"), 0,0);
        gridPane.add(new Label(""), 1,0);
        gridPane.add(new Label("Assemblage?"), 0,1);
        gridPane.add(new Label(""), 1,1);
        this.controlToDisplay = gridPane;
        this.setGraphic(this.controlToDisplay);
    }







}
