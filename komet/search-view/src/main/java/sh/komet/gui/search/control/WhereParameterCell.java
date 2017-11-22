package sh.komet.gui.search.control;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.GridPane;
import sh.isaac.api.query.clauses.DescriptionActiveLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionActiveRegexMatch;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionRegexMatch;
import sh.komet.gui.search.QueryClause;

public class WhereParameterCell extends TreeTableCell<QueryClause, Object>{

    private enum ParamType{
        DEFAULT, TEXT, SINGLESPEC, DOUBLESPEC
    }

    private Node controlToDisplay;
    private ParamType paramType;

    public WhereParameterCell(TreeTableColumn<QueryClause, String> clauseColumn) {
//        gridPane.add(new Label("Assemblage?"), 0, 0);
//        gridPane.add(new Label("Concept?"), 0,1);
//        gridPane.add(new Label(), 1,0);
//        gridPane.add(new Label(), 1,1);
//        controlToDisplay = gridPane;
        this.controlToDisplay = new Label();

    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if(this.getTreeTableRow().getTreeItem() != null){
            Class clauseClass = this.getTreeTableRow().getTreeItem().getValue().getClause().getClass();
            if(clauseClass.equals(DescriptionActiveLuceneMatch.class)
                    || clauseClass.equals(DescriptionActiveRegexMatch.class)
                    || clauseClass.equals(DescriptionLuceneMatch.class)
                    || clauseClass.equals(DescriptionRegexMatch.class)
                    ){
                this.paramType = ParamType.TEXT;
                this.controlToDisplay = new TextField();
            }else{
                this.controlToDisplay = new Label();
                this.paramType = ParamType.DEFAULT;
            }

        }

        this.setGraphic(this.controlToDisplay);
    }
}
