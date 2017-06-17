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



package sh.isaac.provider.query.jaxb;

//~--- JDK imports ------------------------------------------------------------

import sh.isaac.provider.query.jaxb.JaxbForQuery;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import sh.isaac.api.query.Query;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 10/30/14.
 */
public class QuerySerializer {
   /**
    * Marshall.
    *
    * @param q the q
    * @return the string
    * @throws JAXBException the JAXB exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static String marshall(Query q)
            throws JAXBException, IOException {
      // JAXBContext ctx = JaxbForQuery.get();
      q.setup();

      final Marshaller marshaller = JaxbForQuery.get()
                                                .createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      final StringWriter builder = new StringWriter();

      marshaller.marshal(q, builder);
      return builder.toString();
   }

   /**
    * Unmarshall.
    *
    * @param xmlData the xml data
    * @return the query
    * @throws JAXBException the JAXB exception
    * @throws ParserConfigurationException the parser configuration exception
    * @throws Exception the exception
    * @throws Throwable the throwable
    */
   public static Query unmarshall(Reader xmlData)
            throws JAXBException,
                   ParserConfigurationException,
                   Exception,
                   Throwable {
      final JAXBContext  ctx          = JaxbForQuery.get();
      final Unmarshaller unmarshaller = ctx.createUnmarshaller();

      // Query query = (Query) unmarshaller.unmarshal(xmlData);
      // To avoid XXE injection do not use unmarshal methods that process
      // an XML source directly as java.io.File, java.io.Reader or java.io.InputStream.
      // Parse the document with a securely configured parser and use an unmarshal method
      // that takes the secure parser as the XML source
      final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

      domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      domFactory.setExpandEntityReferences(false);

      final DocumentBuilder db       = domFactory.newDocumentBuilder();
      final InputSource     source   = new InputSource(xmlData);
      final Document        document = db.parse(source);
      final Query           query    = (Query) unmarshaller.unmarshal(document);

      return query;
   }
}

