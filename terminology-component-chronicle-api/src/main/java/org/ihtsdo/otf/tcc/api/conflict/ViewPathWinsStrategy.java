package org.ihtsdo.otf.tcc.api.conflict;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class ViewPathWinsStrategy extends ContradictionManagementStrategy {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
        + "<ul><li>checks if conflicting members are present on the users view path(s),</li>"
        + "<li>and if so, suppresses the members that are NOT on the view path(s) from </li>"
        + "<li>participating in the potential contradiction.</ul>"
        + "</html>";
    }

    @Override
    public String getDisplayName() {
        return "Suppress versions NOT on a view path from contradictions";
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        List<T> returnValues = new ArrayList<>(2);
        if (vc.getPositionSet().getViewPathNidSet().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }
        if (vc.getPositionSet().getViewPathNidSet().contains(part2.getPathNid())) {
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
            if (vc.getPositionSet().getViewPathNidSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(versions);
        }
        return returnValues;
    }

}
