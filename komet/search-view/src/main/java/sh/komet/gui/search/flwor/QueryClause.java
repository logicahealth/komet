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
package sh.komet.gui.search.flwor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.query.Clause;
import sh.komet.gui.manifold.Manifold;

//~--- inner classes -------------------------------------------------------
public class QueryClause {

   SimpleObjectProperty<Clause> clauseProperty;
   SimpleStringProperty clauseName;
   SimpleObjectProperty<QueryClauseParameter> parameter;
   Manifold manifold;

   //~--- constructors -----------------------------------------------------
   protected QueryClause(Clause clause, Manifold manifold) {
      this.manifold = manifold;
      this.clauseProperty = new SimpleObjectProperty<>(this, "clauseProperty", clause);
      this.parameter = new SimpleObjectProperty<>(this, "parameter", new QueryClauseParameter());
      this.clauseName = new SimpleStringProperty(this, "clauseName", manifold.getManifoldCoordinate().getPreferredDescriptionText(clause.getClauseConcept()));
      this.clauseProperty.addListener(
              (javafx.beans.value.ObservableValue<? extends sh.isaac.api.query.Clause> ov, sh.isaac.api.query.Clause oldClause, sh.isaac.api.query.Clause newClause)
                      -> this.clauseName.setValue(manifold.getManifoldCoordinate().getPreferredDescriptionText(newClause.getClauseConcept())));
   }

   //~--- methods ----------------------------------------------------------
   public SimpleObjectProperty<QueryClauseParameter> parameterProperty() {
      return parameter;
   }

   @Override
   public String toString() {
      return clauseName.get();
   }

   //~--- get methods ------------------------------------------------------
   public Clause getClause() {
      return clauseProperty.get();
   }

   public String getName() {
      return clauseName.getValue();
   }

}
