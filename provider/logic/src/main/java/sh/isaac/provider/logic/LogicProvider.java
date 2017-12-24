/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.provider.logic;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.DataSource;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.classify.ClassifierProvider;
import sh.isaac.api.component.semantic.SemanticSnapshotService;

//~--- classes ----------------------------------------------------------------
/**
 * The Class LogicProvider.
 *
 * @author kec
 */
@Service(name = "logic provider")
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class LogicProvider
        implements LogicService {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   /**
    * The Constant classifierServiceMap.
    */
   private static final Map<ClassifierServiceKey, ClassifierService> classifierServiceMap = new ConcurrentHashMap<>();

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new logic provider.
    */
   private LogicProvider() {
      // For HK2
      LOG.info("logic provider constructed");
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting LogicProvider.");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping LogicProvider.");
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the classifier service.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    * @param editCoordinate the edit coordinate
    * @return the classifier service
    */
   @Override
   public ClassifierService getClassifierService(StampCoordinate stampCoordinate,
           LogicCoordinate logicCoordinate,
           EditCoordinate editCoordinate) {
      final ClassifierServiceKey key = new ClassifierServiceKey(stampCoordinate, logicCoordinate, editCoordinate);

      if (!classifierServiceMap.containsKey(key)) {
         classifierServiceMap.putIfAbsent(key,
                 new ClassifierProvider(stampCoordinate, logicCoordinate, editCoordinate));
      }

      return classifierServiceMap.get(key);
   }

   /**
    * Gets the logical expression.
    *
    * @param conceptId the concept id
    * @param logicAssemblageId the logic assemblage id
    * @param stampCoordinate the stamp coordinate
    * @return the logical expression
    */
   @Override
   public LatestVersion<? extends LogicalExpression> getLogicalExpression(int conceptId,
           int logicAssemblageId,
           StampCoordinate stampCoordinate) {
      final SemanticSnapshotService<LogicGraphVersionImpl> ssp = Get.assemblageService()
              .getSnapshot(LogicGraphVersionImpl.class,
                      stampCoordinate);

      List<LatestVersion<LogicalExpression>> latestExpressions = new ArrayList<>();
      final List<LatestVersion<LogicGraphVersionImpl>> latestVersions
              = ssp.getLatestSemanticVersionsForComponentFromAssemblage(conceptId,
                      logicAssemblageId);
      for (LatestVersion<LogicGraphVersionImpl> lgs : latestVersions) {
         final LogicalExpression expressionValue
                 = new LogicalExpressionImpl(lgs.get().getGraphData(),
                         DataSource.INTERNAL,
                         lgs.get().getReferencedComponentNid());

         final LatestVersion<LogicalExpression> latestExpressionValue
                 = new LatestVersion<>(expressionValue);

         lgs.contradictions().forEach((LogicGraphVersionImpl contradiction) -> {
            final LogicalExpressionImpl contradictionValue
                    = new LogicalExpressionImpl(contradiction.getGraphData(),
                            DataSource.INTERNAL,
                            contradiction.getReferencedComponentNid());

            latestExpressionValue.addLatest(contradictionValue);
         });

         latestExpressions.add(latestExpressionValue);
      }
      if (latestExpressions.isEmpty()) {
         LOG.warn("No logical expression for: " + Get.conceptDescriptionText(conceptId) + " in: "
                 + Get.conceptDescriptionText(logicAssemblageId) + "\n\n"
                 + Get.conceptService().getConceptChronology(conceptId).toString());
         return new LatestVersion<>();
      } else if (latestExpressions.size() > 1) {
         throw new IllegalStateException("More than one logical expression for concept in assemblage: "
                 + latestVersions);
      }

      return latestExpressions.get(0);
   }

   //~--- inner classes -------------------------------------------------------
   /**
    * The Class ClassifierServiceKey.
    */
   private static class ClassifierServiceKey {

      /**
       * The stamp coordinate.
       */
      StampCoordinate stampCoordinate;

      /**
       * The logic coordinate.
       */
      LogicCoordinate logicCoordinate;

      /**
       * The edit coordinate.
       */
      EditCoordinate editCoordinate;

      //~--- constructors -----------------------------------------------------
      /**
       * Instantiates a new classifier service key.
       *
       * @param stampCoordinate the stamp coordinate
       * @param logicCoordinate the logic coordinate
       * @param editCoordinate the edit coordinate
       */
      public ClassifierServiceKey(StampCoordinate stampCoordinate,
              LogicCoordinate logicCoordinate,
              EditCoordinate editCoordinate) {
         this.stampCoordinate = stampCoordinate;
         this.logicCoordinate = logicCoordinate;
         this.editCoordinate = editCoordinate;
      }

      //~--- methods ----------------------------------------------------------
      /**
       * Equals.
       *
       * @param obj the obj
       * @return true, if successful
       */
      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }

         if (getClass() != obj.getClass()) {
            return false;
         }

         final ClassifierServiceKey other = (ClassifierServiceKey) obj;

         if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
            return false;
         }

         if (!Objects.equals(this.logicCoordinate, other.logicCoordinate)) {
            return false;
         }

         return Objects.equals(this.editCoordinate, other.editCoordinate);
      }

      /**
       * Hash code.
       *
       * @return the int
       */
      @Override
      public int hashCode() {
         int hash = 3;

         hash = 59 * hash + Objects.hashCode(this.logicCoordinate);
         return hash;
      }
   }
}
