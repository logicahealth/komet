/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.bdb.identifier;

import com.sleepycat.je.Database;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.SpinedIntLongMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.provider.bdb.chronology.BdbProvider;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbIdentifierProvider implements ContainerSequenceService {
   private static final Logger LOG = LogManager.getLogger();
   private BdbProvider bdb;
   private Database database;
   private final SpinedIntLongMap nidContainerSequenceMap = new SpinedIntLongMap();
   private final ConcurrentMap<Integer, AtomicInteger> sequenceGeneratorMap = new ConcurrentHashMap<>();

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting bdb identifier provider.");
      bdb = Get.service(BdbProvider.class);
      database = bdb.getIdentifierDatabase();
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping bdb identifier provider.");
   }
   
   @Override
   public long getContainerSequenceForNid(int nid, int assemblageNid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be negative. Found: " + nid);
      }
      int nidKey = Integer.MAX_VALUE + nid;
      
      long containerSequence = nidContainerSequenceMap.get(nidKey);
      if (containerSequence != Long.MAX_VALUE) {
         return containerSequence;
      }
      if (assemblageNid >= 0) {
         throw new IllegalStateException("assemblageNid must be negative. Found: " + nid);
      }
      final int assemblageKey = Integer.MAX_VALUE + assemblageNid;
      
      AtomicInteger sequenceGenerator = sequenceGeneratorMap.computeIfAbsent(assemblageKey, 
              (key) -> {return new AtomicInteger(1);});
      
      return nidContainerSequenceMap.getAndUpdate(nidKey, (currentValue) -> {
         if (currentValue == Long.MAX_VALUE) {
            long record = assemblageKey;
            record = record << 32;
            record += sequenceGenerator.getAndIncrement();
         }
         return currentValue;
      });
   }

   @Override
   public int getNidForContainerSequence(long containerSequence) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void addUuidForNid(UUID uuid, int nid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ObjectChronologyType getChronologyTypeForNid(int nid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<String> getConceptIdentifierForAuthority(int conceptId, UUID identifierAuthorityUuid, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getNidForProxy(ConceptSpecification conceptProxy) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getNidForUuids(Collection<UUID> uuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getNidForUuids(UUID... uuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<String> getIdentifierForAuthority(int nid, UUID identifierAuthorityUuid, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean hasUuid(Collection<UUID> uuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean hasUuid(UUID... uuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<UUID> getUuidPrimordialForNid(int nid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<UUID> getUuidPrimordialFromId(int id) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public List<UUID> getUuidsForNid(int nid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
}
