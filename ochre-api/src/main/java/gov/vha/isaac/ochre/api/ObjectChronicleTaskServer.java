/*
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

package gov.vha.isaac.ochre.api;

import java.nio.file.Path;
import java.time.Instant;

import javafx.concurrent.Task;
import org.jvnet.hk2.annotations.Contract;


/**
 *
 * @author kec
 */
@Contract
public interface ObjectChronicleTaskServer {


    /**
     * Adds an origin to a stamp path. The origin and destination paths must already exist.
     * @param stampPath The path to add the origin to.
     * @param originStampPath The path which is added to the origin set of the stampPath.
     * @param originTime The time of the origin.
     * @return A task for tracking progress.
     */
    Task<Void> addStampPathOrigin(ConceptProxy stampPath, ConceptProxy originStampPath, Instant originTime);

    /**
     *
     * @param filePaths <code>Path</code>s of the input files
     * @return Task that returns an integer reflecting the number of object chronicles imported
     */
    Task<Integer> startImportLogTask(Path... filePaths);

    /**
     *
     * @param filePaths <code>Path</code>s of the input files
     * @return Task that returns an integer reflecting the number of object chronicles imported
     */
    Task<Integer> startLoadTask(Path... filePaths);

    /**
     *
     * @param stampPath All object chronicles will be placed onto this path
     * @param filePaths <code>Path</code>s of the input files
     * @return Task that returns an integer reflecting the number of object chronicles imported
     */
    Task<Integer> startLoadTask(ConceptProxy stampPath, Path... filePaths);

    /**
     *
     * @param filePaths <code>Path</code>s of the input files for the verification
     * @return Task that returns an integer reflecting the number of object chronicles verified
     */
    Task<Boolean> startVerifyTask(Path... filePaths);
    /**
     *
     * @param stampPath All object chronicles on this path will be verified
     * @param filePaths <code>Path</code>s of the input files for the verification
     * @return Task that returns an integer reflecting the number of object chronicles verified
     */
    Task<Boolean> startVerifyTask(ConceptProxy stampPath, Path... filePaths);

    /**
     *
     * @param filePath <code>Path</code> of the export file
     * @return Task that returns an integer reflecting the number of object chronicles exported
     */
    Task<Integer> startExportTask(Path filePath);
    /**
     * 
     * @param stampPath the stampPath to export from (only versions on this path will be exported)
     * @param filePath <code>Path</code> of the export file
     * @return Task that returns an integer reflecting the number of object chronicles exported
     */
    Task<Integer> startExportTask(ConceptProxy stampPath, Path filePath);

    /**
     * Removes all relationships from the object chronicle concepts, so that only the logic graph remains.
     * @param filePath <code>Path</code> of the export file
     * @return Task that returns an integer reflecting the number of object chronicles exported
     */
    Task<Integer> startLogicGraphExportTask(Path filePath);

    /**
     * Removes all relationships from the object chronicle concepts, so that only the logic graph remains.
     * @param stampPath All object chronicles will be placed onto this path in the exported file
     * @param filePath <code>Path</code> of the export file
     * @return Task that returns an integer reflecting the number of object chronicles exported
     */
    Task<Integer> startLogicGraphExportTask(ConceptProxy stampPath, Path filePath);

    /**
     * Perform indexing according to all installed indexers.
     * @return Task that indicates progress.
     */
    Task<Void> startIndexTask();
    

}
