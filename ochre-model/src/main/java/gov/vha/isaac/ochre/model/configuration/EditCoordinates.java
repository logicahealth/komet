package gov.vha.isaac.ochre.model.configuration;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;


/**
 * Created by kec on 2/16/15.
 */
public class EditCoordinates {
    
    private static int getNid(ConceptSpecification spec) {
        return Get.identifierService().getNidForUuids(spec.getUuids());
    }

    public static EditCoordinate getDefaultUserSolorOverlay() {

        EditCoordinate editCoordinate = new EditCoordinateImpl(
                getNid(TermAux.USER),
                getNid(TermAux.SOLOR_OVERLAY_MODULE),
                getNid(TermAux.DEVELOPMENT_PATH));

        return editCoordinate;
    }

    public static EditCoordinate getClassifierSolorOverlay() {
        EditCoordinate editCoordinate = new EditCoordinateImpl(
                getNid(TermAux.IHTSDO_CLASSIFIER),
                getNid(TermAux.SOLOR_OVERLAY_MODULE),
                getNid(TermAux.DEVELOPMENT_PATH));

        return editCoordinate;
    }

    public static EditCoordinate getDefaultUserVeteransAdministrationExtension() {

        EditCoordinate editCoordinate = new EditCoordinateImpl(
                getNid(TermAux.USER),
                getNid(TermAux.VHA_MODULE),
                getNid(TermAux.DEVELOPMENT_PATH));

        return editCoordinate;
    }

    public static EditCoordinate getDefaultUserMetadata()  {

        EditCoordinate editCoordinate = new EditCoordinateImpl(
                getNid(TermAux.USER),
                getNid(TermAux.ISAAC_MODULE),
                getNid(TermAux.DEVELOPMENT_PATH));

        return editCoordinate;
    }
}
