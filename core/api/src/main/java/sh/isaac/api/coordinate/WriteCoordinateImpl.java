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

import java.time.Instant;
import java.util.Optional;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.transaction.Transaction;

/**
 * Transaction is an optional part of this class, to aid in building, but since it isn't part of the actual
 * coordinates that are written, it is not included in the .equals or .hashcode calculations.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class WriteCoordinateImpl implements WriteCoordinate
{
	final int authorNid, moduleNid, pathNid;
	final Optional<Transaction> transaction;
	final Optional<Status> status;
	final Optional<Long> time;
	
	public WriteCoordinateImpl(Status status, long time, int authorNid, int moduleNid, int pathNid, Transaction transaction)
	{
		this.status = Optional.of(status);
		this.time = Optional.of(time);
		this.authorNid = authorNid;
		this.moduleNid = moduleNid;
		this.pathNid = pathNid;
		this.transaction = Optional.ofNullable(transaction);
	}
	
	public WriteCoordinateImpl(long time, int authorNid, int moduleNid, int pathNid, Transaction transaction)
	{
		this.status = Optional.empty();
		this.time = Optional.of(time);
		this.authorNid = authorNid;
		this.moduleNid = moduleNid;
		this.pathNid = pathNid;
		this.transaction = Optional.ofNullable(transaction);
	}

	public WriteCoordinateImpl(int authorNid, int moduleNid, int pathNid, Transaction transaction)
	{
		this.status = Optional.empty();
		this.time = Optional.empty();
		this.authorNid = authorNid;
		this.moduleNid = moduleNid;
		this.pathNid = pathNid;
		this.transaction = Optional.ofNullable(transaction);
	}

	public WriteCoordinateImpl(int authorNid, int moduleNid, int pathNid)
	{
		this(authorNid, moduleNid, pathNid, null);
	}

	public WriteCoordinateImpl(Transaction transaction, int stampSequence)
	{
		Stamp s = Get.stampService().getStamp(stampSequence);
		this.status = Optional.of(s.getStatus());
		this.time = Optional.of(s.getTime());
		this.authorNid = s.getAuthorNid();
		this.moduleNid = s.getModuleNid();
		this.pathNid = s.getPathNid();
		this.transaction = Optional.ofNullable(transaction);
		
	}

	/**
	 * @param writeCoordinate Values to use in the new write coordinate, except for status
	 * @param status status to use in the new WriteCoordinate
	 */
	public WriteCoordinateImpl(WriteCoordinate writeCoordinate, Status status)
	{
		this.status = Optional.of(status);
		this.time = Optional.empty();
		this.authorNid = writeCoordinate.getAuthorNid();
		this.moduleNid = writeCoordinate.getModuleNid();
		this.pathNid = writeCoordinate.getPathNid();
		this.transaction = writeCoordinate.getTransaction();
		
	}

	/**
	 * @param newTransaction
	 * @param writeCoordinate
	 */
	public WriteCoordinateImpl(Transaction newTransaction, WriteCoordinate writeCoordinate)
	{
		this.status = Optional.of(writeCoordinate.getStatus());
		this.time = Optional.of(writeCoordinate.getTime());
		this.authorNid = writeCoordinate.getAuthorNid();
		this.moduleNid = writeCoordinate.getModuleNid();
		this.pathNid = writeCoordinate.getPathNid();
		this.transaction = Optional.of(newTransaction);
	}

	@Override
	public int getAuthorNid()
	{
		return authorNid;
	}

	@Override
	public int getModuleNid()
	{
		return moduleNid;
	}

	@Override
	public int getPathNid()
	{
		return pathNid;
	}

	@Override
	public Optional<Transaction> getTransaction()
	{
		return transaction;
	}

	@Override
	public Status getStatus()
	{
		return status.orElse(WriteCoordinate.super.getStatus());
	}
	
	@Override
	public long getTime()
	{
		return time.orElse(WriteCoordinate.super.getTime());
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("WriteCoordinate{s:");
		sb.append(getStatus());
		sb.append(", t:");
		if (getTime() == Long.MAX_VALUE)
		{
			sb.append(" UNCOMMITTED");
		}
		else if (getTime() == Long.MIN_VALUE)
		{
			sb.append(" CANCELED");
		}
		else
		{
			sb.append(Instant.ofEpochMilli(getTime()));
		}
		sb.append(", a:");
		sb.append(Get.conceptDescriptionText(getAuthorNid()));
		sb.append(", m:");
		sb.append(Get.conceptDescriptionText(getModuleNid()));
		sb.append(", p: ");
		sb.append(Get.conceptDescriptionText(getPathNid()));
		sb.append(", ");
		sb.append(getTransaction().isEmpty() ? "no transaction" : getTransaction().get().toString());
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + authorNid;
		result = prime * result + moduleNid;
		result = prime * result + pathNid;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WriteCoordinateImpl other = (WriteCoordinateImpl) obj;
		if (authorNid != other.authorNid)
			return false;
		if (moduleNid != other.moduleNid)
			return false;
		if (pathNid != other.pathNid)
			return false;
		return true;
	}
}
