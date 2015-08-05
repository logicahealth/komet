package org.ihtsdo.otf.tcc.api.chronicle;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.chronicle.IdentifiedObjectUniversal;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ComponentBI extends IdentifiedObjectUniversal {
   boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException;
   
   String toUserString();

   //~--- get methods ---------------------------------------------------------

   Collection<? extends IdBI> getAdditionalIds() throws IOException;

   Collection<? extends IdBI> getAllIds() throws IOException;

   Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException;

   Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz) throws IOException;

   <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, Class<T> cls)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz, int refexNid)
           throws IOException;

   <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz, int refexNid,
           Class<T> cls)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException;

   int getNid();

   Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException;

   Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException;
    
   boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException;

   boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException;
   
   default List<UUID> getUUIDs() {
       return getUuidList();
   }
}
