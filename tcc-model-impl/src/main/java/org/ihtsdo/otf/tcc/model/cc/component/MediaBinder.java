package org.ihtsdo.otf.tcc.model.cc.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponentBinder;
import org.ihtsdo.otf.tcc.model.cc.media.Media;
import org.ihtsdo.otf.tcc.model.cc.media.MediaRevision;

public class MediaBinder extends ConceptComponentBinder<MediaRevision, Media> {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public MediaBinder() {
		super(new MediaFactory(), encountered, written);
	}
}
