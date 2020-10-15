/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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
package sh.isaac.api.coordinate;

import java.util.Optional;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.transaction.Transaction;

/**
 * Simplified interface for providing coordinates to methods that write content.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface WriteCoordinate
{
	/**
	 * @return The status to use for the version write.  Defaults to Active.
	 */
	public default Status getStatus()
	{
		return Status.ACTIVE;
	}
	
	/**
	 * @return The time to use for the version write.  {@link Long#MAX_VALUE}
	 */
	public default long getTime()
	{
		return Long.MAX_VALUE;
	}
	
	/**
	 * @return The author to use for the version write
	 */
	public int getAuthorNid();
	
	/**
	 * @return The module to use for the version write
	 */
	public int getModuleNid();
	
	/**
	 * @return the path to use for the version write
	 */
	public int getPathNid();
	
	/**
	 * @return The (optional) transaction involved in the version write
	 */
	public Optional<Transaction> getTransaction();
	
	/**
	 * Get (or create) a new stamp sequence, which is properly registered with the Stamp Service.
	 * @return the stamp sequence
	 */
	public default int getStampSequence()
	{
		return Get.stampService().getStampSequence(getTransaction().orElse(null), getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid());
	}
	
	/**
	 * @return A Stamp representation of this Write Coordinate.  Does NOT register this stamp with the stamp service.
	 */
	public default Stamp getStamp() 
	{
		return new Stamp(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid());
	}

	default public EditCoordinateImmutable toEditCoordinate() {
		return EditCoordinateImmutable.make(getAuthorNid(), getModuleNid(), getPathNid(), getModuleNid());
	}
	

	/**
	 * @param transactionName - optional
	 * @return a new WriteCoordinate with a new transaction
	 */
	default public WriteCoordinate startTransaction(String transactionName) 
	{
		return new WriteCoordinateImpl(Get.commitService().newTransaction(transactionName), this);
	}
}
