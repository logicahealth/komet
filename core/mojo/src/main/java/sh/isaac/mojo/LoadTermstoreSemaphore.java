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
package sh.isaac.mojo;

//~--- JDK imports ------------------------------------------------------------
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.cedarsoftware.util.io.JsonWriter;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.datastream.BinaryDatastreamReader;

//~--- classes ----------------------------------------------------------------

/*
* Copyright 2001-2005 The Apache Software Foundation.
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
/**
 * Goal which loads a database from eConcept files.
 */
@Mojo(
        name = "load-termstore-semaphore",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class LoadTermstoreSemaphore
        extends AbstractMojo implements Consumer<IsaacExternalizable> {

    @Parameter(required = false)
    private int duplicatesToPrint = 20;

    /**
     * The preferred mechanism for specifying ibdf files - provide a folder that
     * contains IBDF files, all found IBDF files in this folder will be
     * processed.
     */
    @Parameter(required = false)
    private File ibdfFileFolder;

    /**
     * The optional (old) way to specify ibdf files - requires each file to be
     * listed one by one.
     */
    @Parameter(required = false)
    private File[] ibdfFiles;

    /**
     * The item failure.
     */
    private final AtomicInteger conceptCount = new AtomicInteger();
    private final AtomicInteger semanticCount = new AtomicInteger();
    private final AtomicInteger stampAliasCount = new AtomicInteger();
    private final AtomicInteger stampCommentCount = new AtomicInteger();
    private final AtomicInteger itemCount = new AtomicInteger();
    private final AtomicInteger itemFailure = new AtomicInteger();
    private final Set<Integer> deferredActionNids = new ConcurrentSkipListSet<>();

    //~--- methods -------------------------------------------------------------
    /**
     * Execute.
     *
     * @throws MojoExecutionException the mojo execution exception
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public void execute()
            throws MojoExecutionException {
        Get.configurationService()
                .setDBBuildMode(BuildMode.DB);

        // Load IsaacMetadataAuxiliary first, otherwise, we have issues....
        final AtomicBoolean hasMetadata = new AtomicBoolean(false);
        Set<File> mergedFiles;

        try {
            mergedFiles = new HashSet<>();

            if (this.ibdfFiles != null) {
                for (final File f : this.ibdfFiles) {
                    mergedFiles.add(f.getCanonicalFile());
                    if (f.getName().equals("IsaacMetadataAuxiliary.ibdf")) {
                        hasMetadata.set(true);
                    }
                }
            }

            if (this.ibdfFileFolder != null) {
                if (!this.ibdfFileFolder.isDirectory()) {
                    throw new MojoExecutionException("If ibdfFileFolder is provided, it must point to a folder");
                }

                for (final File f : this.ibdfFileFolder.listFiles()) {
                    if (!f.isFile()) {
                        getLog().info("The file " + f.getAbsolutePath() + " is not a file - ignoring.");
                    } else if (!f.getName()
                            .toLowerCase()
                            .endsWith(".ibdf")) {
                        getLog().info("The file " + f.getAbsolutePath()
                                + " does not match the expected type of ibdf - ignoring.");
                    } else {
                        if (f.getName()
                                .equals("IsaacMetadataAuxiliary.ibdf")) {
                            hasMetadata.set(true);
                        }
                        mergedFiles.add(f);
                    }
                }
            }
        } catch (final IOException e1) {
            throw new MojoExecutionException("Problem reading ibdf files", e1);
        }

        final File[] temp = mergedFiles.toArray(new File[mergedFiles.size()]);

        Arrays.sort(temp,
                (o1, o2) -> {
                    if (o1.getName()
                            .equals("IsaacMetadataAuxiliary.ibdf")) {
                        return -1;
                    } else if (o2.getName()
                            .equals("IsaacMetadataAuxiliary.ibdf")) {
                        return 1;
                    } else {
                        return ((o1.length() - o2.length()) > 0 ? 1
                        : ((o1.length() - o2.length()) < 0 ? -1
                        : 0));
                    }
                });

        if (!hasMetadata.get()) {
            getLog().warn("No Metadata IBDF file found!  This probably isn't good....");
        }

        if (temp.length == 0) {
            throw new MojoExecutionException("Failed to find any ibdf files to load");
        }

        getLog().info("Identified " + temp.length + " ibdf files");

        try {
            for (final File f : temp) {
                if (f.length() == 0) {
                    getLog().info("Skipping empty file: " + f.getCanonicalPath());
                    continue;
                }
                getLog().info("Loading termstore from " + f.getCanonicalPath());

                int duplicateCount = 0;

                final BinaryDatastreamReader reader = new BinaryDatastreamReader(this::accept, f.toPath());

                Get.executor().submit(reader).get();

                getLog().info("Loaded " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, "
                        + this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComment"
                        + ((duplicateCount > 0) ? " Duplicates " + duplicateCount
                                : "") + ((this.itemFailure.get() > 0) ? " Failures " + this.itemFailure
                        : "") + " from file " + f.getName());
                this.conceptCount.set(0);
                this.semanticCount.set(0);
                this.stampAliasCount.set(0);
                this.stampCommentCount.set(0);
            }
            
            Get.service(VersionManagmentPathService.class).rebuildPathMap();

            getLog().info("Completing processing on " + deferredActionNids.size() + " defered items");

            for (final int nid : deferredActionNids) {
                if (IsaacObjectType.SEMANTIC.equals(Get.identifierService()
                        .getObjectTypeForComponent(nid))) {
                    final SemanticChronology sc = Get.assemblageService()
                            .getSemanticChronology(nid);

                    if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                        Get.taxonomyService()
                                .updateTaxonomy(sc);
                    } else {
                        throw new UnsupportedOperationException("1 Unexpected nid in deferred set: " + nid + " " + sc);
                    }
                } else {
                    throw new UnsupportedOperationException("2 Unexpected nid in deferred set: " + nid);
                }
            }
            getLog().info("Final item count: " + this.itemCount);
            Get.startIndexTask().get();
            LookupService.syncAll();
        } catch (final ExecutionException | IOException | InterruptedException | UnsupportedOperationException ex) {
            getLog().info("Loaded with exception " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, "
                    + this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComments");
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Sets the ibdf files.
     *
     * @param files the new ibdf files
     */
    public void setibdfFiles(File[] files) {
        this.ibdfFiles = files;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public void accept(IsaacExternalizable object) {

        this.itemCount.incrementAndGet();

        try {
            if (null != object.getIsaacObjectType()) {
                switch (object.getIsaacObjectType()) {
                    case CONCEPT:
                        Get.conceptService()
                                .writeConcept(((ConceptChronology) object));
                        this.conceptCount.incrementAndGet();
                        break;

                    case SEMANTIC:
                        SemanticChronology sc = (SemanticChronology) object;
                        if (sc.getPrimordialUuid().equals(TermAux.MASTER_PATH_SEMANTIC_UUID)) {
                            getLog().info("Loading master path semantic at count: " + this.itemCount);
                        } else if (sc.getPrimordialUuid().equals(TermAux.DEVELOPMENT_PATH_SEMANTIC_UUID)) {
                            getLog().info("Loading development path semantic at count: " + this.itemCount);
                        }

                        Get.assemblageService()
                                .writeSemanticChronology(sc);
                        if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                            deferredActionNids.add(sc.getNid());
                        }

                        this.semanticCount.incrementAndGet();
                        break;

                    case STAMP_ALIAS:
                        Get.commitService()
                                .addAlias(((StampAlias) object).getStampSequence(),
                                        ((StampAlias) object).getStampAlias(),
                                        null);
                        this.stampAliasCount.incrementAndGet();
                        break;

                    case STAMP_COMMENT:
                        Get.commitService()
                                .setComment(((StampComment) object).getStampSequence(),
                                        ((StampComment) object).getComment());
                        this.stampCommentCount.incrementAndGet();
                        break;

                    default:
                        throw new UnsupportedOperationException("Unknown object type: " + object);
                }
            }
        } catch (final UnsupportedOperationException e) {
            this.itemFailure.incrementAndGet();
            getLog().error("Failure at " + this.conceptCount + " concepts, " + this.semanticCount
                    + " semantics, " + this.stampAliasCount + " stampAlias, " + this.stampCommentCount
                    + " stampComments",
                    e);

            final Map<String, Object> args = new HashMap<>();

            args.put(JsonWriter.PRETTY_PRINT, true);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (JsonWriter json = new JsonWriter(baos, args)) {
                UUID primordial = null;

                if (object instanceof Chronology) {
                    primordial = ((Chronology) object).getPrimordialUuid();
                }

                json.write(object);
                getLog().error("Failed on " + ((primordial == null) ? ": "
                        : "object with primoridial UUID " + primordial.toString() + ": ") + baos.toString());
            }
        }

        if (this.itemCount.get() % 50000 == 0) {
            getLog().info("Read " + this.itemCount + " entries, " + "Loaded " + this.conceptCount
                    + " concepts, " + this.semanticCount + " semantics, " + this.stampAliasCount
                    + " stampAlias, " + this.stampCommentCount + " stampComment");
        }
    }
}
