package org.ihtsdo.otf.tcc.api.description;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentAnalogBI;

public interface DescriptionAnalogBI<A extends DescriptionAnalogBI>
        extends TypedComponentAnalogBI, DescriptionVersionBI<A> {

    public void setInitialCaseSignificant(boolean capStatus) throws PropertyVetoException;
    public void setLang(String lang) throws PropertyVetoException;
    public void setText(String text) throws PropertyVetoException;

}
