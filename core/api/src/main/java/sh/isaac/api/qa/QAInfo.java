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
 */
package sh.isaac.api.qa;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QAInfo
{
	private Severity severity;
	private int component;
	private String message;
	private String failureContext;

	/**
	 * Used to specify a QA failure
	 * 
	 * @param severity the type of error or notice
	 * @param component the nid of the component that triggered the QA rule
	 * @param message the message from the QA rule
	 * @param failureContext the textual representation of the component that failed the QA rule. For example, if a description failed the QA rule,
	 *            this should contain the text of the description.
	 */
	public QAInfo(Severity severity, int component, String message, String failureContext)
	{
		this.severity = severity;
		this.component = component;
		this.message = message;
		this.failureContext = failureContext;
	}
	
	/**
	 * Used to specify a QA failure
	 * 
	 * @param severity the type of error or notice
	 * @param component the nid of the component that triggered the QA rule
	 * @param message the message from the QA rule
	 */
	public QAInfo(Severity severity, int component, String message)
	{
		this.severity = severity;
		this.component = component;
		this.message = message;
	}

	public Severity getSeverity()
	{
		return severity;
	}

	public int getComponent()
	{
		return component;
	}

	public String getMessage()
	{
		return message;
	}

	public String getFailureContext()
	{
		return failureContext;
	}
}
