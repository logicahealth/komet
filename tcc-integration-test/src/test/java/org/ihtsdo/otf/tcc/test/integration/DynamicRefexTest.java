/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.test.integration;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * {@link DynamicRefexTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()
public class DynamicRefexTest
{
	ConceptVersionBI concept1;
	ConceptVersionBI concept2;
	ConceptVersionBI concept3;
	ConceptVersionBI concept4;
	ConceptVersionBI concept5;
	ConceptVersionBI concept6;
	ConceptVersionBI concept7;
	
	private static boolean initRun = false;
	
	TerminologyBuilderBI builder;

	public DynamicRefexTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
	{
	}

	@AfterClass
	public static void tearDownClass()
	{
		try
		{
			Ts.get().cancel();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Before
	public void setUp()
	{
		if (!initRun)
		{
			initRun = true;

			try
			{
				ViewCoordinate vc = StandardViewCoordinates.getSnomedInferredThenStatedLatest();
				EditCoordinate ec = new EditCoordinate(TermAux.USER.getLenient().getNid(), Snomed.CORE_MODULE.getLenient().getNid(), vc.getViewPosition().getPath()
						.getConceptNid());
				builder = Ts.get().getTerminologyBuilder(ec, vc);
	
				ConceptSpec c1 = new ConceptSpec("Alcohol + paricalcitol + propylene glycol", UUID.fromString("c7803205-06d8-3c49-a485-0c99ea931450"));
				//Not a real concept, need to create
				ConceptSpec c2 = new ConceptSpec("test annotation style", UUID.fromString("1d84a17d-d056-58ae-b8a8-a5656378eab3"));
	
				//Not real concept, need to create
				ConceptSpec c3 = new ConceptSpec("test refset style", UUID.fromString("9d09a4f6-c5a2-5520-8874-09449dace55f"));
				ConceptSpec c4 = new ConceptSpec("Percutaneous approach", UUID.fromString("4d78af43-b095-3232-bf10-2b90a6b60d4d"));
				ConceptSpec c5 = new ConceptSpec("Open approach", UUID.fromString("59943708-d6cf-3bfc-b2f5-325a47b40c84"));
				ConceptSpec c6 = new ConceptSpec("Closed approach", UUID.fromString("68ce08de-83c2-3ab3-999b-5cd165b29566"));
				ConceptSpec c7 = new ConceptSpec("Surgical access values", UUID.fromString("7c9b57f0-6dd6-335b-b1ad-4f812c8ed622"));
	
				concept1 = Ts.get().getConceptVersion(vc, c1.getLenient().getConceptNid()); //annotation
				
				//Build concept 2
				//assemblage is -- annotation style refex
				RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(c2.getDescription(), 
						c2.getDescription(), "annotation style test data", new RefexDynamicColumnInfo[] {}, null, true, ec, vc);
				
				//Build concept 3
				//assemblage is refset style refex 
				RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(c3.getDescription(), 
						c3.getDescription(), "refset style test data", new RefexDynamicColumnInfo[] {}, null, false, ec, vc);
				
				concept2 = Ts.get().getConceptVersion(vc, c2.getLenient().getConceptNid());
				concept3 = Ts.get().getConceptVersion(vc, c3.getLenient().getConceptNid());
				
				concept4 = Ts.get().getConceptVersion(vc, c4.getLenient().getConceptNid()); //refset member
				concept5 = Ts.get().getConceptVersion(vc, c5.getLenient().getConceptNid()); //refset member
				concept6 = Ts.get().getConceptVersion(vc, c6.getLenient().getConceptNid()); //refset member
				concept7 = Ts.get().getConceptVersion(vc, c7.getLenient().getConceptNid()); //refset member
				
				//The old refex test used an annotation refex that already existed, I don't have one, need to build it:
				RefexDynamicCAB c1annotBp = new RefexDynamicCAB(concept1.getPrimordialUuid(), concept2.getPrimordialUuid());
				RefexDynamicChronicleBI<?> c1Annot = builder.construct(c1annotBp);
				//            NOTE: if adding a refset member, both the referenced component and the assemblage concept must be added as uncommitted
				Ts.get().addUncommitted(concept1);
				Ts.get().commit();
	
				//            add refset members
				RefexDynamicCAB c4annotBp = new RefexDynamicCAB(concept4.getPrimordialUuid(), concept3.getPrimordialUuid());
				RefexDynamicChronicleBI<?> c4Annot = builder.construct(c4annotBp);
				//            NOTE: if adding a refset member, both the referenced component and the assemblage concept must be added as uncommitted
				Ts.get().addUncommitted(concept4);
				RefexDynamicCAB c5annotBp = new RefexDynamicCAB(concept5.getPrimordialUuid(), concept3.getPrimordialUuid());
				RefexDynamicChronicleBI<?> c5Annot = builder.construct(c5annotBp);
				Ts.get().addUncommitted(concept5);
				RefexDynamicCAB c6annotBp = new RefexDynamicCAB(concept6.getPrimordialUuid(), concept3.getPrimordialUuid());
				RefexDynamicChronicleBI<?> c6Annot = builder.construct(c6annotBp);
				Ts.get().addUncommitted(concept6);
				RefexDynamicCAB c7annotBp = new RefexDynamicCAB(concept7.getPrimordialUuid(), concept3.getPrimordialUuid());
				RefexDynamicChronicleBI<?> c7Annot = builder.construct(c7annotBp);
				Ts.get().addUncommitted(concept7);
	
				Ts.get().addUncommitted(concept3);
				Ts.get().commit();
				
				//TODO [REFEXES] write some tests that validate data column storage
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Logger.getLogger(DynamicRefexTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public void testRefexMembers() throws IOException
	{

		System.out.println("** concept 1 ** : " + concept1.toUserString() + " -- annotation style: " + concept1.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers1 = concept1.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers1);
		//        is annotation member not assemblage concept
		Assert.assertTrue(refsetMembers1.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> annotations1 = concept1.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations1);
		Assert.assertTrue(annotations1.size() == 1);  //This won't have any dynamic annotations
		Collection<? extends RefexDynamicChronicleBI<?>> refexes1 = concept1.getRefexesDynamic();
		System.out.println("refexes: " + refexes1);
		Assert.assertTrue(refexes1.size() == 1); //asssemblage == concept2, rc == concept1

		System.out.println("** concept 2 ** : " + concept2.toUserString() + " -- annotation style: " + concept2.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers2 = concept2.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers2);
		Assert.assertTrue(refsetMembers1.isEmpty()); //is annotation style refex
		Collection<? extends RefexDynamicChronicleBI<?>> annotations2 = concept2.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations2);  //in the old refex code, the only one shown here was sctid, which I don't have
		Assert.assertTrue(annotations2.size() == 0);
		Collection<? extends RefexDynamicChronicleBI<?>> refexes2 = concept2.getRefexesDynamic();
		System.out.println("refexes: " + refexes2);
		Assert.assertTrue(refexes2.size() == 0);

		System.out.println("** concept 3 ** : " + concept3.toUserString() + " -- annotation style: " + concept3.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers3 = concept3.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers3);
		//        is refset style refex, 
		//        1--[assemblage == concept3, rc == concept4]
		//        2--[assemblage == concept3, rc == concept5]
		//        3--[assemblage == concept3, rc == concept6]
		//        4--[assemblage == concept3, rc == concept7]
		Assert.assertTrue(refsetMembers3.size() == 4);
		Collection<? extends RefexDynamicChronicleBI<?>> annotations3 = concept3.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations3);
		Assert.assertTrue(annotations3.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> refexes3 = concept3.getRefexesDynamic();
		System.out.println("refexes: " + refexes3);
		Assert.assertTrue(refexes3.size() == 0);  //old refex code had another one here that wasn't part of the test

		System.out.println("** concept 4 ** : " + concept4.toUserString() + " -- annotation style: " + concept4.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers4 = concept4.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers4);
		//        is refset member not assemblage concept
		Assert.assertTrue(refsetMembers4.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> annotations4 = concept4.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations4);
		Assert.assertTrue(annotations4.size() == 0);
		Collection<? extends RefexDynamicChronicleBI<?>> refexes4 = concept4.getRefexesDynamic();
		System.out.println("refexes: " + refexes4);
		//        1 -- [assemblage == concept3, rc = concept4]
		Assert.assertTrue(refexes4.size() == 1);

		System.out.println("** concept 5 ** : " + concept5.toUserString() + " -- annotation style: " + concept5.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers5 = concept5.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers5);
		//        is refset member not assemblage concept
		Assert.assertTrue(refsetMembers5.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> annotations5 = concept5.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations5);
		Assert.assertTrue(annotations5.size() == 0);
		Collection<? extends RefexDynamicChronicleBI<?>> refexes5 = concept5.getRefexesDynamic();
		System.out.println("refexes: " + refexes5);
		//        1 -- [assemblage == concept3, rc = concept5]
		Assert.assertTrue(refexes5.size() == 1);

		System.out.println("** concept 6 ** : " + concept6.toUserString() + " -- annotation style: " + concept6.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers6 = concept6.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers6);
		//       is refset member not assemblage concept
		Assert.assertTrue(refsetMembers6.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> annotations6 = concept6.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations6);
		Assert.assertTrue(annotations6.size() == 0);
		Collection<? extends RefexDynamicChronicleBI<?>> refexes6 = concept6.getRefexesDynamic();
		System.out.println("refexes: " + refexes6);
		//        1 -- [assemblage == concept3, rc = concept6]
		Assert.assertTrue(refexes6.size() == 1);

		System.out.println("** concept 7 ** : " + concept7.toUserString() + " -- annotation style: " + concept7.isAnnotationStyleRefex());
		Collection<? extends RefexDynamicChronicleBI<?>> refsetMembers7 = concept7.getRefsetDynamicMembers();
		System.out.println("refset members: " + refsetMembers7);
		//       is refset member not assemblage concept
		Assert.assertTrue(refsetMembers7.isEmpty());
		Collection<? extends RefexDynamicChronicleBI<?>> annotations7 = concept7.getRefexDynamicAnnotations();
		System.out.println("annotations: " + annotations7);
		Assert.assertTrue(annotations6.size() == 0);
		Collection<? extends RefexDynamicChronicleBI<?>> refexes7 = concept7.getRefexesDynamic();
		System.out.println("refexes: " + refexes7);
		//        1 -- [assemblage == concept3, rc = concept7]
		Assert.assertTrue(refexes7.size() == 1);
	}
}
