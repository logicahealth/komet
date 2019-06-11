package sh.isaac.solor.direct.umls.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sh.isaac.solor.direct.umls.Terminologies;

@JsonIgnoreProperties({"classType","attributes","definitions","relations","contentViewMemberships", "name", "termType", "language", "suppressible", "obsolete", "concept", "code", "sourceConcept", "sourceDescriptor", "parents", "children", "ancestors", "descendants", "atomCount", "cVMemberCount", "atoms", "concepts", "defaultPreferredAtom", "subsetMemberships"})

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public class TerminologyCode {

    private String ui;
    private String rootSource;

    public String getUi() {

        return this.ui;
    }

    public String getRootSource() {

        return this.rootSource;
    }

    public Terminologies getRootSourceEnum() {
        return Terminologies.valueOf(this.rootSource);
    }

}
