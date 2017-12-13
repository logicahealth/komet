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

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.LoincVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public class LoincVersionImpl 
        extends AbstractVersionImpl
         implements LoincVersion {
   
   private String component = null;
   private String loincNum = null;
   private String longCommonName = null;
   private String methodType = null;
   private String property = null;
   private String scaleType = null;
   private String shortName = null;
   private String loincStatus = null;
   private String system = null;
   private String timeAspect = null;

   public LoincVersionImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public String getComponent() {
      return component;
   }

   @Override
   public void setComponent(String component) {
      this.component = component;
   }

   @Override
   public String getLoincNum() {
      return loincNum;
   }

   @Override
   public void setLoincNum(String loincNum) {
      this.loincNum = loincNum;
   }

   @Override
   public String getLongCommonName() {
      return longCommonName;
   }

   @Override
   public void setLongCommonName(String longCommonName) {
      this.longCommonName = longCommonName;
   }

   @Override
   public String getMethodType() {
      return methodType;
   }

   @Override
   public void setMethodType(String methodType) {
      this.methodType = methodType;
   }

   @Override
   public String getProperty() {
      return property;
   }

   @Override
   public void setProperty(String property) {
      this.property = property;
   }

   @Override
   public String getScaleType() {
      return scaleType;
   }

   @Override
   public void setScaleType(String scaleType) {
      this.scaleType = scaleType;
   }

   @Override
   public String getShortName() {
      return shortName;
   }

   @Override
   public void setShortName(String shortName) {
      this.shortName = shortName;
   }

   @Override
   public String getLoincStatus() {
      return loincStatus;
   }

   @Override
   public void setLoincStatus(String loincStatus) {
      this.loincStatus = loincStatus;
   }

   @Override
   public String getSystem() {
      return system;
   }

   @Override
   public void setSystem(String system) {
      this.system = system;
   }

   @Override
   public String getTimeAspect() {
      return timeAspect;
   }

   @Override
   public void setTimeAspect(String timeAspect) {
      this.timeAspect = timeAspect;
   }

}

