package org.ihtsdo.otf.tcc.api.coordinate;

public enum Precedence {
    
    TIME("time precedence","<html>If two versions are both on a route to the destination, " +
    		"the version with the later time has higher precedence."),
    PATH("path precedence","<html>If two versions are both on route to the destination, " +
    		"but one version is on a path that is closer to the destination, " +
    		"the version on the closer path has higher precedence.<br><br>If two versions " +
        	"are on the same path, the version with the later time has higher precedence."),
    MIXED("mixed precedence","<html>Returns a conflict if time based " +
    		"precendence and path base precendence have different views." +
    		"The conflict is resolved using the conflict resolution " +
        	"policy.");
    
    private String label;
    private String description;
    
    private Precedence(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label;
    }

}
