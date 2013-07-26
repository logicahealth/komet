package org.ihtsdo.otf.tcc.api.conflict;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class EditPathLosesStrategy extends ContradictionManagementStrategy {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
        + "<ul><li>checks if conflicting members are present on the users edit path(s),</li>"
        + "<li>and if so, suppresses members on the edit path(s) from participating in the</li>"
        + "<li>potential contradiction.</ul>"
        + "</html>";
    }

    @Override
    public String getDisplayName() {
        return "Suppress edit path versions from contradictions";
    }


    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        List<T> returnValues = new ArrayList<>(2);
        if (!ec.getEditPaths().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }
        if (!ec.getEditPaths().contains(part2.getPathNid())) {
            returnValues.add(part2);
        }
        if (returnValues.size() == 0) {
            returnValues.add(part1);
            returnValues.add(part2);
        }
        return returnValues;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        List<T> returnValues = new ArrayList<>(2);
        for (T v: versions) {
            if (!ec.getEditPaths().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(versions);
        }
        return returnValues;
    }


}
