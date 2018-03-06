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
package sh.isaac.api.statement.constraints;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion;

/**
 *
 * @author kec
 */
public interface MeasureConstraints extends SemanticVersion, BrittleVersion {
    
    String getConstraintDescription();
    
    float getInitialLowerBound();
    float getInitialUpperBound();
    float getInitialGranularity();
    boolean getInitialIncludeUpperBound();
    boolean getInitialIncludeLowerBound();
    
    float getMinimumValue();
    float getMaximumValue();
    float getMinimumGranularity();
    float getMaximumGranularity();
    
    boolean showRange();
    boolean showGranularity();
    boolean showIncludeBounds();
    
    int getMeasureSemanticConstraintAssemblageNid();
    

    /** 
     * {@inheritDoc}
     */
    @Override
    public default BrittleDataTypes[] getFieldTypes()
    {
        return new BrittleDataTypes[] {
                BrittleDataTypes.STRING,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.BOOLEAN,
                BrittleDataTypes.BOOLEAN,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.FLOAT,
                BrittleDataTypes.BOOLEAN,
                BrittleDataTypes.BOOLEAN,
                BrittleDataTypes.BOOLEAN,
                BrittleDataTypes.INTEGER};
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public default Object[] getDataFields()
    {
        return new Object[] {
                getConstraintDescription(),
                getInitialLowerBound(),
                getInitialUpperBound(),
                getInitialGranularity(),
                getInitialIncludeUpperBound(),
                getInitialIncludeLowerBound(),
                getMinimumValue(),
                getMaximumValue(),
                getMinimumGranularity(),
                getMaximumGranularity(),
                showRange(),
                showGranularity(),
                showIncludeBounds(),
                getMeasureSemanticConstraintAssemblageNid()};
    }
}
