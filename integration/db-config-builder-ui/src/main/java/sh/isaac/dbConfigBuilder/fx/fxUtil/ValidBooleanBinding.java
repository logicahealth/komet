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

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * {@link ValidBooleanBinding}
 * 
 * A BooleanBinding that also carries with it the reason why the boolean field is set to false,
 * which is very useful when this is used for disabling GUI buttons, or showing error markers.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class ValidBooleanBinding extends BooleanBinding
{
	private ReadOnlyStringWrapper reasonWhyInvalid_ = new ReadOnlyStringWrapper("");
	private boolean computeOnInvalidate_ = false;

	/**
	 * Passing in an empty string is the same as {@link #clearInvalidReason()}
	 * 
	 * @param reason
	 */
	protected void setInvalidReason(String reason)
	{
		reasonWhyInvalid_.set(reason);
	}

	protected void clearInvalidReason()
	{
		reasonWhyInvalid_.set("");
	}

	public ReadOnlyStringProperty getReasonWhyInvalid()
	{
		return reasonWhyInvalid_.getReadOnlyProperty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onInvalidating()
	{
		super.onInvalidating();
		if (computeOnInvalidate_)
		{
			get();
		}
	}

	/**
	 * convenience method to let implementers choose to compute on invalidate,
	 * rather than on the next request, which is the default behavior.
	 * 
	 * @param computeOnInvalidate
	 */
	protected void setComputeOnInvalidate(boolean computeOnInvalidate)
	{
		computeOnInvalidate_ = computeOnInvalidate;
		get();
	}
}
