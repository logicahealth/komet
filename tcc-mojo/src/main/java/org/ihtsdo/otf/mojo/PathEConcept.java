/*
 * Copyright 2014 Informatics, Inc..
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
package org.ihtsdo.otf.mojo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.PathSpec;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;

/**
 *
 * @author aimeefurber
 * @author dylangrald
 */
@Mojo(name = "create-path-econcept")
public class PathEConcept extends AbstractMojo {

    private static final String DIR = System.getProperty("user.dir");

    private static final Logger LOGGER = Logger.getLogger(PathEConcept.class.getName());


    /**
     * Paths to add to initial database.
     *
     */
    @Parameter
    private PathSpec[] initialPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File folders = new File(DIR + "/target/generated-resources");
            if (!folders.exists()) {
                folders.mkdirs();
            }
            File out = new File(DIR + "/target/generated-resources/pathEConcept.jbin");
            createPathEConcepts(initialPaths, out);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception writing EConcept file", ex);
            throw new MojoFailureException(ex.getMessage());
        }
    }

    private static void createPathEConcepts(PathSpec[] initialPaths, File out) throws IOException {
        List<TtkRefexAbstractMemberChronicle<?>> pathMembers = new ArrayList<>();
        List<TtkRefexAbstractMemberChronicle<?>> originMembers = new ArrayList<>();

        for (PathSpec spec : initialPaths) {
            long startTime = System.currentTimeMillis();
            TtkRefexUuidMemberChronicle pathMember = new TtkRefexUuidMemberChronicle();
            pathMember.primordialUuid = UUID.randomUUID();
            pathMember.referencedComponentUuid = pathMember.primordialUuid;
            pathMember.setStatus(Status.ACTIVE);
            pathMember.setTime(startTime);
            pathMember.setAuthorUuid(TermAux.USER.getUuids()[0]);
            pathMember.setModuleUuid(TermAux.TERM_AUX_MODULE.getUuids()[0]);
            pathMember.setPathUuid(TermAux.WB_AUX_PATH.getUuids()[0]);
            pathMember.setAssemblageUuid(TermAux.PATH_REFSET.getUuids()[0]);
            pathMember.setUuid1(spec.getPathConcept().getUuids()[0]);
            pathMembers.add(pathMember);

            TtkRefexUuidIntMemberChronicle originMember = new TtkRefexUuidIntMemberChronicle();
            originMember.primordialUuid = UUID.randomUUID();
            originMember.referencedComponentUuid = originMember.primordialUuid;
            originMember.setStatus(Status.ACTIVE);
            originMember.setTime(startTime);
            originMember.setAuthorUuid(TermAux.USER.getUuids()[0]);
            originMember.setModuleUuid(TermAux.TERM_AUX_MODULE.getUuids()[0]);
            originMember.setPathUuid(TermAux.WB_AUX_PATH.getUuids()[0]);
            originMember.setAssemblageUuid(TermAux.PATH_ORIGIN_REFSET.getUuids()[0]);
            originMember.setUuid1(spec.getOriginConcept().getUuids()[0]);
            originMember.setInt1(Integer.MAX_VALUE);
            originMembers.add(originMember);
        }

        TtkConceptChronicle pathRefsetConcept = new TtkConceptChronicle();
        pathRefsetConcept.setPrimordialUuid(TermAux.PATH_REFSET.getUuids()[0]);
        pathRefsetConcept.setRefsetMembers(pathMembers);

        TtkConceptChronicle originRefsetConcept = new TtkConceptChronicle();
        originRefsetConcept.setPrimordialUuid(TermAux.PATH_ORIGIN_REFSET.getUuids()[0]);
        originRefsetConcept.setRefsetMembers(originMembers);

        //write to file (will merge with existing concepts, assumes that Path and Origin concepts have been loaded)
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
            pathRefsetConcept.writeExternal(dos);
            originRefsetConcept.writeExternal(dos);
            dos.flush();
        }
    }

}
