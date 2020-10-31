package sh.komet.gui.control.concept;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.docbook.DocBook;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class MenuSupplierForFocusConcept implements AddToContextMenu {

    private static AddToContextMenu SINGLETON;
    private static AddToContextMenu[] SINGLETON_ARRAY;

    public static AddToContextMenu get() {
        if (SINGLETON == null) {
            SINGLETON = new MenuSupplierForFocusConcept();
        }
        return SINGLETON;
    }

    public static AddToContextMenu[] getArray() {
        if (SINGLETON_ARRAY == null) {
            SINGLETON_ARRAY = new AddToContextMenu[] {get()};
        }
        return SINGLETON_ARRAY;
    }

    private MenuSupplierForFocusConcept() {
    }

    @Override
    public void addToContextMenu(ContextMenu contextMenu, ViewProperties viewProperties,
                                 SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                                 SimpleIntegerProperty selectionIndexProperty,
                                 Runnable unlink) {
        contextMenu.getItems()
                .add(MenuSupplierForFocusConcept.makeCopyMenuItem(Optional.ofNullable(conceptFocusProperty.get()), viewProperties.getManifoldCoordinate()));
        contextMenu.getItems()
                .add(new SeparatorMenuItem());

        HashSet<UUID> hangledViewProperties = new HashSet<>();

        for (ActivityFeed activityFeed: viewProperties.getActivityFeedMap().values()) {
            hangledViewProperties.add(viewProperties.getRootUuid());
            Menu historyMenuForFeed = new Menu(activityFeed.getFeedName());
            contextMenu.getItems().add(historyMenuForFeed);
            MenuSupplierForFocusConcept.setupHistoryMenuItem(viewProperties, activityFeed.feedHistoryProperty(), historyMenuForFeed,
                    conceptFocusProperty, selectionIndexProperty, unlink);
        }
        contextMenu.getItems()
                .add(new SeparatorMenuItem());
        for (ViewProperties propertiesForAnotherView: ViewProperties.getAll()) {
            Menu historyMenuForAnotherView = new Menu(propertiesForAnotherView.getViewName());
            if (!hangledViewProperties.contains(propertiesForAnotherView.getRootUuid())) {
                hangledViewProperties.add(propertiesForAnotherView.getRootUuid());
                Menu historyMenuForOtherView = new Menu(propertiesForAnotherView.getViewName() + " history");
                contextMenu.getItems().add(historyMenuForOtherView);
                for (ActivityFeed activityFeed: propertiesForAnotherView.getActivityFeedMap().values()) {
                    Menu historyMenuForFeed = new Menu(activityFeed.getFeedName());
                    historyMenuForOtherView.getItems().add(historyMenuForFeed);
                    MenuSupplierForFocusConcept.setupHistoryMenuItem(viewProperties, activityFeed.feedHistoryProperty(), historyMenuForFeed,
                            conceptFocusProperty, selectionIndexProperty, unlink);
                }
            }
        }
    }

    private static void setupHistoryMenuItem(ViewProperties viewProperties,
                                             SimpleListProperty<IdentifiedObject> historyCollection,
                                             Menu historyMenu,
                                             SimpleObjectProperty<IdentifiedObject> identifiedObjectFocusProperty,
                                             SimpleIntegerProperty selectionIndexProperty,
                                             Runnable unlink) {
        for (IdentifiedObject historyRecord : historyCollection) {
            Optional<? extends Chronology> optionalObject = Get.identifiedObjectService().getChronology(historyRecord.getNid());
            optionalObject.ifPresent(chronology -> {
                StringBuilder sb = new StringBuilder();
                switch (chronology.getVersionType()) {
                    case CONCEPT:
                        sb.append(viewProperties.getManifoldCoordinate().getPreferredDescriptionText(chronology.getNid()));
                        break;
                    case DESCRIPTION:
                        LatestVersion<Version> latest = chronology.getLatestVersion(viewProperties.getManifoldCoordinate().getViewStampFilter());
                        latest.ifPresent(version -> {
                            sb.append(((DescriptionVersion) latest.get()).getText());
                        });
                        break;
                    default:
                        sb.append(Get.getTextForComponent(chronology));
                }

                MenuItem historyItem = new MenuItemWithText(sb.toString());
                historyItem.setUserData(chronology);
                historyItem.setOnAction((ActionEvent actionEvent) -> {
                    unlink.run();
                    //MenuItem historyMenuItem = (MenuItem) actionEvent.getSource();
                    IdentifiedObject  itemHistoryRecord = (IdentifiedObject)  historyItem.getUserData();
                    identifiedObjectFocusProperty.set(Get.concept(itemHistoryRecord.getNid()));
                });
                historyMenu.getItems().add(historyItem);
            });
        }
    }

    public static Menu makeCopyMenuItem(Optional<IdentifiedObject> concept, ManifoldCoordinate manifoldCoordinate) {
        Menu copyMenu = new Menu("copy");
        MenuItem conceptLoincCodeMenuItem = new MenuItemWithText("Concept LOINC code");
        copyMenu.getItems().add(conceptLoincCodeMenuItem);
        conceptLoincCodeMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                Optional<String> optionalLoincCode =
                        Get.identifierService().getIdentifierFromAuthority(concept.get().getNid(),
                                MetaData.LOINC_ID_ASSEMBLAGE____SOLOR,
                                manifoldCoordinate.getViewStampFilter());

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
            if (concept.isPresent()) {
                Optional<String> optionalSnomedCode =
                        Get.identifierService().getIdentifierFromAuthority(concept.get().getNid(),
                                MetaData.SCTID____SOLOR,
                                manifoldCoordinate.getViewStampFilter());

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
            if (concept.isPresent()) {
                String fqnString = manifoldCoordinate.getFullyQualifiedDescriptionText(concept.get().getNid());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(fqnString);
                clipboard.setContent(content);
            }
        });

        MenuItem conceptUuidMenuItem = new MenuItemWithText("Concept UUID");
        copyMenu.getItems().add(conceptUuidMenuItem);
        conceptUuidMenuItem.setOnAction((event) -> {
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
            if (concept.isPresent()) {
                String docbookXml = DocBook.getInlineEntry(Get.conceptSpecification(concept.get().getNid()),
                        manifoldCoordinate);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });

        MenuItem copyDocBookMenuItem = new MenuItemWithText("Docbook glossary entry");
        copyMenu.getItems().add(copyDocBookMenuItem);
        copyDocBookMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                String docbookXml = DocBook.getGlossentry(Get.conceptSpecification(concept.get().getNid()),
                        manifoldCoordinate);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });

        MenuItem copyJavaShortSpecMenuItem = new MenuItemWithText("Java concept specification");
        copyMenu.getItems().add(copyJavaShortSpecMenuItem);
        copyJavaShortSpecMenuItem.setOnAction((event) -> {
            if (concept.isPresent()) {
                ConceptSpecification conceptSpec = Get.conceptSpecification(concept.get().getNid());

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
             if (concept.isPresent()) {
                ConceptSpecification conceptSpec = Get.conceptSpecification(concept.get().getNid());

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
            if (concept.isPresent()) {
                ConceptChronology conceptChronicle = Get.concept(concept.get().getNid());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(conceptChronicle.toLongString());
                clipboard.setContent(content);
            }
        });
        return copyMenu;
    }

}
