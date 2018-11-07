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
package sh.isaac.model.xml;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.model.configuration.DefaultCoordinateProvider;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;

/**
 *
 * @author kec
 */
public class TestJaxb {

    public static void main(String[] args) throws Exception {
        
        
        JAXBContext jc = JAXBContext.newInstance(StampCoordinateImpl.class, ConceptProxy.class, LanguageCoordinateImpl.class, JaxbMap.class);


        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(TermAux.ISAAC_UUID, System.out);
        
        DefaultCoordinateProvider defaultCoordinateProvider = new DefaultCoordinateProvider();

        marshaller.marshal(defaultCoordinateProvider.getDefaultStampCoordinate().getStampCoordinate(), System.out);

        marshaller.marshal(defaultCoordinateProvider.getDefaultLanguageCoordinate().getLanguageCoordinate(), System.out);
        
        Map<String, Object> letMap = new HashMap();
        letMap.put("stamp1", defaultCoordinateProvider.getDefaultStampCoordinate().getStampCoordinate());
        letMap.put("language", defaultCoordinateProvider.getDefaultLanguageCoordinate().getLanguageCoordinate());

        JaxbMap jaxbMap = JaxbMap.of(letMap);
        marshaller.marshal(jaxbMap, System.out);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        //File xml = new File("src/forum13178824/input.xml");
        //ConceptSpecification root = (ConceptSpecification) unmarshaller.unmarshal(new StringReader("<concept fqn=\"UUID (SOLOR)\" uuids=\"2faa9262-8fb2-11db-b606-0800200c9a66 680f3f6c-7a2a-365d-b527-8c9a96dd1a94\"/>"));
        //System.out.println(root);
    }    
}
