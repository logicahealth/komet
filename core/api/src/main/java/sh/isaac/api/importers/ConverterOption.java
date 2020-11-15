package sh.isaac.api.importers;

public interface ConverterOption {

    /**
     * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1 when running the
     * conversion as a maven / pom conversion.
     *
     * @return true, if allow multi select
     */
    boolean isAllowMultiSelectInPomMode();

    /**
     * true if it is valie for the user to select more than 1 entry from the pick list, false if they may select at most 1 when running the
     * conversion as a direct conversion.
     *
     * @return true, if allow multi select
     */
    boolean isAllowMultiSelectInDirectMode();

    /**
     * true if it is valid for the user to select 0 entries from the pick list, false if they must select 1 or more.
     *
     * @return true, if allow no selection
     */
    boolean isAllowNoSelection();

    /**
     * The description of this option suitable to display to the end user, in a GUI.
     *
     * @return the description
     */
    String getDescription();

    /**
     * The displayName of this option - suitable for GUI use to the end user.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * The internalName of this option - use when creating the pom file.
     *
     * @return the internal name
     */
    String getInternalName();

    /**
     * Gets the suggested pick list values.
     *
     * @return the suggested pick list values
     */
    ConverterOptionParamSuggestedValue[] getSuggestedPickListValues();

    /**
     * Get the default values for direct mode
     *
     * @return
     */
    String[] getDefaultsForDirectMode();
}
