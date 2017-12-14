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



package sh.isaac.model.semantic.version.brittle;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.LoincVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class LoincVersionImpl
        extends AbstractVersionImpl
         implements LoincVersion {
   private String component      = null;
   private String loincNum       = null;
   private String longCommonName = null;
   private String methodType     = null;
   private String property       = null;
   private String scaleType      = null;
   private String shortName      = null;
   private String loincStatus    = null;
   private String system         = null;
   private String timeAspect     = null;

   //~--- constructors --------------------------------------------------------

   public LoincVersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   //~--- methods -------------------------------------------------------------

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
      final LoincVersionImpl newVersion = new LoincVersionImpl((SemanticChronology) this, stampSequence);
      newVersion.setComponent(this.component);
      newVersion.setLoincNum(this.loincNum);
      newVersion.setLoincStatus(loincStatus);
      newVersion.setLongCommonName(longCommonName);
      newVersion.setMethodType(methodType);
      newVersion.setProperty(property);
      newVersion.setScaleType(scaleType);
      newVersion.setShortName(shortName);
      newVersion.setSystem(system);
      newVersion.setTimeAspect(timeAspect);
      
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      LoincVersionImpl another = (LoincVersionImpl) other;
      if (this.component == null ? another.component != null : !this.component.equals(another.component)) {
         editDistance++;
      }
      if (this.loincNum == null ? another.loincNum != null : !this.loincNum.equals(another.loincNum)) {
         editDistance++;
      }
      if (this.loincStatus == null ? another.loincStatus != null : !this.loincStatus.equals(another.loincStatus)) {
         editDistance++;
      }
      if (this.longCommonName == null ? another.longCommonName != null : !this.longCommonName.equals(another.longCommonName)) {
         editDistance++;
      }
      if (this.methodType == null ? another.methodType != null : !this.methodType.equals(another.methodType)) {
         editDistance++;
      }
      if (this.property == null ? another.property != null : !this.property.equals(another.property)) {
         editDistance++;
      }
      if (this.scaleType == null ? another.scaleType != null : !this.scaleType.equals(another.scaleType)) {
         editDistance++;
      }
     if (this.shortName == null ? another.shortName != null : !this.shortName.equals(another.shortName)) {
         editDistance++;
      }
     if (this.system == null ? another.system != null : !this.system.equals(another.system)) {
         editDistance++;
      }
     if (this.timeAspect == null ? another.timeAspect != null : !this.timeAspect.equals(another.timeAspect)) {
         editDistance++;
      }
      return editDistance;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getComponent() {
      return component;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setComponent(String component) {
      this.component = component;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getLoincNum() {
      return loincNum;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLoincNum(String loincNum) {
      this.loincNum = loincNum;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getLoincStatus() {
      return loincStatus;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLoincStatus(String loincStatus) {
      this.loincStatus = loincStatus;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getLongCommonName() {
      return longCommonName;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLongCommonName(String longCommonName) {
      this.longCommonName = longCommonName;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getMethodType() {
      return methodType;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setMethodType(String methodType) {
      this.methodType = methodType;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getProperty() {
      return property;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setProperty(String property) {
      this.property = property;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getScaleType() {
      return scaleType;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setScaleType(String scaleType) {
      this.scaleType = scaleType;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getShortName() {
      return shortName;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setShortName(String shortName) {
      this.shortName = shortName;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getSystem() {
      return system;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setSystem(String system) {
      this.system = system;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getTimeAspect() {
      return timeAspect;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTimeAspect(String timeAspect) {
      this.timeAspect = timeAspect;
   }
}

