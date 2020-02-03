/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.statement.constraint;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.statement.constraints.MeasureConstraints;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class MeasureConstraintImpl 
        extends AbstractVersionImpl implements MeasureConstraints, BrittleVersion {

    String constraintDescription;
    float initialLowerBound;
    float initialUpperBound;
    float initialGranularity;
    boolean initialIncludeUpperBound;
    boolean initialIncludeLowerBound;
    float minimumValue;
    float maximumValue;
    float minimumGranularity;
    float maximumGranularity;
    boolean showRange;
    boolean showGranularity;
    boolean showIncludeBounds;
    int measureSemanticConstraintAssemblageNid;
    

   public MeasureConstraintImpl(SemanticChronology container, int stampSequence) {
      super(container, stampSequence);
   }

   public MeasureConstraintImpl(SemanticChronology container, 
           int stampSequence, ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.constraintDescription = data.getUTF();
      this.initialLowerBound = data.getFloat();
      this.initialUpperBound = data.getFloat();
      this.initialGranularity = data.getFloat();
      this.initialIncludeUpperBound = data.getBoolean();
      this.initialIncludeLowerBound = data.getBoolean();
      this.minimumValue = data.getFloat();
      this.maximumValue = data.getFloat();
      this.minimumGranularity = data.getFloat();
      this.maximumGranularity = data.getFloat();
      this.showRange = data.getBoolean();
      this.showGranularity = data.getBoolean();
      this.showIncludeBounds = data.getBoolean();
      this.measureSemanticConstraintAssemblageNid = data.getNid();
   }
   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putUTF(this.constraintDescription);
      data.putFloat(this.initialLowerBound);
      data.putFloat(this.initialUpperBound);
      data.putFloat(this.initialGranularity);
      data.putBoolean(this.initialIncludeUpperBound);
      data.putBoolean(this.initialIncludeLowerBound);
      data.putFloat(this.minimumValue);
      data.putFloat(this.maximumValue);
      data.putFloat(this.minimumGranularity);
      data.putFloat(this.maximumGranularity);
      data.putBoolean(this.showRange);
      data.putBoolean(this.showGranularity);
      data.putBoolean(this.showIncludeBounds);
      data.putNid(this.measureSemanticConstraintAssemblageNid);
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final MeasureConstraintImpl newVersion = new MeasureConstraintImpl((SemanticChronology) this, stampSequence);
      newVersion.setConstraintDescription(constraintDescription);
      newVersion.setInitialLowerBound(initialLowerBound);
      newVersion.setInitialUpperBound(initialUpperBound);
      newVersion.setInitialGranularity(initialGranularity);
      newVersion.setInitialIncludeUpperBound(initialIncludeUpperBound);
      newVersion.setInitialIncludeLowerBound(initialIncludeLowerBound);
      newVersion.setMinimumValue(minimumValue);
      newVersion.setMaximumValue(maximumValue);
      newVersion.setMinimumGranularity(minimumGranularity);
      newVersion.setMaximumGranularity(maximumGranularity);
      newVersion.setShowRange(showRange);
      newVersion.setShowGranularity(showGranularity);
      newVersion.setShowIncludeBounds(showIncludeBounds);
      newVersion.setMeasureSemanticConstraintAssemblageNid(measureSemanticConstraintAssemblageNid);
      
      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      return editDistance3(other, 0) == 0;
   }

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      MeasureConstraintImpl another = (MeasureConstraintImpl) other;
      if (this.constraintDescription == null ? another.constraintDescription != null : !this.constraintDescription.equals(another.constraintDescription)) {
         editDistance++;
      }
      if (this.initialLowerBound != another.initialLowerBound) {
         editDistance++;
      }
      if (this.initialUpperBound != another.initialUpperBound) {
         editDistance++;
      }
      if (this.initialGranularity != another.initialGranularity) {
         editDistance++;
      }
      if (this.initialIncludeUpperBound != another.initialIncludeUpperBound) {
         editDistance++;
      }
      if (this.initialIncludeLowerBound != another.initialIncludeLowerBound) {
         editDistance++;
      }
      if (this.minimumValue != another.minimumValue) {
         editDistance++;
      }
     if (this.maximumValue != another.maximumValue) {
         editDistance++;
      }
     if (this.minimumGranularity != another.minimumGranularity) {
         editDistance++;
      }
     if (this.maximumGranularity != another.maximumGranularity) {
         editDistance++;
      }

     if (this.showRange != another.showRange) {
         editDistance++;
      }
     if (this.showGranularity != another.showGranularity) {
         editDistance++;
      }
     if (this.measureSemanticConstraintAssemblageNid != another.measureSemanticConstraintAssemblageNid) {
         editDistance++;
      }
     
     return editDistance;
   }
    
    @Override
    public String getConstraintDescription() {
        return constraintDescription;
    }

    public void setConstraintDescription(String constraintDescription) {
        this.constraintDescription = constraintDescription;
    }

    @Override
    public float getInitialLowerBound() {
        return initialLowerBound;
    }

    public void setInitialLowerBound(float initialLowerBound) {
        this.initialLowerBound = initialLowerBound;
    }

    @Override
    public float getInitialUpperBound() {
        return initialUpperBound;
    }

    public void setInitialUpperBound(float initialUpperBound) {
        this.initialUpperBound = initialUpperBound;
    }

    @Override
    public float getInitialGranularity() {
        return initialGranularity;
    }

    public void setInitialGranularity(float initialGranularity) {
        this.initialGranularity = initialGranularity;
    }

    @Override
    public boolean getInitialIncludeUpperBound() {
        return initialIncludeUpperBound;
    }

    public void setInitialIncludeUpperBound(boolean initialIncludeUpperBound) {
        this.initialIncludeUpperBound = initialIncludeUpperBound;
    }

    @Override
    public boolean getInitialIncludeLowerBound() {
        return initialIncludeLowerBound;
    }

    public void setInitialIncludeLowerBound(boolean initialIncludeLowerBound) {
        this.initialIncludeLowerBound = initialIncludeLowerBound;
    }

    @Override
    public float getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(float minimumValue) {
        this.minimumValue = minimumValue;
    }

    @Override
    public float getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(float maximumValue) {
        this.maximumValue = maximumValue;
    }

    @Override
    public float getMinimumGranularity() {
        return minimumGranularity;
    }

    public void setMinimumGranularity(float minimumGranularity) {
        this.minimumGranularity = minimumGranularity;
    }

    @Override
    public float getMaximumGranularity() {
        return maximumGranularity;
    }

    public void setMaximumGranularity(float maximumGranularity) {
        this.maximumGranularity = maximumGranularity;
    }

    @Override
    public boolean showRange() {
        return showRange;
    }

    public void setShowRange(boolean showRange) {
        this.showRange = showRange;
    }

    @Override
    public boolean showGranularity() {
        return showGranularity;
    }

    public void setShowGranularity(boolean showGranularity) {
        this.showGranularity = showGranularity;
    }

    @Override
    public boolean showIncludeBounds() {
        return showIncludeBounds;
    }

    public void setShowIncludeBounds(boolean showIncludeBounds) {
        this.showIncludeBounds = showIncludeBounds;
    }

    @Override
    public int getMeasureSemanticConstraintAssemblageNid() {
        return measureSemanticConstraintAssemblageNid;
    }

    public void setMeasureSemanticConstraintAssemblageNid(int measureSemanticConstraintAssemblageNid) {
        this.measureSemanticConstraintAssemblageNid = measureSemanticConstraintAssemblageNid;
    }

    @Override
    public VersionType getSemanticType() {
        return VersionType.MEASURE_CONSTRAINTS;
    }
}
