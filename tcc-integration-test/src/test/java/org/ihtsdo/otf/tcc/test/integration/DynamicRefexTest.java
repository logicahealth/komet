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
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexLong;
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
@BdbTestRunnerConfig(bdbLocation = "test-resources/DynamicRefexTestDB/berkeley-db")
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
	
	//			ConceptSpec c1 = new ConceptSpec("Alcohol + paricalcitol + propylene glycol", UUID.fromString("c7803205-06d8-3c49-a485-0c99ea931450"));
	//			ConceptSpec c2 = new ConceptSpec("Virtual therapeutic moiety simple reference set", UUID.fromString("1a090a21-28c4-3a87-9d04-766f04600494"));
	//
	//			ConceptSpec c3 = new ConceptSpec("Access (valid targets) refset", UUID.fromString("91359120-2b7f-40ad-bc68-5520ed5ae4e0"));
	//			ConceptSpec c4 = new ConceptSpec("Percutaneous approach", UUID.fromString("4d78af43-b095-3232-bf10-2b90a6b60d4d"));
	//			ConceptSpec c5 = new ConceptSpec("Open approach", UUID.fromString("59943708-d6cf-3bfc-b2f5-325a47b40c84"));
	//			ConceptSpec c6 = new ConceptSpec("Closed approach", UUID.fromString("68ce08de-83c2-3ab3-999b-5cd165b29566"));
	//			ConceptSpec c7 = new ConceptSpec("Surgical access values", UUID.fromString("7c9b57f0-6dd6-335b-b1ad-4f812c8ed622"));
	//
	//			concept1 = Ts.get().getConceptVersion(vc, c1.getLenient().getConceptNid()); //annotation
	//			concept2 = Ts.get().getConceptVersion(vc, c2.getLenient().getConceptNid()); //assemblage is -- annotation style refex
	//
	//			concept3 = Ts.get().getConceptVersion(vc, c3.getLenient().getConceptNid()); //assemblage is refset style refex 
	//			concept4 = Ts.get().getConceptVersion(vc, c4.getLenient().getConceptNid()); //refset member
	//			concept5 = Ts.get().getConceptVersion(vc, c5.getLenient().getConceptNid()); //refset member
	//			concept6 = Ts.get().getConceptVersion(vc, c6.getLenient().getConceptNid()); //refset member
	//			concept7 = Ts.get().getConceptVersion(vc, c7.getLenient().getConceptNid()); //refset member
	
	//			//            add refset members
	//			RefexCAB c4annotBp = new RefexCAB(RefexType.MEMBER, concept4.getPrimordialUuid(), concept3.getPrimordialUuid(), IdDirective.GENERATE_REFEX_CONTENT_HASH,
	//					RefexDirective.EXCLUDE);
	//			RefexChronicleBI<?> c4Annot = builder.construct(c4annotBp);
	//			//            NOTE: if adding a refset member, both the referenced component and the assemblage concept must be added as uncommitted
	//			Ts.get().addUncommitted(concept4);
	//			RefexCAB c5annotBp = new RefexCAB(RefexType.MEMBER, concept5.getPrimordialUuid(), concept3.getPrimordialUuid(), IdDirective.GENERATE_REFEX_CONTENT_HASH,
	//					RefexDirective.EXCLUDE);
	//			RefexChronicleBI<?> c5Annot = builder.construct(c5annotBp);
	//			Ts.get().addUncommitted(concept5);
	//			RefexCAB c6annotBp = new RefexCAB(RefexType.MEMBER, concept6.getPrimordialUuid(), concept3.getPrimordialUuid(), IdDirective.GENERATE_REFEX_CONTENT_HASH,
	//					RefexDirective.EXCLUDE);
	//			RefexChronicleBI<?> c6Annot = builder.construct(c6annotBp);
	//			Ts.get().addUncommitted(concept6);
	//			RefexCAB c7annotBp = new RefexCAB(RefexType.MEMBER, concept7.getPrimordialUuid(), concept3.getPrimordialUuid(), IdDirective.GENERATE_REFEX_CONTENT_HASH,
	//					RefexDirective.EXCLUDE);
	//			RefexChronicleBI<?> c7Annot = builder.construct(c7annotBp);
	//			Ts.get().addUncommitted(concept7);
	//
	//			Ts.get().addUncommitted(concept3);
	//			Ts.get().commit();
				
				
				//Setup some stuff for dynamicRefexTesting
				
				ConceptCB cab = createNewConcept("RefexTest - contains Annotation");
				
				//cab.getConceptAttributeAB().addAnnotationBlueprint(new RefexDynamicCAB(cab.getComponentUuid(), UUID.fromString("dfe2c9dd-2da8-3980-879c-518c1a38907f")));
				ConceptChronicleBI newCon = builder.construct(cab);
				Ts.get().addUncommitted(newCon);
				

				RefexDynamicCAB rcab = new RefexDynamicCAB(cab.getComponentUuid(), UUID.fromString("dfe2c9dd-2da8-3980-879c-518c1a38907f"));
				
				RefexDynamicDataBI[] data = new RefexDynamicDataBI[1];
				data[0] = new RefexLong(42, "the answer");
				
				rcab.setData(data);
				RefexDynamicChronicleBI newRefex = builder.construct(rcab);
				Ts.get().addUncommitted(newCon);
				
				//Ts.get().commit();
	
				concept1 = newCon.getVersion(vc);
				
				System.out.println(concept1.toLongString());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Logger.getLogger(DynamicRefexTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private ConceptCB createNewConcept(String name) throws IOException, InvalidCAB, ContradictionException
	{
		String fsn = name;
		String prefTerm = name + " - preferred";
		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
		UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
		UUID parents[] = new UUID[] { UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029") };  //Term Aux Concept

		return new ConceptCB(fsn, prefTerm, lc, isA, idDir, module, parents);
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public void testDynamicRefexMembers()
	{
		System.out.println("hi");
	}

//	@Test
//	public void testRefexMembers() throws IOException
//	{
//
//		System.out.println("** concept 1 ** : " + concept1.toUserString() + " -- annotation style: " + concept1.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers1 = concept1.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers1);
//		//        is annotation member not assemblage concept
//		Assert.assertTrue(refsetMembers1.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> annotations1 = concept1.getAnnotations();
//		System.out.println("annotations: " + annotations1);
//		//??        asssemblage == concept2, rc == concept1 -->why isn't there an annotation for "SNOMED integer id"
//		Assert.assertTrue(annotations1.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes1 = concept1.getRefexes();
//		System.out.println("refexes: " + refexes1);
//		Assert.assertTrue(refexes1.size() == 1); //asssemblage == concept2, rc == concept1
//
//		System.out.println("** concept 2 ** : " + concept2.toUserString() + " -- annotation style: " + concept2.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers2 = concept2.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers2);
//		Assert.assertTrue(refsetMembers1.isEmpty()); //is annotation style refex
//		Collection<? extends RefexChronicleBI<?>> annotations2 = concept2.getAnnotations();
//		System.out.println("annotations: " + annotations2);
//		//??        assemblage == "SNOMED integer id", rc == concept2 --> why is this? long form doesn't show refex
//		Assert.assertTrue(annotations2.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes2 = concept2.getRefexes();
//		System.out.println("refexes: " + refexes2);
//		//        assemblage == "SNOMED integer id", rc == concept2
//		Assert.assertTrue(refexes2.size() == 1);
//
//		System.out.println("** concept 3 ** : " + concept3.toUserString() + " -- annotation style: " + concept3.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers3 = concept3.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers3);
//		//        is refset style refex, 
//		//        1--[assemblage == concept3, rc == concept4]
//		//        2--[assemblage == concept3, rc == concept5]
//		//        3--[assemblage == concept3, rc == concept6]
//		//        4--[assemblage == concept3, rc == concept7]
//		Assert.assertTrue(refsetMembers3.size() == 4);
//		Collection<? extends RefexChronicleBI<?>> annotations3 = concept3.getAnnotations();
//		System.out.println("annotations: " + annotations3);
//		Assert.assertTrue(annotations3.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> refexes3 = concept3.getRefexes();
//		System.out.println("refexes: " + refexes3);
//		//        assemblage == "Access (valid targets) promotion refset", rc == concept3
//		Assert.assertTrue(refexes3.size() == 1);
//
//		System.out.println("** concept 4 ** : " + concept4.toUserString() + " -- annotation style: " + concept4.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers4 = concept4.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers4);
//		//        is refset member not assemblage concept
//		Assert.assertTrue(refsetMembers4.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> annotations4 = concept4.getAnnotations();
//		System.out.println("annotations: " + annotations4);
//		//        assemblage == "SNOMED integer id", rc == concept4
//		Assert.assertTrue(annotations4.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes4 = concept4.getRefexes();
//		System.out.println("refexes: " + refexes4);
//		//        1 -- [assemblage == "SNOMED integer id", rc == concept4] 2 -- [assemblage == concept3, rc = concept4]
//		Assert.assertTrue(refexes4.size() == 2);
//
//		System.out.println("** concept 5 ** : " + concept5.toUserString() + " -- annotation style: " + concept5.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers5 = concept5.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers5);
//		//        is refset member not assemblage concept
//		Assert.assertTrue(refsetMembers5.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> annotations5 = concept5.getAnnotations();
//		System.out.println("annotations: " + annotations5);
//		//        assemblage == "SNOMED integer id", rc == concept5
//		Assert.assertTrue(annotations5.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes5 = concept5.getRefexes();
//		System.out.println("refexes: " + refexes5);
//		//        1 -- [assemblage == "SNOMED integer id", rc == concept5] 2 -- [assemblage == concept3, rc = concept5]
//		Assert.assertTrue(refexes5.size() == 2);
//
//		System.out.println("** concept 6 ** : " + concept6.toUserString() + " -- annotation style: " + concept6.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers6 = concept6.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers6);
//		//       is refset member not assemblage concept
//		Assert.assertTrue(refsetMembers6.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> annotations6 = concept6.getAnnotations();
//		System.out.println("annotations: " + annotations6);
//		//        assemblage == "SNOMED integer id", rc == concept6
//		Assert.assertTrue(annotations6.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes6 = concept6.getRefexes();
//		System.out.println("refexes: " + refexes6);
//		//        1 -- [assemblage == "SNOMED integer id", rc == concept6] 2 -- [assemblage == concept3, rc = concept6]
//		Assert.assertTrue(refexes6.size() == 2);
//
//		System.out.println("** concept 7 ** : " + concept7.toUserString() + " -- annotation style: " + concept7.isAnnotationStyleRefex());
//		Collection<? extends RefexChronicleBI<?>> refsetMembers7 = concept7.getRefsetMembers();
//		System.out.println("refset members: " + refsetMembers7);
//		//       is refset member not assemblage concept
//		Assert.assertTrue(refsetMembers7.isEmpty());
//		Collection<? extends RefexChronicleBI<?>> annotations7 = concept7.getAnnotations();
//		System.out.println("annotations: " + annotations7);
//		//        assemblage == "SNOMED integer id", rc == concept7
//		Assert.assertTrue(annotations6.size() == 1);
//		Collection<? extends RefexChronicleBI<?>> refexes7 = concept7.getRefexes();
//		System.out.println("refexes: " + refexes7);
//		//        1 -- [assemblage == "SNOMED integer id", rc == concept7] 2 -- [assemblage == concept3, rc = concept7]
//		Assert.assertTrue(refexes7.size() == 2);
//	}
}
