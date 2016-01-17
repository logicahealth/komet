/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.logic.mojo;

import java.util.concurrent.ExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.classifier.ClassifierResults;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicService;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;
import gov.vha.isaac.ochre.mojo.external.QuasiMojo;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
@Service(name = "full-classification")
public class FullClassification extends QuasiMojo
{

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			LogicService logicService = LookupService.getService(LogicService.class);
			EditCoordinate editCoordinate = EditCoordinates.getDefaultUserSolorOverlay();
			LogicCoordinate logicCoordinate = LogicCoordinates.getStandardElProfile();
			editCoordinate = new EditCoordinateImpl(logicCoordinate.getClassifierSequence(), editCoordinate.getModuleSequence(), editCoordinate.getModuleSequence());

			Task<ClassifierResults> classifyTask = logicService
					.getClassifierService(StampCoordinates.getDevelopmentLatest(), LogicCoordinates.getStandardElProfile(), editCoordinate).classify();
			classifyTask.get();
		}
		catch (InterruptedException | ExecutionException ex)
		{
			throw new MojoExecutionException(ex.toString(), ex);
		}
	}
}
