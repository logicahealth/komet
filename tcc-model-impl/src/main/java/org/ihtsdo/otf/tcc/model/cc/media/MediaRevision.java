package org.ihtsdo.otf.tcc.model.cc.media;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;

public class MediaRevision extends Revision<MediaRevision, Media>
        implements MediaVersionFacade {
    protected String textDescription;
    protected int    typeNid;

   //~--- constructors --------------------------------------------------------

   protected MediaRevision() {
      super();
   }

   MediaRevision(Media primoridalMember) {
      super(primoridalMember.primordialStamp, primoridalMember);
      this.textDescription = primoridalMember.getTextDescription();
      this.typeNid         = primoridalMember.getTypeNid();
   }

   MediaRevision(MediaRevision another, Media primoridalMember) {
      super(another.stamp, primoridalMember);
      this.textDescription = another.textDescription;
      this.typeNid         = another.typeNid;
   }

   public MediaRevision(TtkMediaRevision eiv, Media primoridalMember) throws IOException {
      super(eiv.getStatus(), eiv.getTime(), PersistentStore.get().getNidForUuids(eiv.getAuthorUuid()),
            PersistentStore.get().getNidForUuids(eiv.getModuleUuid()), PersistentStore.get().getNidForUuids(eiv.getPathUuid()), primoridalMember);
      this.textDescription = eiv.getTextDescription();
      this.typeNid         = PersistentStore.get().getNidForUuids(eiv.getTypeUuid());
   }

   protected MediaRevision(MediaVersionBI another, Status status, long time, int authorNid,
           int moduleNid, int pathNid, Media primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.textDescription = another.getTextDescription();
      this.typeNid         = another.getTypeNid();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addComponentNids(Set<Integer> allNids) {
      allNids.add(typeNid);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (MediaRevision.class.isAssignableFrom(obj.getClass())) {
         MediaRevision another = (MediaRevision) obj;

         if (this.stamp == another.stamp) {
            return true;
         }
      }

      return false;
   }

   @Override
   public MediaRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);

         return this;
      }

      MediaRevision newR;

      newR = new MediaRevision(this.primordialComponent, status, time, authorNid,
              moduleNid, pathNid,this.primordialComponent);
      this.primordialComponent.addRevision(newR);

      return newR;
   }
   
   @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB{
        MediaCAB mediaBp = new MediaCAB(getConceptNid(),
                getTypeNid(),
                getFormat(),
                getTextDescription(),
                getMedia(),
                getVersion(vc),
                Optional.of(vc), idDirective, refexDirective);
        return mediaBp;
    }

   @Override
   public boolean readyToWriteRevision() {
      assert textDescription != null : assertionString();
      assert typeNid != Integer.MAX_VALUE : assertionString();

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
      buf.append(" textDescription:" + "'").append(this.textDescription).append("'");
      buf.append(" typeNid:").append(this.typeNid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      StringBuffer buf = new StringBuffer();

      ConceptComponent.addTextToBuffer(buf, typeNid);
      buf.append("; ");
      buf.append(primordialComponent.getFormat());
      buf.append(": ");
      buf.append(textDescription);

      return buf.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getConceptNid() {
      return primordialComponent.enclosingConceptNid;
   }

   @Override
   public String getFormat() {
      return primordialComponent.getFormat();
   }

   @Override
   public byte[] getMedia() {
      return primordialComponent.getMedia();
   }

   @Override
   public Media getPrimordialVersion() {
      return primordialComponent;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#getTextDescription()
    */
   @Override
   public String getTextDescription() {
      return textDescription;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
    */

   @Override
   public int getTypeNid() {
      return typeNid;
   }
   
   @Override
   public Optional<MediaVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      return primordialComponent.getVersion(c);
   }

   @Override
   public List<? extends MediaVersionFacade> getVersions() {
      return ((Media) primordialComponent).getVersions();
   }

   @Override
   public List<? extends MediaVersionFacade> getVersionList() {
      return ((Media) primordialComponent).getVersions();
   }

   @Override
   public Collection<MediaVersion> getVersions(ViewCoordinate c) {
      return primordialComponent.getVersions(c);
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart)
    */
   public boolean hasNewData(MediaRevision another) {
      return ((this.getPathNid() != another.getPathNid()) || (this.getStatus() != another.getStatus())
              || ((this.textDescription.equals(another.getTextDescription()) == false)
                  || (this.typeNid != another.getTypeNid())));
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
    */

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTextDescription(String name) {
      this.textDescription = name;
      modified();
   }

   @Override
   public void setTypeNid(int type) {
      this.typeNid = type;
      modified();
   }

    @Override
    public Optional<LatestVersion<MediaVersionBI>> getLatestVersion(Class<MediaVersionBI> type, StampCoordinate<?> coordinate) {
        return this.primordialComponent.getLatestVersion(type, coordinate);
    }
}
