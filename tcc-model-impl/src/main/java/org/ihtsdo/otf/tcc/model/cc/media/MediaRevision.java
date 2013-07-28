package org.ihtsdo.otf.tcc.chronicle.cc.media;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import java.io.IOException;


import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.Revision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaRevision;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;

public class MediaRevision extends Revision<MediaRevision, Media>
        implements MediaVersionFacade {
   private String textDescription;
   private int    typeNid;

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
      super(eiv.getStatus(), eiv.getTime(), P.s.getNidForUuids(eiv.getAuthorUuid()),
            P.s.getNidForUuids(eiv.getModuleUuid()), P.s.getNidForUuids(eiv.getPathUuid()), primoridalMember);
      this.textDescription = eiv.getTextDescription();
      this.typeNid         = P.s.getNidForUuids(eiv.getTypeUuid());
   }

   protected MediaRevision(TupleInput input, Media primoridalMember) {
      super(input.readInt(), primoridalMember);
      this.textDescription = input.readString();
      this.typeNid         = input.readInt();
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
                vc, idDirective, refexDirective);
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

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeString(textDescription);
      output.writeInt(typeNid);
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

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
    */

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList partComponentNids = new IntArrayList(3);

      partComponentNids.add(typeNid);

      return partComponentNids;
   }

   @Override
   public Media.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return primordialComponent.getVersion(c);
   }

   @Override
   public Collection<? extends MediaVersionFacade> getVersions() {
      return ((Media) primordialComponent).getVersions();
   }

   @Override
   public Collection<Media.Version> getVersions(ViewCoordinate c) {
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
}
