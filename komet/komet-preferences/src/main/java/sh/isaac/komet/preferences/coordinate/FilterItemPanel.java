package sh.isaac.komet.preferences.coordinate;

public class FilterItemPanel {

    /*

    private void setup(Manifold manifold) {

        nameProperty.set(groupNameProperty().get());
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.stampCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.stampCoordinates().put(itemKey, pathCoordinateItem);
        });

        getItemList().add(new PropertySheetStatusSetWrapper(manifold, pathCoordinateItem.allowedStatesProperty()));
        ObservableStampPosition stampPosition = pathCoordinateItem.stampPositionProperty().get();
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Path",
                stampPosition.pathConceptProperty(), TermAux.DEVELOPMENT_PATH, TermAux.MASTER_PATH));
        getItemList().add(new PropertySheetItemDateTimeWrapper("Time", stampPosition.timeProperty()));
        getItemList().add(new PropertySheetStampPrecedenceWrapper("Precedence", pathCoordinateItem.stampPrecedenceProperty()));
        getItemList().add(new PropertySheetConceptSetWrapper(manifold, pathCoordinateItem.moduleSpecificationsProperty()));
        getItemList().add(new PropertySheetConceptListWrapper(manifold, pathCoordinateItem.modulePreferenceListForVersionsProperty()));
        getItemList().add(new PropertySheetConceptSetWrapper(manifold, pathCoordinateItem.authorSpecificationsProperty()));
        revert();
        save();
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        ByteArrayDataBuffer buff = new ByteArrayDataBuffer();
        this.pathCoordinateItem.putExternal(buff);
        buff.trimToSize();
        getPreferencesNode().putByteArray(PathItemPanel.Keys.PATH_COORDINATE_DATA, buff.getData());
    }

    @Override
    protected void revertFields() throws BackingStoreException {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        ByteArrayDataBuffer revertbuffer = new ByteArrayDataBuffer();
        this.pathCoordinateItem.getPathCoordinate().putExternal(revertbuffer);
        revertbuffer.trimToSize();
        byte[] data = getPreferencesNode().getByteArray(PathItemPanel.Keys.PATH_COORDINATE_DATA, revertbuffer.getData());
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(data);
        StampPathImmutable pathCoordinate = StampPathImmutable.make(buffer);
        if (!pathCoordinate.getStampCoordinateUuid().equals(this.pathCoordinateItem.getStampCoordinateUuid())) {
            this.pathCoordinateItem.allowedStatesProperty().getValue().clear();
            this.pathCoordinateItem.allowedStatesProperty().getValue().addAll(pathCoordinate.getAllowedStates());

            this.pathCoordinateItem.authorSpecificationsProperty().getValue().clear();
            this.pathCoordinateItem.authorSpecificationsProperty().getValue().addAll(pathCoordinate.getAuthorSpecifications());

            this.pathCoordinateItem.modulePreferenceListForVersionsProperty().clear();
            this.pathCoordinateItem.modulePreferenceListForVersionsProperty().addAll(pathCoordinate.getModulePreferenceOrderForVersions());

            this.pathCoordinateItem.moduleSpecificationsProperty().clear();
            this.pathCoordinateItem.moduleSpecificationsProperty().addAll(pathCoordinate.getModuleSpecifications());

            this.pathCoordinateItem.stampPositionProperty().get().stampPathConceptSpecificationProperty().setValue(pathCoordinate.getStampPosition().getStampPathSpecification());
            this.pathCoordinateItem.stampPositionProperty().get().timeProperty().setValue(pathCoordinate.getStampPosition().getTime());

            this.pathCoordinateItem.stampPrecedenceProperty().setValue(pathCoordinate.getStampPrecedence());
        }
    }
    */

}
