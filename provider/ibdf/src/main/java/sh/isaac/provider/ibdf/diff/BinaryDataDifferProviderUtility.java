/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package sh.isaac.provider.ibdf.diff;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.MutableComponentNidVersion;
import sh.isaac.api.component.semantic.version.MutableDescriptionVersion;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLongVersion;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.model.configuration.StampCoordinates;

/**
 * Utility methods in support of BinaryDataDifferProvider used to see if two components are the same and to create new versions when necessary.
 * 
 * {@link BinaryDataDifferProvider}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class BinaryDataDifferProviderUtility {
	static long newImportDate;
	static boolean componentChangeFound = false;
	boolean diffOnStatus;
	boolean diffOnTimestamp;
	boolean diffOnAuthor;
	boolean diffOnModule;
	boolean diffOnPath;
	private SemanticBuilderService<?> SemanticBuilderService_;

	public BinaryDataDifferProviderUtility(Boolean diffOnStatus, Boolean diffOnTimestamp, Boolean diffOnAuthor, Boolean diffOnModule, Boolean diffOnPath) {
		this.diffOnStatus = diffOnStatus;
		this.diffOnTimestamp = diffOnTimestamp;
		this.diffOnAuthor = diffOnAuthor;
		this.diffOnModule = diffOnModule;
		this.diffOnPath = diffOnPath;
		SemanticBuilderService_ = Get.semanticBuilderService();
	}

	public Chronology diff(Chronology oldChron, Chronology newChron, int stampSeq, IsaacObjectType type) {
		List<Version> oldVersions = null;
		List<Version> newVersions = newChron.getVersionList();

		if (oldChron == null) {
			return createNewChronology(newChron, type, stampSeq);
		}

		oldVersions = oldChron.getVersionList();

		boolean addedAVersion = false;
		for (StampedVersion nv : newVersions) {
			boolean equivalenceFound = false;

			for (StampedVersion ov : oldVersions) {
				if (isEquivalent(ov, nv, type)) {
					equivalenceFound = true;
					break;
				}
			}

			if (!equivalenceFound) {
				addNewActiveVersion(oldChron, nv, type, stampSeq);
				addedAVersion = true;
			}
		}
		
		if (addedAVersion)
		{
			return oldChron;
		}
		else
		{
			return null;  //TODO fix this silly API
		}
	}

	private boolean isEquivalent(StampedVersion ov, StampedVersion nv, IsaacObjectType type) {
		if ((diffOnStatus && ov.getStatus() != nv.getStatus()) || (diffOnTimestamp && ov.getTime() != nv.getTime()) || (diffOnAuthor && ov.getAuthorNid() != nv.getAuthorNid())
				|| (diffOnModule && ov.getModuleNid() != nv.getModuleNid()) || (diffOnPath && ov.getPathNid() != nv.getPathNid())) {
			return false;
		} else if (type == IsaacObjectType.CONCEPT) {
			// No other value to analyze equivalence for a concept, so return
			// true
			return true;
		} else {
			// Analyze Sememe
			SemanticVersion os = (SemanticVersion) ov;
			SemanticVersion ns = (SemanticVersion) nv;

			if ((os.getAssemblageNid() != ns.getAssemblageNid()) || os.getReferencedComponentNid() != ns.getReferencedComponentNid()) {
				return false;
			}

			switch (os.getChronology().getVersionType()) {
				case COMPONENT_NID:
					return ((ComponentNidVersion) os).getComponentNid() == ((ComponentNidVersion) ns).getComponentNid();
				case DESCRIPTION:
					return ((DescriptionVersion) os).getText().equals(((DescriptionVersion) ns).getText());
				case LONG:
					return ((LongVersion) os).getLongValue() == ((LongVersion) ns).getLongValue();
				case STRING:
					return ((StringVersion) os).getString().equals(((StringVersion) ns).getString());
				case DYNAMIC:
					return ((DynamicVersion) os).dataToString().equals(((DynamicVersion) ns).dataToString());
				case LOGIC_GRAPH:
					return Arrays.deepEquals(((LogicGraphVersion) os).getGraphData(), ((LogicGraphVersion) ns).getGraphData());
				case MEMBER:
					return true;
				case UNKNOWN:
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	private SemanticVersion populateData(SemanticVersion newVer, SemanticVersion originalVersion, int inactiveStampSeq) {
		switch (newVer.getChronology().getVersionType()) {
			case MEMBER:
				return newVer;
			case COMPONENT_NID:
				((MutableComponentNidVersion) newVer).setComponentNid(((ComponentNidVersion) originalVersion).getComponentNid());
				return newVer;
			case DESCRIPTION:
				((MutableDescriptionVersion) newVer).setText(((DescriptionVersion) originalVersion).getText());
				((MutableDescriptionVersion) newVer).setDescriptionTypeConceptNid(((DescriptionVersion) originalVersion).getDescriptionTypeConceptNid());
				((MutableDescriptionVersion) newVer).setCaseSignificanceConceptNid(((DescriptionVersion) originalVersion).getCaseSignificanceConceptNid());
				((MutableDescriptionVersion) newVer).setLanguageConceptNid(((DescriptionVersion) originalVersion).getLanguageConceptNid());
				return newVer;
			case DYNAMIC:
				if (((DynamicVersion) originalVersion).getData() != null && ((DynamicVersion) originalVersion).getData().length > 0) {
					((MutableDynamicVersion) newVer).setData(((DynamicVersion) originalVersion).getData());
				}
				return newVer;
			case LONG:
				((MutableLongVersion) newVer).setLongValue(((LongVersion) originalVersion).getLongValue());
				return newVer;
			case STRING:
				((MutableStringVersion) newVer).setString(((StringVersion) originalVersion).getString());
				return newVer;
			case LOGIC_GRAPH:
				((MutableLogicGraphVersion) newVer).setGraphData(((LogicGraphVersion) originalVersion).getGraphData());
				return newVer;
			case UNKNOWN:
			default:
				throw new UnsupportedOperationException();

		}
	}

	private Chronology createNewChronology(Chronology newChron, IsaacObjectType type, int stampSeq) {
		try {
			if (type == IsaacObjectType.CONCEPT) {
				return newChron;
			} else if (type == IsaacObjectType.SEMANTIC) {
				List<Chronology> builtObjects = new ArrayList<>();
				SemanticChronology sememe = null;
				for (StampedVersion version : newChron.getVersionList()) {
					SemanticBuilder<?> builder = getBuilder((SemanticVersion) version);
					sememe = (SemanticChronology) builder.build(stampSeq, builtObjects);
				}

				return sememe;
			} else {
				throw new Exception("Unsupported IsaacObjectType: " + type);
			}
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	private SemanticBuilder<? extends SemanticChronology> getBuilder(SemanticVersion version) {
		SemanticBuilder<? extends SemanticChronology> builder = null;

		switch (version.getChronology().getVersionType()) {
			case COMPONENT_NID:
				ComponentNidVersion compNidSememe = (ComponentNidVersion) version;
				builder = SemanticBuilderService_.getComponentSemanticBuilder(compNidSememe.getComponentNid(), compNidSememe.getReferencedComponentNid(),
						compNidSememe.getAssemblageNid());
				break;
			case DESCRIPTION:
				DescriptionVersion descSememe = (DescriptionVersion) version;
				builder = SemanticBuilderService_.getDescriptionBuilder(descSememe.getCaseSignificanceConceptNid(), descSememe.getLanguageConceptNid(),
						descSememe.getDescriptionTypeConceptNid(), descSememe.getText(), descSememe.getReferencedComponentNid());
				break;
			case DYNAMIC:
				DynamicVersion<?> dynSememe = (DynamicVersion<?>) version;
				builder = SemanticBuilderService_.getDynamicBuilder(dynSememe.getReferencedComponentNid(), dynSememe.getAssemblageNid(), dynSememe.getData());
				break;
			case LONG:
				LongVersion longSememe = (LongVersion) version;
				builder = SemanticBuilderService_.getLongSemanticBuilder(longSememe.getLongValue(), longSememe.getReferencedComponentNid(), longSememe.getAssemblageNid());
				break;
			case MEMBER:
				builder = SemanticBuilderService_.getMembershipSemanticBuilder(version.getReferencedComponentNid(), version.getAssemblageNid());
				break;
			case STRING:
				StringVersion stringSememe = (StringVersion) version;
				builder = SemanticBuilderService_.getStringSemanticBuilder(stringSememe.getString(), stringSememe.getReferencedComponentNid(), stringSememe.getAssemblageNid());
				break;
			case LOGIC_GRAPH:
				LogicGraphVersion logicGraphSememe = (LogicGraphVersion) version;
				builder = SemanticBuilderService_.getLogicalExpressionBuilder(logicGraphSememe.getLogicalExpression(), logicGraphSememe.getReferencedComponentNid(),
						logicGraphSememe.getAssemblageNid());
				break;
			case UNKNOWN:
			default:
				throw new UnsupportedOperationException();
		}

		builder.setPrimordialUuid(version.getPrimordialUuid());

		return builder;
	}

	public void addNewInactiveVersion(IsaacExternalizable oldChron, IsaacObjectType type, int inactiveStampSeq) {

		if (type == IsaacObjectType.CONCEPT) {

			Version latestVersion = ((Chronology) oldChron).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly()).get();
			latestVersion.getChronology().createMutableVersion(inactiveStampSeq);
		} else if (type == IsaacObjectType.SEMANTIC) {
			Version originalVersion = ((Chronology) oldChron).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly()).get();
			SemanticVersion createdVersion = ((Chronology) oldChron).createMutableVersion(inactiveStampSeq);
			createdVersion = populateData(createdVersion, (SemanticVersion) originalVersion, inactiveStampSeq);
		} else {
			throw new RuntimeException("Jesse didn't finish");
		}
	}

	private void addNewActiveVersion(Chronology oldChron, StampedVersion newVersion, IsaacObjectType type, int activeStampSeq) {
		if (type == IsaacObjectType.CONCEPT) {
			((ConceptChronology) oldChron).createMutableVersion(((ConceptVersion) newVersion).getStampSequence());
		} else if (type == IsaacObjectType.SEMANTIC) {
			SemanticVersion createdVersion = ((SemanticChronology) oldChron).createMutableVersion(((SemanticVersion) newVersion).getStampSequence());
			createdVersion = populateData(createdVersion, (SemanticVersion) newVersion, activeStampSeq);
		} else {
			throw new RuntimeException("Jesse didn't finish");
		}
	}

	public void setNewImportDate(String importDate) {
		// Must be in format of 2005-10-06
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			newImportDate = sdf.parse(importDate).getTime();
		} catch (ParseException e) {
			Date d = new Date();
			newImportDate = d.getTime();
		}
	}

	public long getNewImportDate() {
		return newImportDate;
	}
}
