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



package test;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ChangeListener;

import javafx.concurrent.Task;

import sh.isaac.api.util.WorkExecutors;
import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.GitPublish;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TestSourceUploadConfiguration.
 */
public class TestSourceUploadConfiguration {
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws Throwable the throwable
    */
   public static void main(String[] args)
            throws Throwable {
      final String gitTestURL         = "https://git.isaac.sh/git/r/junk.git";
      final String gitUsername        = "";
      final char[] gitPassword        = "".toCharArray();
      final String artifactRepository = "https://some.nexus.server:8080/nexus/content/sites/ets_tooling_snapshot/";
      final String repositoryUsername = "";
      final String repositoryPassword = "";

      System.setProperty("java.awt.headless", "true");

      final File f = new File("testJunk");

      f.mkdir();
      Files.write(new File(f, "foo.txt").toPath(),
                  "Hi there".getBytes(),
                  StandardOpenOption.WRITE,
                  StandardOpenOption.CREATE);

      final ArrayList<File> files = new ArrayList<>();

      files.add(new File(f, "foo.txt"));
      System.out.println(GitPublish.readTags(gitTestURL, gitUsername, gitPassword));

      final Task<String> t = SrcUploadCreator.createSrcUploadConfiguration(SupportedConverterTypes.SCT_EXTENSION,
                                                                           "50.6",
                                                                           "us",
                                                                           files,
                                                                           gitTestURL,
                                                                           gitUsername,
                                                                           gitPassword,
                                                                           artifactRepository,
                                                                           repositoryUsername,
                                                                           repositoryPassword);

      t.progressProperty()
       .addListener((ChangeListener<Number>) (observable, oldValue,
            newValue) -> System.out.println("[Change] Progress " + newValue));
      t.messageProperty()
       .addListener((ChangeListener<String>) (observable, oldValue,
            newValue) -> System.out.println("[Change] Message " + newValue));
      t.titleProperty()
       .addListener((ChangeListener<String>) (observable, oldValue,
            newValue) -> System.out.println("[Change] Title " + newValue));
      WorkExecutors.get()
                   .getExecutor()
                   .execute(t);
      System.out.println("Result " + t.get());
      FileUtil.recursiveDelete(f);
   }
}

