package sh.komet.gui.control.manifold;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.LongConsumer;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.Activity;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.NavigationCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.coordinate.VertexSort;
import sh.isaac.api.coordinate.VertexSortNaturalOrder;
import sh.isaac.api.coordinate.VertexSortNone;
import sh.isaac.api.observable.coordinate.ObservableCoordinate;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableNavigationCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.api.observable.coordinate.PropertyWithOverride;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.utility.Frills;
import sh.komet.gui.util.FxGet;

public class CoordinateMenuFactory {

    /**
     * The
     * @param manifoldCoordinate Used to get preferred concept names
     * @param menuItems Menu item list add the menu item to
     * @param observableCoordinate The coordinate to make an display menu for.
     */
    public static void makeCoordinateDisplayMenu(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                 ObservableCoordinate observableCoordinate) {

        makeRecursiveOverrideMenu(manifoldCoordinate, menuItems,
                observableCoordinate);

        for (Property<?> baseProperty: observableCoordinate.getBaseProperties()) {
            menuItems.add(new MenuItem(getNameAndValueString(manifoldCoordinate, baseProperty)));
        }
        for (ObservableCoordinate<?> compositeCoordinate: observableCoordinate.getCompositeCoordinates()) {
            String propertyName = getPropertyNameWithOverride(manifoldCoordinate, compositeCoordinate);
            Menu compositeMenu = new Menu(propertyName);
            menuItems.add(compositeMenu);
            makeCoordinateDisplayMenu(manifoldCoordinate, compositeMenu.getItems(), compositeCoordinate);
        }


        if (observableCoordinate instanceof ManifoldCoordinate) {
            addSeparator(menuItems);
            //addRemoveOverrides(menuItems, observableCoordinate);
            addChangeItemsForManifold(manifoldCoordinate, menuItems, (ObservableManifoldCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof LanguageCoordinate) {
            addSeparator(menuItems);
            addChangeItemsForLanguage(manifoldCoordinate, menuItems, (ObservableLanguageCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof LogicCoordinate) {
            //menuItems.add(new SeparatorMenuItem());
            addChangeItemsForLogic(manifoldCoordinate, menuItems, (ObservableLogicCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof NavigationCoordinate) {
            addSeparator(menuItems);
            addChangeItemsForNavigation(manifoldCoordinate, menuItems, (ObservableNavigationCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof EditCoordinate) {
            addSeparator(menuItems);
            addChangeItemsForEdit(manifoldCoordinate, menuItems, (ObservableEditCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof StampFilter) {
            addSeparator(menuItems);
            addChangeItemsForFilter(manifoldCoordinate, menuItems, (ObservableStampFilter) observableCoordinate);
        }
    }

    private static void addSeparator(ObservableList<MenuItem> menuItems) {
        if (menuItems.get(menuItems.size() -1) instanceof SeparatorMenuItem) {
            // already a separator, don't duplicate.
        } else {
            menuItems.add(new SeparatorMenuItem());
        }

    }

    private static void addRemoveOverrides(ObservableList<MenuItem> menuItems, ObservableCoordinate observableCoordinate) {
        if (observableCoordinate.hasOverrides()) {
            MenuItem removeOverrides = new MenuItem("Remove overrides");
            menuItems.add(removeOverrides);
            removeOverrides.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.removeOverrides();
                });
                event.consume();
            });
        }
    }

    private static void addChangeItemsForFilter(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems, ObservableStampFilter observableCoordinate) {


        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (UuidStringKey key: FxGet.pathCoordinates().keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathForMenu = FxGet.pathCoordinates().get(key);
            item.setSelected(pathForMenu.getPathConceptNid() == observableCoordinate.getPathNidForFilter());
            item.setUserData(FxGet.pathCoordinates().get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.pathConceptProperty().setValue(Get.concept(path.getPathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForFilter(menuItems, observableCoordinate);

        changeStates(menuItems, "Change filter states", observableCoordinate.allowedStatusProperty());

        addIncludedModulesMenu(menuItems, observableCoordinate, manifoldCoordinate);

        addExcludedModulesMenu(menuItems, observableCoordinate, manifoldCoordinate);

    }
    private static void changeStates(ObservableList<MenuItem> menuItems, String menuText, ObservableManifoldCoordinate observableManifoldCoordinate) {
        Menu changeAllowedStatusMenu = new Menu(menuText);
        menuItems.add(changeAllowedStatusMenu);
        for (StatusSet statusSet: new StatusSet[] { StatusSet.ACTIVE_ONLY, StatusSet.ACTIVE_AND_INACTIVE, StatusSet.INACTIVE,
                StatusSet.WITHDRAWN, StatusSet.INACTIVE_ONLY}) {
            CheckMenuItem item = new CheckMenuItem(statusSet.toUserString());
            if (observableManifoldCoordinate.getVertexStatusSet() == observableManifoldCoordinate.getViewStampFilter().getAllowedStates()) {
                item.setSelected(statusSet.equals(observableManifoldCoordinate.getVertexStatusSet()));
            }
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableManifoldCoordinate.setAllowedStates(statusSet);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }

    }

    private static void changeStates(ObservableList<MenuItem> menuItems, String menuText, ObjectProperty<StatusSet> statusProperty) {
        Menu changeAllowedStatusMenu = new Menu(menuText);
        menuItems.add(changeAllowedStatusMenu);

        for (StatusSet statusSet: new StatusSet[] { StatusSet.ACTIVE_ONLY, StatusSet.ACTIVE_AND_INACTIVE, StatusSet.INACTIVE,
                StatusSet.WITHDRAWN, StatusSet.INACTIVE_ONLY}) {
            CheckMenuItem item = new CheckMenuItem(statusSet.toUserString());
            item.setSelected(statusSet.equals(statusProperty.get()));
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    statusProperty.setValue(statusSet);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }
    }

    private static void addChangePositionForManifold(ObservableList<MenuItem> menuItems, ObservableManifoldCoordinate observableCoordinate) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> {
                observableCoordinate.getViewStampFilter().timeProperty().setValue(time);
            });
        });
    }

    private static void addChangePositionForFilter(ObservableList<MenuItem> menuItems, ObservableStampFilter observableCoordinate) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> observableCoordinate.timeProperty().setValue(time));
        });
    }

    private static void addChangePositionMenu(ObservableList<MenuItem> menuItems, LongConsumer setPosition) {
        Menu changePositionMenu = new Menu("Change position");

        menuItems.add(changePositionMenu);
        MenuItem latestItem = new MenuItem("latest");
        changePositionMenu.getItems().add(latestItem);
        latestItem.setOnAction(event -> {
            Platform.runLater(() -> {
                setPosition.accept(Long.MAX_VALUE);
            });
            event.consume();
        });

        ImmutableLongList times = Get.stampService().getTimesInUse().toReversed();

        MutableIntObjectMap<Menu> yearMenuMap = IntObjectMaps.mutable.empty();
        for (long time: times.toArray()) {
            LocalDateTime localTime = DateTimeUtil.epochToZonedDateTime(time).toLocalDateTime();
            Menu aYearMenu = yearMenuMap.getIfAbsentPutWithKey(localTime.getYear(), (int year) -> {
                Menu yearMenu = new Menu(Integer.toString(year));
                changePositionMenu.getItems().add(yearMenu);
                yearMenu.getItems().add(new Menu("Jan"));
                yearMenu.getItems().add(new Menu("Feb"));
                yearMenu.getItems().add(new Menu("Mar"));
                yearMenu.getItems().add(new Menu("Apr"));
                yearMenu.getItems().add(new Menu("May"));
                yearMenu.getItems().add(new Menu("Jun"));
                yearMenu.getItems().add(new Menu("Jul"));
                yearMenu.getItems().add(new Menu("Aug"));
                yearMenu.getItems().add(new Menu("Sep"));
                yearMenu.getItems().add(new Menu("Oct"));
                yearMenu.getItems().add(new Menu("Nov"));
                yearMenu.getItems().add(new Menu("Dec"));
                return yearMenu;
            });
            Menu monthMenu = (Menu) aYearMenu.getItems().get(localTime.getMonthValue() - 1);
            MenuItem positionMenu = new MenuItem(
                    localTime.getDayOfMonth() + DateTimeUtil.getDayOfMonthSuffix(localTime.getDayOfMonth()) +
                            " " + DateTimeUtil.EASY_TO_READ_TIME_FORMAT.format(DateTimeUtil.epochToZonedDateTime(time)));
            monthMenu.getItems().add(positionMenu);
            positionMenu.setOnAction(event -> {
                Platform.runLater(() -> setPosition.accept(time));
                event.consume();
            });
        }

        yearMenuMap.values().forEach(yearMenu -> {
            ArrayList<MenuItem> toRemove = new ArrayList<>();
            for (MenuItem monthMenu: yearMenu.getItems()) {
                if (((Menu) monthMenu).getItems().isEmpty()) {
                    toRemove.add(monthMenu);
                }
            }
            yearMenu.getItems().removeAll(toRemove);
        });
    }

    private static void addIncludedModulesMenu(ObservableList<MenuItem> menuItems,
                                               ObservableStampFilter observableCoordinate,
                                               ManifoldCoordinate manifoldCoordinate) {
        Menu addIncludedModulesMenu = new Menu("Change included modules");
        menuItems.add(addIncludedModulesMenu);
        CheckMenuItem allModulesItem = new CheckMenuItem("all module wildcard");
        allModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().isEmpty());
        addIncludedModulesMenu.getItems().add(allModulesItem);
        allModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.moduleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem allIndividualModulesItem = new CheckMenuItem("all individual modules");

        allIndividualModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().containsAll(
                Get.stampService().getModuleConceptsInUse().castToSet()));
        addIncludedModulesMenu.getItems().add(allIndividualModulesItem);
        allIndividualModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet();
                newSet.addAll(Get.stampService().getModuleConceptsInUse().castToSet());
                observableCoordinate.moduleSpecificationsProperty().setValue(newSet);
            });
            event.consume();
        });

        Get.stampService().getModuleConceptsInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(moduleConcept));
            item.setSelected(observableCoordinate.moduleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.moduleSpecificationsProperty().remove(moduleConcept);
                    });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.moduleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            addIncludedModulesMenu.getItems().add(item);
        });
    }


    private static void addExcludedModulesMenu(ObservableList<MenuItem> menuItems,
                                               ObservableStampFilter observableCoordinate,
                                               ManifoldCoordinate manifoldCoordinate) {
        Menu excludedModulesMenu = new Menu("Change excluded modules");
        menuItems.add(excludedModulesMenu);
        CheckMenuItem noExclusionsWildcard = new CheckMenuItem("no exclusions wildcard");
        noExclusionsWildcard.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().isEmpty());
        excludedModulesMenu.getItems().add(noExclusionsWildcard);
        noExclusionsWildcard.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.excludedModuleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem excludeAllIndividualModulesItem = new CheckMenuItem("exclude all individual modules");

        excludeAllIndividualModulesItem.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().containsAll(
                Get.stampService().getModuleConceptsInUse().castToSet()));
        excludedModulesMenu.getItems().add(excludeAllIndividualModulesItem);
        if (excludeAllIndividualModulesItem.isSelected()) {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.excludedModuleSpecificationsProperty().clear();
                });
                event.consume();
            });
        } else {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet();
                    newSet.addAll(Get.stampService().getModuleConceptsInUse().castToSet());
                    observableCoordinate.excludedModuleSpecificationsProperty().setValue(newSet);
                });
                event.consume();
            });
        }
        Get.stampService().getModuleConceptsInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(moduleConcept));
            item.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.excludedModuleSpecificationsProperty().remove(moduleConcept);
                    });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.excludedModuleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            excludedModulesMenu.getItems().add(item);
        });
    }

    private static void addChangeItemsForEdit(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems, ObservableEditCoordinate observableCoordinate) {
        Menu changeAuthorMenu = new Menu("Change author");
        menuItems.add(changeAuthorMenu);

        Set<Integer> authors = Frills.getAllChildrenOfConcept(TermAux.USER.getNid(), true, true, manifoldCoordinate.getViewStampFilter());
        authors.add(TermAux.USER.getNid());
        
        // Create author assemblage
        for (Integer author: authors) {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(author));
            item.setSelected(observableCoordinate.getAuthorNidForChanges() == author);
            changeAuthorMenu.getItems().add(item);
            item.setOnAction(event -> {
                observableCoordinate.authorForChangesProperty().setValue(new ConceptProxy(author));
                event.consume();
            });
        }


        Menu changeDefaultModuleMenu = new Menu("Change default module");
        menuItems.add(changeDefaultModuleMenu);
        // Create module assemblage
        for (ConceptSpecification module: new ConceptSpecification[] {TermAux.SOLOR_OVERLAY_MODULE, TermAux.SOLOR_MODULE,
                MetaData.KOMET_MODULE____SOLOR, MetaData.TEST_MODULE____SOLOR, MetaData.TEST_PROMOTION_MODULE____SOLOR}) {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(module));
            item.setSelected(observableCoordinate.getDefaultModuleNid() == module.getNid());
            changeDefaultModuleMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.defaultModuleProperty().setValue(module));
                event.consume();
            });
        }

        Menu changeDestinationModuleMenu = new Menu("Change destination module");
        menuItems.add(changeDestinationModuleMenu);
        // Create module assemblage
        for (ConceptSpecification module: new ConceptSpecification[] {TermAux.SOLOR_OVERLAY_MODULE, TermAux.SOLOR_MODULE,
                MetaData.KOMET_MODULE____SOLOR}) {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(module));
            item.setSelected(observableCoordinate.getDestinationModuleNid() == module.getNid());
            changeDestinationModuleMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.promotionPathProperty().setValue(module));
                event.consume();
            });
        }


        Menu changePromotionPathMenu = new Menu("Change promotion path");
        menuItems.add(changePromotionPathMenu);
        for (StampPathImmutable path: Get.versionManagmentPathService().getPaths()) {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(path.getPathConceptNid()));
            item.setSelected(observableCoordinate.getPromotionPathNid() == path.getPathConceptNid());
            changePromotionPathMenu.getItems().add(item);
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.promotionPathProperty().setValue(Get.concept(path.getPathConceptNid())));
                event.consume();
            });
        }
    }

    private static void addChangeItemsForNavigation(ManifoldCoordinate manifoldCoordinate,
                                                    ObservableList<MenuItem> menuItems,
                                                    ObservableNavigationCoordinate observableCoordinate) {
        Menu changeNavigationMenu = new Menu("Change navigation");
        menuItems.add(changeNavigationMenu);
        for (ImmutableList<ConceptSpecification> navOption: FxGet.navigationOptions()) {
            StringBuilder menuText = new StringBuilder();
            for (ConceptSpecification navConcept: navOption) {
                if (menuText.length() > 0) {
                    menuText.append(", ");
                }
                menuText.append(manifoldCoordinate.getPreferredDescriptionText(navConcept));
            }
            CheckMenuItem item = new CheckMenuItem(menuText.toString());
            if (navOption.size() == observableCoordinate.getNavigationConceptNids().size()) {
                boolean foundAll = true;
                for (ConceptSpecification navConcept: navOption) {
                    if (!observableCoordinate.getNavigationConceptNids().contains(navConcept.getNid())) {
                        foundAll = false;
                    }
                }
                item.setSelected(foundAll);
            }
            if (!item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet(navOption.toArray(new ConceptSpecification[navOption.size()]));
                        observableCoordinate.navigatorIdentifierConceptsProperty().setValue(newSet);
                    });
                    event.consume();
                });
            }
            changeNavigationMenu.getItems().add(item);
        }
    }

    private static void addChangeItemsForLogic(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                               ObservableLogicCoordinate observableCoordinate) {
    }

    private static void addChangeItemsForLanguage(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                  ObservableLanguageCoordinate observableCoordinate) {

        Menu changeTypeOrder = new Menu("Change description preference");
        menuItems.add(changeTypeOrder);
        for (ImmutableList<? extends ConceptSpecification> typePreferenceList: FxGet.allowedDescriptionTypeOrder()) {
            CheckMenuItem typeOrderItem = new CheckMenuItem(manifoldCoordinate.toConceptString(typePreferenceList.castToList(), manifoldCoordinate::getPreferredDescriptionText));
            changeTypeOrder.getItems().add(typeOrderItem);
            typeOrderItem.setSelected(observableCoordinate.descriptionTypePreferenceListProperty().getValue().equals(typePreferenceList.castToList()));
            typeOrderItem.setOnAction(event -> {
                ObservableList<ConceptSpecification> prefList = FXCollections.observableArrayList(typePreferenceList.toArray(new ConceptSpecification[0]));
                Platform.runLater(() ->
                        observableCoordinate.descriptionTypePreferenceListProperty().setValue(prefList)
                );
                event.consume();
            });
        }

        Menu changeLanguageMenu = new Menu("Change language");
        menuItems.add(changeLanguageMenu);
        for (ConceptSpecification language: FxGet.allowedLanguages()) {
            CheckMenuItem languageItem = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(language));
            changeLanguageMenu.getItems().add(languageItem);
            languageItem.setSelected(language.getNid() == observableCoordinate.languageConceptProperty().get().getNid());
            languageItem.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.languageConceptProperty().setValue(language));
                event.consume();
            });
        }

        Menu changeDialectOrder = new Menu("Change dialect preference order");
        menuItems.add(changeDialectOrder);
        for (ImmutableList<? extends ConceptSpecification> dialectPreferenceList: FxGet.allowedDialectTypeOrder()) {
            CheckMenuItem dialectOrderItem = new CheckMenuItem(manifoldCoordinate.toConceptString(dialectPreferenceList.castToList(), manifoldCoordinate::getPreferredDescriptionText));
            changeDialectOrder.getItems().add(dialectOrderItem);
            dialectOrderItem.setSelected(observableCoordinate.dialectAssemblagePreferenceListProperty().getValue().equals(dialectPreferenceList.castToList()));
            dialectOrderItem.setOnAction(event -> {
                ObservableList<ConceptSpecification> prefList = FXCollections.observableArrayList(dialectPreferenceList.toArray(new ConceptSpecification[0]));
                Platform.runLater(() -> observableCoordinate.dialectAssemblagePreferenceListProperty().setValue(prefList));
                event.consume();
            });
        }
    }

    private static void addChangeItemsForManifold(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                  ObservableManifoldCoordinate observableCoordinate) {

        Menu changeActivityMenu = new Menu("Change activity");
        menuItems.add(changeActivityMenu);
        for (Activity activity: Activity.values()) {
            CheckMenuItem activityItem = new CheckMenuItem(activity.toUserString());
            changeActivityMenu.getItems().add(activityItem);
            activityItem.setSelected(observableCoordinate.getCurrentActivity() == activity);
            activityItem.setOnAction(event -> {
                Platform.runLater(() ->
                        observableCoordinate.activityProperty().setValue(activity)
                );
                event.consume();
            });
        }

        changeStates(menuItems, "Change allowed states", observableCoordinate);

        changeStates(menuItems, "Change allowed edge and language states", observableCoordinate.getViewStampFilter().allowedStatusProperty());

        changeStates(menuItems, "Change allowed vertex states", observableCoordinate.vertexStatusSetProperty());

        Menu changeDescriptionPreferenceMenu = new Menu("Change description preference");
        menuItems.add(changeDescriptionPreferenceMenu);

        for (ImmutableList<? extends ConceptSpecification> typePreferenceList: FxGet.allowedDescriptionTypeOrder()) {
            CheckMenuItem typeOrderItem = new CheckMenuItem(manifoldCoordinate.toConceptString(typePreferenceList.castToList(), manifoldCoordinate::getPreferredDescriptionText));
            changeDescriptionPreferenceMenu.getItems().add(typeOrderItem);
            typeOrderItem.setSelected(observableCoordinate.getLanguageCoordinate().descriptionTypePreferenceListProperty().getValue().equals(typePreferenceList.castToList()));
            typeOrderItem.setOnAction(event -> {
                ObservableList<ConceptSpecification> prefList = FXCollections.observableArrayList(typePreferenceList.toArray(new ConceptSpecification[0]));
                Platform.runLater(() ->
                        observableCoordinate.getLanguageCoordinate().descriptionTypePreferenceListProperty().setValue(prefList)
                );
                event.consume();
            });
        }

        addChangeItemsForNavigation(manifoldCoordinate,
                menuItems,
                observableCoordinate.getNavigationCoordinate());


        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (UuidStringKey key: FxGet.pathCoordinates().keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathCoordinate = FxGet.pathCoordinates().get(key);
            int pathNid = pathCoordinate.getPathConceptNid();
            item.setSelected(pathNid == observableCoordinate.getViewStampFilter().getPathNidForFilter());
            item.setUserData(FxGet.pathCoordinates().get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.setManifoldPath(path.getPathConceptNid()));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForManifold(menuItems, observableCoordinate);

        Menu changeVertexSortMenu = new Menu("Change sort");
        menuItems.add(changeVertexSortMenu);
        VertexSort[] sorts = new VertexSort[] {VertexSortNaturalOrder.SINGLETON, VertexSortNone.SINGLETON};
        for (VertexSort vertexSort: sorts) {
            CheckMenuItem item = new CheckMenuItem(vertexSort.getVertexSortName());
            item.setSelected(observableCoordinate.getVertexSort().equals(vertexSort));
            item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.vertexSortProperty().setValue(vertexSort));
                event.consume();
            });
            changeVertexSortMenu.getItems().add(item);
        }

        MenuItem reloadManifoldMenu = new MenuItem("Reload manifold menu");
        menuItems.add(reloadManifoldMenu);
        reloadManifoldMenu.setOnAction(event -> {
            Platform.runLater(() -> {
                menuItems.clear();
                CoordinateMenuFactory.makeCoordinateDisplayMenu(manifoldCoordinate, menuItems,
                        observableCoordinate);
            });
            event.consume();
        });

    }

    private static boolean makeRecursiveOverrideMenu(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                     ObservableCoordinate observableCoordinate) {

        if (observableCoordinate.hasOverrides()) {
            Menu overridesMenu = new Menu(manifoldCoordinate.toPreferredConceptString(observableCoordinate.getName()) + " has overrides");
            menuItems.add(overridesMenu);
            for (Property property: observableCoordinate.getBaseProperties()) {
                if (property instanceof PropertyWithOverride) {
                    PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                    if (propertyWithOverride.isOverridden()) {
                        overridesMenu.getItems().add(new MenuItem(getNameAndValueString(manifoldCoordinate, propertyWithOverride)));
                    }
                }
            }
            addRemoveOverrides(menuItems, observableCoordinate);


            for (ObservableCoordinate compositeCoordinate: observableCoordinate.getCompositeCoordinates()) {
                if (makeRecursiveOverrideMenu(manifoldCoordinate, overridesMenu.getItems(),
                        compositeCoordinate)) {
                    addSeparator(menuItems);
                }
            }
            return true;
        }
        return false;
    }

    private static String getNameAndValueString(ManifoldCoordinate manifoldCoordinate, Property<?> baseProperty) {
        String propertyName = getPropertyNameWithOverride(manifoldCoordinate, baseProperty);
        StringBuilder sb = new StringBuilder(propertyName + ": ");
        Object value = baseProperty.getValue();
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            if (collection.isEmpty()) {
                if (propertyName.toLowerCase().startsWith("modules")) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    manifoldCoordinate.toConceptString(Get.stampService().getModuleConceptsInUse(),
                            manifoldCoordinate::getPreferredDescriptionText,
                            collectionBuilder);
                    sb.append(" (*)\n").append(collectionBuilder);
                } else {
                    manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, sb);
                }
            } else {
                Object obj = collection.iterator().next();
                if (obj instanceof ConceptSpecification) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, collectionBuilder);
                    sb.append("\n").append(collectionBuilder);
                } else {
                        if (collection instanceof Set) {
                            Object[] objects = collection.toArray();
                            Arrays.sort(objects, (o1, o2) ->
                                    NaturalOrder.compareStrings(o1.toString(), o2.toString()));
                            sb.append(Arrays.toString(objects));
                        } else {
                            sb.append(collection.toString());
                        }

                    }
                }

        } else if (value instanceof Activity) {
            sb.append(((Activity) value).toUserString());
        } else if (value instanceof StatusSet) {
            sb.append(((StatusSet) value).toUserString());
        }  else {
            manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, sb);
        }
        return sb.toString();
    }

    private static String getPropertyNameWithOverride(ManifoldCoordinate manifoldCoordinate, Property<?> baseProperty) {
        String propertyName;
        if (baseProperty instanceof PropertyWithOverride) {
            PropertyWithOverride propertyWithOverride = (PropertyWithOverride) baseProperty;
            propertyName = propertyWithOverride.getOverrideName(manifoldCoordinate);
        } else {
            propertyName = manifoldCoordinate.toPreferredConceptString(baseProperty.getName());
        }
        return propertyName;
    }

}
