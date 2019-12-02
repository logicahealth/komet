/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.gui.control.concept;

//~--- JDK imports ------------------------------------------------------------

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.WindowEvent;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.docbook.DocBook;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.drag.drop.DragAndDropHelper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ManifoldLinkedConceptLabel
        extends Label implements PropertyEditor<Object> {

    public static final String EMPTY_TEXT = "empty";
     SimpleObjectProperty<Manifold> manifoldProperty;
    SimpleIntegerProperty selectionIndexProperty;
    Consumer<ManifoldLinkedConceptLabel> descriptionTextUpdater;
    final Supplier<List<MenuItem>> menuSupplier;
    final DragAndDropHelper dragAndDropHelper;

    //~--- constructors --------------------------------------------------------
    public ManifoldLinkedConceptLabel(SimpleObjectProperty<Manifold> manifoldProperty,
                                      SimpleIntegerProperty selectionIndexProperty,
            Consumer<ManifoldLinkedConceptLabel> descriptionTextUpdater,
            Supplier<List<MenuItem>> menuSupplier) {
        super(EMPTY_TEXT);
        if (menuSupplier == null) {
            throw new IllegalStateException("Supplier<List<MenuItem>> menuSupplier cannot be null");
        }
        this.manifoldProperty = manifoldProperty;
        this.selectionIndexProperty = selectionIndexProperty;
        this.descriptionTextUpdater = descriptionTextUpdater;
        this.menuSupplier = menuSupplier;
        this.manifoldProperty.addListener((observable, oldValue, newValue) -> {
            oldValue.manifoldSelectionProperty().removeListener(this::selectionListChanged);
            newValue.manifoldSelectionProperty().addListener(this::selectionListChanged);
        });
        this.manifoldProperty.get().manifoldSelectionProperty().addListener(this::selectionListChanged);
        if (this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get()).isPresent()) {
           this.descriptionTextUpdater.accept(this);
        }
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.dragAndDropHelper = new DragAndDropHelper(this, () -> {
            if (manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get()).isPresent()) {
                return manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get()).get();
            }
            return null;

        } , this::setValue, mouseEvent -> true,
                dragEvent -> true);


        this.setMinWidth(100);

        ContextMenu contextMenu = new ContextMenu();

        for (String manifoldGroupName : Manifold.getGroupNames()) {
            MenuItem item = new MenuItemWithText(manifoldGroupName + " history");
            contextMenu.getItems()
                    .add(item);
        }

        this.setContextMenu(contextMenu);
        contextMenu.setOnShowing(this::handle);
    }

    //~--- methods -------------------------------------------------------------
    private void selectionListChanged(ListChangeListener.Change<? extends ComponentProxy> c) {
        this.descriptionTextUpdater.accept(this);
    }

    private MenuItem makeCopyMenuItem() {
        Menu copyMenu = new Menu("copy");

        MenuItem conceptLoincCodeMenuItem = new MenuItemWithText("Concept LOINC code");
        copyMenu.getItems().add(conceptLoincCodeMenuItem);
        conceptLoincCodeMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(selectionIndexProperty.get());
            if (concept.isPresent()) {
                Optional<String> optionalLoincCode = Get.identifierService().getIdentifierFromAuthority(concept.get().getNid(), MetaData.LOINC_ID_ASSEMBLAGE____SOLOR, manifoldProperty.get());

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                if (optionalLoincCode.isPresent()) {
                    content.putString(optionalLoincCode.get());
                } else {
                    content.putString("Not found");
                }
                clipboard.setContent(content);
            }
        });

        MenuItem conceptSnomedCodeItem = new MenuItemWithText("Concept SNOMED code");
        copyMenu.getItems().add(conceptSnomedCodeItem);
        conceptSnomedCodeItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                Optional<String> optionalSnomedCode = Get.identifierService().getIdentifierFromAuthority(concept.get().getNid(), MetaData.SCTID____SOLOR, manifoldProperty.get());

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                if (optionalSnomedCode.isPresent()) {
                    content.putString(optionalSnomedCode.get());
                } else {
                    content.putString("Not found");
                }
                clipboard.setContent(content);
            }
        });


        MenuItem conceptFQNMenuItem = new MenuItemWithText("Concept Fully Qualified Name");
        copyMenu.getItems().add(conceptFQNMenuItem);
        conceptFQNMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                String fqnString = concept.get().getFullyQualifiedName();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(fqnString);
                clipboard.setContent(content);
            }
        });


        MenuItem conceptUuidMenuItem = new MenuItemWithText("Concept UUID");
        copyMenu.getItems().add(conceptUuidMenuItem);
        conceptUuidMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                String uuidStr = concept.get().getPrimordialUuid().toString();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(uuidStr);
                clipboard.setContent(content);
            }
        });


        MenuItem docBookInlineReferenceMenuItem = new MenuItemWithText("Docbook inline concept reference");
        copyMenu.getItems().add(docBookInlineReferenceMenuItem);
        docBookInlineReferenceMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                String docbookXml = DocBook.getInlineEntry(concept.get(), manifoldProperty.get());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });

        MenuItem copyDocBookMenuItem = new MenuItemWithText("Docbook glossary entry");
        copyMenu.getItems().add(copyDocBookMenuItem);
        copyDocBookMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                String docbookXml = DocBook.getGlossentry(concept.get(), manifoldProperty.get());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });
        MenuItem copyJavaShortSpecMenuItem = new MenuItemWithText("Java concept specification");
        copyMenu.getItems().add(copyJavaShortSpecMenuItem);
        copyJavaShortSpecMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                ConceptSpecification conceptSpec = concept.get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString("new ConceptProxy(\"" +
                        conceptSpec.getFullyQualifiedName() +
                        "\", UUID.fromString(\"" +
                        conceptSpec.getPrimordialUuid().toString() +
                        "\"))");
                clipboard.setContent(content);
            }
        });
        MenuItem copyJavaSpecMenuItem = new MenuItemWithText("Java qualified concept specification");
        copyMenu.getItems().add(copyJavaSpecMenuItem);
        copyJavaSpecMenuItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                ConceptSpecification conceptSpec = concept.get();

                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString("new sh.isaac.api.ConceptProxy(\"" +
                        conceptSpec.toExternalString() +
                        "\")");
                clipboard.setContent(content);
            }
        });
        
        MenuItem copyConceptDetailedInfoItem = new MenuItemWithText("Copy concept detailed info");
        copyMenu.getItems().add(copyConceptDetailedInfoItem);
        copyConceptDetailedInfoItem.setOnAction((event) -> {
            Optional<ConceptChronology> concept = this.manifoldProperty.get().getOptionalFocusedConcept(this.selectionIndexProperty.get());
            if (concept.isPresent()) {
                ConceptChronology conceptChronicle = Get.concept(concept.get());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(conceptChronicle.toLongString());
                clipboard.setContent(content);
            }
        });

        return copyMenu;

    }

    private void handle(WindowEvent event) {
        ContextMenu contextMenu = (ContextMenu) event.getSource();
        contextMenu.getItems().clear();

        if (this.menuSupplier != null) {
            List<MenuItem> menuItems = this.menuSupplier.get();
            if (!menuItems.isEmpty()) {
                for (MenuItem menu : menuItems) {
                    contextMenu.getItems().add(menu);
                }
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
        }
        contextMenu.getItems()
                .add(makeCopyMenuItem());
        contextMenu.getItems()
                .add(new SeparatorMenuItem());

        Menu manifoldHistoryMenu = new Menu("history");
        contextMenu.getItems().add(manifoldHistoryMenu);
        Collection<ComponentProxy> historyCollection = this.manifoldProperty.get().getHistoryRecords();

        setupHistoryMenuItem(historyCollection, manifoldHistoryMenu);

        for (Manifold.ManifoldGroup group: Manifold.ManifoldGroup.values()) {
            Manifold manifoldForGroup = Manifold.get(group);
            Menu groupHistory = new Menu(group.getGroupName() + " history");
            contextMenu.getItems().add(groupHistory);
            setupHistoryMenuItem(manifoldForGroup.getHistoryRecords(), groupHistory);
        }
    }

    private void setupHistoryMenuItem(Collection<ComponentProxy> historyCollection, Menu manifoldHistoryMenu) {
        for (ComponentProxy historyRecord : historyCollection) {
            MenuItem historyItem = new MenuItemWithText(historyRecord.getComponentString());
            historyItem.setUserData(historyRecord);
            historyItem.setOnAction((ActionEvent actionEvent) -> {
                unlink();
                //MenuItem historyMenuItem = (MenuItem) actionEvent.getSource();
                ComponentProxy itemHistoryRecord = (ComponentProxy) historyItem.getUserData();
                setConceptChronology(Get.concept(itemHistoryRecord.getNid()));
            });
            manifoldHistoryMenu.getItems().add(historyItem);
        }
    }

    private void unlink() {
        if (!this.manifoldProperty.get()
                .getGroupName()
                .equals(ManifoldGroup.UNLINKED.getGroupName())) {
            this.manifoldProperty.get().manifoldSelectionProperty().removeListener(this::selectionListChanged);
            this.manifoldProperty.set(Manifold.get(ManifoldGroup.UNLINKED));
            this.manifoldProperty.get().manifoldSelectionProperty().addListener(this::selectionListChanged);
            this.selectionIndexProperty.set(0);
            FxGet.dialogs().showErrorDialog("Unsupported operation.", "Can't unlink manifold yet...",
                    "Have to implement a shared manifold property to unlink, and deal with change in index...");

        }
    }

    public void setConceptChronology(ConceptChronology conceptChronology) {
        unlink();
        this.manifoldProperty.get().manifoldSelectionProperty().setAll((ComponentProxy) conceptChronology);
    }

    //~--- set methods ---------------------------------------------------------
    private void setDescriptionText(DescriptionVersion latestDescriptionVersion) {
        if (latestDescriptionVersion != null) {
            this.setText(latestDescriptionVersion.getText());
        }
    }

    private void setEmptyText() {
        setEmptyText(this);
    }

    private static void setEmptyText(Label label) {
        label.setText(EMPTY_TEXT);
    }

    public static void setFullySpecifiedText(ManifoldLinkedConceptLabel label) {
        
        if (label.manifoldProperty.get().getOptionalFocusedConcept(label.selectionIndexProperty.get()).isPresent()) {
            ConceptChronology focusedConcept = Get.concept(label.manifoldProperty.get().getOptionalFocusedConcept(label.selectionIndexProperty.get()).get());
            label.manifoldProperty.get().getDescription(focusedConcept.getNid(), new int[] {
                    MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(), 
                    MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid()})
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }

    public static void setPreferredText(ManifoldLinkedConceptLabel label) {
        if (label.manifoldProperty.get().getOptionalFocusedConcept(label.selectionIndexProperty.get()).isPresent()) {
            ConceptChronology focusedConcept = Get.concept(label.manifoldProperty.get().getOptionalFocusedConcept(label.selectionIndexProperty.get()).get());
            label.manifoldProperty.get().getDescription(focusedConcept.getNid(), new int[] {
                  MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid(), 
                  MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid()})
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }

    @Override
    public Node getEditor() {
        return this;
    }

    @Override
    public Integer getValue() {
        Optional<ConceptChronology> optionalConcept = manifoldProperty.get().getOptionalFocusedConcept(selectionIndexProperty.get());
        return optionalConcept.map(IdentifiedObject::getNid).orElse(Integer.MAX_VALUE);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Integer) {
            Integer intValue = (Integer) value;
            if (intValue < 0) {
                this.setConceptChronology(Get.concept((Integer) value));
            }
        } else if (value instanceof ConceptSpecification) {
            ConceptSpecification spec = (ConceptSpecification) value;
            if (spec.getNid() < 0) {
                this.setConceptChronology(Get.concept((ConceptSpecification) value));
            }

        } else {
            throw new UnsupportedOperationException("ConceptLabel can't handle: " + value);
        }

    }

}
