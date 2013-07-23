/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 *
 * @author marc
 */
public class SnomedMetadataRfx {

    private static boolean isReleaseFormatSetupB = false;
    private static int releaseFormat = 0;
    // --- NIDs ---
    // DESCRIPTION NIDs
    private static int DES_FULL_SPECIFIED_NAME_NID;
    private static int DES_SYNONYM_PREFERRED_NAME_NID;
    private static int DES_SYNONYM_NID;
    private static int ACCEPTABLE_NID;
    private static int PREFERRED_NID;
    // RELATIONSHIP NIDs
    private static int REL_CH_DEFINING_CHARACTERISTIC_NID;
    private static int REL_CH_INFERRED_RELATIONSHIP_NID;
    private static int REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    private static int REL_CH_STATED_RELATIONSHIP_NID;
    private static int REL_HISTORY_HISTORIC_NID;
    private static int REL_HISTORY_MOVED_TO_NID;
    private static int REL_OPTIONAL_REFINABILITY_NID;
    private static int REL_NOT_REFINABLE_NID;
    private static int REL_MANDATORY_REFINABILITY_NID;
    // STATUS NIDs
    private static int STATUS_CURRENT_NID;
    private static int STATUS_LIMITED_NID;
    private static int STATUS_RETIRED_NID;
    private static int STATUS_INAPPROPRIATE_NID;
    // REFEX NIDs
    private static int US_DIALECT_REFEX_NID;
    private static int GB_DIALECT_REFEX_NID;
    private static int SYNONYMY_REFEX_NID;
    private static int REFERS_TO_REFEX_NID;
    private static int STATUS_DUPLICATE_NID;
    private static int STATUS_AMBIGUOUS_NID;
    private static int STATUS_ERRONEOUS_NID;
    private static int STATUS_OUTDATED_NID;
    // DESCRIPTION CONCEPTSPECS
    private static ConceptSpec DESC_PREFERRED;
    private static ConceptSpec DESC_ACCEPTABLE;
    // STATUS CONCEPTSPECS
    private static ConceptSpec STATUS_DUPLICATE;
    private static ConceptSpec STATUS_AMBIGUOUS;
    private static ConceptSpec STATUS_ERRONEOUS;
    private static ConceptSpec STATUS_OUTDATED;
    private static ConceptSpec STATUS_LIMITED;
    private static ConceptSpec STATUS_CURRENT;
    private static ConceptSpec STATUS_RETIRED;
    //REFEX CONCEPTS
    private static ConceptSpec REFEX_NON_HUMAN;
    private static ConceptSpec REFEX_VTM;
    private static ConceptSpec REFEX_VMP;
    private static ConceptSpec REFEX_SYNONYMY;

    public static int getDES_FULL_SPECIFIED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return DES_FULL_SPECIFIED_NAME_NID;
    }

    public static int getDES_SYNONYM_PREFERRED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return DES_SYNONYM_PREFERRED_NAME_NID;
    }

    public static int getDES_SYNONYM_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return DES_SYNONYM_NID;
    }

    public static int getDESC_ACCEPTABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return ACCEPTABLE_NID;
    }

    public static int getDESC_PREFERRED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return PREFERRED_NID;
    }

    public static int getREL_CH_DEFINING_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_CH_DEFINING_CHARACTERISTIC_NID;
    }

    public static int getREL_CH_INFERRED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_CH_INFERRED_RELATIONSHIP_NID;
    }

    public static int getREL_CH_QUALIFIER_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    }

    public static int getREL_CH_STATED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_CH_STATED_RELATIONSHIP_NID;
    }

    public static int getREL_HISTORY_HISTORIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_HISTORY_HISTORIC_NID;
    }

    public static int getREL_HISTORY_MOVED_TO_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_HISTORY_MOVED_TO_NID;
    }

    public static int getREL_MANDATORY_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_MANDATORY_REFINABILITY_NID;
    }

    public static int getREL_NOT_REFINABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_NOT_REFINABLE_NID;
    }

    public static int getREL_OPTIONAL_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REL_OPTIONAL_REFINABILITY_NID;
    }

    public static int getReleaseFormat() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
    }

    public static int getUS_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return US_DIALECT_REFEX_NID;
    }

    public static int getGB_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return GB_DIALECT_REFEX_NID;
    }

    public static int getSYNONYMY_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return SYNONYMY_REFEX_NID;
    }

    public static int getREFERS_TO_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REFERS_TO_REFEX_NID;
    }
    // DESCRIPTION CONCEPTS

    public static ConceptSpec getSTATUS_DUPLICATE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return STATUS_DUPLICATE;
    }
    // STATUS CONCEPTS

    public static ConceptSpec getDESC_PREFERRED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return DESC_PREFERRED;
    }

    public static ConceptSpec getDESC_ACCEPTABLE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return DESC_ACCEPTABLE;
    }

    public static ConceptSpec getSTATUS_AMBIGUOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return STATUS_AMBIGUOUS;
    }

    public static ConceptSpec getSTATUS_ERRONEOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return STATUS_ERRONEOUS;
    }

    public static ConceptSpec getSTATUS_OUTDATED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return STATUS_OUTDATED;
    }

    public static ConceptSpec getSTATUS_LIMITED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return STATUS_LIMITED;
    }

    // REFEX CONCEPTS
    public static ConceptSpec getREFEX_NON_HUMAN() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REFEX_NON_HUMAN;
    }

    public static ConceptSpec getREFEX_VTM() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REFEX_VTM;
    }

    public static ConceptSpec getREFEX_VMP() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REFEX_VMP;
    }

    public static ConceptSpec getREFEX_SYNONYMY() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return REFEX_SYNONYMY;
    }

    private static void setupSnoRf1Rf2() throws IOException {
        TerminologyStoreDI tf = Ts.get();

        isReleaseFormatSetupB = true;
        releaseFormat = 2;

        if (releaseFormat == 1) {
            // DESCRIPTIONS
            DES_FULL_SPECIFIED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUuids());
            DES_SYNONYM_PREFERRED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getUuids());
            DES_SYNONYM_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getUuids());
            ACCEPTABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getUuids());
            PREFERRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getUuids());
            // RELATIONSHIPS
            REL_CH_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_CH_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_CH_QUALIFIER_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.QUALIFIER_CHARACTERISTICS_TYPE_RF1.getUuids());
            REL_CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_HISTORY_HISTORIC_NID =
                    tf.getNidForUuids(SnomedMetadataRf1.HISTORICAL_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_HISTORY_MOVED_TO_NID =
                    tf.getNidForUuids(SnomedMetadataRf1.MOVED_TO_RF1.getUuids());
            REL_OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.OPTIONAL_REFINABILITY_TYPE_RF1.getUuids());
            REL_NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.NOT_REFINABLE_REFINABILITY_TYPE_RF1.getUuids());
            REL_MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.MANDATORY_REFINABILITY_TYPE_RF1.getUuids());
            // STATUS: 0 CURRENT, 1 RETIRED
            STATUS_CURRENT = SnomedMetadataRf1.CURRENT_RF1;
            STATUS_CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.CURRENT_RF1.getUuids());
            STATUS_LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getUuids());
            STATUS_RETIRED = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1;
            STATUS_RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getUuids());
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getUuids());
            // REFEX
            US_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getUuids());
            GB_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getUuids());
            SYNONYMY_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1.getUuids());
            REFERS_TO_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.REFERS_TO_REFSET_RF1.getUuids());
            // DESCRIPTION CONCEPT
            DESC_PREFERRED = SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1;
            DESC_ACCEPTABLE = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1;
            // STATUS CONCEPTSPECS
            STATUS_DUPLICATE = SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1;
            STATUS_DUPLICATE_NID = tf.getNidForUuids(STATUS_DUPLICATE.getUuids());
            STATUS_AMBIGUOUS = SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1;
            STATUS_AMBIGUOUS_NID = tf.getNidForUuids(STATUS_AMBIGUOUS.getUuids());
            STATUS_ERRONEOUS = SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1;
            STATUS_ERRONEOUS_NID = tf.getNidForUuids(STATUS_ERRONEOUS.getUuids());
            STATUS_OUTDATED = SnomedMetadataRf1.OUTDATED_INACTIVE_STATUS_RF1;
            STATUS_OUTDATED_NID = tf.getNidForUuids(STATUS_OUTDATED.getUuids());
            STATUS_LIMITED = SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1;
            // REFEX CONCEPTSPECS
            REFEX_NON_HUMAN = SnomedMetadataRf1.NON_HUMAN_RF1;
            REFEX_VTM = SnomedMetadataRf1.VTM_RF1;
            REFEX_VMP = SnomedMetadataRf1.VMP_RF1;
            REFEX_SYNONYMY = SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1;

        } else if (releaseFormat == 2) {
            // DESCRIPTIONS
            DES_FULL_SPECIFIED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids());
            DES_SYNONYM_PREFERRED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.SYNONYM_RF2.getUuids());
            DES_SYNONYM_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.SYNONYM_RF2.getUuids());
            ACCEPTABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids());
            PREFERRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.PREFERRED_RF2.getUuids());
            // RELATIONSHIPS
            REL_CH_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
            REL_CH_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
            REL_CH_QUALIFIER_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids());
            REL_CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
            REL_HISTORY_HISTORIC_NID =
                    tf.getNidForUuids(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getUuids());
            REL_HISTORY_MOVED_TO_NID =
                    tf.getNidForUuids(SnomedMetadataRf2.MOVED_TO_REFSET_RF2.getUuids());
            REL_OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
            REL_NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
            REL_MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
            STATUS_CURRENT = SnomedMetadataRf2.ACTIVE_VALUE_RF2;
            STATUS_CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
            STATUS_LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
            STATUS_RETIRED = SnomedMetadataRf2.INACTIVE_VALUE_RF2;
            STATUS_RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getUuids());
            //REFEX
            US_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids());
            GB_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getUuids());
            SYNONYMY_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getUuids());
            REFERS_TO_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getUuids());
            // DESCRIPTION CONCEPT
            DESC_PREFERRED = SnomedMetadataRf2.PREFERRED_RF2;
            DESC_ACCEPTABLE = SnomedMetadataRf2.ACCEPTABLE_RF2;
            // STATUS CONCEPT
            STATUS_DUPLICATE = SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2;
            STATUS_DUPLICATE_NID = tf.getNidForUuids(STATUS_DUPLICATE.getUuids());
            STATUS_AMBIGUOUS = SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2;
            STATUS_AMBIGUOUS_NID = tf.getNidForUuids(STATUS_AMBIGUOUS.getUuids());
            STATUS_ERRONEOUS = SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2;
            STATUS_ERRONEOUS_NID = tf.getNidForUuids(STATUS_ERRONEOUS.getUuids());
            STATUS_OUTDATED = SnomedMetadataRf2.OUTDATED_COMPONENT_RF2;
            STATUS_OUTDATED_NID = tf.getNidForUuids(STATUS_OUTDATED.getUuids());
            STATUS_LIMITED = SnomedMetadataRf2.LIMITED_COMPONENT_RF2;
            //REFEX CONCEPTS
            REFEX_NON_HUMAN = SnomedMetadataRf2.NON_HUMAN_RF2;
            REFEX_VTM = SnomedMetadataRf2.VTM_RF2;
            REFEX_VMP = SnomedMetadataRf2.VMP_RF2;
            REFEX_SYNONYMY = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2;

        } else {
            throw new IOException("SnomedMetadataRfx releaseFormat must equal 1 or 2.");
        }
    }
}
