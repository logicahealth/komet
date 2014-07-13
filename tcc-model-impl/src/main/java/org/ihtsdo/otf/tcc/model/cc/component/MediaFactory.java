package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.DataInputStream;
import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.component.ComponentFactory;
import org.ihtsdo.otf.tcc.model.cc.media.Media;
import org.ihtsdo.otf.tcc.model.cc.media.MediaRevision;

public class MediaFactory extends ComponentFactory<MediaRevision, Media> {

	@Override
	public Media create(ConceptChronicle enclosingConcept, 
			DataInputStream input) throws IOException {
		return new Media(enclosingConcept, 
				input);
	}

}