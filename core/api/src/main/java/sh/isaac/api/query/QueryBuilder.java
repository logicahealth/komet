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
package sh.isaac.api.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * @author kec
 */
public class QueryBuilder  {
   public static final String DEFAULT_MANIFOLD_COORDINATE_KEY = "DEFAULT_MANIFOLD_COORDINATE_KEY";
   int sequence = 0;
   ManifoldCoordinate manifoldCoordinate;
   ParentClause root;
   
   List<ComponentCollectionTypes> forClause = new ArrayList();
   Map<String, Object> letClauses = new HashMap<>();
   List<?> whereClauses = new ArrayList();
   List<Object> orderByClauses = new ArrayList();
   List<Object> returnClauses = new ArrayList();

   public QueryBuilder(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
      letClauses.put(DEFAULT_MANIFOLD_COORDINATE_KEY, manifoldCoordinate);
   }
   
   /**
    * From instead of for, since for is a reserved word...
    * @param forClause
    * @return 
    */
   public QueryBuilder from(ComponentCollectionTypes forClause) {
      this.forClause.add(forClause);
      return this;
   }
   
   public QueryBuilder let(String key, Object value) {
      letClauses.put(key, value);
      return this;
   }


   /**
    * Ordered list of ordering statements, where primary sort 
    * is from first propertySpec, next subsort is by second propertySpec...
    * @param propertySpec
    * @return 
    */
   public QueryBuilder orderBy(Object... propertySpec) {
      orderByClauses.addAll(Arrays.asList(propertySpec));
      return this;
   }
   
   public ParentClause setWhereRoot(ParentClause root) {
      if (this.root != null) {
         throw new IllegalStateException("Root already set to: " + this.root + ". Cannot set root to: " + root);
      }
      this.root = root;
      return this.root;
   }
   
  /**
   * Ordered list of return values
   * @param propertySpec properties to return
   * @return 
   */ 
   public QueryBuilder returnValues(Object... propertySpec) {
      returnClauses.addAll(Arrays.asList(propertySpec));
      return this;
   }
   
   public Query build() {
      
      QueryBuilderQuery query = new QueryBuilderQuery(this.manifoldCoordinate);
      
      
      
      return query;
      
   }
   
   public int getSequence() {
      return sequence++;
   }
   
   class QueryBuilderQuery extends Query {


      public QueryBuilderQuery(ManifoldCoordinate manifoldCoordinate) {
         super(manifoldCoordinate);
         getLetDeclarations().putAll(letClauses);
      }
      
      

      @Override
      public void Let() {
         // TODO
      }

      @Override
      public Clause Where() {
         return root;
      }

      @Override
      protected ForSetSpecification ForSetSpecification() {
         return new ForSetSpecification(forCollectionTypes.toArray(new ComponentCollectionTypes[forCollectionTypes.size()]));
      }
      
      
   }
   
}
   

