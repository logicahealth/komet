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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.model.observable.version.*;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.control.property.ViewProperties;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.observable.ObservableSemanticChronologyImpl;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class AddAttachmentMenuItems {

    private static final Logger LOG = LogManager.getLogger();

    final List<MenuItem> menuItems = new ArrayList<>();
    final ManifoldCoordinate manifoldCoordinate;
    final ObservableCategorizedVersion categorizedVersion;
    final BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer;
    final HashMap<String, PropertySheetMenuItem> propertySheetMenuItems = new HashMap<>();

    public AddAttachmentMenuItems(ManifoldCoordinate manifoldCoordinate, ObservableCategorizedVersion categorizedVersion,
                                  BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.categorizedVersion = categorizedVersion;
        this.newAttachmentConsumer = newAttachmentConsumer;
    }

    public void sortMenuItems() {
        // TODO
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public VersionType getVersionType() {
        return this.categorizedVersion.getSemanticType();
    }

    public ConceptSpecification getAssemblageSpec() {
        return Get.conceptSpecification(this.categorizedVersion.getAssemblageNid());
    }

    public PropertySheetMenuItem makePropertySheetMenuItem(String menuText, ConceptSpecification assemblageSpecification) {
        if (propertySheetMenuItems.containsKey(menuText)) {
            return propertySheetMenuItems.get(menuText);
        }
        PropertySheetMenuItem propertySheetMenuItem = new PropertySheetMenuItem(manifoldCoordinate, categorizedVersion);
        propertySheetMenuItems.put(menuText, propertySheetMenuItem);
        MenuItem menuItem = new MenuItemWithText(menuText);
        menuItem.setOnAction((event) -> {
            try {
                ObservableVersion newVersion = makeNewVersion(assemblageSpecification);

                propertySheetMenuItem.setVersionInFlight(newVersion);

                propertySheetMenuItem.prepareToExecute();
                newAttachmentConsumer.accept(propertySheetMenuItem, assemblageSpecification);
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        });
        menuItems.add(menuItem);
        return propertySheetMenuItem;
    }

    protected ObservableVersion makeNewVersion(ConceptSpecification assemblageSpecification) throws NoSuchElementException, InterruptedException, IllegalStateException, ExecutionException {
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageSpecification, manifoldCoordinate.getVertexStampFilter());
        if (optionalSemanticConceptNid.isPresent()) {
            int semanticTypeNid = optionalSemanticConceptNid.getAsInt();
            if (semanticTypeNid == MetaData.CONCEPT_SEMANTIC____SOLOR.getNid()
                    || semanticTypeNid == MetaData.COMPONENT_SEMANTIC____SOLOR.getNid()) {

                ObservableComponentNidVersionImpl version
                        = new ObservableComponentNidVersionImpl(Get.newUuidWithAssignment(),
                                this.categorizedVersion.getPrimordialUuid(),
                                assemblageSpecification.getNid());
                version.setComponentNid(TermAux.UNINITIALIZED_COMPONENT_ID.getNid());
                setupWithChronicle(version);
                
                return version;
            } else if (semanticTypeNid == MetaData.INTEGER_SEMANTIC____SOLOR.getNid()) {

                ObservableLongVersionImpl version = new ObservableLongVersionImpl(Get.newUuidWithAssignment(),
                        this.categorizedVersion.getPrimordialUuid(),
                        assemblageSpecification.getNid());
                version.setLongValue(-1);
                setupWithChronicle(version);
            } else if (semanticTypeNid == MetaData.MEMBERSHIP_SEMANTIC____SOLOR.getNid()) {
                ObservableSemanticVersionImpl version = new ObservableSemanticVersionImpl(Get.newUuidWithAssignment(),
                        this.categorizedVersion.getPrimordialUuid(),
                        assemblageSpecification.getNid());
                setupWithChronicle(version);
                return version;
            } else if (semanticTypeNid == MetaData.STRING_SEMANTIC____SOLOR.getNid()) {
                ObservableStringVersionImpl version = new ObservableStringVersionImpl(Get.newUuidWithAssignment(),
                        this.categorizedVersion.getPrimordialUuid(),
                        assemblageSpecification.getNid());
                version.setString("");
                setupWithChronicle(version);
                return version;
            } else if (semanticTypeNid == MetaData.IMAGE_SEMANTIC____SOLOR.getNid()) {
                ObservableImageVersionImpl version = new ObservableImageVersionImpl(Get.newUuidWithAssignment(),
                        this.categorizedVersion.getPrimordialUuid(),
                        assemblageSpecification.getNid());
                version.setImageData(new byte[0]);
                setupWithChronicle(version);
                return version;
            } else {
                throw new UnsupportedOperationException("Can't handle: " + Get.conceptDescriptionText(semanticTypeNid));
            }
        }

        LOG.warn("No semantic type defined for assemblage: " + Get.conceptDescriptionText(assemblageSpecification.getNid()));

        ObservableStringVersionImpl version = new ObservableStringVersionImpl(Get.newUuidWithAssignment(),
                this.categorizedVersion.getPrimordialUuid(),
                assemblageSpecification.getNid());
        version.setString("");
                setupWithChronicle(version);
        return version;
    }

    protected void setupWithChronicle(ObservableVersionImpl version) throws NoSuchElementException {
        version.setStatus(Status.ACTIVE);
        version.setAuthorNid(FxGet.editCoordinate().getAuthorNid());
        version.setModuleNid(FxGet.editCoordinate().getModuleNid());
        version.setPathNid(FxGet.editCoordinate().getPathNid());
        version.setChronology(new ObservableSemanticChronologyImpl((SemanticChronology) version.createIndependentChronicle()));
    }

}
