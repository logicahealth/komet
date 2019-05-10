package sh.isaac.solor.direct.umls.apis;

import sh.isaac.solor.direct.umls.Terminologies;
import sh.isaac.solor.direct.umls.model.TerminologyCode;

import java.util.List;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public abstract class ApiClient {
    Terminologies sourceTerminology;
    List<Terminologies> targetTerminolgies;

    public ApiClient(Terminologies sourceTerminology, List<Terminologies> targetTerminolgies) {
        this.sourceTerminology = sourceTerminology;
        this.targetTerminolgies = targetTerminolgies;
    }

    public abstract List<TerminologyCode> getTargetCodes(String code);

    public Terminologies getSourceTerminology() { return this.sourceTerminology; }

    public List<Terminologies> getTargetTerminolgies() { return this.targetTerminolgies; }

}
