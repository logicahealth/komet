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
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.query.QueryFromJaxb;

/**
 *
 * @author kec
 */
@Path("/query")
public class QueryResource {
    static {
        BdbSingleton.get();
    }

    @GET
    @Produces("text/plain")
    public String doQuery(@QueryParam("VIEWPOINT") String viewValue,
                          @QueryParam("FOR") String forValue,
                          @QueryParam("LET") String letValue, 
                          @QueryParam("WHERE") String whereValue) throws IOException, JAXBException, Exception  {
        String queryString = forValue + "\n   " + letValue+ "\n   " + whereValue;
        System.out.println("Recieved: \n   " + queryString);
        QueryFromJaxb query = new QueryFromJaxb(viewValue, forValue, letValue, whereValue);
        NativeIdSetBI resultSet = query.compute();
        NativeIdSetItrBI iterator = resultSet.getIterator();
        List<Integer> results = new ArrayList<>(resultSet.size());
        while (iterator.next()) {
            results.add(iterator.nid());
        }
        
        
        return results.toString();
    }
    
}
