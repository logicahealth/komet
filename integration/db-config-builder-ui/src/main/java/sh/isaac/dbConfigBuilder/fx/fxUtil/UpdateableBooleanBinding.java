/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.dbConfigBuilder.fx.fxUtil;

import java.util.HashSet;
import com.sun.javafx.binding.BindingHelperObserver;
import javafx.beans.Observable;

/**
 * {@link UpdateableBooleanBinding}
 * 
 * No idea why BooleanBinding has these variations of these methods that are protected and final...
 * And the remove was implemented in such a way that you can't remove individual items.
 * (because they nulled themselves after a remove). Copied code here, fixed to allow individual
 * removals.
 * 
 * *** WARNING *** - make _sure_ you maintain a reference to your UpdateableBooleanBinding object.
 * Because the addBinding mechanism makes use of WeakReferences - if you don't maintain a reference,
 * the binding will be dropped at a random point - and you will stop getting invalidation calls!
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public abstract class UpdateableBooleanBinding extends ValidBooleanBinding
{
	private BindingHelperObserver observer;
	protected HashSet<Observable> listeningTo = new HashSet<>();

	public final void addBinding(Observable... dependencies)
	{
		if ((dependencies != null) && (dependencies.length > 0))
		{
			if (observer == null)
			{
				observer = new BindingHelperObserver(this);
			}
			for (final Observable dep : dependencies)
			{
				dep.addListener(observer);
				listeningTo.add(dep);
			}
			invalidate();
		}
	}

	/**
	 * Stop observing the dependencies for changes.
	 * 
	 * @param dependencies
	 *            the dependencies to stop observing
	 */
	public final void removeBinding(Observable... dependencies)
	{
		if (observer != null)
		{
			for (final Observable dep : dependencies)
			{
				dep.removeListener(observer);
				listeningTo.remove(dep);
			}
			if (listeningTo.size() == 0)
			{
				observer = null;
			}
			invalidate();
		}
	}

	public final void clearBindings()
	{
		while (listeningTo.size() > 0)
		{
			removeBinding(listeningTo.iterator().next());
		}
	}

	/**
	 * Iterate through all current bindings, return the message associated with the binding (if the binding is invalid)
	 * otherwise, return an empty string.
	 * 
	 * This method only works if all of the binding targets are {@link ValidBooleanBinding} objects - otherwise, throws a RuntimeException.
	 * 
	 * @return an empty string, or the invalid reason
	 */
	public String getInvalidReasonFromAllBindings()
	{
		for (Observable b : listeningTo)
		{
			if (b instanceof ValidBooleanBinding)
			{
				ValidBooleanBinding vbb = (ValidBooleanBinding) b;
				if (!vbb.get())
				{
					return vbb.getReasonWhyInvalid().get();
				}
			}
			else
			{
				throw new RuntimeException("The method getInvalidReasonFromAllBindings can only be used if all of the binding are type 'ValidBooleanBinding'");
			}
		}
		return "";
	}

	/**
	 * Iterate through all current bindings, return true if all are valid, false otherwise.
	 * 
	 * This method only works if all of the binding targets are {@link ValidBooleanBinding} objects - otherwise, throws a RuntimeException.
	 * 
	 * @return true if all bindings valid
	 */
	public boolean allBindingsValid()
	{
		for (Observable b : listeningTo)
		{
			if (b instanceof ValidBooleanBinding)
			{
				ValidBooleanBinding vbb = (ValidBooleanBinding) b;
				if (!vbb.get())
				{
					return false;
				}
			}
			else
			{
				throw new RuntimeException("The method getInvalidReasonFromAllBindings can only be used if all of the binding are type 'ValidBooleanBinding'");
			}
		}
		return true;
	}
}
