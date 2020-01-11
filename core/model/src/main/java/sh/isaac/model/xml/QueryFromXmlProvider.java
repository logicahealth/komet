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

import java.io.Reader;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.query.ManifoldCoordinateForQuery;
import sh.isaac.api.query.Query;

/**
 *
 * @author kec
 */
@Service
public class QueryFromXmlProvider {
    public Query fromXml(Reader reader) throws Exception {
        Query q = (Query) Jaxb.createUnmarshaller().unmarshal(reader);
        if (q.getRoot() != null) {
            q.getRoot().setEnclosingQuery(q);
        }
        for (Object obj: q.getLetDeclarations().values()) {
            if (obj instanceof ManifoldCoordinateForQuery) {
                ((ManifoldCoordinateForQuery) obj).setQuery(q);
            }
        }
        return q;
    }
    
}
