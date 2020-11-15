package sh.komet.converter.turtle;

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.IsaacObjectType;

public class TempDynamicSemanticHelper {
    private String niceName = null;
    private IsaacObjectType referencedComponentTypeRestriction = null;
    private VersionType referencedComponentTypeSubRestriction = null;

    public TempDynamicSemanticHelper(String uri)
    {
        if (uri.contains("/"))
        {
            String tail = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
            if (tail.contains("#"))
            {
                tail = tail.substring(tail.lastIndexOf('#') + 1, tail.length());
            }

            StringBuilder sb = new StringBuilder();
            //magic: https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
            for (String s : tail.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
            {
                sb.append(s.toLowerCase());
                sb.append(" ");
            }
            sb.setLength(sb.length() - 1);
            niceName = sb.toString();
        }
    }

    public TempDynamicSemanticHelper(String niceName, IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction)
    {
        this.niceName = niceName;
        this.referencedComponentTypeRestriction = referencedComponentTypeRestriction;
        this.referencedComponentTypeSubRestriction = referencedComponentTypeSubRestriction;
    }

    public String getNiceName()
    {
        return niceName;
    }

    public IsaacObjectType getReferencedComponentTypeRestriction()
    {
        return referencedComponentTypeRestriction;
    }

    public VersionType getReferencedComponentTypeSubRestriction()
    {
        return referencedComponentTypeSubRestriction;
    }
}
