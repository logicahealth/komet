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



package sh.isaac.api.util.metainf;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.LookupService;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MetaInfReader}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MetaInfReader
{
   private static final Logger LOG = LogManager.getLogger();
   
   public static MavenArtifactInfo readDbMetadata() throws IOException 
   {
      try
      {
         MavenArtifactInfo isaacDbDependency = new MavenArtifactInfo();;
         //Read the db metadata
         AtomicBoolean readDbMetadataFromProperties = new AtomicBoolean(false);
         AtomicBoolean readDbMetadataFromPom = new AtomicBoolean(false);
         
         java.nio.file.Path dbLocation = LookupService.get().getService(ConfigurationService.class).getDataStoreFolderPath();
         //find the pom.properties file in the hierarchy
         File dbMetadata = new File(dbLocation.toFile(), "META-INF");
         AtomicReference<String> metadataVersion = new AtomicReference<String>("");
         if (dbMetadata.isDirectory())
         {
            Files.walkFileTree(dbMetadata.toPath(), new SimpleFileVisitor<java.nio.file.Path>()
            {
               /**
                * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
                */
               @Override
               public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) throws IOException
               {
                  File f = path.toFile();
                  if (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).equals("pom.properties"))
                  {
                     Properties p = new Properties();
                     FileReader fileReader = null;
                     try {
                        p.load(fileReader = new FileReader(f));
                     } finally {
                        if (fileReader != null) {
                           fileReader.close();
                        }
                     }
   
                     isaacDbDependency.setValues(
                                 p.getProperty("project.groupId"),
                                 p.getProperty("project.artifactId"),
                                 p.getProperty("project.version"),
                                 p.getProperty("resultArtifactClassifier"),
                                 p.getProperty("chronicles.type"));
                     
                     metadataVersion.set(p.getProperty("isaac-metadata.version"));
                     readDbMetadataFromProperties.set(true);
                     return readDbMetadataFromPom.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                  } else if (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).equals("pom.xml")) {
                     DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                     DocumentBuilder builder;
                     Document dDoc = null;
                     XPath xPath = XPathFactory.newInstance().newXPath();
   
                     try {
                        domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                        builder = domFactory.newDocumentBuilder();
                        
                        dDoc = builder.parse(f);
                        
                        {
                           NodeList dbLicensesNodes = ((NodeList) xPath.evaluate("/project/licenses/license/name", dDoc, XPathConstants.NODESET));
   
                           LOG.debug("Found {} license names in DB pom.xml", dbLicensesNodes.getLength());
                           for (int i = 0; i < dbLicensesNodes.getLength(); i++) {
                              Node currentLicenseNameNode = dbLicensesNodes.item(i);
                              String name = currentLicenseNameNode.getTextContent();
   
                              MavenLicenseInfo license =
                                    new MavenLicenseInfo(
                                          name,
                                          ((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/url", dDoc, XPathConstants.NODE)).getTextContent(),
                                          ((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/comments", dDoc, XPathConstants.NODE)).getTextContent());
                              isaacDbDependency.dbLicenses.add(license);
   
                              LOG.debug("Extracted license \"{}\" from DB pom.xml: {}", name, license.toString());
                           }
                        }
                        
                        {
                           NodeList dbDependenciesNodes = ((NodeList) xPath.evaluate("/project/dependencies/dependency/artifactId", dDoc, XPathConstants.NODESET));
   
                           LOG.debug("Found {} dependency artifactIds in DB pom.xml", dbDependenciesNodes.getLength());
                           for (int i = 0; i < dbDependenciesNodes.getLength(); i++) {
                              Node currentDbDependencyArtifactIdNode = dbDependenciesNodes.item(i);
                              
                              String artifactId = currentDbDependencyArtifactIdNode.getTextContent();
                              String groupId = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/groupId", dDoc, XPathConstants.NODE)).getTextContent();
                              String version = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/version", dDoc, XPathConstants.NODE)).getTextContent();
                     
                              String classifier = null;
                              try {
                                 classifier = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/classifier", dDoc, XPathConstants.NODE)).getTextContent();
                              } catch (Throwable t) {
                                 LOG.debug("Problem reading \"classifier\" element for {}", artifactId);
                              }
                              String type = null;
                              try {
                                 type = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/type", dDoc, XPathConstants.NODE)).getTextContent();
                              } catch (Throwable t) {
                                 LOG.debug("Problem reading \"type\" element for {}", artifactId);
                              }
   
                              MavenArtifactInfo dependencyInfo = new MavenArtifactInfo(groupId, artifactId, version, classifier, type);
                              isaacDbDependency.dbDependencies.add(dependencyInfo);
   
                              LOG.debug("Extracted dependency \"{}\" from DB pom.xml: {}", artifactId, dependencyInfo.toString());
                           }
                        }
                     } catch (XPathExpressionException | SAXException | ParserConfigurationException e) {
                        throw new IOException(e);
                     }
   
                     readDbMetadataFromPom.set(true);
                     return readDbMetadataFromProperties.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                  }
   
                  return FileVisitResult.CONTINUE;
               }
            });
         }
   
         if (!readDbMetadataFromProperties.get())
         {
            if (dbMetadata.isDirectory())
            {
               LOG.error("Failed to read the metadata about the database from the database package.");
            }
            else
            {
               LOG.info("No metadata folder found in DB folder - this is expected, if we are building a new database");
            }
         }
         else
         {
            //Due to a quirk in how the DB poms are set up, we need to fill in this property
            for (MavenArtifactInfo dependency : isaacDbDependency.dbDependencies)
            {
               if (dependency.version != null && "${isaac-metadata.version}".equals(dependency.version))
               {
                  dependency.version = metadataVersion.get();
                  break;
               }
            }
            LOG.debug("Successfully read db properties from maven config files.  dbGroupId: {} dbArtifactId: {} dbVersion: {} dbClassifier: {} dbType: {}", 
                  isaacDbDependency.groupId, isaacDbDependency.artifactId, isaacDbDependency.version, isaacDbDependency.classifier, isaacDbDependency.type);
         }
         return isaacDbDependency;
      }
      catch (IOException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new IOException(e);
      }
   }
}