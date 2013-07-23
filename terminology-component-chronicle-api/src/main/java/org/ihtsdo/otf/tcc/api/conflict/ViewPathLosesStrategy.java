package org.ihtsdo.otf.tcc.api.conflict;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class ViewPathLosesStrategy extends ContradictionManagementStrategy {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
        + "<ul><li>checks if conflicting members are present on the users view path(s),</li>"
        + "<li>and if so, suppresses the members on the view path from participating in the</li>"
        + "<li>potential contradiction.</ul>"
        + "</html>";
    }

    @Override
    public String getDisplayName() {
        return "Suppress view path versions from contradictions";
    }


    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        List<T> returnValues = new ArrayList<>(2);
        if (!vc.getPositionSet().getViewPathNidSet().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }
        if (!vc.getPositionSet().getViewPathNidSet().contains(part2.getPathNid())) {
            returnValues.add(part2);
        }
        if (returnValues.isEmpty()) {
            returnValues.add(part1);
            returnValues.add(part2);
        }
        return returnValues;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        List<T> returnValues = new ArrayList<>(2);
        for (T v: versions) {
            if (!vc.getPositionSet().getViewPathNidSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.isEmpty()) {
            returnValues.addAll(versions);
        }
        return returnValues;
    }

}