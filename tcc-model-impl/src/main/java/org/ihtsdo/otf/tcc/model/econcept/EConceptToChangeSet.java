/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.model.econcept;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 * Convert eConcept files to change set files, using the current time as the commit time for the change set.
 *
 * @author kec
 */
public class EConceptToChangeSet {
    /**
     * Create change set files, based on the input econFiles, with each file appended with ".eccs".
     * @param econFiles Files to convert to change set files. 
     * @throws IOException
     * @throws ClassNotFoundException 
     */
   public static void convert(File[] econFiles) throws IOException, ClassNotFoundException {
      File[] eccsFiles = new File[econFiles.length];

      for (int i = 0; i < eccsFiles.length; i++) {
         eccsFiles[i] = new File(econFiles[i].getParent(), econFiles[i].getName() + ".eccs");
      }

      convert(econFiles, eccsFiles);
   }

   /**
    * Create change set files from the source econFiles[i], writing the change set to the corresponding
    * eccsFiles[i] (correspondence based on array index). This method allows output files to be written to 
    * any directory. 
    * @param econFiles Files to convert to change set files. 
    * @param eccsFiles Corresponding files to store the change set. If file already exists, 
    * it will be overwritten. 
    * @throws IOException
    * @throws ClassNotFoundException 
    */
    public static void convert(File[] econFiles, File[] eccsFiles) throws IOException, ClassNotFoundException {
        assert econFiles.length == eccsFiles.length;
      for (int i = 0; i < econFiles.length; i++) {
         System.out.println("Starting load from: " + econFiles[i].getAbsolutePath());

         AtomicInteger        conceptsRead = new AtomicInteger();
         FileInputStream      fis          = new FileInputStream(econFiles[i]);
         BufferedInputStream  bis          = new BufferedInputStream(fis);
         DataInputStream      in           = new DataInputStream(bis);
         File                 outputFile   = eccsFiles[i];
         FileOutputStream     fos          = new FileOutputStream(outputFile);
         BufferedOutputStream bos          = new BufferedOutputStream(fos);
         DataOutputStream     dos          = new DataOutputStream(bos);

         try {
            while (true) {
               long      time     = System.currentTimeMillis();
               TtkConceptChronicle eConcept = new TtkConceptChronicle(in);
               int       read     = conceptsRead.incrementAndGet();

               dos.writeLong(time);
               eConcept.writeExternal(dos);

               if (read % 100 == 0) {
                  if (read % 8000 == 0) {
                     System.out.println('.');
                     System.out.print(read + "-");
                  } else {
                     System.out.print('.');
                  }
               }
            }
         } catch (EOFException e) {
             //
         } finally {
            in.close();
            dos.close();             
         }

         System.out.println("\nFinished conversion of " + conceptsRead + " concepts from: "
                            + econFiles[i].getAbsolutePath() + " to: " + outputFile.getAbsolutePath());
      }
   }
}
