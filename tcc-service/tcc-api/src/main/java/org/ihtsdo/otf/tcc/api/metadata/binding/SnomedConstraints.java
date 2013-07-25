package org.ihtsdo.otf.tcc.api.metadata.binding;

import org.ihtsdo.otf.tcc.api.constraint.DescriptionConstraint;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraintIncoming;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraintOutgoing;

public class SnomedConstraints {

    public static RelConstraintOutgoing FINDING_SITE_OUT =
            SnomedRelSpec.FINDING_SITE.getOriginatingRelConstraint();
    public static RelConstraintIncoming FINDING_SITE_IN =
            SnomedRelSpec.FINDING_SITE.getDestinationRelConstraint();
    public static DescriptionConstraint FS_SEMANTIC_TAG =
            new DescriptionConstraint(Taxonomies.SNOMED,
            WbDescType.FULLY_SPECIFIED, "\\(.*\\)$");
}
