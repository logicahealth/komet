package sh.komet.converter.turtle;

import java.util.UUID;

public class TempDynamicTypeMapColumnInfo {
    private TempDynamicTypeMap dtm;
    private boolean moreThanOneValue = false;
    private boolean columnRequired = true;
    private UUID columnLabelConcept;
    private String uri;

    public TempDynamicTypeMapColumnInfo(String uri, TempDynamicTypeMap dynamicTypeMap, boolean moreThanOneValue, boolean required, UUID columnLabelConcept)
    {
        this.uri = uri;
        this.dtm = dynamicTypeMap;
        this.moreThanOneValue = moreThanOneValue;
        this.columnLabelConcept = columnLabelConcept;
        this.columnRequired = required;
    }

    public void setMoreThanOneValue()
    {
        this.moreThanOneValue = true;
    }

    public void setColumnNotRequired()
    {
        this.columnRequired = false;
    }

    public TempDynamicTypeMap getDynamicTypeMap()
    {
        return this.dtm;
    }

    public boolean hasMoreThanOneValue()
    {
        return moreThanOneValue;
    }

    public UUID getColumnLabelConcept()
    {
        return columnLabelConcept;
    }

    public boolean columnRequired()
    {
        return columnRequired;
    }

    public String getURI()
    {
        return uri;
    }
}
