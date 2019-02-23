package sh.komet.gui.filter;

import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.util.UuidT3Generator;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.util.FxGet;

import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
@Singleton
public class ExtensionFilterMenuOption implements MenuProvider {
    private final Logger LOG = LogManager.getLogger();
    ;

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.TOOLS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuItem[] getMenuItems(AppMenu appMenu, Window window) {
        if (appMenu == AppMenu.TOOLS) {
            MenuItem filter = new MenuItem("Filter existing content from extension");
            filter.setOnAction(event -> {
                filterExtension(window);
            });
            return new MenuItem[]{filter};
        }
        return new MenuItem[]{};
    }

    private void filterExtension(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify zip file to filter...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Extension zip files", "*.zip"));
        fileChooser.setInitialFileName("native-export.zip");
        File extensionZipFile = fileChooser.showOpenDialog(window);
        if (extensionZipFile != null) {
            String filterdFileName = extensionZipFile.getName().replace(".zip", "-filtered.zip");
            fileChooser.setInitialDirectory(extensionZipFile.getParentFile());
            fileChooser.setInitialFileName(filterdFileName);
            File filteredZipFile = fileChooser.showSaveDialog(window);
            if (filteredZipFile != null) {

                try (ZipFile zipFile = new ZipFile(extensionZipFile, Charset.forName("UTF-8"));
                     ZipOutputStream outZipFile = new ZipOutputStream(new FileOutputStream(filteredZipFile))) {
                    zipFile.stream().forEach((ZipEntry entry) -> {
                        if (!entry.isDirectory()) {
                            if (entry.getName().contains("sct2_Concept")) {
                                LOG.info("Extension entry: " + entry.getName());
                                handleConceptEntry(zipFile, outZipFile, entry);
                            } else if (entry.getName().contains("sct2_Description")) {
                                LOG.info("Extension entry: " + entry.getName());
                                handleDescriptionEntry(zipFile, outZipFile, entry);
                            } else if (entry.getName().contains("sct2_Text")) {
                                LOG.info("Extension entry: " + entry.getName());
                                handleDescriptionEntry(zipFile, outZipFile, entry);
                            } else if (entry.getName().contains("der2_cRefset_Language")) {
                                LOG.info("Extension entry: " + entry.getName());
                                handleDialectEntry(zipFile, outZipFile, entry);
                            } else {
                                LOG.info("Extension entry: " + entry.getName());
                                handleGenericEntry(zipFile, outZipFile, entry);
                            }
                        }
                    });
                } catch (IOException ex) {
                    FxGet.dialogs().showErrorDialog(ex);
                }
            }
        }
    }

    protected String[] splitRow(String rowString) {
        return rowString.split("\t");
    }

    private void handleGenericEntry(ZipFile zipFile, ZipOutputStream outZipFile, ZipEntry entry) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")))) {
           handleHeader(outZipFile, entry, br);
            String rowString;
            boolean empty;
            while ((rowString = br.readLine()) != null) {
                empty = false;
                outZipFile.write(rowString.getBytes());
                outZipFile.write("\n".getBytes());
            }
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    private void handleHeader(ZipOutputStream outZipFile, ZipEntry entry, BufferedReader br) throws IOException {
        outZipFile.putNextEntry(new ZipEntry(entry.getName()));


        String rowString;
        rowString = br.readLine();  // header row handled different
        outZipFile.write(rowString.getBytes());
        outZipFile.write("\n".getBytes());

        boolean empty = true;
    }

    private void handleDialectEntry(ZipFile zipFile, ZipOutputStream outZipFile, ZipEntry entry) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")))) {

            handleHeader(outZipFile, entry, br);
            String rowString;
            boolean empty;
            while ((rowString = br.readLine()) != null) {
                empty = false;
                String[] columns = splitRow(rowString);
                UUID sctUuid = UuidT3Generator.fromSNOMED(columns[5]); // description id
                if (Get.identifierService().hasUuid(sctUuid)) {
                    LOG.info("Suppressing acceptability for existing descriptiton: " + columns[0] + " " +
                            columns[5]);
                } else {
                    outZipFile.write(rowString.getBytes());
                    outZipFile.write("\n".getBytes());
                }
            }


        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    private void handleDescriptionEntry(ZipFile zipFile, ZipOutputStream outZipFile, ZipEntry entry) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")))) {

            handleHeader(outZipFile, entry, br);
            String rowString;
            boolean empty;
            while ((rowString = br.readLine()) != null) {
                empty = false;
                String[] columns = splitRow(rowString);
                UUID sctUuid = UuidT3Generator.fromSNOMED(columns[0]); // description id
                if (Get.identifierService().hasUuid(sctUuid)) {
                    LOG.info("Suppressing existing description: " + columns[0] + " " + columns[7]);
                } else {
                    UUID sctConceptUuid = UuidT3Generator.fromSNOMED(columns[4]); // concept id
                    if (Get.identifierService().hasUuid(sctConceptUuid)) {
                        LOG.info("Suppressing new description, existing concept: " + columns[0] + " " + columns[7]);
                    } else {
                        outZipFile.write(rowString.getBytes());
                        outZipFile.write("\n".getBytes());
                    }
                }
            }


        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    private void handleConceptEntry(ZipFile zipFile, ZipOutputStream outZipFile, ZipEntry entry) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")))) {

            handleHeader(outZipFile, entry, br);
            String rowString;
            boolean empty;
            while ((rowString = br.readLine()) != null) {
                empty = false;
                String[] columns = splitRow(rowString);
                UUID sctUuid = UuidT3Generator.fromSNOMED(columns[0]); // concept id
                if (Get.identifierService().hasUuid(sctUuid)) {
                    LOG.info("Suppressing existing concept: " + columns[0] + " " +
                            Get.conceptDescriptionText(Get.nidForUuids(sctUuid)));
                } else {
                    outZipFile.write(rowString.getBytes());
                    outZipFile.write("\n".getBytes());
                }
            }


        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }
}
