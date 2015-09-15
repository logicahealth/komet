/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.classifier;

import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public interface ClassifierService {
    
	/**
	 * 
	 * @return A task that can be used to block, if the caller wishes to wait
	 * for the results. 
	 */
    Task<ClassifierResults> classify();

	 /**
	  * NOTE: this method call may cause a full or incremental classification if 
	  * changes have been made to the axioms since the last classify. 
	  * @param expression Expression to identify the concept identifier for. 
	  * @param editCoordinate edit coordinate in case the expression represents a 
	  * new concept, and thus needs to be added, and classified/ 
	  * @return  A task that can be used to block, if the caller wishes to wait
	 * for the results.
	  */
    Task<Integer> getConceptSequenceForExpression(LogicalExpression expression, 
            EditCoordinate editCoordinate);
    
}
