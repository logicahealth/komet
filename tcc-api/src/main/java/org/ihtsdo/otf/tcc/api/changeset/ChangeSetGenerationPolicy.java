package org.ihtsdo.otf.tcc.api.changeset;

public enum ChangeSetGenerationPolicy {
    /**
     * Don't generate change sets.
     */
    OFF("no changeset"),
    /**
     * Only include changes that represent the stampSequences from the current commit.
     */
    INCREMENTAL("incremental changeset"),

    /**
     * Include all changes.
     */
    COMPREHENSIVE("comprehensive changeset");

    String displayString;

    private ChangeSetGenerationPolicy(String displayString) {
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }

    
}
