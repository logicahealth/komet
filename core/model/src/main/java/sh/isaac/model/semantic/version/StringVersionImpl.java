/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.model.semantic.version;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------
/**
 * The Class StringVersionImpl.
 *
 * @author kec
 */
public class StringVersionImpl
        extends AbstractVersionImpl
        implements StringVersion, MutableStringVersion {

   /**
    * The string.
    */
   private String string = null;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new string sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    */
   public StringVersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   /**
    * Instantiates a new string sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param data the data
    */
   public StringVersionImpl(SemanticChronology container,
           int stampSequence,
           ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.string = data.getUTF();
   }

   private StringVersionImpl(StringVersionImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
      this.string = other.getString();
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
              .getStampSequence(
                      this.getState(),
                      Long.MAX_VALUE,
                      ec.getAuthorNid(),
                      this.getModuleNid(),
                      ec.getPathNid());
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final StringVersionImpl newVersion = new StringVersionImpl(this, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   //~--- methods -------------------------------------------------------------
   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{String≤");
      sb.append(this.string);
      toString(sb);
      sb.append("≥S}");
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putUTF(this.string);
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSemanticType() {
      return VersionType.STRING;
   }

   /**
    * Gets the string.
    *
    * @return the string
    */
   @Override
   public String getString() {
      return this.string;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Sets the string.
    *
    * @param string the new string
    */
   @Override
   public void setString(String string) {
      if (this.string != null) {
         checkUncommitted();
      }

      this.string = string;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      StringVersionImpl otherString = (StringVersionImpl) other;
      if (!this.string.equals(otherString.string)) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof StringVersionImpl)) {
         return false;
      }
      StringVersionImpl otherString = (StringVersionImpl) other;
      return this.string.equals(otherString.string);
   }

}
