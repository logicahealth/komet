package sh.isaac.komet.preferences.coordinate;

public class DigraphItemPanel {
    /*
   public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
        PREMISE_TYPE_KEY,
        STAMP_FOR_ORIGIN_KEY,
        STAMP_FOR_DESTINATION_KEY,
        LANGUAGE_COORDINATE_KEY,
        LOGIC_COORDINATE_KEY,
        SELECTED_COMPONENTS,
        HISTORY_RECORDS,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<UuidStringKey> originStampCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.ORIGIN_STAMP_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> originStampCoordinateKeyWrapper;

    private final SimpleObjectProperty<UuidStringKey> destinationStampCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.DESTINATION_STAMP_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> destinationStampCoordinateKeyWrapper;

    private final SimpleObjectProperty<UuidStringKey> languageCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> languageCoordinateKeyWrapper;

    private final SimpleObjectProperty<UuidStringKey> logicCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LOGIC_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> logicCoordinateKeyWrapper;

    private final PropertySheetItemObjectListWrapper<PremiseType> premiseTypeWrapper;

    private final PropertySheetComponentListWrapper selectionWrapper;
    private final PropertySheetComponentListWrapper historyWrapper;

    private final Manifold manifold;

    private final UuidStringKey itemKey;


    public ManifoldCoordinateItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode, "Manifold"), manifold, kpc);

        // replaces the manifold with the manifold specified by the groupNameProperty...
        manifold = Manifold.get(groupNameProperty().get());
        nameProperty.set(groupNameProperty().get());

        this.manifold = manifold;

        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));

        ObservableList<PremiseType> premiseTypes =
                FXCollections.observableArrayList(PremiseType.INFERRED, PremiseType.STATED);

        this.premiseTypeWrapper = new PropertySheetItemObjectListWrapper("Premise type", manifold.getDigraph().premiseTypeProperty(),
                premiseTypes);
        getItemList().add(this.premiseTypeWrapper);

        this.originStampCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("STAMP for origin", originStampCoordinateKeyProperty, FxGet.stampCoordinateKeys());
        getItemList().add(this.originStampCoordinateKeyWrapper);

        this.destinationStampCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("STAMP for destination", destinationStampCoordinateKeyProperty, FxGet.stampCoordinateKeys());
        getItemList().add(destinationStampCoordinateKeyWrapper);

        this.languageCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("Language coordinate", languageCoordinateKeyProperty, FxGet.languageCoordinateKeys());
        getItemList().add(this.languageCoordinateKeyWrapper);

        this.logicCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("Logic coordinate", logicCoordinateKeyProperty, FxGet.logicCoordinateKeys());
        getItemList().add(this.logicCoordinateKeyWrapper);

        this.selectionWrapper = new PropertySheetComponentListWrapper(manifold, manifold.manifoldSelectionProperty());
        getItemList().add(this.selectionWrapper);

        this.historyWrapper = new PropertySheetComponentListWrapper(manifold, manifold.getHistoryRecords());
        getItemList().add(this.historyWrapper);


        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.manifoldCoordinates().put(itemKey, this.manifold);
        FxGet.setManifoldForManifoldCoordinate(itemKey, manifold);
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.languageCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.manifoldCoordinates().put(itemKey, this.manifold);
            FxGet.setManifoldForManifoldCoordinate(itemKey, this.manifold);
        });
        this.manifold.manifoldSelectionProperty().setAll(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS));
        this.manifold.setSelectionPreferenceUpdater(this);
        revertFields();
        save();
        if (originStampCoordinateKeyProperty.get() == null) {
            throw new NullPointerException("originStampCoordinateKeyProperty value cannot be null");
        }
        if (destinationStampCoordinateKeyProperty.get() == null) {
            throw new NullPointerException("destinationStampCoordinateKeyProperty value cannot be null");
        }
        if (languageCoordinateKeyProperty.get() == null) {
            throw new NullPointerException("languageCoordinateKeyProperty value cannot be null");
        }
        if (logicCoordinateKeyProperty.get() == null) {
            throw new NullPointerException("logicCoordinateKeyProperty value cannot be null");
        }
        this.premiseTypeWrapper.setValue(PremiseType.valueOf(getPreferencesNode().get(Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name())));
        this.manifold.getDigraphManifold().stampCoordinateProperty().setValue(FxGet.stampCoordinates().get(originStampCoordinateKeyProperty.get()));
        this.manifold.getDigraphManifold().destinationStampCoordinateProperty().setValue(FxGet.stampCoordinates().get(destinationStampCoordinateKeyProperty.get()));
        this.manifold.getDigraphManifold().languageCoordinateProperty().setValue(FxGet.languageCoordinates().get(languageCoordinateKeyProperty.get()));
        this.manifold.getDigraphManifold().logicCoordinateProperty().setValue(FxGet.logicCoordinates().get(logicCoordinateKeyProperty.get()));

        originStampCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getDigraphManifold().stampCoordinateProperty().setValue(FxGet.stampCoordinates().get(newValue));
            } else {
                this.manifold.getDigraphManifold().stampCoordinateProperty().setValue(null);
            }
        });
        destinationStampCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getDigraphManifold().destinationStampCoordinateProperty().setValue(FxGet.stampCoordinates().get(newValue));
            } else {
                this.manifold.getDigraphManifold().destinationStampCoordinateProperty().setValue(null);
            }
        });
        languageCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getDigraphManifold().languageCoordinateProperty().setValue(FxGet.languageCoordinates().get(newValue));
            } else {
                this.manifold.getDigraphManifold().languageCoordinateProperty().setValue(null);
            }
        });
        logicCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getDigraphManifold().logicCoordinateProperty().setValue(FxGet.logicCoordinates().get(newValue));
            } else {
                this.manifold.getDigraphManifold().logicCoordinateProperty().setValue(null);
            }
        });

    }

    @Override
    public void run() {
        save();
    }

    @Override
    protected void saveFields() {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        getPreferencesNode().put(Keys.PREMISE_TYPE_KEY, manifold.getDigraphManifold().getTaxonomyPremiseType().name());
        if (originStampCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.STAMP_FOR_ORIGIN_KEY, originStampCoordinateKeyProperty.get().toStringArray());
        }
        if (destinationStampCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.STAMP_FOR_DESTINATION_KEY, destinationStampCoordinateKeyProperty.get().toStringArray());
        }
        if (languageCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.LANGUAGE_COORDINATE_KEY, languageCoordinateKeyProperty.get().toStringArray());
        }
        if (logicCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.LOGIC_COORDINATE_KEY, logicCoordinateKeyProperty.get().toStringArray());
        }


        getPreferencesNode().putComponentList(Keys.SELECTED_COMPONENTS, manifold.manifoldSelectionProperty());
        getPreferencesNode().putComponentList(Keys.HISTORY_RECORDS, manifold.getHistoryRecords());
    }

    @Override
    protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        this.manifold.getDigraphManifold().taxonomyPremiseTypeProperty()
                .setValue(PremiseType.valueOf(getPreferencesNode().get(Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name())));

        if (getPreferencesNode().hasKey(Keys.STAMP_FOR_ORIGIN_KEY)) {
            this.originStampCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.STAMP_FOR_ORIGIN_KEY)));
            this.originStampCoordinateKeyWrapper.setValue(this.originStampCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.STAMP_FOR_DESTINATION_KEY)) {
            this.destinationStampCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.STAMP_FOR_DESTINATION_KEY)));
            this.destinationStampCoordinateKeyWrapper.setValue(this.destinationStampCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.LANGUAGE_COORDINATE_KEY)) {
            this.languageCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.LANGUAGE_COORDINATE_KEY)));
            this.languageCoordinateKeyWrapper.setValue(this.languageCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.LOGIC_COORDINATE_KEY)) {
            this.logicCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.LOGIC_COORDINATE_KEY)));
            this.logicCoordinateKeyWrapper.setValue(this.logicCoordinateKeyProperty.get());
        }

        if (!manifold.manifoldSelectionProperty().get().equals(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS))) {
            manifold.manifoldSelectionProperty().clear();
            manifold.manifoldSelectionProperty().addAll(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS));
        }
        if (!manifold.getHistoryRecords().get().equals(getPreferencesNode().getComponentList(Keys.HISTORY_RECORDS))) {
            manifold.getHistoryRecords().clear();
            manifold.getHistoryRecords().addAll(getPreferencesNode().getComponentList(Keys.HISTORY_RECORDS));
        }
    }


    private static class ComponentProxyEditorStub implements PropertyEditor<ComponentProxy> {

        private final Manifold manifold;

        ComponentProxy value;

        public ComponentProxyEditorStub(Object manifold) {
            this.manifold = (Manifold) manifold;
        }

        @Override
        public Node getEditor() {
            return new Label("ComponentProxyEditorStub: ");
        }

        @Override
        public ComponentProxy getValue() {
            return value;
        }

        @Override
        public void setValue(ComponentProxy value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }
     */
}
