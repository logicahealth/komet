package sh.komet.gui.search;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToolBar;
import sh.komet.gui.util.FxGet;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class SearchToolbar {

    SearchBarController searchBarController;
    ToolBar searchToolbar;

    public SearchToolbar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SearchBar.fxml"));
            this.searchToolbar = loader.load();
            this.searchBarController = loader.getController();
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    public ToolBar getSearchToolbar() {
        return searchToolbar;
    }

    public void setResults(Collection<Object> resultCollection) {
        searchBarController.setResults(resultCollection);
    }

    public void addResult(Object match) {
        searchBarController.addResult(match);
    }

    public void setSearchConsumer(Consumer<String> searchConsumer) {
        searchBarController.setSearchConsumer(searchConsumer);
    }

    public Object getSelectedObject() {
        return searchBarController.getSelectedObject();
    }

    public SimpleObjectProperty<Object> selectedObjectProperty() {
        return searchBarController.selectedObjectProperty();
    }

    public void setProgress(double value) {
        this.searchBarController.setProgress(value);
    }
}
