/*
 * Copyright 2018 ISAAC's KOMET Collaborators.
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
package sh.komet.fx.stage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.externalizable.IsaacObjectType;
import static sh.isaac.api.externalizable.IsaacObjectType.UNKNOWN;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ChronologyImpl;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class NativeExport extends TimedTaskWithProgressTracker<Integer> {

    final File exportFile;
    int identifierCount = 0;
    int exportCount = 0;
    
    public NativeExport(File exportFile) {
      this.exportFile = exportFile;
      updateTitle("Native export to " + exportFile.getName());
      Get.activeTasks().add(this);  
    }

    @Override
    protected Integer call() throws Exception {
        try {
           updateMessage("Counting identifiers...");
           int[] assemblageNids = Get.identifierService().getAssemblageNids();
           for (int assemblageNid: assemblageNids) {
               Get.identifierService().getNidsForAssemblage(assemblageNid).forEach((nid) -> {
                   identifierCount++;
               });
           }
           addToTotalWork(2*identifierCount);
           updateMessage("Exporting identifiers...");
           LOG.info("Identifier count: " + identifierCount);
    
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(exportFile), StandardCharsets.UTF_8)) {
              DataOutputStream dos = new DataOutputStream( zipOut );
              ZipEntry identifierEntry = new ZipEntry("identifiers");
              zipOut.putNextEntry(identifierEntry);
              dos.writeInt(identifierCount);
              for (int assemblageNid: assemblageNids) {
                  VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
                  Get.identifierService().getNidsForAssemblage(assemblageNid).forEach((nid) -> {
                      try {
                          dos.writeInt(nid);
                          UUID[] uuids = Get.identifierService().getUuidArrayForNid(nid);
                          dos.writeInt(uuids.length);
                          for (UUID uuid: uuids) {
                              dos.writeLong(uuid.getMostSignificantBits());
                              dos.writeLong(uuid.getLeastSignificantBits());
                          }
                          IsaacObjectType objectType = Get.identifierService().getObjectTypeForComponent(nid);
                          dos.writeByte(objectType.getToken());
                          dos.writeByte(versionType.getVersionTypeToken());
                          completedUnitOfWork();
                      } catch (IOException ex) {
                          throw new RuntimeException(ex);
                      }
                  });
              }
              zipOut.closeEntry();
              
              updateMessage("Exporting STAMPs...");
              ZipEntry stampEntry = new ZipEntry("stamp");
              zipOut.putNextEntry(stampEntry);
              StampService stampService = Get.stampService();
              int[] stampSequences = stampService.getStampSequences().toArray();
              dos.writeInt(stampSequences.length);
              for (int stampSequence: stampSequences) {
                  try {
                      dos.writeInt(stampSequence);
                      Stamp stamp = stampService.getStamp(stampSequence);
                      dos.writeUTF(stamp.getStatus().name());
                      dos.writeLong(stamp.getTime());
                      dos.writeInt(stamp.getAuthorNid());
                      dos.writeInt(stamp.getModuleNid());
                      dos.writeInt(stamp.getPathNid());
                  } catch (IOException ex) {
                          throw new RuntimeException(ex);
                  }
              }
              
              zipOut.closeEntry();
              
              updateMessage("Exporting objects...");
              ZipEntry ibdfEntry = new ZipEntry("ibdf");
              zipOut.putNextEntry(ibdfEntry);
              for (int assemblageNid: assemblageNids) {
                  Get.identifierService().getNidsForAssemblage(assemblageNid).forEach((nid) -> {
                      Optional<? extends Chronology> chronologyOptional = Get.identifiedObjectService().getChronology(nid);
                      if (chronologyOptional.isPresent()) {
                          exportCount++;
                          try {
                              ChronologyImpl chronology = (ChronologyImpl) chronologyOptional.get();
                              dos.writeByte(chronology.getIsaacObjectType().getToken());
                              List<byte[]> dataToWrite = ChronologyImpl.getDataList(chronology);
                              dos.writeInt(dataToWrite.size());
                              for (byte[] data: dataToWrite) {
                                 dos.writeInt(data.length); 
                                 dos.write(data);
                              }
                          } catch (IOException ex) {
                            throw new RuntimeException(ex);
                          }
                      }
                      completedUnitOfWork();
                 });
              }
              dos.writeByte(UNKNOWN.getToken());
              zipOut.closeEntry();
            } catch (IOException ex) {
                FxGet.dialogs().showErrorDialog("Failure during native export to zip file.", ex);
            }
        LOG.info("Export count: " + exportCount);
                return identifierCount;
        } finally {
            Get.activeTasks().remove(this);
        }
    }
    
}
