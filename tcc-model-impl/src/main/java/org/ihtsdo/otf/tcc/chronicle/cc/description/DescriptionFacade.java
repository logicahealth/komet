package org.ihtsdo.otf.tcc.chronicle.cc.description;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.chronicle.cc.component.TypedComponentFacade;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public interface DescriptionFacade
        extends TypedComponentFacade, DescriptionVersionBI {

 void setInitialCaseSignificant(boolean capStatus) throws PropertyVetoException;
 void setLang(String lang) throws PropertyVetoException;
 public void setText(String text) throws PropertyVetoException;
 @Override
 DescriptionRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid);

}
