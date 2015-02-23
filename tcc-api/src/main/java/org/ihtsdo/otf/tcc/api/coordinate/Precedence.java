package org.ihtsdo.otf.tcc.api.coordinate;

import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;

public enum Precedence {
    
    TIME(StampPrecedence.TIME),
    PATH(StampPrecedence.PATH);

    
    private final gov.vha.isaac.ochre.api.coordinate.StampPrecedence stampPrecedence;

    public StampPrecedence getStampPrecedence() {
        return stampPrecedence;
    }
    
    private Precedence(gov.vha.isaac.ochre.api.coordinate.StampPrecedence ochrePrecedence) {
        this.stampPrecedence = ochrePrecedence;
    }

    public String getDescription() {
        return stampPrecedence.getDescription();
    }

    @Override
    public String toString() {
        return stampPrecedence.toString();
    }

}
