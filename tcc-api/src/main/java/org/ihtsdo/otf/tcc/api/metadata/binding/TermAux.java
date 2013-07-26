package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class TermAux {

    public static ConceptSpec USER =
            new ConceptSpec("user",
            UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
    public static ConceptSpec IS_A =
            new ConceptSpec("is a (relationship type)",
            UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec CURRENT =
            new ConceptSpec("current (active status type)",
            UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpec RETIRED =
            new ConceptSpec("retired (inactive status type)",
            UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
    public static ConceptSpec INACTIVE_STATUS =
            new ConceptSpec("inactive (inactive status type)",
            UUID.fromString("1464ec56-7118-3051-9d21-0f95c1a39080"));
    public static ConceptSpec MOVED_TO =
            new ConceptSpec("moved elsewhere (inactive status type)",
            UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
    public static ConceptSpec REL_QUALIFIER_CHAR =
            new ConceptSpec("qualifier (characteristic type)",
            UUID.fromString("416ad0e4-b6bc-386c-900e-121c58b20f55"));
    public static ConceptSpec REL_HISTORIC =
            new ConceptSpec("historical (characteristic type)",
            UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    public static ConceptSpec REL_STATED_CHAR =
            new ConceptSpec("stated (defining characteristic type)",
            UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    public static ConceptSpec REL_INFERED_CHAR =
            new ConceptSpec("defining",
            UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec REL_NOT_REFINABLE =
            new ConceptSpec("not refinable (refinability type)",
            UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec REL_OPTIONALLY_REFINABLE =
            new ConceptSpec("optional (refinability type)",
            UUID.fromString("c3d997d3-b0a4-31f8-846f-03fa874f5479"));
     public static ConceptSpec WB_AUX_PATH =
            new ConceptSpec("Workbench Auxiliary",
            UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));
     public static ConceptSpec IHTSDO_CLASSIFIER  =
            new ConceptSpec("IHTSDO Classifier",
            UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
     public static ConceptSpec PATH  =
            new ConceptSpec("path",
            UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
    public static ConceptSpec PATH_ORIGIN_REFSET  =
            new ConceptSpec("Path origin reference set",
            UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));
    public static ConceptSpec PATH_REFSET  =
            new ConceptSpec("Path reference set",
            UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));
    public static ConceptSpec GENERATED_UUID  =
            new ConceptSpec("generated UUID",
            UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpec SNOMED_IDENTIFIER  =
            new ConceptSpec("SNOMED integer id",
            UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"));
}
