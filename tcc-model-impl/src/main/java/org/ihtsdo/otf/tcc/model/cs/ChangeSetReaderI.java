/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.chronicle;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

/**
 * Provides an interface to read change sets. Provides methods to allow reading
 * of a collection
 * of change sets to be imported in a serialized sequence according to their
 * commit time.
 * 
 * @author kec
 * 
 */
public interface ChangeSetReaderI extends Serializable {

    /**
     * 
     * @return the time in ms of the next commit contined within this change
     *         set.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public long nextCommitTime() throws IOException, ClassNotFoundException;

    /**
     * Read this change set until the specified commit time.
     * 
     * @param time the commit time to read until.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readUntil(long time, Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException, ClassNotFoundException;

    /**
     * Read this file until the end.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void read(Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException, ClassNotFoundException;

    /**
     * 
     * @param changeSetFile the change set file to validate and read.
     */
    public void setChangeSetFile(File changeSetFile);

    /**
     * 
     * @param changeSetFile the change set file to validate and read.
     */
    public File getChangeSetFile();

    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException;

	boolean isContentMerged();
}
