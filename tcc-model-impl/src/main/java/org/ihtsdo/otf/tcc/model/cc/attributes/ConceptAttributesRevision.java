package org.ihtsdo.otf.tcc.model.cc.attributes;

//~--- non-JDK imports --------------------------------------------------------


import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeAnalogBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;

public class ConceptAttributesRevision extends Revision<ConceptAttributesRevision, ConceptAttributes>
        implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {  
   protected boolean defined = false;

   //~--- constructors --------------------------------------------------------
   public ConceptAttributesRevision() {
      super();
   }
   public ConceptAttributesRevision(ConceptAttributeAnalogBI another, ConceptAttributes primoridalMember) {
      super(another.getStatus(), another.getTime(), another.getAuthorNid(), another.getModuleNid(),
              another.getPathNid(), primoridalMember);
      this.defined = another.isDefined();
   }

   public ConceptAttributesRevision(TtkConceptAttributesRevision another, ConceptAttributes primoridalMember) throws IOException{
      super(another.getStatus(), another.getTime(), PersistentStore.get().getNidForUuids(another.getAuthorUuid()),
            PersistentStore.get().getNidForUuids(another.getModuleUuid()), PersistentStore.get().getNidForUuids(another.getPathUuid()), primoridalMember);
      this.defined = another.isDefined();
   }

   public ConceptAttributesRevision(int statusAtPositionNid, ConceptAttributes primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
   }

   public ConceptAttributesRevision(DataInputStream input, ConceptAttributes primoridalMember) throws IOException {
      super(input, primoridalMember);
      defined = input.readBoolean();
   }

   public ConceptAttributesRevision(Status status, long time, int authorNid,
                                    int moduleNid, int pathNid, ConceptAttributes primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
   }
   
   public ConceptAttributesRevision(ConceptAttributeAnalogBI another, Status status, long time, int authorNid,
                                    int moduleNid, int pathNid, ConceptAttributes primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.defined = another.isDefined();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addComponentNids(Set<Integer> allNids) {

      // nothing to add
   }


   // TODO Verify this is a correct implementation
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ConceptAttributesRevision.class.isAssignableFrom(obj.getClass())) {
         ConceptAttributesRevision another = (ConceptAttributesRevision) obj;

         if (this.stamp == another.stamp) {
            return true;
         }
      }

      return false;
   }

   @Override
   public ConceptAttributesRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);
         return this;
      }

      ConceptAttributesRevision newR;

      newR = new ConceptAttributesRevision(this, status, time, authorNid, pathNid,
              moduleNid, this.primordialComponent);
      this.primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append("conceptAttributes: ").append(this.primordialComponent.nid);
      buf.append(" defined: ").append(this.defined);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      StringBuilder buf = new StringBuilder();

      buf.append("concept ");

      if (defined) {
         buf.append("is fully defined");
      } else {
         buf.append("is primitive");
      }

      return buf.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConceptAttributes getPrimordialVersion() {
      return primordialComponent;
   }

   @Override
   public ConceptAttributesVersion getVersion(ViewCoordinate c) throws ContradictionException {
      return primordialComponent.getVersion(c);
   }

   @Override
   public List<ConceptAttributesVersion> getVersions() {
      return ((ConceptAttributes) primordialComponent).getVersions();
   }
   

   @Override
   public List<ConceptAttributesVersion> getVersionList() {
      return ((ConceptAttributes) primordialComponent).getVersionList();
   }
   

   @Override
   public Collection<ConceptAttributesVersion> getVersions(ViewCoordinate c) {
      return primordialComponent.getVersions(c);
   }

   @Override
   public boolean isDefined() {
      return defined;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDefined(boolean defined) {
      this.defined = defined;
      modified();
   }

    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        ConceptAttributeAB conAttrBp = new ConceptAttributeAB(primordialComponent.getConceptNid(), defined, getVersion(vc), vc,
                refexDirective, idDirective);
        return conAttrBp;
    }

    @Override
    public Optional<LatestVersion<ConceptAttributeVersionBI>> getLatestVersion(Class<ConceptAttributeVersionBI> type, StampCoordinate coordinate) {
       return primordialComponent.getLatestVersion(type, coordinate);
    }
    
}
