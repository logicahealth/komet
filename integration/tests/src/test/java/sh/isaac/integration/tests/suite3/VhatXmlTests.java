package sh.isaac.integration.tests.suite3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.prefs.BackingStoreException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.convert.delta.vhat.VHATDeltaImport;
import sh.isaac.misc.associations.AssociationInstance;
import sh.isaac.misc.associations.AssociationUtilities;
import sh.isaac.misc.constants.VHATConstants;
import sh.isaac.misc.exporters.VetsExporter;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.mojo.LoadTermstore;
import sh.isaac.utility.Frills;

/**
 * 
 */
@HK2("integration")
@Test(suiteName = "suite3")
public class VhatXmlTests
{
	private static final Logger LOG = LogManager.getLogger();

	private boolean debugMode = false;

	private XPath xpath_ = XPathFactory.newInstance().newXPath();

	// xpaths
	private static final String XPATH_DELIMITER = "/";
	private static final String TERMINOLOGY = "Terminology";
	private static final String CODE_SYSTEM = TERMINOLOGY + XPATH_DELIMITER + "CodeSystem";
	private static final String VERSION = CODE_SYSTEM + XPATH_DELIMITER + "Version";
	private static final String CODED_CONCEPT = VERSION + XPATH_DELIMITER + "CodedConcepts/CodedConcept";
	private static final String CONCEPT_PROPERTY = CODED_CONCEPT + XPATH_DELIMITER + "Properties/Property";
	private static final String RELATIONSHIP = CODED_CONCEPT + XPATH_DELIMITER + "/Relationships/Relationship";
	private static final String DESIGNATION = CODED_CONCEPT + XPATH_DELIMITER + "Designations/Designation";
	private static final String DESIGNATION_PROPERTY = DESIGNATION + XPATH_DELIMITER + "Properties/Property";
	private static final String SUBSET_MEMBERSHIP = DESIGNATION + XPATH_DELIMITER + "SubsetMemberships/SubsetMembership";

	@BeforeClass
	public void configure() throws Exception
	{
		LOG.info("Suite 3 setup");
		File db = new File("target/suite3");
		RecursiveDelete.delete(db);
		db.mkdirs();
		Get.configurationService().setDataStoreFolderPath(db.toPath());
		Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
		LookupService.startupIsaac();
		
		LoadTermstore lt = new LoadTermstore(new File("target/suite3data/"), false, false);
		lt.execute();
	}

	@AfterClass
	public void shutdown() throws BackingStoreException
	{
		LOG.info("Suite 4 teardown");
		LookupService.shutdownSystem();
	}

	@Test(groups = { "vhat-xml" })
	public void test_super()
	{
		Path file = Paths.get("src/test/resources/xml/set1/super.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			String types = "/Terminology/Types/Type";
			Assert.assertTrue(validateXPath(doc, "count(" + types + ") = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'PropertyType'" + " and ./Name/text() = 'A0_Hello'" + "]) = 1"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'RelationshipType'" + " and ./Name/text() = 'A0_Garshk'" + "]) = 1"));

			String subsets = "/Terminology/Subsets/Subset";
			Assert.assertTrue(validateXPath(doc, "count(" + subsets + ") = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'A0 Super Subset'"
					+ " and ./VUID/text() = '600015'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "700000", "COW GOES MOO", "700000", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "700001", "Preferred Name", null, "700001", "COW GOES MOO", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "600015", "true"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "A0_Hello", "Hi Yourself", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "A0_Garshk", "4711495", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_super_trim() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set1/super_trim.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
				Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		String types = "/Terminology/Types/Type";
		Assert.assertTrue(validateXPath(doc, "count(" + types + ") = 2"));
		Assert.assertTrue(validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'PropertyType'" + " and ./Name/text() = 'A0_Hello2'" + "]) = 1"));
		Assert.assertTrue(
				validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'RelationshipType'" + " and ./Name/text() = 'A0_Garshk2'" + "]) = 1"));

		String subsets = "/Terminology/Subsets/Subset";
		Assert.assertTrue(validateXPath(doc, "count(" + subsets + ") = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'A0 Super Subset2'"
				+ " and ./VUID/text() = '600016'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateCodeConcept("add", "700002", "COW GOES MOO", "700002", "true").evaluate(doc, XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateDesignation("add", "700003", "Preferred Name", null, "700003", "COW GOES MOO", null, "true").evaluate(doc,
				XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateSubsetMembership(doc, "add", "600016", "true"));

		Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateConceptProperty("add", "A0_Hello2", "Hi Yourself", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateRelationshipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateRelationship("add", "A0_Garshk2", "4711495", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
	}

	@Test(groups = { "vhat-xml" })
	public void test_trim1()
	{
		Path file = Paths.get("src/test/resources/xml/set1/trim_test.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "190", "A0 AAA", "190", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(7).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("add", "191", "Preferred Name", null, "191", "A0 AAA", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "192", "Preferred Name", null, "192", "A0 BBB 2 SPACES IN FRONT", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "193", "Preferred Name", null, "193", "A0 CCC 1 SPACES IN FRONT", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "194", "Preferred Name", null, "194", "A0 DDD 2 SPACES IN BACK", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "195", "Preferred Name", null, "195", "A0 EEE 1 SPACES IN BACK", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "196", "Preferred Name", null, "196", "A0 FFF 2 SPACES  HERE", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "197", "Preferred Name", null, "197", "A0 SUBSET TEST", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(7).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignationProperty("add", "Search_Term", "2 LEADING SPACE", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignationProperty("add", "Search_Term", "1 LEADING SPACE", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "2 LEADING SPACE AND 2 END SPACE", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "1 LEADING SPACE AND 1 END SPACE", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "2 END SPACE", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "1 END SPACE", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "LEADING   SPACE IN MIDDLE", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(5).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4707831", "true"));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4708487", "true"));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4712514", "true"));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4712512", "true"));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4708360", "true"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(4).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "VistA_Combination_Immunization", "2 SPACE FRONT", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "VistA_Combination_Immunization", "1 SPACE FRONT", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "VistA_Combination_Immunization", "1 SPACE BACK", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "VistA_Combination_Immunization", "2 SPACE BACK", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_trim1" })
	public void test_trim2() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set1/trim_test2.xml");
		LOG.info("Testing {}", file.getFileName().toString());
		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateCodeConcept("none", "190", "A0 AAAAA", "190", "true").evaluate(doc, XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateDesignation("update", "191", "Preferred Name", null, "191", "A0 AAAAA", "A0 AAA", "true").evaluate(doc,
				XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateDesignationProperty("update", "Search_Term", "NO SPACE", "2 LEADING SPACE", "true").evaluate(doc,
				XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateConceptProperty("update", "VistA_Combination_Immunization", "2 SPACE BACK", "2 SPACE FRONT", "true")
				.evaluate(doc, XPathConstants.BOOLEAN));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_trim1" }, expectedExceptions = {
			java.io.IOException.class }, expectedExceptionsMessageRegExp = ".* doesn't seem to exist .*")
	public void test_trim3() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/trim_test3.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_trim1" }, expectedExceptions = {
			java.io.IOException.class }, expectedExceptionsMessageRegExp = ".* doesn't seem to exist .*")
	public void test_trim4() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/trim_test4.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" })
	public void test_vuid_autogen() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set1/concept_no_vuid.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), () -> ThreadLocalRandom.current().nextInt(), new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateCodeConcept("add", null, "Concept without VUID", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateDesignation("add", null, "Preferred Name", null, null, "Concept without VUID", null, "true").evaluate(doc,
				XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "4712075", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//VUID");
		NodeList retList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		Set<String> vuids = new HashSet<>();
		Assert.assertTrue(retList.getLength() > 0);
		for (int i = 0; i < retList.getLength(); i++)
		{
			String vuid = retList.item(i).getTextContent();
			if (!vuids.add(vuid))
			{
				Assert.fail("Duplicate vuid: " + vuid);
			}
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate1() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_concept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate2() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_desig.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate3()
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_subset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate4()
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_mapset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate5()
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_mapentry.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is not unique .*")
	public void test_vuid_duplicate6()
	{
		Path file = Paths.get("src/test/resources/xml/set1/vuid_duplicate_mapentry_desig.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".* is already in use")
	public void test_vuid_reuse()
	{
		Path file1 = Paths.get("src/test/resources/xml/set1/vuid_check1.xml");
		Path file2 = Paths.get("src/test/resources/xml/set1/vuid_check2.xml");
		LOG.info("Testing {}", "test_vuid_reuse");

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file1)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			new VHATDeltaImport(new String(Files.readAllBytes(file2)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_AddNewConceptPreAssignedVUID() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set1/AddNewConceptPreAssignedVUID.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter use
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateCodeConcept("add", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateDesignation("add", "-506", "Preferred Name", null, "-506", "AA SQA IMMUN PROCEDURE", null, "true").evaluate(doc,
				XPathConstants.BOOLEAN));

		Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5197590", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported, need to re-index to search
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();
//         
//         StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         LanguageCoordinate defUsLangCoord = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            int cs = Get.specifyingConcept(nid.get()).getNid();
//            Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, activeStampCoord));
//            NidSet css = new NidSet();
//            css.add(cs);
//            
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, activeStampCoord, defUsLangCoord, "AA SQA IMMUN PROCEDURE"));
//               Assert.assertTrue(vuidExistsForConcept((ConceptChronology) cc, activeStampCoord, -506L));
//               Assert.assertTrue(codeExistsForConcept((ConceptChronology) cc, activeStampCoord, -506L));
//               Map<String, Boolean> relMap = relationshipExistsForConcept((ConceptChronology) cc, activeStampCoord, "has_parent", "5197590", "");
//               Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
//               Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
//            }
//         }
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_AddNewConceptPreAssignedVUID" })
	public void test_AddConceptProperties()
	{
		Path file = Paths.get("src/test/resources/xml/set1/AddConceptProperties.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VHAT Exporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateConceptProperty("add", "VistA_Combination_Immunization", "N", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateConceptProperty("add", "VistA_Immunization_Group", "ZOSTER", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         LanguageCoordinate defUsLangCoord = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, activeStampCoord, defUsLangCoord, "AA SQA IMMUN PROCEDURE"));
//               Assert.assertTrue(vuidExistsForConcept((ConceptChronology) cc, activeStampCoord, -506L));
//               Assert.assertTrue(codeExistsForConcept((ConceptChronology) cc, activeStampCoord, -506L));
//               Map<String, Boolean> propMap = propertyExistsForConcept((ConceptChronology) cc, activeStampCoord, "VistA_Combination_Immunization", "N", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//               propMap = propertyExistsForConcept((ConceptChronology) cc, activeStampCoord, "VistA_Immunization_Group", "ZOSTER", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_AddConceptProperties" })
	public void test_CreateNewPropRelationshipTypes()
	{
		Path file = Paths.get("src/test/resources/xml/set1/CreateNewPropRelationshipTypes.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			String types = "/Terminology/Types/Type";
			Assert.assertTrue(validateXPath(doc, "count(" + types + ") = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'PropertyType'" + " and ./Name/text() = 'AAA_VistA_CVX_Code'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + types + "[" + "./Kind/text() = 'RelationshipType'" + " and ./Name/text() = 'aaa_vista_has_newflag'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateCodeConceptCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();

			Assert.assertTrue(propertyTypeExists(activeStampCoord, "AAA_VistA_CVX_Code"));
			Assert.assertTrue(relationshipTypeExists(activeStampCoord, "aaa_vista_has_newflag"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_CreateNewPropRelationshipTypes" })
	public void test_AddDesignationsToConcept()
	{
		Path file = Paths.get("src/test/resources/xml/set1/AddDesignationsToConcept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			// Count all nodes not Properties or Relationships
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "/*[" + "name() != 'Action'" + "and name() != 'Code'" + "and name() != 'Name'"
					+ "and name() != 'VUID'" + "and name() != 'Active'" + "and name() != 'Properties'" + "and name() != 'Relationships'" + "]) = 0"));

			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "AAA_VistA_CVX_Code", "183", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(3).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "aaa_vista_has_newflag", "5197590", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "vista_has_vis", "5198344", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "vista_has_vis", "5198346", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               @SuppressWarnings("rawtypes")
//               ConceptChronology concept = (ConceptChronology) cc;
//               Map<String, Boolean> propMap = this.propertyExistsForConcept(concept, activeStampCoord, "AAA_VistA_CVX_Code", "183", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//               propMap = this.propertyExistsForConcept(concept, activeStampCoord, "VistA_Combination_Immunization", "N", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//               propMap = this.propertyExistsForConcept(concept, activeStampCoord, "VistA_Immunization_Group", "ZOSTER", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//               
//               Map<String, Boolean> relMap = this.relationshipExistsForConcept(concept, activeStampCoord, "aaa_vista_has_newflag", "5197590", "");
//               Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
//               Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
//               relMap = this.relationshipExistsForConcept(concept, activeStampCoord, "vista_has_vis", "5198344", "");
//               Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
//               Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
//               relMap = this.relationshipExistsForConcept(concept, activeStampCoord, "vista_has_vis", "5198346", "");
//               Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
//               Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_AddDesignationsToConcept" })
	public void test_CreateNewSubset()
	{
		Path file = Paths.get("src/test/resources/xml/set1/CreateNewSubset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			String subsets = "/Terminology/Subsets/Subset";
			Assert.assertTrue(validateXPath(doc, "count(" + subsets + ") = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'AAA SQA Laboratory Tests'"
					+ " and ./VUID/text() = '-602'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'AAA SQA New Imm Procedures'"
					+ " and ./VUID/text() = '-601'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateCodeConceptCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();

			Assert.assertTrue(this.subsetExists(activeStampCoord, "AAA SQA New Imm Procedures", -601L));
			Assert.assertTrue(this.subsetExists(activeStampCoord, "AAA SQA Laboratory Tests", -602L));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_CreateNewSubset" })
	public void test_AddDesignationToSubset()
	{
		Path file = Paths.get("src/test/resources/xml/set1/AddDesignationToSubset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("none", "-506", "Preferred Name", null, "-506", null, "AA SQA IMMUN PROCEDURE", "true")
					.evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "-601", "true"));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "-602", "true"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-506L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				boolean found601 = false;
				boolean found602 = false;
				for (Object sc : Get.assemblageService().getSemanticChronologyStreamForComponent(nid.get()).toArray())
				{
					SemanticChronology semantic = (SemanticChronology) sc;
					long vuid = Frills.getVuId(semantic.getAssemblageNid(), activeStampCoord).orElse(0L).longValue();
					if (vuid == -601L)
					{
						found601 = true;
					}
					if (vuid == -602L)
					{
						found602 = true;
					}
				}
				Assert.assertTrue(found601);
				Assert.assertTrue(found602);
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_AddDesignationToSubset" })
	public void test_ChangeDesigProperty()
	{
		Path preFile = Paths.get("src/test/resources/xml/set1/AddDesigProperty.xml");
		Path file = Paths.get("src/test/resources/xml/set1/ChangeDesigProperty.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Preliminary import to setup concept
			new VHATDeltaImport(new String(Files.readAllBytes(preFile)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               Map<String, Boolean> propMap = this.propertyExistsForConcept(concept, stampCoord, "VistA_Combination_Immunization", "N", "");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//            }
//         }

			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("none", "-506", "Preferred Name", null, "-506", null, "AA SQA IMMUN PROCEDURE", "true")
					.evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignationProperty("update", "VistA_Combination_Immunization", "Y", "N", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("update", "aaa_vista_has_newflag", null, "4712477", "true").evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               Map<String, Boolean> propMap = propertyExistsForConcept(concept, stampCoord, "VistA_Combination_Immunization", "Y", "N");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertTrue(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_AddDesignationToSubset" })
	public void test_InactivateConcept()
	{
		Path file = Paths.get("src/test/resources/xml/set1/InactivateConcept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("update", "-505", "AA SQA IMMUN PROCEDURE", "-505", "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();
//         
			StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-505L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				int cs = Get.conceptSpecification(nid.get()).getNid();
				Assert.assertFalse(Get.conceptActiveService().isConceptActive(cs, activeStampCoord));
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_InactivateConcept" })
	public void test_ReactivateConcept()
	{
		Path file = Paths.get("src/test/resources/xml/set1/ReactivateConcept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("update", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();
//         
			StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-505L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				int cs = Get.conceptSpecification(nid.get()).getNid();
				Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, activeStampCoord));
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_ReactivateConcept" })
	public void test_InactivateDesignation()
	{
		Path file = Paths.get("src/test/resources/xml/set1/InactivateDesignation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "-506", "Preferred Name", null, "-506", null, null, "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               Assert.assertTrue(descriptionExists(concept, stampCoord, "Preferred Name", -506L, Status.ACTIVE));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_InactivateDesignation" })
	public void test_ReactivateDesignation()
	{
		Path file = Paths.get("src/test/resources/xml/set1/ReactivateDesignation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "-505", "AA SQA IMMUN PROCEDURE", "-505", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "-506", "Preferred Name", null, "-506", null, null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               // TODO Assert.assertTrue(descriptionExists(concept, stampCoord, "Preferred Name", -506L));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_ReactivateDesignation" })
	public void test_InactivatePropDesig()
	{
		Path preFile = Paths.get("src/test/resources/xml/set1/AddPropDesig.xml");
		Path file = Paths.get("src/test/resources/xml/set1/InactivatePropDesig.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Preliminary import to setup concept
			new VHATDeltaImport(new String(Files.readAllBytes(preFile)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			// TODO
//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               Map<String, Boolean> propMap = propertyExistsForConcept(concept, stampCoord, "AAA_VistA_CVX_Code", "184", "");
//               // TODO Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               // TODO Assert.assertFalse(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//            }
//         }

			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-505'"
					+ " and ./Name/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./VUID/text() = '-505'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + CODED_CONCEPT + "/Designations/Designation[" + "./Action/text() = 'none'" + " and ./Code/text() = '-506'"
							+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '-506'" + " and ./ValueOld/text() = 'AA SQA IMMUN PROCEDURE'"
							+ " and ./Active/text() = 'true']) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + CODED_CONCEPT + "/Designations/Designation/Properties/Property" + "[" + "./Action/text() = 'update'"
							+ " and ./TypeName/text() = 'AAA_VistA_CVX_Code'" + " and ./ValueOld/text() = '184'" + " and ./ValueNew/text() = '184NewValue'"
							+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			// TODO
//         stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
//         
//         nid = Frills.getNidForVUID(-505L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               ConceptChronology concept = (ConceptChronology) cc;
//               // TODO Map<String, Boolean> propMap = propertyExistsForConcept(concept, stampCoord, "AAA_VistA_CVX_Code", "184NewValue", "184");
//               // TODO Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_InactivatePropDesig" })
	public void test_InactivateRelationship()
	{
		Path file = Paths.get("src/test/resources/xml/set1/InactivateRelationship.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-505'"
					+ " and ./Name/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./VUID/text() = '-505'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "/Relationships/Relationship[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'vista_has_vis'" + " and ./OldTargetCode/text() = '5198344'" + " and ./Active/text() = 'false'" + "]) = 1"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-505L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				int cs = Get.conceptSpecification(nid.get()).getNid();
				Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, stampCoord));
				NidSet css = new NidSet();
				css.add(cs);

				for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
				{
					Map<String, Boolean> relMap = relationshipExistsForConcept((ConceptChronology) cc, stampCoord, "vista_has_vis", "", "5198344");
					Assert.assertTrue(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
					Assert.assertFalse(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
				}
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { java.io.IOException.class }, expectedExceptionsMessageRegExp = "Update of subset .*")
	public void test_InactivateSubset() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/InactivateSubset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Updating Subsets is not supported
		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = { java.io.IOException.class }, expectedExceptionsMessageRegExp = "Update of subset .*")
	public void test_ReactivateSubset() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set1/ReactivateSubset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Updating Subsets is not supported
		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_InactivateRelationship" })
	public void test_InactivateSubsetmembership()
	{
		Path file = Paths.get("src/test/resources/xml/set1/InactivateSubsetmembership.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-505'"
					+ " and ./Name/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./VUID/text() = '-505'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-506'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '-506'" + " and ./ValueOld/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./Active/text() = 'true'"
							+ "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '-601'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-505L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				int cs = Get.conceptSpecification(nid.get()).getNid();
				Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, stampCoord));
				NidSet css = new NidSet();
				css.add(cs);

				for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
				{
					boolean exists = subsetMembershipExistsForConcept((ConceptChronology) cc, stampCoord, -601L);
					Assert.assertTrue(exists);
				}
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	// test_InactivateSubsetmembership()
	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_InactivateSubsetmembership" })
	public void test_ReactivateSubsetMembership()
	{
		Path file = Paths.get("src/test/resources/xml/set1/ReactivateSubsetMembership.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter use
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-505'"
					+ " and ./Name/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./VUID/text() = '-505'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '-506'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '-506'" + " and ./ValueOld/text() = 'AA SQA IMMUN PROCEDURE'" + " and ./Active/text() = 'true'"
							+ "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '-601'"
					+ " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

//         String codedConcepts = "/Terminology/CodeSystem/Version/CodedConcepts/CodedConcept";
//         XPathExpression expr = xpath.compile("count(" + codedConcepts + ") = 1");
//         Boolean retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);
//         
//         expr = xpath.compile("count(" + codedConcepts + "["
//               + "./Action/text() = 'none'"
//               + " and ./Code/text() = '-505'"
//               + " and ./Name/text() = 'AA SQA IMMUN PROCEDURE'"
//               + " and ./VUID/text() = '-505'"
//               + " and ./Active/text() = 'true'"
//               + "]) = 1");
//         retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);

//         String designations = codedConcepts + "/Designations/Designation";
//         expr = xpath.compile("count(" + designations + ") = 1");
//         retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);
//         
//         expr = xpath.compile("count(" + designations + "["
//               + "./Action/text() = 'none'"
//               + " and ./Code/text() = '-506'"
//               + " and ./TypeName/text() = 'Preferred Name'"
//               + " and ./VUID/text() = '-506'"
//               + " and ./ValueOld/text() = 'AA SQA IMMUN PROCEDURE'"
//               + " and ./Active/text() = 'true'"
//               + "]) = 1");
//         retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);

//         String subsetMemberships = designations + "/SubsetMemberships/SubsetMembership";
//         expr = xpath.compile("count(" + subsetMemberships + ") = 1");
//         retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);
//         
//         expr = xpath.compile("count(" + subsetMemberships + "["
//               + "./Action/text() = 'update'"
//               + " and ./VUID/text() = '-601'"
//               + " and ./Active/text() = 'true'"
//               + "]) = 1");
//         retval = (Boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
//         Assert.assertTrue(retval);

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
			Optional<Integer> nid = Frills.getNidForVUID(-505L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				int cs = Get.conceptSpecification(nid.get()).getNid();
				Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, stampCoord));
				NidSet css = new NidSet();
				css.add(cs);

				for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
				{
					boolean exists = subsetMembershipExistsForConcept((ConceptChronology) cc, stampCoord, -601L);
					Assert.assertTrue(exists);
				}
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_1() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set2/1.create_new_taxonomy_tree_branch.xml");
		LOG.info("Testing {}", file.getFileName().toString());
		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '400000'"
				+ " and ./Name/text() = 'MIKE LABS'" + " and ./VUID/text() = '400000'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc,
				"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '400001'" + " and ./TypeName/text() = 'Preferred Name'"
						+ " and ./VUID/text() = '400001'" + " and ./ValueNew/text() = 'MIKE LABS'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
				+ " and ./NewTargetCode/text() = '5245593'" + " and ./Active/text() = 'true'" + "]) = 1"));

		StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
		LanguageCoordinate defUsLangCoord = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
		Optional<Integer> nid = Frills.getNidForVUID(400000L);
		Assert.assertTrue(nid.isPresent());

		if (nid.isPresent())
		{
			int cs = Get.conceptSpecification(nid.get()).getNid();
			Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, activeStampCoord));
			NidSet css = new NidSet();
			css.add(cs);

			for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
			{
				Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, activeStampCoord, defUsLangCoord, "MIKE LABS"));
				Assert.assertTrue(vuidExistsForConcept((ConceptChronology) cc, activeStampCoord, 400001L));
				Assert.assertTrue(codeExistsForConcept((ConceptChronology) cc, activeStampCoord, 400001L));
				Map<String, Boolean> relMap = relationshipExistsForConcept((ConceptChronology) cc, activeStampCoord, "has_parent", "5245593", "");
				Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
				Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
			}
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_2() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set2/2.create_new_prop_type_and_values.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();

		Assert.assertFalse(propertyTypeExists(activeStampCoord, "A0_Mike_New_Property"));
		Assert.assertFalse(propertyTypeExists(activeStampCoord, "A0_Mike_Property2"));
		Assert.assertFalse(relationshipTypeExists(activeStampCoord, "A1_Bad_Relationships"));

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(0).evaluate(doc, XPathConstants.BOOLEAN));

		String types = "/Terminology/Types/Type";
		Assert.assertTrue(validateXPath(doc, "count(" + types + ") = 3"));
		Assert.assertTrue(
				validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'PropertyType'" + " and ./Name/text() = 'A0_Mike_New_Property'" + "]) = 1"));
		Assert.assertTrue(
				validateXPath(doc, "count(" + types + "[" + "./Kind/text() = 'PropertyType'" + " and ./Name/text() = 'A0_Mike_Property2'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc,
				"count(" + types + "[" + "./Kind/text() = 'RelationshipType'" + " and ./Name/text() = 'A1_Bad_Relationships'" + "]) = 1"));


		Assert.assertTrue(propertyTypeExists(activeStampCoord, "A0_Mike_New_Property"));
		Assert.assertTrue(propertyTypeExists(activeStampCoord, "A0_Mike_Property2"));
		Assert.assertTrue(relationshipTypeExists(activeStampCoord, "A1_Bad_Relationships"));
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_3() throws XPathExpressionException, FileNotFoundException, IOException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set2/3.create_new_subset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		StampCoordinate activeStampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();

		Assert.assertFalse(subsetExists(activeStampCoord, "A0 Loser Subset Membership", 600001L));
		Assert.assertFalse(subsetExists(activeStampCoord, "A0 Cool Subset Membership", 600000L));

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(0).evaluate(doc, XPathConstants.BOOLEAN));

		String subsets = "/Terminology/Subsets/Subset";
		Assert.assertTrue(validateXPath(doc, "count(" + subsets + ") = 2"));
		Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'A0 Loser Subset Membership'"
				+ " and ./VUID/text() = '600001'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + subsets + "[" + "./Action/text() = 'add'" + " and ./Name/text() = 'A0 Cool Subset Membership'"
				+ " and ./VUID/text() = '600000'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue(subsetExists(activeStampCoord, "A0 Loser Subset Membership", 600001L));
		Assert.assertTrue(subsetExists(activeStampCoord, "A0 Cool Subset Membership", 600000L));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_3" })
	public void test_set2_4() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
	{
		Path file = Paths.get("src/test/resources/xml/set2/4.my_test_1.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));

		// 1
		Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600100'"
				+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true']" + ") = 1"));

		Assert.assertTrue((Boolean) validateDesignationCount(3).evaluate(doc, XPathConstants.BOOLEAN));

		String d1 = DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600101'" + " and ./TypeName/text() = 'Preferred Name'"
				+ " and ./VUID/text() = '600101'" + " and ./ValueNew/text() = 'A1 AAA COOL TEST'" + " and ./Active/text() = 'true'" + "]";
		Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

		Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
		Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'"
				+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueNew/text() = '1234-5'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'"
				+ " and ./TypeName/text() = 'A0_Mike_Property2'" + " and ./ValueNew/text() = 'For Winners'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
				+ " and ./VUID/text() = '600000'" + " and ./Active/text() = 'true'" + "]) = 1"));

		// 2
		String d2 = DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600102'" + " and ./TypeName/text() = 'VistA Name'"
				+ " and ./VUID/text() = '600102'" + " and ./ValueNew/text() = 'A1 AAA LOSER TEST'" + " and ./Active/text() = 'true'" + "]";

		Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

		Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
		Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'"
				+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueNew/text() = '0'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'"
				+ " and ./TypeName/text() = 'A0_Mike_Property2'" + " and ./ValueNew/text() = 'For Losers'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
				+ " and ./VUID/text() = '600001'" + " and ./Active/text() = 'true'" + "]) = 1"));

		// 3
		String d3 = DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600103'" + " and ./TypeName/text() = 'Synonym'"
				+ " and ./VUID/text() = '600103'" + " and ./ValueNew/text() = 'A1 AAA NOTHING TEST'" + " and ./Active/text() = 'true'" + "]";

		Assert.assertTrue(validateXPath(doc, "count(" + d3 + ") = 1"));

		Assert.assertTrue(validateXPath(doc, "count(" + d3 + "/Properties/Property) = 0"));
		Assert.assertTrue(validateXPath(doc, "count(" + d3 + "/SubsetMemberships/SubsetMembership) = 0"));

		// Other
		Assert.assertTrue((Boolean) validateConceptPropertyCount(2).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Scale_Type'"
				+ " and ./ValueNew/text() = 'Qn'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(
				validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'A0_Mike_New_Property'"
						+ " and ./ValueNew/text() = 'Concept Property'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateRelationshipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
				+ " and ./NewTargetCode/text() = '400000'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'A1_Bad_Relationships'"
				+ " and ./NewTargetCode/text() = '4711495'" + " and ./Active/text() = 'true'" + "]) = 1"));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_4" })
	public void test_set2_5()
	{
		Path file = Paths.get("src/test/resources/xml/set2/5.Recipient_1.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600200'"
					+ " and ./Name/text() = 'A1 AAA RECIPIENT'" + " and ./VUID/text() = '600200'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600201'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '600201'" + " and ./ValueNew/text() = 'A1 AAA RECIPIENT'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./NewTargetCode/text() = '400000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();

			Optional<Integer> nid = Frills.getNidForVUID(600200L);
			Assert.assertTrue(nid.isPresent());

			if (nid.isPresent())
			{
				NidSet css = new NidSet();
				css.add(Get.conceptSpecification(nid.get()).getNid());
				for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
				{
					Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, stampCoord, null, "A1 AAA RECIPIENT"));

					Map<String, Boolean> relMap = relationshipExistsForConcept((ConceptChronology) cc, stampCoord, "has_parent", "400000", "");
					Assert.assertTrue(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
					Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
				}
			}
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_4" })
	public void test_set2_6()
	{
		Path file = Paths.get("src/test/resources/xml/set2/6.inactivate_dproperty.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600101'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '600101'" + " and ./ValueOld/text() = 'A1 AAA COOL TEST'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueOld/text() = '1234-5'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(600100L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, stampCoord, null, "A1 AAA COOL TEST"));
//               
//               Map<String, Boolean> relMap = propertyExistsForConcept((ConceptChronology) cc, stampCoord, "VistA_LOINC_Code", "", "1234-5");
//               Assert.assertFalse(relMap.containsKey("NewTargetCode") && relMap.get("NewTargetCode"));
//               Assert.assertFalse(relMap.containsKey("OldTargetCode") && relMap.get("OldTargetCode"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_6" })
	public void test_set2_7()
	{
		Path file = Paths.get("src/test/resources/xml/set2/7.change_dproperty_value.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600101'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '600101'" + " and ./ValueOld/text() = 'A1 AAA COOL TEST'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'A0_Mike_Property2'"
							+ " and ./ValueOld/text() = 'For Winners'" + " and ./ValueNew/text() = 'BOO'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(600100L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            NidSet css = new NidSet();
//            css.add(Get.specifyingConcept(nid.get()).getNid());
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               Assert.assertTrue(descriptionExistsForConcept((ConceptChronology) cc, stampCoord, null, "A1 AAA COOL TEST"));
//               
//               Map<String, Boolean> propMap = propertyExistsForConcept((ConceptChronology) cc, stampCoord, "A0_Mike_Property2", "BOO", "For Winners");
//               Assert.assertTrue(propMap.containsKey("ValueNew") && propMap.get("ValueNew"));
//               Assert.assertTrue(propMap.containsKey("ValueOld") && propMap.get("ValueOld"));
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_7" })
	public void test_set2_8()
	{
		Path file = Paths.get("src/test/resources/xml/set2/8.move_designation_with_props_subs.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Re-index to enable searching for new data
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(2).evaluate(doc, XPathConstants.BOOLEAN));

			// 1
			String c1 = CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'" + " and ./Name/text() = 'A1 AAA COOL TEST'"
					+ " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + ") = 1"));

			String d1 = c1 + "/Designations/Designation[" + "./Action/text() = 'update'" + " and ./Code/text() = '600102'"
					+ " and ./TypeName/text() = 'VistA Name'" + " and ./VUID/text() = '600102'"
					// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
					// + " and ./ValueNew/text() = 'A1 AAA COOL TEST'"
					+ " and ./Active/text() = 'false'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueOld/text() = '0'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'A0_Mike_Property2'" + " and ./ValueOld/text() = 'For Losers'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'update'"
					+ " and ./VUID/text() = '600001'" + " and ./Active/text() = 'false'" + "]) = 1"));

			// 2
			String c2 = CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600200'" + " and ./Name/text() = 'A1 AAA RECIPIENT'"
					+ " and ./VUID/text() = '600200'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + ") = 1"));

			String d2 = c2 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '600102'"
					+ " and ./TypeName/text() = 'VistA Name'" + " and ./VUID/text() = '600102'" + " and ./ValueNew/text() = 'A1 AAA LOSER TEST'"
					+ " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueNew/text() = '0'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'A0_Mike_Property2'" + " and ./ValueNew/text() = 'For Losers'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '600001'" + " and ./Active/text() = 'true'" + "]) = 1"));

			// Other
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			// Validate data in ISAAC
//         StampCoordinate stampCoord = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
//         StampCoordinate activeStampCoord = Get.coordinateFactory().createMasterLatestActiveOnlyStampCoordinate();
//         //LanguageCoordinate defUsLangCoord = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
//         
//         Optional<Integer> nid = Frills.getNidForVUID(600100L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            int cs = Get.specifyingConcept(nid.get()).getNid();
//            Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, stampCoord));
//            NidSet css = new NidSet();
//            css.add(cs);
//            
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               boolean exists = subsetMembershipExistsForConcept((ConceptChronology) cc, stampCoord, 600001L);
//               Assert.assertTrue(exists);
//               exists = subsetMembershipExistsForConcept((ConceptChronology) cc, activeStampCoord, 600001L);
//               Assert.assertFalse(exists);
//            }
//         }
//         
//         nid = Frills.getNidForVUID(600200L);
//         Assert.assertTrue(nid.isPresent());
//         
//         if (nid.isPresent())
//         {
//            int cs = Get.specifyingConcept(nid.get()).getNid();
//            Assert.assertTrue(Get.conceptActiveService().isConceptActive(cs, stampCoord));
//            NidSet css = new NidSet();
//            css.add(cs);
//            
//            for (Object cc : Get.conceptService().getConceptChronologyStream(css).toArray())
//            {
//               boolean exists = subsetMembershipExistsForConcept((ConceptChronology) cc, activeStampCoord, 600001L);
//               // TODO Assert.assertTrue(exists);
//            }
//         }
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}

	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_8" })
	public void test_set2_9()
	{
		Path file = Paths.get("src/test/resources/xml/set2/9.move_designation_basic.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(2).evaluate(doc, XPathConstants.BOOLEAN));

			// 1
			String c1 = CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'" + " and ./Name/text() = 'A1 AAA COOL TEST'"
					+ " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + ") = 1"));

			String d1 = c1 + "/Designations/Designation[" + "./Action/text() = 'update'" + " and ./Code/text() = '600103'"
					+ " and ./TypeName/text() = 'Synonym'" + " and ./VUID/text() = '600103'"
					// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
					// + " and ./ValueNew/text() = 'A1 AAA COOL TEST'"
					+ " and ./Active/text() = 'false'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 0"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 0"));

			// 2
			String c2 = CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600200'" + " and ./Name/text() = 'A1 AAA RECIPIENT'"
					+ " and ./VUID/text() = '600200'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + ") = 1"));

			String d2 = c2 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '600103'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '600103'" + " and ./ValueNew/text() = 'A1 AAA NOTHING TEST'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 0"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 0"));

			// Other
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_9" })
	public void test_set2_10()
	{
		Path file = Paths.get("src/test/resources/xml/set2/10.inactivate_subset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600102'" + " and ./TypeName/text() = 'VistA Name'"
							+ " and ./VUID/text() = '600102'" + " and ./ValueOld/text() = 'A1 AAA LOSER TEST'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '600001'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_10" })
	public void test_set2_11()
	{
		Path file = Paths.get("src/test/resources/xml/set2/11.reactivate_subset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600102'" + " and ./TypeName/text() = 'VistA Name'"
							+ " and ./VUID/text() = '600102'" + " and ./ValueOld/text() = 'A1 AAA LOSER TEST'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '600001'"
					+ " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}

	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_11" })
	public void test_set2_12()
	{
		Path file = Paths.get("src/test/resources/xml/set2/12.inactivate_concept_prop.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueOld/text() = 'Qn'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_12" })
	public void test_set2_13()
	{
		Path file = Paths.get("src/test/resources/xml/set2/13.change_concept_prop_value.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'A0_Mike_New_Property'"
							+ " and ./ValueOld/text() = 'Concept Property'" + " and ./ValueNew/text() = 'Magical Props'" + " and ./Active/text() = 'true'"
							+ "]) = 1"));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_13" })
	public void test_set2_14()
	{
		Path file = Paths.get("src/test/resources/xml/set2/14.inactivate_relationship.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'A1_Bad_Relationships'"
							+ " and ./OldTargetCode/text() = '4711495'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_14" })
	public void test_set2_15()
	{
		Path file = Paths.get("src/test/resources/xml/set2/15.change_relationship_targetcode.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'A1_Bad_Relationships'"
					// This is expected output from the exporter when UPDATE is done
					// + " and ./OldTargetCode/text() = '4711495'"
					// + " and ./NewTargetCode/text() = '5100558'"
							+ " and ./OldTargetCode/text() = '5100558'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_15" })
	public void test_set2_16()
	{
		Path file = Paths.get("src/test/resources/xml/set2/16.reactivate_dproperty.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600101'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '600101'" + " and ./ValueOld/text() = 'A1 AAA COOL TEST'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueOld/text() = '1234-5'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_16" })
	public void test_set2_17()
	{
		Path file = Paths.get("src/test/resources/xml/set2/17.reactivate_concept_prop.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueOld/text() = 'Qn'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_17" })
	public void test_set2_18()
	{
		Path file = Paths.get("src/test/resources/xml/set2/18.reactivate_relationship.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600100'"
					+ " and ./Name/text() = 'A1 AAA COOL TEST'" + " and ./VUID/text() = '600100'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'A1_Bad_Relationships'"
							+ " and ./OldTargetCode/text() = '5100558'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_20()
	{
		Path file = Paths.get("src/test/resources/xml/set2/20.create_concept_for_remove.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(3).evaluate(doc, XPathConstants.BOOLEAN));

			// 1
			String c1 = CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '800000'" + " and ./Name/text() = 'A1 RAD1'"
					+ " and ./VUID/text() = '800000'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + ") = 1"));

			String d1 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800001'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '800001'" + " and ./ValueNew/text() = 'A1 RAD1'"
					+ " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'true'" + "]) = 1"));

			String d2 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800002'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '800002'" + " and ./ValueNew/text() = 'A1 RAD2'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueNew/text() = 'OH'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Time_Aspect'"
					+ " and ./ValueNew/text() = 'Any Time'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Relationships/Relationship) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245614'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));

			// 2
			String c2 = CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '800003'" + " and ./Name/text() = 'A2 RAD1'"
					+ " and ./VUID/text() = '800003'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + ") = 1"));

			d1 = c2 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800004'" + " and ./TypeName/text() = 'Preferred Name'"
					+ " and ./VUID/text() = '800004'" + " and ./ValueNew/text() = 'A2 RAD1'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'true'" + "]) = 1"));

			d2 = c2 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800005'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '800005'" + " and ./ValueNew/text() = 'A2 RAD2'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueNew/text() = 'OH'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Time_Aspect'"
					+ " and ./ValueNew/text() = 'Any Time'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Relationships/Relationship) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245614'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c2 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));

			// 3
			String c3 = CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '800006'" + " and ./Name/text() = 'A3 RAD1'"
					+ " and ./VUID/text() = '800006'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + ") = 1"));

			d1 = c3 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800007'" + " and ./TypeName/text() = 'Preferred Name'"
					+ " and ./VUID/text() = '800007'" + " and ./ValueNew/text() = 'A3 RAD1'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'true'" + "]) = 1"));

			d2 = c3 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '800008'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '800008'" + " and ./ValueNew/text() = 'A3 RAD2'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueNew/text() = 'OH'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Time_Aspect'"
					+ " and ./ValueNew/text() = 'Any Time'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245614'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_20" })
	public void test_set2_21() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set2/21.remove_des_prop.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800000'"
				+ " and ./Name/text() = 'A1 RAD1'" + " and ./VUID/text() = '800000'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc,
				"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800001'" + " and ./TypeName/text() = 'Preferred Name'"
						+ " and ./VUID/text() = '800001'" + " and ./ValueOld/text() = 'A1 RAD1'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc,
				"count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Long_Description'"
						+ " and ./ValueOld/text() = 'This is a really long description'" + " and ./Active/text() = 'false'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_20" })
	public void test_set2_22()
	{
		Path file = Paths.get("src/test/resources/xml/set2/22.remove_concept_prop.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800000'"
					+ " and ./Name/text() = 'A1 RAD1'" + " and ./VUID/text() = '800000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'Scale_Type'"
					+ " and ./ValueOld/text() = 'OH'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_20" })
	public void test_set2_23()
	{
		Path file = Paths.get("src/test/resources/xml/set2/23.remove_relationship.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800000'"
					+ " and ./Name/text() = 'A1 RAD1'" + " and ./VUID/text() = '800000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./OldTargetCode/text() = '5245595'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_20" })
	public void test_set2_24()
	{
		Path file = Paths.get("src/test/resources/xml/set2/24.remove_subset.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800000'"
					+ " and ./Name/text() = 'A1 RAD1'" + " and ./VUID/text() = '800000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '800001'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '800001'" + " and ./ValueOld/text() = 'A1 RAD1'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '5242850'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_20" })
	public void test_set2_25()
	{
		Path file = Paths.get("src/test/resources/xml/set2/25.remove_concept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			// 3
			String c3 = CODED_CONCEPT + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '800006'" + " and ./Name/text() = 'A3 RAD1'"
					+ " and ./VUID/text() = '800006'" + " and ./Active/text() = 'false'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + ") = 1"));

			String d1 = c3 + "/Designations/Designation[" + "./Action/text() = 'update'" + " and ./Code/text() = '800007'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '800007'"
					// This is expected, that the exporter will not output this value.
					// + " and ./ValueNew/text() = 'A3 RAD1'"
					+ " and ./Active/text() = 'false'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueOld/text() = 'This is a really long description'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueOld/text() = 'Short desc'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'update'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'update'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'false'" + "]) = 1"));

			String d2 = c3 + "/Designations/Designation[" + "./Action/text() = 'update'" + " and ./Code/text() = '800008'"
					+ " and ./TypeName/text() = 'Synonym'" + " and ./VUID/text() = '800008'"
					// This is expected, that the exporter will not output this value.
					// + " and ./ValueOld/text() = 'A3 RAD2'"
					+ " and ./Active/text() = 'false'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueOld/text() = 'This is a really long description'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueOld/text() = 'Short desc'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'update'"
					+ " and ./VUID/text() = '5242851'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'Scale_Type'" + " and ./ValueOld/text() = 'OH'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Properties/Property[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'Time_Aspect'" + " and ./ValueOld/text() = 'Any Time'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship) = 2"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./OldTargetCode/text() = '5245614'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c3 + "/Relationships/Relationship[" + "./Action/text() = 'update'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./OldTargetCode/text() = '5245595'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_26()
	{
		Path file = Paths.get("src/test/resources/xml/set2/26. new_concept_for_remove_designation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			String c1 = CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '810000'" + " and ./Name/text() = 'A1 HAS EVERYTHING'"
					+ " and ./VUID/text() = '810000'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + ") = 1"));

			String d1 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '810001'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '810001'" + " and ./ValueNew/text() = 'A1 HAS EVERYTHING'"
					+ " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d1 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d1 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'true'" + "]) = 1"));

			String d2 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '810002'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '810002'" + " and ./ValueNew/text() = 'A1 HAS PROPERTIES ONLY'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d2 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/Properties/Property) = 2"));
			Assert.assertTrue(
					validateXPath(doc, "count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueNew/text() = 'This is a really long description'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + d2 + "/Properties/Property[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueNew/text() = 'Short desc'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d2 + "/SubsetMemberships/SubsetMembership) = 0"));

			String d3 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '810003'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '810003'" + " and ./ValueNew/text() = 'A1 HAS SUBSET ONLY'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d3 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d3 + "/Properties/Property) = 0"));

			Assert.assertTrue(validateXPath(doc, "count(" + d3 + "/SubsetMemberships/SubsetMembership) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + d3 + "/SubsetMemberships/SubsetMembership[" + "./Action/text() = 'add'"
					+ " and ./VUID/text() = '5242850'" + " and ./Active/text() = 'true'" + "]) = 1"));

			String d4 = c1 + "/Designations/Designation[" + "./Action/text() = 'add'" + " and ./Code/text() = '810004'" + " and ./TypeName/text() = 'Synonym'"
					+ " and ./VUID/text() = '810004'" + " and ./ValueNew/text() = 'A1 HAS NOTHING BASIC'" + " and ./Active/text() = 'true'" + "]";
			Assert.assertTrue(validateXPath(doc, "count(" + d4 + ") = 1"));

			Assert.assertTrue(validateXPath(doc, "count(" + d4 + "/Properties/Property) = 0"));

			Assert.assertTrue(validateXPath(doc, "count(" + d4 + "/SubsetMemberships/SubsetMembership) = 0"));

			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Relationships/Relationship) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + c1 + "/Relationships/Relationship[" + "./Action/text() = 'add'"
					+ " and ./TypeName/text() = 'has_parent'" + " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_26" })
	public void test_set2_27_1()
	{
		Path file = Paths.get("src/test/resources/xml/set2/27-1.remove_designation1.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '810000'"
					+ " and ./Name/text() = 'A1 HAS EVERYTHING'" + " and ./VUID/text() = '810000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '810001'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '810001'"
					// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
					// + " and ./ValueNew/text() = 'A1 HAS EVERYTHING'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueOld/text() = 'This is a really long description'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueOld/text() = 'Short desc'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '5242850'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_26" })
	public void test_set2_27_2()
	{
		Path file = Paths.get("src/test/resources/xml/set2/27-2.remove_designation2.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '810000'"
					+ " and ./Name/text() = 'A1 HAS EVERYTHING'" + " and ./VUID/text() = '810000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '810002'"
					+ " and ./TypeName/text() = 'Synonym'" + " and ./VUID/text() = '810002'"
					// This is expected, that the exporter will not output this value. Should be ValueOld in input?
					// + " and ./ValueNew/text() = 'A1 HAS PROPERTIES ONLY'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Long_Description'"
							+ " and ./ValueOld/text() = 'This is a really long description'" + " and ./Active/text() = 'false'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'VistA_Short_Description'"
							+ " and ./ValueOld/text() = 'Short desc'" + " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_26" })
	public void test_set2_27_3()
	{
		Path file = Paths.get("src/test/resources/xml/set2/27-3.remove_designation3.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '810000'"
					+ " and ./Name/text() = 'A1 HAS EVERYTHING'" + " and ./VUID/text() = '810000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '810003'"
					+ " and ./TypeName/text() = 'Synonym'" + " and ./VUID/text() = '810003'"
					// This is expected, that the exporter will not output this value. Should be ValueOld in input?
					// + " and ./ValueNew/text() = 'A1 HAS PROPERTIES ONLY'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '5242850'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_26" })
	public void test_set2_27_4()
	{
		Path file = Paths.get("src/test/resources/xml/set2/27-4.remove_designation4.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '810000'"
					+ " and ./Name/text() = 'A1 HAS EVERYTHING'" + " and ./VUID/text() = '810000'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '810004'"
					+ " and ./TypeName/text() = 'Synonym'" + " and ./VUID/text() = '810004'"
					// This is expected, that the exporter will not output this value. Should be ValueOld in input?
					// + " and ./ValueNew/text() = 'A1 HAS PROPERTIES ONLY'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_27_4" })
	public void test_set2_27_5()
	{
		Path file = Paths.get("src/test/resources/xml/set2/27-5.reactivate_designation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "810000", "A1 HAS EVERYTHING", "810000", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "810003", "Synonym", null, "810003", null, null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = {
			RuntimeException.class }, expectedExceptionsMessageRegExp = ".* extended designations types aren't supported .*")
	public void test_set2_30()
	{
		Path file = Paths.get("src/test/resources/xml/set2/30.create_new_extended_description.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_40()
	{
		Path file = Paths.get("src/test/resources/xml/set2/40.create_concept_inactivate_designation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '5001'"
					+ " and ./Name/text() = 'A1 INACTIVATE ME'" + " and ./VUID/text() = '5001'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '5002'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '5002'" + " and ./ValueNew/text() = 'A1 INACTIVATE ME'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Search_Term'"
					+ " and ./ValueNew/text() = 'COMPLEX'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'add'" + " and ./VUID/text() = '4707831'"
					+ " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'VistA_LOINC_Code'"
					+ " and ./ValueNew/text() = '1234-5'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./NewTargetCode/text() = '4712075'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_40" })
	public void test_set2_41()
	{
		Path file = Paths.get("src/test/resources/xml/set2/41.inactivate_designation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '5001'"
					+ " and ./Name/text() = 'A1 INACTIVATE ME'" + " and ./VUID/text() = '5001'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '5002'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '5002'"
					// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
					// + " and ./ValueNew/text() = 'A1 INACTIVATE ME'"
					+ " and ./Active/text() = 'false'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_41" })
	public void test_set2_42()
	{
		Path file = Paths.get("src/test/resources/xml/set2/42.reactivate_designation.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '5001'"
					+ " and ./Name/text() = 'A1 INACTIVATE ME'" + " and ./VUID/text() = '5001'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '5002'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '5002'"
					// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
					// + " and ./ValueNew/text() = 'A1 INACTIVATE ME'"
					+ " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_50()
	{
		Path file = Paths.get("src/test/resources/xml/set2/50.create_concept_change_des_name.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "601000", "A1 HELLO NEILL", "601000", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "601001", "Preferred Name", null, "601001", "A1 HELLO NEILL", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_50" })
	public void test_set2_51()
	{
		Path file = Paths.get("src/test/resources/xml/set2/51.change_des_name.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "601000", "A1 BYE BYE NEILL", "601000", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("update", "601001", "Preferred Name", null, "601001", "A1 BYE BYE NEILL", "A1 HELLO NEILL", "true")
					.evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_51" })
	public void test_set2_52()
	{
		Path file = Paths.get("src/test/resources/xml/set2/52.change_des_type.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "601000", "A1 BYE BYE NEILL", "601000", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			// The exporter won't print ValueOld or ValueNew if the values are the same, by design
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "601001", "VistA Name", null, "601001", null, null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_55()
	{
		Path file = Paths.get("src/test/resources/xml/set2/55.concept_create_for_des_change.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '601010'"
					+ " and ./Name/text() = 'A1 CHANGE ME'" + " and ./VUID/text() = '601010'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '601011'" + " and ./TypeName/text() = 'VistA Name'"
							+ " and ./VUID/text() = '601011'" + " and ./ValueNew/text() = 'A1 CHANGE ME'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_55" })
	public void test_set2_56()
	{
		Path file = Paths.get("src/test/resources/xml/set2/56.change_des_type.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '601010'"
					+ " and ./Name/text() = 'A1 CHANGE ME'" + " and ./VUID/text() = '601010'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '601011'"
					+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '601011'"
					// This is expected, that the exporter will not output this value.
					// + " and ./ValueNew/text() = 'A1 CHANGE ME'"
					+ " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, expectedExceptions = {
			RuntimeException.class }, expectedExceptionsMessageRegExp = ".* extended designations types aren't supported .*")
	public void test_set2_60()
	{
		Path file = Paths.get("src/test/resources/xml/set2/60.create_designation_type.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
		}
		catch (IOException e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_60" }, expectedExceptions = {
			java.io.IOException.class }, expectedExceptionsMessageRegExp = "Unexpected TypeName .*")
	public void test_set2_61() throws IOException
	{
		Path file = Paths.get("src/test/resources/xml/set2/61.create_concept_new_des_type.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_70()
	{
		Path file = Paths.get("src/test/resources/xml/set2/70. create_concept_unretire.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "333", "A1 UNRETIRE ME", "333", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "334", "Preferred Name", null, "334", "A1 UNRETIRE ME", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "Search_Term", "COMPLEX", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4707831", "true"));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "4712075", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_70" })
	public void test_set2_71()
	{
		Path file = Paths.get("src/test/resources/xml/set2/71. remove_des.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "333", "A1 I AM BACK", "333", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("update", "334", "Preferred Name", null, "334", "A1 I AM BACK", "A1 UNRETIRE ME", "false")
					.evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("update", "Search_Term", null, "COMPLEX", "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "update", "4707831", "false"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_100() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException
	{
		Path file = Paths.get("src/test/resources/xml/set2/100. base xml file.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600702'"
				+ " and ./Name/text() = 'A1 ARGH'" + " and ./VUID/text() = '600702'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc,
				"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600703'" + " and ./TypeName/text() = 'Preferred Name'"
						+ " and ./VUID/text() = '600703'" + " and ./ValueNew/text() = 'A1 ARGH'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'add'"
				+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueNew/text() = '1234-5'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'add'" + " and ./VUID/text() = '5242850'"
				+ " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'Scale_Type'"
				+ " and ./ValueNew/text() = 'Qn'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateRelationshipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
				+ " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
		Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
				+ " and ./NewTargetCode/text() = '5245614'" + " and ./Active/text() = 'true'" + "]) = 1"));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_100" })
	public void test_set2_102() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
	{
		Path file = Paths.get("src/test/resources/xml/set2/102.inactivate.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		// For VetsExporter
		long now = System.currentTimeMillis();

		// Test VHAT Delta Importer
		new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
				TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Run VetsExporter
		VetsExporter ve = new VetsExporter();
		ve.export(baos, now, Long.MAX_VALUE, false);

		// Local testing, to view output
		if (debugMode)
		{
			try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
			{
				fos.write(baos.toByteArray());
			}
		}

		// Validate export
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600702'"
				+ " and ./Name/text() = 'A1 ARGH'" + " and ./VUID/text() = '600702'" + " and ./Active/text() = 'true'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION + "[" + "./Action/text() = 'update'" + " and ./Code/text() = '600703'"
				+ " and ./TypeName/text() = 'Preferred Name'" + " and ./VUID/text() = '600703'"
				// This is expected, that the exporter will not output this value. Should be ValueOld, perhaps?
				// + " and ./ValueNew/text() = 'A1 ARGH'"
				+ " and ./Active/text() = 'false'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + DESIGNATION_PROPERTY + "[" + "./Action/text() = 'update'"
				+ " and ./TypeName/text() = 'VistA_LOINC_Code'" + " and ./ValueOld/text() = '1234-5'" + " and ./Active/text() = 'false'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + SUBSET_MEMBERSHIP + "[" + "./Action/text() = 'update'" + " and ./VUID/text() = '5242850'"
				+ " and ./Active/text() = 'false'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
		Assert.assertTrue(validateXPath(doc, "count(" + CONCEPT_PROPERTY + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'Scale_Type'"
				+ " and ./ValueOld/text() = 'Qn'" + " and ./Active/text() = 'false'" + "]) = 1"));

		Assert.assertTrue((Boolean) validateRelationshipCount(0).evaluate(doc, XPathConstants.BOOLEAN));
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_100" })
	public void test_set2_103()
	{
		Path file = Paths.get("src/test/resources/xml/set2/103.inactivate_relationship.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600702'"
					+ " and ./Name/text() = 'A1 ARGH'" + " and ./VUID/text() = '600702'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./OldTargetCode/text() = '5245614'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set2_200()
	{
		Path file = Paths.get("src/test/resources/xml/set2/200.base xml file.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600800'"
					+ " and ./Name/text() = 'A1 HAS PARENT ISSUE'" + " and ./VUID/text() = '600800'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc,
					"count(" + DESIGNATION + "[" + "./Action/text() = 'add'" + " and ./Code/text() = '600801'" + " and ./TypeName/text() = 'Preferred Name'"
							+ " and ./VUID/text() = '600801'" + " and ./ValueNew/text() = 'A1 HAS PARENT ISSUE'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./NewTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'add'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./NewTargetCode/text() = '5245614'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_200" })
	public void test_set2_201()
	{
		Path file = Paths.get("src/test/resources/xml/set2/201.inactivate_rel.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600800'"
					+ " and ./Name/text() = 'A1 HAS PARENT ISSUE'" + " and ./VUID/text() = '600800'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./OldTargetCode/text() = '5245595'" + " and ./Active/text() = 'false'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set2_201" })
	public void test_set2_202()
	{
		Path file = Paths.get("src/test/resources/xml/set2/202.reactivate_rel.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + CODED_CONCEPT + "[" + "./Action/text() = 'none'" + " and ./Code/text() = '600800'"
					+ " and ./Name/text() = 'A1 HAS PARENT ISSUE'" + " and ./VUID/text() = '600800'" + " and ./Active/text() = 'true'" + "]) = 1"));

			Assert.assertTrue((Boolean) validateDesignationCount(0).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptPropertyCount(0).evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateXPath(doc, "count(" + RELATIONSHIP + "[" + "./Action/text() = 'update'" + " and ./TypeName/text() = 'has_parent'"
					+ " and ./OldTargetCode/text() = '5245595'" + " and ./Active/text() = 'true'" + "]) = 1"));
		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" })
	public void test_set3_001()
	{
		Path file = Paths.get("src/test/resources/xml/set3/1.add.donor.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "900550", "A1 DONOR", "900550", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "900551", "Preferred Name", null, "900551", "A1 DONOR", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "900552", "Synonym", null, "900552", "A1 DONOR SYNONYM", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "VistA_LOINC_Code", "1234-5", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateSubsetMembershipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4708610", "true"));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("add", "Scale_Type", "Qn", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set3_001" })
	public void test_set3_002()
	{
		Path file = Paths.get("src/test/resources/xml/set3/2.add.recipient.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("add", "900553", "A1 RECIPIENT", "900553", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "900554", "Preferred Name", null, "900554", "A1 RECIPIENT", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("add", "has_parent", "5245595", null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set3_002" })
	public void test_set3_003()
	{
		Path file = Paths.get("src/test/resources/xml/set3/3.move.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			// counts
			Assert.assertTrue((Boolean) validateCodeConceptCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateSubsetMembershipCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationPropertyCount(2).evaluate(doc, XPathConstants.BOOLEAN));

			// 1st coded concept
			Assert.assertTrue((Boolean) validateCodeConcept("none", "900550", "A1 DONOR", "900550", "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "900551", null, null, "900551", null, null, "false").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignationProperty("update", "VistA_LOINC_Code", null, "1234-5", "false").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "update", "4708610", "false"));

			// 2nd coded concept
			Assert.assertTrue((Boolean) validateCodeConcept("none", "900553", "A1 RECIPIENT", "900553", "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignation("add", "900551", "Preferred Name", null, "900551", "A1 DONOR", null, "true").evaluate(doc,
					XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateDesignationProperty("add", "VistA_LOINC_Code", "1234-5", null, "true").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(validateSubsetMembership(doc, "add", "4708610", "true"));

		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set3_003" })
	public void test_set3_004()
	{
		Path file = Paths.get("src/test/resources/xml/set3/4.reactivate_des.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("none", "900550", "A1 DONOR", "900550", "true").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "900551", null, null, "900551", null, null, "true").evaluate(doc, XPathConstants.BOOLEAN));

		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	@Test(groups = { "vhat-xml" }, dependsOnMethods = { "test_set3_004" })
	public void test_set3_005()
	{
		Path file = Paths.get("src/test/resources/xml/set3/5.remove_concept.xml");
		LOG.info("Testing {}", file.getFileName().toString());

		try
		{
			// For VetsExporter
			long now = System.currentTimeMillis();

			// Test VHAT Delta Importer
			new VHATDeltaImport(new String(Files.readAllBytes(file)), TermAux.USER.getPrimordialUuid(), 
					Get.identifierService().getUuidPrimordialForNid(Frills.createAndGetDefaultEditModule(TermAux.VHAT_MODULES.getNid())),
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), null, new File("target"));

			// Test concepts imported
//         Get.startIndexTask((Class<IndexServiceBI>[])null).get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Run VetsExporter
			VetsExporter ve = new VetsExporter();
			ve.export(baos, now, Long.MAX_VALUE, false);

			// Local testing, to view output
			if (debugMode)
			{
				try (OutputStream fos = new FileOutputStream("target/output-" + file.getFileName().toString()))
				{
					fos.write(baos.toByteArray());
				}
			}

			// Validate export
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
			// XPath xpath_ = XPathFactory.newInstance().newXPath();

			Assert.assertTrue((Boolean) validateCodeConceptCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateCodeConcept("update", "900550", "A1 DONOR", null, "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateDesignationCount(2).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "900551", null, null, "900551", null, null, "false").evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue(
					(Boolean) validateDesignation("update", "900552", null, null, "900552", null, null, "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateConceptPropertyCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateConceptProperty("update", "Scale_Type", null, "Qn", "false").evaluate(doc, XPathConstants.BOOLEAN));

			Assert.assertTrue((Boolean) validateRelationshipCount(1).evaluate(doc, XPathConstants.BOOLEAN));
			Assert.assertTrue((Boolean) validateRelationship("update", "has_parent", null, "5245595", "false").evaluate(doc, XPathConstants.BOOLEAN));

		}
		catch (Exception e)
		{
			Assert.fail(e.getClass().getName(), e);
		}
	}

	private String getCodeFromNid(int componentNid, StampCoordinate stamp)
	{

		Optional<SemanticChronology> sc = Get.assemblageService()
				.getSemanticChronologyStreamForComponentFromAssemblage(componentNid, MetaData.CODE____SOLOR.getNid()).findFirst();
		if (sc.isPresent())
		{
			// There was a bug in the older terminology loaders which loaded 'Code' as a static semantic, but marked it as a dynamic semantic.
			// So during edits, new entries would get saves as dynamic semantics, while old entries were static. Handle either....

			if (sc.get().getVersionType() == VersionType.STRING)
			{
				LatestVersion<StringVersion> sv = sc.get().getLatestVersion(stamp);
				if (sv.isPresent())
				{
					return sv.get().getString();
				}
			}
			else if (sc.get().getVersionType() == VersionType.DYNAMIC)  // this path will become dead code, after the data is fixed.
			{
				LatestVersion<DynamicVersion> sv = sc.get().getLatestVersion(stamp);
				if (sv.isPresent())
				{
					if (sv.get().getData() != null && sv.get().getData().length == 1)
					{
						return sv.get().getData()[0].dataToString();
					}
				}
			}

		}
		return null;
	}

	private String getPreferredNameDescriptionType(int conceptNid, StampCoordinate stamp)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		ArrayList<String> descriptions = new ArrayList<>(1);
		ArrayList<String> inActiveDescriptions = new ArrayList<>(1);
		Get.assemblageService().getDescriptionsForComponent(conceptNid).forEach(semanticChronology -> {
			LatestVersion<DescriptionVersion> latestVersion = semanticChronology.getLatestVersion(defStamp);
			if (latestVersion.isPresent() && VHATConstants.VHAT_PREFERRED_NAME.getPrimordialUuid()
					.equals(Frills.getDescriptionExtendedTypeConcept(defStamp, semanticChronology.getNid(), true).orElse(null)))
			{
				if (latestVersion.get().getStatus() == Status.ACTIVE)
				{
					descriptions.add(latestVersion.get().getText());
				}
				else
				{
					inActiveDescriptions.add(latestVersion.get().getText());
				}
			}
		});

		if (descriptions.size() == 0)
		{
			descriptions.addAll(inActiveDescriptions);
		}
		if (descriptions.size() == 0)
		{
			// This doesn't happen for concept that represent subsets, for example.
			String description = Frills.getDescription(conceptNid, defStamp, LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate())
					.orElse("ERROR!");
			return description;
		}
		return descriptions.get(0);
	}

	private boolean vuidExistsForConcept(ConceptChronology concept, StampCoordinate stamp, long vuidToCheck)
	{
		boolean foundVuid = false;
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		for (Object semanticObj : Get.assemblageService().getSemanticChronologyStreamForComponent(concept.getNid()).toArray())
		{
			SemanticChronology semantic = (SemanticChronology) semanticObj;
			if (semantic.getVersionType() == VersionType.DESCRIPTION)
			{
				LatestVersion<DescriptionVersion> descriptionVersion = semantic.getLatestVersion(defStamp);
				if (descriptionVersion.isPresent())
				{
					List<DescriptionVersion> coll = semantic.getVisibleOrderedVersionList(defStamp);
					Collections.reverse(coll);
					for (DescriptionVersion s : coll)
					{
						Optional<Long> v = Frills.getVuId(s.getNid(), defStamp);
						if (v.isPresent() && v.get().equals(vuidToCheck))
						{
							foundVuid = true;
						}
					}
				}
			}
		}
		return foundVuid;
	}

	private boolean codeExistsForConcept(ConceptChronology concept, StampCoordinate stamp, long codeToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		for (Object semanticObj : Get.assemblageService().getSemanticChronologyStreamForComponent(concept.getNid()).toArray())
		{
			SemanticChronology semantic = (SemanticChronology) semanticObj;
			String code = getCodeFromNid(semantic.getNid(), defStamp);

			if (code != null)
			{
				return code.equals(codeToCheck + "");
			}
		}
		return false;
	}

	private boolean descriptionExistsForConcept(ConceptChronology concept, StampCoordinate stamp, LanguageCoordinate lang, String descToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		final LanguageCoordinate defLang = (lang == null) ? Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate() : lang;

		boolean foundDesc = false;

		Optional<String> description = Frills.getDescription(concept.getNid(), defStamp, defLang);
		if (description.isPresent())
		{
			foundDesc = description.get().trim().equalsIgnoreCase(descToCheck.trim());
		}

		return foundDesc;
	}

	private Map<String, Boolean> relationshipExistsForConcept(ConceptChronology concept, StampCoordinate stamp, String tpyeNameToCheck,
			String newTargetCodeToCheck, String oldTargetCodeToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		final Map<String, Boolean> relMap = new HashMap<>();

		for (AssociationInstance ai : AssociationUtilities.getSourceAssociations(concept.getNid(), defStamp))
		{
			if (!ai.getTargetComponent().isPresent())
			{
				continue;
			}

			if (!ai.getAssociationType().getAssociationName().equals(tpyeNameToCheck))
			{
				continue;
			}

			SemanticChronology sc = ai.getData().getChronology();

			String newTargetCode = null;
			String oldTargetCode = null;
			if (ai.getTargetComponent().isPresent())
			{
				newTargetCode = getCodeFromNid(Get.identifierService().getNidForUuids(ai.getTargetComponent().get().getPrimordialUuid()), defStamp);
				if (newTargetCode != null && !newTargetCode.isEmpty() && newTargetCode.equals(newTargetCodeToCheck))
				{
					relMap.put("NewTargetCode", true);
				}
			}

			// Get the old target value
			List<DynamicVersion> coll = ((SemanticChronology) sc).getVisibleOrderedVersionList(defStamp);
			Collections.reverse(coll);
			for (DynamicVersion s : coll)
			{
				AssociationInstance assocInst = AssociationInstance.read(s, null);
				oldTargetCode = getCodeFromNid(Get.identifierService().getNidForUuids(assocInst.getTargetComponent().get().getPrimordialUuid()), defStamp);
				if (oldTargetCode != null && !oldTargetCode.isEmpty() && oldTargetCode.equals(oldTargetCodeToCheck))
				{
					relMap.put("OldTargetCode", true);
				}
			}
		}

		return relMap;
	}

	// TODO
	/*
	 * private Map<String, Boolean> propertyExistsForConcept(ConceptChronology concept, StampCoordinate stamp, String propTypeName, String
	 * valueNewToCheck, String valueOldToCheck)
	 * {
	 * final StampCoordinate defStamp = (stamp == null)
	 * ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate()
	 * : stamp;
	 * 
	 * final Map<String, Boolean> propMap = new HashMap<>();
	 * 
	 * for (Object semanticObj : Get.assemblageService().getSemanticsForComponent(concept.getNid()).toArray())
	 * {
	 * SemanticChronology semantic = (SemanticChronology) semanticObj;
	 * 
	 * if (!getPreferredNameDescriptionType(Get.identifierService().getConceptNid(semantic.getAssemblageNid()), defStamp).equals(propTypeName))
	 * {
	 * continue;
	 * }
	 * 
	 * String newValue = null;
	 * String oldValue = null;
	 * if (semantic.getVersionType() == VersionType.DYNAMIC)
	 * {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" })
	 * Optional<LatestVersion<DynamicSemantic<?>>> semanticVersion = ((SemanticChronology)semantic).getLatestVersion(DynamicSemantic.class, defStamp);
	 * if (semanticVersion.isPresent() && semanticVersion.get().value().getDynamicSemanticUsageDescription().getColumnInfo().length == 1 &&
	 * semanticVersion.get().value().getDynamicSemanticUsageDescription().getColumnInfo()[0].getColumnDataType() == DynamicSemanticDataType.STRING)
	 * {
	 * newValue = semanticVersion.get().value().getData()[0] == null ? null : semanticVersion.get().value().getData()[0].dataToString();
	 * if (newValue != null && newValue.equals(valueNewToCheck))
	 * {
	 * propMap.put("ValueNew", true);
	 * }
	 * 
	 * if (!valueOldToCheck.isEmpty())
	 * {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" })
	 * List<DynamicSemantic<?>> coll = ((SemanticChronology) semantic).getVisibleOrderedVersionList(defStamp);
	 * Collections.reverse(coll);
	 * for(DynamicSemantic<?> s : coll)
	 * {
	 * if (s.getData()[0] != null && s.getData()[0].dataToString().equals(valueOldToCheck))
	 * {
	 * propMap.put("ValueOld", true);
	 * break;
	 * }
	 * }
	 * }
	 * }
	 * }
	 * else if (semantic.getVersionType() == VersionType.STRING)
	 * {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" })
	 * Optional<LatestVersion<? extends StringSemantic>> semanticVersion = ((SemanticChronology) semantic).getLatestVersion(StringSemantic.class,
	 * defStamp);
	 * if (semanticVersion.isPresent())
	 * {
	 * newValue = semanticVersion.get().value().getString();
	 * if (newValue != null && newValue.equals(valueNewToCheck))
	 * {
	 * propMap.put("ValueNew", true);
	 * }
	 * 
	 * if (!valueOldToCheck.isEmpty())
	 * {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" })
	 * List<StringSemantic<?>> coll = ((SemanticChronology) semantic).getVisibleOrderedVersionList(defStamp);
	 * Collections.reverse(coll);
	 * for(StringSemantic<?> s : coll)
	 * {
	 * if (s.getString() != null && s.getString().equals(valueOldToCheck))
	 * {
	 * propMap.put("ValueOld", true);
	 * break;
	 * }
	 * }
	 * }
	 * }
	 * }
	 * }
	 * 
	 * return propMap;
	 * }
	 */

	private boolean relationshipTypeExists(StampCoordinate stamp, String typeToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		for (int conceptId : Get.taxonomyService().getSnapshot(new ManifoldCoordinateImpl(defStamp, null))
				.getTaxonomyChildConceptNids(VHATConstants.VHAT_ASSOCIATION_TYPES.getNid()))
		{
			ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
			String prefName = getPreferredNameDescriptionType(concept.getNid(), defStamp);
			if (prefName.equals(typeToCheck))
			{
				return true;
			}
		}

		return false;
	}

	private boolean propertyTypeExists(StampCoordinate stamp, String typeToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		for (int conceptId : Get.taxonomyService().getSnapshot(new ManifoldCoordinateImpl(defStamp, null))
				.getTaxonomyChildConceptNids(VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid()))
		{
			ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
			String prefName = getPreferredNameDescriptionType(concept.getNid(), defStamp);
			if (prefName.equals(typeToCheck))
			{
				return true;
			}
		}

		return false;
	}

	// TODO
	/*
	 * private boolean designationTypeExists(StampCoordinate stamp, String typeToCheck)
	 * {
	 * final StampCoordinate defStamp = (stamp == null)
	 * ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate()
	 * : stamp;
	 * 
	 * for (int conceptId : Get.taxonomyService().getAllRelationshipOriginSequences(
	 * VHATConstants.VHAT_DESCRIPTION_TYPES.getNid()).toArray())
	 * {
	 * ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService().getConcept(conceptId);
	 * String prefName = getPreferredNameDescriptionType(concept.getNid(), defStamp);
	 * if (prefName.equals(typeToCheck))
	 * {
	 * return true;
	 * }
	 * }
	 * 
	 * return false;
	 * }
	 */

	private boolean subsetExists(StampCoordinate stamp, String subsetToCheck, Long subsetVuidToCheck)
	{
		final StampCoordinate defStamp = (stamp == null) ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate() : stamp;

		for (int conceptId : Get.taxonomyService().getSnapshot(new ManifoldCoordinateImpl(defStamp, null))
				.getTaxonomyChildConceptNids(VHATConstants.VHAT_REFSETS.getNid()))
		{
			ConceptChronology concept = Get.conceptService().getConceptChronology(conceptId);
			String prefName = getPreferredNameDescriptionType(concept.getNid(), defStamp);
			Optional<Long> vuid = Frills.getVuId(concept.getNid());
			if (prefName.equals(subsetToCheck) && vuid.isPresent() && vuid.get().equals(subsetVuidToCheck))
			{
				return true;
			}
		}

		return false;
	}

	// TODO
	/*
	 * private boolean descriptionExists(ConceptChronology concept, StampCoordinate stamp, String typeNameToCheck, Long vuidToCheck, State
	 * stateToCheck)
	 * {
	 * final StampCoordinate defStamp = (stamp == null)
	 * ? Get.coordinateFactory().createDevelopmentLatestStampCoordinate()
	 * : stamp;
	 * 
	 * boolean vuidMatches = false;
	 * boolean descTypeMatches = true; // TODO
	 * boolean stateMatches = false;
	 * 
	 * for (Object sc : Get.assemblageService().getSemanticsForComponent(concept.getNid()).toArray())
	 * {
	 * SemanticChronology semantic = (SemanticChronology) sc;
	 * if (semantic.getVersionType() == VersionType.DESCRIPTION)
	 * {
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" })
	 * Optional<LatestVersion<DescriptionSemantic>> descriptionVersion
	 * = ((SemanticChronology) semantic).getLatestVersion(DescriptionSemantic.class, defStamp);
	 * if (descriptionVersion.isPresent())
	 * {
	 * // TODO
	 * // Optional<UUID> descType = Frills.getDescriptionExtendedTypeConcept(defStamp, descriptionVersion.get().value().getNid());
	 * // if (descType.isPresent())
	 * // {
	 * // if (!descTypeMatches && VHATConstants.VHAT_PREFERRED_NAME.getPrimordialUuid().equals(descType.get()))
	 * // {
	 * // descTypeMatches = true;
	 * // }
	 * // }
	 * 
	 * long vuid = Frills.getVuId(descriptionVersion.get().value().getNid(), defStamp).orElse(0L).longValue();
	 * if (!vuidMatches && vuidToCheck.equals(vuid))
	 * {
	 * vuidMatches = true;
	 * break;
	 * }
	 * 
	 * stateMatches = descriptionVersion.get().value().getState() == stateToCheck;
	 * }
	 * }
	 * }
	 * 
	 * return vuidMatches && descTypeMatches && stateMatches;
	 * }
	 */

	private boolean subsetMembershipExistsForConcept(ConceptChronology concept, StampCoordinate stamp, Long vuidToCheck)
	{
		for (Object sc : Get.assemblageService().getSemanticChronologyStreamForComponent(concept.getNid()).toArray())
		{
			SemanticChronology semantic = (SemanticChronology) sc;
			if (semantic.getVersionType() == VersionType.DESCRIPTION)
			{
				LatestVersion<DescriptionVersion> descriptionVersion = semantic.getLatestVersion(stamp);
				if (descriptionVersion.isPresent())
				{
					for (Object nsc : Get.assemblageService().getSemanticChronologyStreamForComponent(semantic.getNid()).toArray())
					{
						SemanticChronology nestedSemantic = (SemanticChronology) nsc;
						if (nestedSemantic.getAssemblageNid() != MetaData.VUID____SOLOR.getNid()
								&& nestedSemantic.getAssemblageNid() != MetaData.CODE____SOLOR.getNid())
						{
							if (nestedSemantic.getVersionType() == VersionType.DYNAMIC)
							{
								long vuid = Frills.getVuId(nestedSemantic.getAssemblageNid(), stamp).orElse(0L).longValue();
								if (vuidToCheck.equals(vuid))
								{
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private XPathExpression validateCodeConceptCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + CODED_CONCEPT + ") = " + count);
	}

	private XPathExpression validateCodeConcept(String action, String code, String name, String vuid, String active) throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(CODED_CONCEPT).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (code != null)
			sb.append(" and ./Code/text() = '").append(code).append("'");
		if (name != null)
			sb.append(" and ./Name/text() = '").append(name).append("'");
		if (vuid != null)
			sb.append(" and ./VUID/text() = '").append(vuid).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return xpath_.compile(sb.toString());
	}

	private XPathExpression validateDesignationCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + DESIGNATION + ") = " + count);
	}

	private XPathExpression validateDesignation(String action, String code, String typeName, String name, String vuid, String valueNew, String valueOld,
			String active) throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(DESIGNATION).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (code != null)
			sb.append(" and ./Code/text() = '").append(code).append("'");
		if (typeName != null)
			sb.append(" and ./TypeName/text() = '").append(typeName).append("'");
		if (name != null)
			sb.append(" and ./Name/text() = '").append(name).append("'");
		if (valueNew != null)
			sb.append(" and ./ValueNew/text() = '").append(valueNew).append("'");
		if (valueOld != null)
			sb.append(" and ./ValueOld/text() = '").append(valueOld).append("'");
		if (vuid != null)
			sb.append(" and ./VUID/text() = '").append(vuid).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return xpath_.compile(sb.toString());
	}

	private XPathExpression validateDesignationPropertyCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + DESIGNATION_PROPERTY + ") = " + count);
	}

	private XPathExpression validateDesignationProperty(String action, String typeName, String valueNew, String valueOld, String active)
			throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(DESIGNATION_PROPERTY).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (typeName != null)
			sb.append(" and ./TypeName/text() = '").append(typeName).append("'");
		if (valueNew != null)
			sb.append(" and ./ValueNew/text() = '").append(valueNew).append("'");
		if (valueOld != null)
			sb.append(" and ./ValueOld/text() = '").append(valueOld).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return xpath_.compile(sb.toString());
	}

	private XPathExpression validateConceptPropertyCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + CONCEPT_PROPERTY + ") = " + count);
	}

	private XPathExpression validateConceptProperty(String action, String typeName, String valueNew, String valueOld, String active)
			throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(CONCEPT_PROPERTY).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (typeName != null)
			sb.append(" and ./TypeName/text() = '").append(typeName).append("'");
		if (valueNew != null)
			sb.append(" and ./ValueNew/text() = '").append(valueNew).append("'");
		if (valueOld != null)
			sb.append(" and ./ValueOld/text() = '").append(valueOld).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return xpath_.compile(sb.toString());
	}

	private XPathExpression validateRelationshipCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + RELATIONSHIP + ") = " + count);
	}

	private XPathExpression validateRelationship(String action, String typeName, String newTargetCode, String oldTargetCode, String active)
			throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(RELATIONSHIP).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (typeName != null)
			sb.append(" and ./TypeName/text() = '").append(typeName).append("'");
		if (newTargetCode != null)
			sb.append(" and ./NewTargetCode/text() = '").append(newTargetCode).append("'");
		if (oldTargetCode != null)
			sb.append(" and ./OldTargetCode/text() = '").append(oldTargetCode).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return xpath_.compile(sb.toString());
	}

	private XPathExpression validateSubsetMembershipCount(int count) throws XPathExpressionException
	{
		return xpath_.compile("count(" + SUBSET_MEMBERSHIP + ") = " + count);
	}

	private Boolean validateSubsetMembership(Document doc, String action, String vuid, String active) throws XPathExpressionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("count(").append(SUBSET_MEMBERSHIP).append("[");
		if (action != null)
			sb.append("./Action/text() = '").append(action).append("'");
		if (vuid != null)
			sb.append(" and ./VUID/text() = '").append(vuid).append("'");
		if (active != null)
			sb.append(" and ./Active/text() = '").append(active).append("'");
		sb.append("]) = 1");

		return validateXPath(doc, sb.toString());
	}

	private Boolean validateXPath(Document doc, String xpath) throws XPathExpressionException
	{
		return (Boolean) xpath_.compile(xpath).evaluate(doc, XPathConstants.BOOLEAN);
	}
}
