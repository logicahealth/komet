package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributesRevision;

public class ConceptAttributesBinder extends ConceptComponentBinder<ConceptAttributesRevision, ConceptAttributes> {
	

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public ConceptAttributesBinder() {
		super(new ConceptAttributesFactory(), encountered, written);
	}

}
