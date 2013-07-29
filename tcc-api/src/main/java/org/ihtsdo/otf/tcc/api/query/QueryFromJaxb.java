/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.api.query;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
public class QueryFromJaxb extends Query {

    
    public QueryFromJaxb(String viewCoordinateXml, String forXml,
            String letXml, String whereXml) throws JAXBException {
        super((ViewCoordinate) JaxbForQuery.get().createUnmarshaller()
                          .unmarshal(new StringReader(viewCoordinateXml)));
        Unmarshaller unmarshaller = JaxbForQuery.get().createUnmarshaller();
        LetMap letMap = (LetMap) unmarshaller.unmarshal(new StringReader(letXml));
        getLetDeclarations().putAll(letMap.getMap());
    }

    @Override
    protected NativeIdSetBI For() throws IOException {
        return Ts.get().getAllConceptNids();
    }

    @Override
    protected void Let() throws IOException {
        // lets are set in the constructor. 
    }

    @Override
    protected Clause Where() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
