package sh.isaac.komet.batch.action;

import org.apache.mahout.math.map.OpenIntObjectHashMap;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.provider.commit.TransactionImpl;

public class Action {

    int assemblageNid;
    int fieldNid;

    public Action(int assemblageNid, int fieldNid) {
         this.assemblageNid = assemblageNid;
        this.fieldNid = fieldNid;
    }

    int getComponentUpdate() {
        throw new UnsupportedOperationException();
    }
    long getTimeUpdate() {
        throw new UnsupportedOperationException();
    }
    Status getStatusUpdate() {
        throw new UnsupportedOperationException();
    }

    public void apply(Chronology chronology, Transaction transaction, StampCoordinate stampCoordinate, EditCoordinate editCoordinate) {
        LatestVersion<Version> latestVersion = chronology.getLatestVersion(stampCoordinate);
        if (latestVersion.isAbsent()) {
            throw new UnsupportedOperationException("Batch editing requires a latest version to update. None found for: " + chronology);
        }
        if (assemblageNid == TermAux.ANY_ASSEMBLAGE.getNid() || chronology.getAssemblageNid() == assemblageNid) {
            switch(PropertyForAction.getPropertyForAction(this.fieldNid)) {
                case STATUS: {
                    Status update = getStatusUpdate();
                    if (latestVersion.get().getStatus() != update) {
                        Version versionToEdit = chronology.getVersionToEdit(stampCoordinate, editCoordinate.getAuthorNid(), editCoordinate.getPathNid(), transaction);
                        versionToEdit.setStatus(update);
                    }
                }
                break;
                case TIME: {
                    long update = getTimeUpdate();
                    if (latestVersion.get().getTime() != update) {
                        Version versionToEdit = chronology.getVersionToEdit(stampCoordinate, editCoordinate.getAuthorNid(), editCoordinate.getPathNid(), transaction);
                        versionToEdit.setTime(update);
                    }
                }
                break;
                case AUTHOR: {
                    int update = getComponentUpdate();
                    if (latestVersion.get().getAuthorNid() != update) {
                        Version versionToEdit = chronology.getVersionToEdit(stampCoordinate, editCoordinate.getAuthorNid(), editCoordinate.getPathNid(), transaction);
                        versionToEdit.setAuthorNid(update);
                    }
                }
                break;
                case MODULE: {
                    int update = getComponentUpdate();
                    if (latestVersion.get().getModuleNid() != update) {
                        Version versionToEdit = chronology.getVersionToEdit(stampCoordinate, editCoordinate.getAuthorNid(), editCoordinate.getPathNid(), transaction);
                        versionToEdit.setModuleNid(update);
                    }

                }
                break;
                case PATH: {
                    int update = getComponentUpdate();
                    if (latestVersion.get().getPathNid() != update) {
                        Version versionToEdit = chronology.getVersionToEdit(stampCoordinate, editCoordinate.getAuthorNid(), editCoordinate.getPathNid(), transaction);
                        versionToEdit.setPathNid(update);
                    }
                }
                break;
            }

        } else {
            // Ignore. Version does not meet action criterion.
        }

    }


    static OpenIntObjectHashMap<PropertyForAction> nidPropertyForActionMap = new OpenIntObjectHashMap<>();

    private enum PropertyForAction {
        STATUS(TermAux.STATUS_FOR_VERSION.getNid()),
        TIME(TermAux.TIME_FOR_VERSION.getNid()),
        AUTHOR(TermAux.AUTHOR_NID_FOR_VERSION.getNid()),
        MODULE(TermAux.MODULE_NID_FOR_VERSION.getNid()),
        PATH(TermAux.PATH_NID_FOR_VERSION.getNid());


        int[] nids;

        PropertyForAction(int... nids) {
            this.nids = nids;
            for (int nid: nids) {
                Action.nidPropertyForActionMap.put(nid, this);
            }

        }

        private static PropertyForAction getPropertyForAction(int nid) {
            return nidPropertyForActionMap.get(nid);
        }

    }
}
