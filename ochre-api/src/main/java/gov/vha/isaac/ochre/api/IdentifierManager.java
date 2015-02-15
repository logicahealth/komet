/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface IdentifierManager {
    
    int getNidForUuids(UUID... uuids);
    
    UUID getPrimordialUuidForNid(int nid);
    
    List<UUID> getUuidsForNid(int nid) throws IOException;
    
    boolean hasUuid(UUID... uuids);

}
