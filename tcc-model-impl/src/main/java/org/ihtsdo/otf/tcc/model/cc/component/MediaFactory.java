package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.chronicle.cc.media.Media;
import org.ihtsdo.otf.tcc.chronicle.cc.media.MediaRevision;

public class MediaFactory extends ComponentFactory<MediaRevision, Media> {

	@Override
	public Media create(ConceptChronicle enclosingConcept, 
			TupleInput input) throws IOException {
		return new Media(enclosingConcept, 
				input);
	}

}