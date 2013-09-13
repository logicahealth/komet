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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * This resource is deliberately simple, to demonstrate proper
 * connectivity and service availability. When
 * a path of "hello/frank" is provided, it will return "hello
 * frank." Similarly, if "hello/bob" is provided, it will return
 * "hello bob."
 * @author kec
 */
@Path("chronicle/hello")
public class HelloResource {

    /**
     * Method that says hello to a person. 
     * @param name the name of the person to say hello to.
     * @return the hello string. 
     */
    @GET
    @Path("{name}")
    @Produces("text/plain")
    public String sayHello(@PathParam("name") String name)   {
        System.out.println("Saying hello to: " + name);
        return "hello " + name + ".";
    }

}
