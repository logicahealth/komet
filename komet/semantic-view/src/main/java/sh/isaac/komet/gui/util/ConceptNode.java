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
package sh.isaac.komet.gui.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.util.TaskCompleteCallback;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.utility.Frills;
import sh.isaac.utility.SimpleDisplayConcept;
import sh.komet.gui.drag.drop.DragRegistry;
import sh.komet.gui.manifold.Manifold;

/**
 * {@link ConceptNode}
 * 
 *  This class handles the GUI display of concepts with many other useful tidbits, 
 *  such as allowing users to enter UUIDs, SCTIDs, NIDS, or do type ahead searches.
 *  
 *  Validation lookups are background threaded.
 *  
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ConceptNode implements TaskCompleteCallback<ConceptSnapshot>
{
	private static Logger logger = LogManager.getLogger(ConceptNode.class);

	private HBox hbox_;
	private ComboBox<SimpleDisplayConcept> cb_;
	private ProgressIndicator pi_;
	private ImageView lookupFailImage_;
	private ConceptSnapshot c_;
	private ObjectBinding<ConceptSnapshot> conceptBinding_;
	private SimpleDisplayConcept codeSetComboBoxConcept_ = null;
	private SimpleValidBooleanProperty isValid = new SimpleValidBooleanProperty(true, null);
	private boolean flagAsInvalidWhenBlank_ = true;
	private volatile long lookupUpdateTime_ = 0;
	private AtomicInteger lookupsCurrentlyInProgress_ = new AtomicInteger();
	private BooleanBinding isLookupInProgress_ = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return lookupsCurrentlyInProgress_.get() > 0;
		}
	};
	
	private ListChangeListener<SimpleDisplayConcept> listChangeListener_;
	private volatile boolean disableChangeListener_ = false;
	private Function<ConceptChronology, String> descriptionReader_;
	private ObservableList<SimpleDisplayConcept> dropDownOptions_;
	private ContextMenu cm_;
	
	public ConceptNode(ConceptSnapshot initialConcept, boolean flagAsInvalidWhenBlank, Supplier<Manifold> manifoldProvider)
	{
		this(initialConcept, flagAsInvalidWhenBlank, null, null, manifoldProvider);
	}

	/**
	 * descriptionReader is optional
	 * @param initialConcept 
	 * @param flagAsInvalidWhenBlank 
	 * @param dropDownOptions 
	 * @param descriptionReader 
	 * @param manifoldProvider 
	 */
	@SuppressWarnings("deprecation")
	public ConceptNode(ConceptSnapshot initialConcept, boolean flagAsInvalidWhenBlank, ObservableList<SimpleDisplayConcept> dropDownOptions, 
			Function<ConceptChronology, String> descriptionReader, Supplier<Manifold> manifoldProvider)
	{
		c_ = initialConcept;
		//We can't simply use the ObservableList from the CommonlyUsedConcepts, because it infinite loops - there doesn't seem to be a way 
		//to change the items in the drop down without changing the selection.  So, we have this hack instead.
		listChangeListener_ = new ListChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void onChanged(Change<? extends SimpleDisplayConcept> c)
			{
				//TODO I still have an infinite loop here.  Find and fix.
				logger.debug("updating concept dropdown");
				disableChangeListener_ = true;
				SimpleDisplayConcept temp = cb_.getValue();
				cb_.setItems(FXCollections.observableArrayList(dropDownOptions_));
				cb_.setValue(temp);
				cb_.getSelectionModel().select(temp);
				disableChangeListener_ = false;
			}
		};
		descriptionReader_ = (descriptionReader == null ? (conceptVersion) -> 
		{
			return conceptVersion == null ? "" : conceptVersion.getRegularName().orElse(conceptVersion.getFullyQualifiedName());
		} : descriptionReader);
		
		//TODO recently used if there is the notion of a recently-used concept list, use this here instead of a blank list
		dropDownOptions_ = dropDownOptions == null ? FXCollections.observableArrayList() : dropDownOptions;
		dropDownOptions_.addListener(new WeakListChangeListener<SimpleDisplayConcept>(listChangeListener_));
		conceptBinding_ = new ObjectBinding<ConceptSnapshot>()
		{
			@Override
			protected ConceptSnapshot computeValue()
			{
				return c_;
			}
		};
		
		flagAsInvalidWhenBlank_ = flagAsInvalidWhenBlank;
		cb_ = new ComboBox<>();
		cb_.setConverter(new StringConverter<SimpleDisplayConcept>()
		{
			@Override
			public String toString(SimpleDisplayConcept object)
			{
				return object == null ? "" : object.getDescription();
			}

			@Override
			public SimpleDisplayConcept fromString(String string)
			{
				return new SimpleDisplayConcept(string, 0);
			}
		});
		cb_.setValue(new SimpleDisplayConcept("", 0));
		cb_.setEditable(true);
		cb_.setMaxWidth(Double.MAX_VALUE);
		cb_.setPrefWidth(ComboBox.USE_COMPUTED_SIZE);
		cb_.setMinWidth(200.0);
		cb_.setPromptText("Type, drop or select a concept");

		cb_.setItems(FXCollections.observableArrayList(dropDownOptions_));
		cb_.setVisibleRowCount(11);
		
		cm_ = new ContextMenu();
		
		MenuItem copyText = new MenuItem("Copy Description");
		copyText.setGraphic(Images.COPY.createImageView());
		copyText.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				CustomClipboard.set(cb_.getEditor().getText());
			}
		});
		cm_.getItems().add(copyText);

		//TODO common menu builder functionality?
//		CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider() {
//			@Override
//			public Set<Integer> getNIds() {
//				Set<Integer> nids = new HashSet<>();
//				if (c_ != null) {
//					nids.add(c_.getNid());
//				}
//				return nids;
//			}
//		};
//		CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
//		menuBuilder.setInvisibleWhenFalse(isValid);
//		CommonMenus.addCommonMenus(cm_, menuBuilder, nidProvider);
		
		cb_.getEditor().setContextMenu(cm_);

		updateGUI();
		
		new LookAheadConceptPopup(cb_, manifoldProvider);

		if (cb_.getValue().getNid() == 0)
		{
			if (flagAsInvalidWhenBlank_)
			{
				isValid.setInvalid("Concept Required");
			}
		}
		else
		{
			isValid.setValid();
		}

		cb_.valueProperty().addListener(new ChangeListener<SimpleDisplayConcept>()
		{
			@Override
			public void changed(ObservableValue<? extends SimpleDisplayConcept> observable, SimpleDisplayConcept oldValue, SimpleDisplayConcept newValue)
			{
				if (newValue == null)
				{
					logger.debug("Combo Value Changed - null entry");
				}
				else
				{
					logger.debug("Combo Value Changed: {} {}", newValue.getDescription(), newValue.getNid());
				}
				
				if (disableChangeListener_)
				{
					logger.debug("change listener disabled");
					return;
				}
				
				if (newValue == null)
				{
					//This can happen if someone calls clearSelection() - it passes in a null.
					cb_.setValue(new SimpleDisplayConcept("", 0));
					return;
				}
				else
				{
					if (newValue.customLogic() != null && newValue.customLogic().get())
					{
						logger.debug("One time change ignore");
						return;
					}
					//Whenever the focus leaves the combo box editor, a new combo box is generated.  But, the new box will have 0 for an id.  detect and ignore
					if (oldValue != null && oldValue.getDescription().equals(newValue.getDescription()) && newValue.getNid() == 0)
					{
						logger.debug("Not a real change, ignore");
						newValue.setNid(oldValue.getNid());
						return;
					}
					lookup();
				}
			}
		});
		
		//this hack fires an update if the user deleted all the text
		cb_.editorProperty().get().focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (oldValue.booleanValue() && !newValue.booleanValue() && cb_.editorProperty().getValue().getText().length() == 0)
				{
					cb_.setValue(null);
					lookup();
				}
				
			}
		});

		LookupService.get().getService(DragRegistry.class).setupDragAndDrop(cb_, true);
		
		pi_ = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		pi_.visibleProperty().bind(isLookupInProgress_);
		pi_.setPrefHeight(16.0);
		pi_.setPrefWidth(16.0);
		pi_.setMaxWidth(16.0);
		pi_.setMaxHeight(16.0);

		lookupFailImage_ = Images.EXCLAMATION.createImageView();
		lookupFailImage_.visibleProperty().bind(isValid.not().and(isLookupInProgress_.not()));
		Tooltip t = new Tooltip();
		t.textProperty().bind(isValid.getReasonWhyInvalid());
		Tooltip.install(lookupFailImage_, t);

		StackPane sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		sp.getChildren().add(cb_);
		sp.getChildren().add(lookupFailImage_);
		sp.getChildren().add(pi_);
		StackPane.setAlignment(cb_, Pos.CENTER_LEFT);
		StackPane.setAlignment(lookupFailImage_, Pos.CENTER_RIGHT);
		StackPane.setMargin(lookupFailImage_, new Insets(0.0, 30.0, 0.0, 0.0));
		StackPane.setAlignment(pi_, Pos.CENTER_RIGHT);
		StackPane.setMargin(pi_, new Insets(0.0, 30.0, 0.0, 0.0));

		hbox_ = new HBox();
		hbox_.setSpacing(5.0);
		hbox_.setAlignment(Pos.CENTER_LEFT);

		hbox_.getChildren().add(sp);
		HBox.setHgrow(sp, Priority.SOMETIMES);
	}
	
	public void addMenu(MenuItem mi)
	{
		cm_.getItems().add(mi);
	}

	private void updateGUI()
	{
		//	Note - this can only be read once - if it returns true after the first call, 
		//	 it resets itself to false for every subsequent call.  It will only return true once 
		Supplier<Boolean> customLogic = new Supplier<Boolean>()
		{
			AtomicInteger ai = new AtomicInteger(1);
			public Boolean get()
			{
				return ai.getAndDecrement() == 1;
			}
		};
		
		logger.debug("update gui - is concept null? {}", c_ == null);
		if (c_ == null)
		{
			//Keep the user entry, if it was invalid, so they can edit it.
			codeSetComboBoxConcept_ = new SimpleDisplayConcept((cb_.getValue() != null ? cb_.getValue().getDescription() : ""), 0, customLogic);
			cb_.setTooltip(null);
		}
		else
		{
			codeSetComboBoxConcept_ = new SimpleDisplayConcept(descriptionReader_.apply(c_.getChronology()), c_.getNid(), customLogic);
			
			//In case the description is too long, also put it in a tooltip
			Tooltip t = new Tooltip(codeSetComboBoxConcept_.getDescription());
			cb_.setTooltip(t);
		}
		cb_.setValue(codeSetComboBoxConcept_);
	}

	private synchronized void lookup()
	{
		lookupsCurrentlyInProgress_.incrementAndGet();
		isLookupInProgress_.invalidate();
		if (cb_.getValue().getNid() != 0)
		{
			Frills.lookupConceptSnapshot(cb_.getValue().getNid(), this, null, null, null);
		}
		else
		{
			Frills.lookupConceptForUnknownIdentifier(cb_.getValue().getDescription(), this, null, null, null);
		}
	}

	public HBox getNode()
	{
		return hbox_;
	}

	public ConceptSnapshot getConcept()
	{
		if (isLookupInProgress_.get())
		{
			synchronized (lookupsCurrentlyInProgress_)
			{
				while (lookupsCurrentlyInProgress_.get() > 0)
				{
					try
					{
						lookupsCurrentlyInProgress_.wait();
					}
					catch (InterruptedException e)
					{
						// noop
					}
				}
			}
		}
		return c_;
	}
	
	public ConceptSnapshot getConceptNoWait()
	{
		return c_;
	}
	
	protected String getDisplayedText()
	{
		return cb_.getValue().getDescription();
	}

	protected void set(String newValue)
	{
		cb_.setValue(new SimpleDisplayConcept(newValue, 0));
	}
	
	public void set(ConceptSnapshot newValue)
	{
		if (newValue == null)
		{
			clear();
		}
		else
		{
			cb_.setValue(new SimpleDisplayConcept(newValue.getChronology(), descriptionReader_));
		}
	}
	
	public void set(SimpleDisplayConcept newValue)
	{
		if (newValue == null)
		{
			cb_.setValue(new SimpleDisplayConcept("", 0));
		}
		else
		{
			cb_.setValue(newValue);
		}
	}
	
	public SimpleValidBooleanProperty isValid()
	{
		return isValid;
	}
	
	public void revalidate()
	{
		lookup();
	}
	
	@Override
	public void taskComplete(final ConceptSnapshot concept, final long submitTime, Integer callId)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				logger.debug("lookupComplete - found '{}'", (concept == null  ? "-null-" : concept.toUserString()));
				synchronized (lookupsCurrentlyInProgress_)
				{
					lookupsCurrentlyInProgress_.decrementAndGet();
					isLookupInProgress_.invalidate();
					lookupsCurrentlyInProgress_.notifyAll();
				}
				
				if (submitTime < lookupUpdateTime_)
				{
					// Throw it away, we already got back a newer lookup.
					logger.debug("throwing away a lookup");
					return;
				}
				else
				{
					lookupUpdateTime_ = submitTime;
				}

				if (concept != null)
				{
					c_ = concept;
					//TODO recently used list update recently used concept list
					isValid.setValid();
				}
				else
				{
					// lookup failed
					c_ = null;
					if (StringUtils.isNotBlank(cb_.getValue().getDescription()))
					{
						isValid.setInvalid("The specified concept was not found in the database");
					}
					else if (flagAsInvalidWhenBlank_)
					{
						isValid.setInvalid("Concept required");
					}
					else
					{
						isValid.setValid();
					}
				}
				updateGUI();
				conceptBinding_.invalidate();
			}
		});
	}
	
	public void setPromptText(String promptText)
	{
		cb_.setPromptText(promptText);
	}
	
	public ObjectBinding<ConceptSnapshot> getConceptProperty()
	{
		return conceptBinding_;
	}
	
	public void clear()
	{
		logger.debug("Clear called");
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				cb_.setValue(new SimpleDisplayConcept("", 0));
			}
		};
		
		if (Platform.isFxApplicationThread())
		{
			r.run();
		}
		else
		{
			Platform.runLater(r);
		}
	}

	/**
	 * If, for some reason, you want a concept node selection box that is completely disabled - call this method after constructing
	 * the concept node.  Though one wonders, why you wouldn't just use a label in this case....
	 */
	public void disableEdit() {
		LookupService.get().getService(DragRegistry.class).removeDragCapability(cb_);
		cb_.setEditable(false);
		dropDownOptions_.removeListener(listChangeListener_);
		listChangeListener_ = null;
		cb_.setItems(FXCollections.observableArrayList());
		cb_.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(0), new Insets(0))));
	}
}
