package org.ihtsdo.otf.tcc.api.chronicle;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;

public interface ComponentVersionBI extends ComponentBI, VersionPointBI {
   boolean stampIsInRange(int min, int max);

   String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException;

   //~--- get methods ---------------------------------------------------------

   Set<Integer> getAllNidsForVersion() throws IOException;

   int getAuthorNid();
   
   int getModuleNid();

   ComponentChronicleBI getChronicle();

   PositionBI getPosition() throws IOException;

   int getStamp();

   Status getStatus();
   
   public boolean isUncommitted();
   
   /**
    *
    * @return  <code>true</code> if this version is stored in the read-only
    * database, rather than in the mutable database. <code>false</code> otherwise.
    */
   boolean isBaselineGeneration();
   
   /**
    *
    * @param vc1 ViewCoordinate of the first version
    * 
    * @param vc2 ViewCoordinate of the second version
    * 
    * @param compareAuthoring Set to <code>true</code> to compare the author and path of the 
    * versions. Otherwise <code>false</code> to disregard author and path.
    * 
    * @return <code>true</code> if the versions are equal. <code>false</code> otherwise.
    */
   boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring);

    /**
     * Makes blueprint of a version of a component as specified by the given
     * <code>viewCoordinate</code>. The blueprint is a clone of the component
     * and is the preferred method for editing or creating a new version of a
     * component.
     *
     * @param viewCoordinate the view coordinate specifying which version is active or inactive
     * @param idDirective 
     * @param refexDirective 
     * @return the blueprint of the component
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    CreateOrAmendBlueprint makeBlueprint(ViewCoordinate viewCoordinate, 
            IdDirective idDirective, RefexDirective refexDirective)
            throws IOException, ContradictionException, InvalidCAB;
    
    boolean isActive() throws IOException;
}
