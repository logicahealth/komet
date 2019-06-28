package sh.komet.gui.importation;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Window;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.StringUtils;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.komet.gui.util.FxUtils;

public class ArtifactImporter
{
	protected static final Logger LOG = LogManager.getLogger();

	public static void startArtifactImport(Window parentWindow)
	{
		//TODO tie this to a real StoredPrefs in the GUI.  For now, just a default, so we can at least read a local .m2 folder
		//make this system property read go away.  Need to integrate this with the rest of the prefs system...
		StoredPrefs storedPrefs = new StoredPrefs("".toCharArray());
		String temp = System.getProperty("M2_PATH");
		if (StringUtils.isNotBlank(temp))
		{
			storedPrefs.setLocalM2FolderPath(temp);
		}

		ListView<SDOSourceContent> sdoPicker = new ListView<>();
		ArrayList<SDOSourceContent> sdoSourceFiles_ = new ArrayList<>();

		HashMap<SupportedConverterTypes, DirectConverter> converterTypeMapping = new HashMap<>();
		for (DirectConverter dc : LookupService.get().getAllServices(DirectConverter.class))
		{
			for (SupportedConverterTypes sct : dc.getSupportedTypes())
			{
				if (converterTypeMapping.put(sct, dc) != null)
				{
					LOG.warn("Converter type {} is supported by multiple converters???", sct);
				}
			}
		}

		FxUtils.waitWithProgress("Reading Content Files", "Reading available content source files",
				MavenArtifactUtils.readAvailableSourceFiles(storedPrefs, (results) -> {
					for (SDOSourceContent sdo : results)
					{
						SupportedConverterTypes found = SupportedConverterTypes.findBySrcArtifactId(sdo.getArtifactId());
						if (converterTypeMapping.containsKey(found))
						{
							sdoSourceFiles_.add(sdo);
						}
					}

				}), parentWindow);

		sdoPicker.setItems(FXCollections.observableArrayList(sdoSourceFiles_));

		sdoPicker.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		sdoPicker.setCellFactory(param -> new ListCell<SDOSourceContent>()
		{
			@Override
			protected void updateItem(SDOSourceContent item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty || item == null)
				{
					setText(null);
				}
				else
				{
					setText(item.getArtifactId() + (item.hasClassifier() ? " : " + item.getClassifier() : "") + " : " + item.getVersion());
				}
			}
		});

		Alert sdoDialog = new Alert(AlertType.CONFIRMATION);
		sdoDialog.setTitle("Select File");
		sdoDialog.setHeaderText("Select the content to import");
		sdoDialog.getDialogPane().setContent(sdoPicker);
		sdoPicker.setPrefWidth(1024);
		sdoDialog.setResizable(true);
		
		sdoDialog.initOwner(parentWindow);

		if (sdoDialog.showAndWait().orElse(null) == ButtonType.OK)
		{
			SDOSourceContent sdo = sdoPicker.getSelectionModel().getSelectedItem();
			Optional<File> local = sdo.getLocalPath(storedPrefs);
			LOG.info("importing " + local);
			SupportedConverterTypes selected = SupportedConverterTypes.findBySrcArtifactId(sdo.getArtifactId());
			DirectConverter dc = converterTypeMapping.get(selected);

			final TimedTask<Void> tt = new TimedTask<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					Get.activeTasks().add(this);
					try
					{
						this.updateTitle("Importing " + sdo.toString());
						this.updateMessage("Importing artifact");

						FileSystem fs = FileSystems.newFileSystem(local.get().toPath(), null);
						Path root = null;
						for (Path p : fs.getRootDirectories())
						{
							if (root != null)
							{
								LOG.error("Didn't expect more than one root!");
							}
							root = p;
						}

						dc.configure(null, root, sdo.getVersion(), Get.defaultCoordinate());

						//TODO in the future, add the GUI widgets that let the users specify the options.
						//Use the defaults for now, just to get things working...
						if (dc.getConverterOptions() != null)
						{
							for (ConverterOptionParam cop : dc.getConverterOptions())
							{
								if (cop.getDefaultsForDirectMode() != null)
								{
									dc.setConverterOption(cop.getInternalName(), cop.getDefaultsForDirectMode());
								}
							}
						}
						Transaction transaction = Get.commitService().newTransaction(ChangeCheckerMode.INACTIVE);

						dc.convertContent(transaction, string -> updateTitle(string), (work, total) -> updateProgress(work, total));
						transaction.commit();
						fs.close();
						Get.indexDescriptionService().refreshQueryEngine();

						Platform.runLater(() -> {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("The import of " + sdo.getArtifactId() + " has finished");
							alert.setHeaderText("Import complete");
							alert.initOwner(parentWindow);
							alert.setResizable(true);
							alert.showAndWait();
						});
					}
					catch (Exception e)
					{
						LOG.error("Import failure", e);
					}
					Get.activeTasks().remove(this);
					return null;
				}
			};
			Get.workExecutors().getExecutor().execute(tt);
		}
	}
}
