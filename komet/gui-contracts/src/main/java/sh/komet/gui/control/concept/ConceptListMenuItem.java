package sh.komet.gui.control.concept;

import javafx.scene.control.MenuItem;
import org.eclipse.collections.api.list.ImmutableList;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.control.property.ViewProperties;

public class ConceptListMenuItem extends MenuItem  {

    private final ViewProperties viewProperties;
    private final ImmutableList<? extends IdentifiedObject> identifiedObjects;

    //~--- constructors --------------------------------------------------------
    public ConceptListMenuItem(ImmutableList<? extends IdentifiedObject> identifiedObjects,
                               ViewProperties viewProperties) {
        super(viewProperties.getDescriptionsAsText(identifiedObjects));
        this.viewProperties = viewProperties;
        this.identifiedObjects = identifiedObjects;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public String toString() {
        return super.getText();
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public ImmutableList<? extends IdentifiedObject> getIdentifiedObjects() {
        return identifiedObjects;
    }
}
