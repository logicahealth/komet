/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.ddo.concept.component.refex.logicgraph;

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;

/**
 *
 * @author kec
 */
@XmlRootElement()
public class LogicGraphChronicleDdo  
    extends RefexChronicleDdo<LogicGraphVersionDdo, LogicGraphSememe> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /**
    * Constructs ...
    *
    */
   public LogicGraphChronicleDdo() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param ss
    * @param concept
    * @param another
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public LogicGraphChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept,
       SememeChronology<LogicGraphSememe> another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getVersionList().get(0));
   }

   /**
    * Method description
    *
    *
    * @param ss
    * @param version
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   protected LogicGraphVersionDdo makeVersion(TaxonomyCoordinate ss,
       LogicGraphSememe version)
           throws IOException, ContradictionException {
      return new LogicGraphVersionDdo(this, ss, version);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.LOGIC;
   }
}
