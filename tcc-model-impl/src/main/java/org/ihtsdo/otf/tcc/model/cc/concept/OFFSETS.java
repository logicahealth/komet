/**
 * 
 */
package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public enum OFFSETS {
	FORMAT_VERSION(4, null),
 	DATA_VERSION(8, FORMAT_VERSION),
	ANNOTATION_STYLE_REFSET(1, DATA_VERSION),
	ATTRIBUTES(4, ANNOTATION_STYLE_REFSET),
	DESCRIPTIONS(4, ATTRIBUTES),
	SOURCE_RELS(4, DESCRIPTIONS),
	REFSET_MEMBERS(4, SOURCE_RELS), 
	REFSET_DYNAMIC_MEMBERS(4, REFSET_MEMBERS), 
	DESC_NIDS(4, REFSET_DYNAMIC_MEMBERS), 
	SRC_REL_NIDS(4, DESC_NIDS), 
	IMAGE_NIDS(4, SRC_REL_NIDS), 
	MEMBER_NIDS(4, IMAGE_NIDS),
	IMAGES(4, MEMBER_NIDS),
	DATA_SIZE(4, IMAGES),
	HEADER_SIZE(0, DATA_SIZE);
	
	public static int CURRENT_FORMAT_VERSION = 4;

	
	public int offset;
	public int bytes;
	public OFFSETS prev;
	
	OFFSETS(int bytes, OFFSETS prev) {
		this.bytes = bytes;
		if (prev == null) {
			offset = 0;
		} else {
			offset = prev.offset + prev.bytes;
		}
		this.prev = prev;
	}
	
	public int getOffset(byte[] data) throws IOException {
        ByteArrayInputStream offsetInput = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(offsetInput);
		offsetInput.skip(offset);
		return dis.readInt();
	}

	public int getOffset() {
		return offset;
	}

	public int getBytes() {
		return bytes;
	}
	
	public static int getHeaderSize() {
		return HEADER_SIZE.getOffset();
	}
}