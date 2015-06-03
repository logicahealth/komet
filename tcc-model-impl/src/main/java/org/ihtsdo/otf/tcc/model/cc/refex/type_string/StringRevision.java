package org.ihtsdo.otf.tcc.model.cc.refex.type_string;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class StringRevision extends RefexRevision<StringRevision, StringMember>
        implements RefexStringAnalogBI<StringRevision> {
   protected String stringValue;

   //~--- constructors --------------------------------------------------------

   public StringRevision() {
      super();
   }

   public StringRevision(int statusAtPositionNid, StringMember another) {
      super(statusAtPositionNid, another);
      stringValue = another.getString1();
   }

   public StringRevision(TtkRefexStringRevision eVersion, StringMember primoridalMember) throws IOException {
      super(eVersion, primoridalMember);
      this.stringValue = eVersion.getString1();
   }

   public StringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, StringMember another) {
      super(status, time, authorNid, moduleNid, pathNid, another);
      stringValue = another.getString1();
   }

   protected StringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, StringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      stringValue = another.stringValue;
   }
   
   public StringRevision(RefexStringAnalogBI another, Status status, long time, int authorNid,
            int moduleNid, int pathNid, StringMember primoridalMember) {
        super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
        this.stringValue = another.getString1();
    }
    
    public StringRevision(RefexStringAnalogBI another, StringMember primordialMember){
        super(another.getStatus(), another.getTime(), another.getAuthorNid(), another.getModuleNid(),
              another.getPathNid(), primordialMember);
        this.stringValue = another.getString1();
    }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StringRevision.class.isAssignableFrom(obj.getClass())) {
         StringRevision another = (StringRevision) obj;

         return stringValue.equals(another.stringValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public StringRevision makeAnalog() {
      return new StringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public StringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      StringRevision newR = new StringRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert stringValue != null;

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
      buf.append(" stringValue:" + "'").append(this.stringValue).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return stringValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.STR;
   }

   @Override
   public Optional<StringMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<StringRevision, StringMember>> temp =  ((StringMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (StringMemberVersion)temp.get() : null);
   }

   @Override
   public List<StringMemberVersion> getVersions() {
      return ((StringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<StringRevision>> getVersions(ViewCoordinate c) {
      return ((StringMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.stringValue = str;
      modified();
   }

   public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
      modified();
   }
}
