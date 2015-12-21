/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.logic.csiro.classify.tasks;

import gov.vha.isaac.ochre.logic.csiro.classify.ClassifierData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.task.TimedTaskWithProgressTracker;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kec
 */
public class ExtractAxioms extends TimedTaskWithProgressTracker<Void> {

	StampCoordinate stampCoordinate;
	LogicCoordinate logicCoordinate;

	public ExtractAxioms(StampCoordinate stampCoordinate,
			  LogicCoordinate logicCoordinate) {
		this.stampCoordinate = stampCoordinate;
		this.logicCoordinate = logicCoordinate;
		updateTitle("Extract axioms");
	}

	@Override
	protected Void call() throws Exception {
		AtomicInteger logicGraphMembers = new AtomicInteger();
		ClassifierData cd = ClassifierData.get(stampCoordinate, logicCoordinate);
		if (cd.isIncrementalAllowed()) {
			// axioms are already extracted. 
		} else {
			cd.clearAxioms();
			processAllStatedAxioms(stampCoordinate, logicCoordinate,
					  cd, logicGraphMembers);
		}
		return null;
	}

	protected void processAllStatedAxioms(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate, ClassifierData cd, AtomicInteger logicGraphMembers) {
		SememeSnapshotService<LogicGraphSememeImpl> sememeSnapshot = Get.sememeService().getSnapshot(LogicGraphSememeImpl.class, stampCoordinate);
		sememeSnapshot.getLatestSememeVersionsFromAssemblage(logicCoordinate.getStatedAssemblageSequence(), this).forEach(
				  (LatestVersion<LogicGraphSememeImpl> latest) -> {
					  LogicGraphSememeImpl lgs = latest.value();
					  int conceptSequence = Get.identifierService().getConceptSequence(lgs.getReferencedComponentNid());
					  if (Get.conceptService().isConceptActive(conceptSequence, stampCoordinate)) {
						  cd.translate(lgs);
						  logicGraphMembers.incrementAndGet();
					  }
				  });
	}

}
