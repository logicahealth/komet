/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.komet.gui.interfaces;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link IsaacAppConfigI}
 * 
 * This interface only exists as a mechanism to provide read-only access to the
 * items generated out of the AppConfigSchema.xsd file. This interface should be
 * kept in sync with the definitions within the AppConfigSchema.xsd file - as
 * that is what end users will be writing - which eventually populates the
 * values that return from these getters.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface IsaacAppConfigI {

  /**
   * An optional field the specifies what archetype artifact produced this application bundle.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeGroupId();

  /**
   * An optional field the specifies what archetype artifact produced this application bundle.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeArtifactId();

  /**
   * An optional field the specifies what archetype artifact produced this application bundle.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getArchetypeVersion();

  /**
   * ISAAC toolkit version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getIsaacVersion();
  
  /**
   * ISAAC JavaFx GUI toolkit version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getIsaacFxGUIVersion();
  
  /**
   * The version of the entire ISAAC assembly.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getAssemblyVersion();

  /**
   * Browsable URL for PA project code.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getScmUrl();

  /**
   * Browsable URL for PA project code.
   * 
   * @return a URL
   */
  public URL getScmUrlAsURL();

  /**
   * Database group id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbGroupId();

  /**
   * Database artifact id.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbArtifactId();

  /**
   * Database version.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbVersion();

  /**
   * Database classifier.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbClassifier();

  /**
   * Database archetype type.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDbType();

  /**
   * The text string that is displayed in the ISAAC title bar, about box, and
   * other typical locations.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getApplicationTitle();


  /**
   * The default SVN or GIT URL that will be used to synchronize user profiles and
   * changesets for this bundle.  With SSH urls, such as "ssh://someuser@csfe.aceworkspace.net..." 
   * the contents between 'ssh://' and '@' should be replaced with the currently logged in user's
   * syncUserName - as specified in that users profile before using this value.  
   * 
   * This may may differ from {@link #getDefaultChangeSetUrl()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultChangeSetUrl();
  
  /**
   * The current SVN or GIT URL that will be used to synchronize user profiles and
   * changesets for this bundle.  With SSH urls, such as "ssh://someuser@csfe.aceworkspace.net..." 
   * the contents between 'ssh://' and '@' should be replaced with the currently logged in user's
   * syncUserName - as specified in that users profile before using this value.  
   * 
   * This may may differ from {@link #getDefaultChangeSetUrl()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentChangeSetUrl();

  /**
   * The type of sync service to use.  Currently, returns either SVN or GIT.W
   * @return the change set url type name
   */
  public String getChangeSetUrlTypeName();


  /**
   * Current edit path name.
   * 
   * This may differ from {@link #getDefaultEditPathName()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentEditPathName();


  /**
   * Current edit path uuid.
   * 
   * This may differ from {@link #getDefaultEditPathUuid()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentEditPathUuid();


  /**
   * Current view path name.
   * 
   * This may differ from {@link #getDefaultViewPathName()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentViewPathName();


  /**
   * Current view path uuid.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentViewPathUuid();

  /**
   * The default full URL for the REST API of the KIE Workflow server.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultWorkflowServerUrl();
  
  /**
   * The current full URL for the REST API of the KIE Workflow server.
   * 
   * This may differ from {@link #getDefaultWorkflowServerUrl()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentWorkflowServerUrl();

  /**
   * The default full URL for the REST API of the KIE Workflow server.
   * 
   * @return possible object is {@link String }
   * 
   */
  public URL getDefaultWorkflowServerUrlAsURL();
  
  /**
   * The current full URL for the REST API of the KIE Workflow server.
   * 
   * @return possible object is {@link String }
   * 
   */
  public URL getCurrentWorkflowServerUrlAsURL();

  /**
   * The default deployment ID for the KIE workflow server.
   * 
   * This may differ from {@link #getCurrentWorkflowServerUrlAsURL()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   */
  public String getDefaultWorkflowServerDeploymentId();
  
  /**
   * The default deployment ID for the KIE workflow server.
   *
   * @return possible object is {@link String }
   */
  public String getCurrentWorkflowServerDeploymentId();

  /**
   * The default name for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * This may differ from {@link #getCurrentWorkflowServerDeploymentId()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultWorkflowPromotionPathName();
  
  /**
   * The default name for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentWorkflowPromotionPathName();

  /**
   * The default UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * This may differ from {@link #getCurrentWorkflowPromotionPathName()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDefaultWorkflowPromotionPathUuid();
  
  /**
   * The current UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * This may differ from {@link #getDefaultWorkflowPromotionPathUuid()} if the user has changed the value.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCurrentWorkflowPromotionPathUuid();

  /**
   * The default UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * @return the UUID
   */
  public UUID getDefaultWorkflowPromotionPathUuidAsUUID();
  
  /**
   * The current UUID for the Path to which content published via Workflow will
   * automatically be promoted to.
   * 
   * This may differ from {@link #getDefaultWorkflowPromotionPathUuidAsUUID()} if the user has changed the value.
   * 
   * @return the UUID
   */
  public UUID getCurrentWorkflowPromotionPathUuidAsUUID();

  /**
   * Returns the url for string.
   *
   * @param value the value
   * @return the url for string
   */
  public static URL getUrlForString(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      return null;
    }
  }


  /**
   * Returns the uuid for string.
   *
   * @param value the value
   * @return the uuid for string
   */
  public static UUID getUuidForString(String value) {
    if (value == null) {
      return null;
    }
    return UUID.fromString(value);
  }
}
