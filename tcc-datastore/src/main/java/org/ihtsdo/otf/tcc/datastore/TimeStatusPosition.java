/**
 * 
 */
package org.ihtsdo.otf.tcc.datastore;

public class TimeStatusPosition implements Comparable<TimeStatusPosition> {
	
	/**
	 * When converted to a UUID, time is stored in <code>uuid[0]</code>
	 */
	private long time;
	
	/**
	 * When converted to a UUID, statusNid is stored in the lower 32 bits of <code>uuid[1]</code>
	 */
	private int statusNid;
	
	/**
	 * When converted to a UUID, pathNid is stored in the upper 32 bits of <code>uuid[1]</code>
	 */
	private int pathNid;
	
	public static long[] timeStatusPositionToUuid(long time, int statusNid, int pathNid)  {
		long[] uuid = new long[2];
		uuid[0] = time;
		uuid[1] = pathNid;
		uuid[1] = uuid[1] & 0x00000000FFFFFFFFL;
		long statusNidLong = statusNid;
		statusNidLong = statusNidLong & 0x00000000FFFFFFFFL;
		uuid[1] = uuid[1] << 32;
		uuid[1] = uuid[1] | statusNidLong;
		return uuid;
	}

	public static long getTime(long[] data)  {
		return data[0];
	}
	public static int getStatusNid(long[] data)  {
		return (int)  data[1];
	}
	public static int getPathNid(long[] data)  {
		return (int) (data[1] >>> 32);
	}

	private TimeStatusPosition(long[] data) {
		time = data[0];
		statusNid = (int) data[1];
		pathNid  = (int) (data[1] >>> 32);
	}
	
	private TimeStatusPosition(long time, int statusNid, int pathNid) {
		super();
		this.time = time;
		this.statusNid = statusNid;
		this.pathNid = pathNid;
		assert time != 0;
		assert statusNid != 0;
		assert pathNid != 0;
	}
	
	public long[] toUuid() {
		return timeStatusPositionToUuid(time, statusNid, pathNid);
	}
	
	@Override
	public int compareTo(TimeStatusPosition o) {
		if (this.time > o.time) {
			return 1;
		}
		
		if (this.time < o.time) {
			return -1;
		}
		
		if (this.statusNid != o.statusNid) {
			return this.statusNid - o.statusNid;
		}
		
		return this.pathNid - o.pathNid;
	}

	public long getTime() {
		return time;
	}

	public int getStatusNid() {
		return statusNid;
	}

	public int getPathNid() {
		return pathNid;
	}
}