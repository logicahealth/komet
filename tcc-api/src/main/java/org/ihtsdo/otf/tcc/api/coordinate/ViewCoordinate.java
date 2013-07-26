package org.ihtsdo.otf.tcc.api.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.nid.NidList;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

public class ViewCoordinate implements Externalizable {
   private long                   lastModSequence = Long.MIN_VALUE;
   private EnumSet<Status>        allowedStatus;
   private int                    classifierNid;
   private ContradictionManagerBI contradictionManager;
   private NidListBI              langPrefList;
   private LANGUAGE_SORT          langSort;
   private int                    languageNid;
   private String                 name;
   private PositionSetBI          positionSet;
   private Precedence             precedence;
   private RelAssertionType       relAssertionType;
   private UUID                   vcUuid;
   private ViewCoordinate         vcWithAllStatusValues;    // transient
   
   private static int isaNid = 0;

   //~--- constructors --------------------------------------------------------

   public ViewCoordinate() throws ValidationException {
      super();
   }

   protected ViewCoordinate(ViewCoordinate another) {
      super();
      this.vcUuid     = another.vcUuid;
      this.name       = another.name;
      this.precedence = another.precedence;

      if (another.positionSet != null) {
         this.positionSet = new PositionSet(another.positionSet);
      }

      if (another.allowedStatus != null) {
         this.allowedStatus = another.allowedStatus.clone();
      }

      this.contradictionManager = another.contradictionManager;
      this.languageNid          = another.languageNid;
      this.classifierNid        = another.classifierNid;
      this.relAssertionType     = another.relAssertionType;

      if (another.langPrefList != null) {
         this.langPrefList = new NidList(another.langPrefList.getListArray());
      }

      this.langSort        = another.langSort;
      this.lastModSequence = another.lastModSequence;
   }

   public ViewCoordinate(UUID vcUuid, String name, ViewCoordinate another) {
      this(another);
      this.vcUuid = vcUuid;
      this.name   = name;
   }

   public ViewCoordinate(UUID vcUuid, String name, Precedence precedence, PositionSetBI positionSet,
                         EnumSet<Status> allowedStatus,
                         ContradictionManagerBI contradictionManager, int languageNid, int classifierNid,
                         RelAssertionType relAssertionType, NidListBI langPrefList, LANGUAGE_SORT langSort) {
      super();
      assert precedence != null;
      assert contradictionManager != null;
      this.vcUuid     = vcUuid;
      this.name       = name;
      this.precedence = precedence;

      if (positionSet != null) {
         this.positionSet = new PositionSet(positionSet);
      }

      if (allowedStatus != null) {
         this.allowedStatus = allowedStatus.clone();
      }

      this.contradictionManager = contradictionManager;
      this.languageNid          = languageNid;
      this.classifierNid        = classifierNid;
      this.relAssertionType     = relAssertionType;

      if (langPrefList != null) {
         this.langPrefList = new NidList(langPrefList.getListArray());
      } else {
          this.langPrefList = new NidList(new int[] {languageNid});
      }

      this.langSort = langSort;
   }

   //~--- enums ---------------------------------------------------------------

   public enum LANGUAGE_SORT {
      LANG_BEFORE_TYPE("language before type"), TYPE_BEFORE_LANG("type before language"),
      LANG_REFEX("use language refex"), RF2_LANG_REFEX("use RF2 language refex");

      private String desc;

      //~--- constructors -----------------------------------------------------

      private LANGUAGE_SORT(String desc) {
         this.desc = desc;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public String toString() {
         return desc;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o instanceof ViewCoordinate) {
         ViewCoordinate another = (ViewCoordinate) o;

         if (!testEquals(precedence, another.precedence)) {
            return false;
         }

         if (!testEquals(positionSet, another.positionSet)) {
            return false;
         }

         if (!testEquals(allowedStatus, another.allowedStatus)) {
            return false;
         }

         if (!testEquals(contradictionManager, another.contradictionManager)) {
            return false;
         }

         if (!testEquals(languageNid, another.languageNid)) {
            return false;
         }

         if (!testEquals(classifierNid, another.classifierNid)) {
            return false;
         }

         if (!testEquals(relAssertionType, another.relAssertionType)) {
            return false;
         }

         if (!testEquals(langPrefList, another.langPrefList)) {
            return false;
         }

         if (!testEquals(langSort, another.langSort)) {
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hashCode = 0;

      for (PositionBI pos : positionSet.getPositionArray()) {
         hashCode = Hashcode.computeLong(hashCode, pos.getPath().getConceptNid(), pos.getTime());
      }

      return hashCode;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      TerminologyStoreDI ts = Ts.get();

      lastModSequence = in.readLong();

      allowedStatus = (EnumSet<Status>) in.readObject();

      classifierNid        = ts.getNidForUuids((UUID) in.readObject());
      contradictionManager = (ContradictionManagerBI) in.readObject();
      Object readObject = in.readObject();

      if (readObject == null) {
         langPrefList = null;
      } else {
         langPrefList = new NidList();
         langPrefList.addAll(ts.getNidCollection((Collection<UUID>) readObject));
      }

      langSort         = (LANGUAGE_SORT) in.readObject();
      languageNid      = ts.getNidForUuids((UUID) in.readObject());
      name             = (String) in.readObject();
      positionSet      = (PositionSetBI) in.readObject();
      precedence       = (Precedence) in.readObject();
      relAssertionType = (RelAssertionType) in.readObject();
      vcUuid           = (UUID) in.readObject();
   }

   private static boolean testEquals(Object o1, Object o2) {
      if ((o1 == null) && (o2 == null)) {
         return true;
      }

      if (o1 == o2) {
         return true;
      }

      if (o1 instanceof NidSetBI) {
         NidSetBI ns1 = (NidSetBI) o1;
         NidSetBI ns2 = (NidSetBI) o2;

         return Arrays.equals(ns1.getSetValues(), ns2.getSetValues());
      }

      if (o1 instanceof NidListBI) {
         NidListBI ns1 = (NidListBI) o1;
         NidListBI ns2 = (NidListBI) o2;

         return Arrays.equals(ns1.getListArray(), ns2.getListArray());
      }

      if (o1 instanceof PositionSetBI) {
         PositionSetBI ns1 = (PositionSetBI) o1;
         PositionSetBI ns2 = (PositionSetBI) o2;

         if (ns1.size() == 1) {
            if (ns2.size() == 1) {
               return ns1.getPositionArray()[0].equals(ns2.getPositionArray()[0]);
            }
         }

         return Arrays.equals(ns1.getPositionArray(), ns2.getPositionArray());
      }

      if (o1.equals(o2)) {
         return true;
      }

      return false;
   }

   @Override
   public String toString() {
      TerminologySnapshotDI snap = Ts.get().getSnapshot(this);
      StringBuilder         sb   = new StringBuilder();

      sb.append("name: ").append(name);
      sb.append("\n     vcUuid: ").append(vcUuid);
      sb.append("\n precedence: ").append(precedence);
      sb.append("\n  positions: ").append(positionSet);

      String statusStr = "all";

      if (allowedStatus != null) {
         statusStr = allowedStatus.toString();
      }

      sb.append(" \nallowedStatus: ");

      if (statusStr.length() < 50) {
         sb.append(statusStr);
      } else {
         sb.append(statusStr.substring(0, 50)).append("...");
      }
      sb.append(" \ncontradiction: ").append(contradictionManager);
      getConceptText(sb.append(" \nlanguage: "), snap, languageNid);
      getConceptText(sb.append(" \nclassifier: "), snap, classifierNid);
      sb.append(" \nrelAssertionType: ").append(relAssertionType);

      return sb.toString();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      TerminologyStoreDI ts = Ts.get();

      out.writeLong(lastModSequence);

      out.writeObject(allowedStatus);

      out.writeObject(ts.getUuidPrimordialForNid(classifierNid));
      out.writeObject(contradictionManager);

      if (langPrefList == null) {
         out.writeObject(null);
      } else {
         out.writeObject(ts.getUuidCollection(langPrefList.getListValues()));
      }

      out.writeObject(langSort);
      out.writeObject(ts.getUuidPrimordialForNid(languageNid));
      out.writeObject(name);
      out.writeObject(positionSet);
      out.writeObject(precedence);
      out.writeObject(relAssertionType);
      out.writeObject(vcUuid);
   }

   //~--- get methods ---------------------------------------------------------

   public EnumSet<Status> getAllowedStatus() {
      return allowedStatus;
   }

   public int getClassifierNid() {
      return classifierNid;
   }

   private void getConceptText(StringBuilder sb, TerminologySnapshotDI snap, int nid) {
      if (nid == Integer.MAX_VALUE) {
         sb.append("Integer.MAX_VALUE");

         return;
      }

      if (nid == Integer.MIN_VALUE) {
         sb.append("Integer.MIN_VALUE");

         return;
      }

      try {
         if ((snap.getConceptVersion(nid) != null)
                 && (snap.getConceptVersion(nid).getPreferredDescription() != null)) {
            sb.append(snap.getConceptVersion(nid).getPreferredDescription().getText());
         } else {
            sb.append(Integer.toString(nid));
         }
      } catch (IOException | ContradictionException ex) {
         sb.append(ex.getLocalizedMessage());
      }
   }

   public ContradictionManagerBI getContradictionManager() {
      return contradictionManager;
   }

   public int getIsaNid() {
       if (isaNid == 0) {
           try {
               isaNid = Snomed.IS_A.getNid();
           } catch (ValidationException ex) {
              throw new RuntimeException(ex);
           } catch (IOException ex) {
              throw new RuntimeException(ex);
           }
       }
      return isaNid;
   }

   public NidListBI getLangPrefList() {
      return langPrefList;
   }

   public LANGUAGE_SORT getLangSort() {
      return langSort;
   }

   public int getLanguageNid() {
      return languageNid;
   }

   public long getLastModSequence() {
      return lastModSequence;
   }

   public String getName() {
      return name;
   }

   public PositionSetBI getPositionSet() {
      return positionSet;
   }

   public Precedence getPrecedence() {
      return precedence;
   }

   public RelAssertionType getRelationshipAssertionType() {
      return relAssertionType;
   }

   public UUID getVcUuid() {
      return vcUuid;
   }

   public ViewCoordinate getVcWithAllStatusValues() {
      if (vcWithAllStatusValues == null) {
         vcWithAllStatusValues                   = new ViewCoordinate(this);
         vcWithAllStatusValues.allowedStatus = null;
      }

      return vcWithAllStatusValues;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAllowedStatus(EnumSet<Status> allowedStatus) {
      this.lastModSequence   = Ts.get().getSequence();
      this.allowedStatus = allowedStatus;
   }

   public void setClassifierNid(int classifierNid) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.classifierNid         = classifierNid;
   }

   public void setContradictionManager(ContradictionManagerBI contradictionManager) {
      this.lastModSequence      = Ts.get().getSequence();
      this.contradictionManager = contradictionManager;
   }

   public void setPositionSet(PositionSetBI positionSet) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.positionSet           = positionSet;
   }

   public void setPrecedence(Precedence precedence) {
      this.precedence = precedence;
   }

   public void setRelAssertionType(RelAssertionType relAssertionType) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.relAssertionType      = relAssertionType;
   }
   
   public TerminologySnapshotDI getSnapshot() {
       return Ts.get().getSnapshot(this);
   }
   
   public TerminologySnapshotDI getCachedSnapshot() {
       return Ts.get().cacheSnapshot(vcUuid, this);
   }
   
}
