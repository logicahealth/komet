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



package sh.isaac.provider.query.lucene;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

//~--- classes ----------------------------------------------------------------

/**
 * {@link PerFieldAnalyzer}
 * An analyzer that indexes everything with the {@link StandardAnalyzer} unless the field ends
 * with the string "_wsa" - in which case, it is indexed with the {@link WhitespaceAnalyzer} and
 * a {@link LowerCaseFilter}
 *
 * This can be enhanced in the future to properly handle other languages as well.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class PerFieldAnalyzer
        extends AnalyzerWrapper {
   /** The Constant WHITE_SPACE_FIELD_MARKER. */
   public static final String WHITE_SPACE_FIELD_MARKER = "_wsa";

   //~--- fields --------------------------------------------------------------

   /** The wa. */
   private final WhitespaceAnalyzer wa = new WhitespaceAnalyzer();

   /** The sa. */
   private final StandardAnalyzer sa = new StandardAnalyzer();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new per field analyzer.
    */
   public PerFieldAnalyzer() {
      super(Analyzer.PER_FIELD_REUSE_STRATEGY);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "PerFieldAnalyzer(default=" + this.sa + ", fields ending with '" + WHITE_SPACE_FIELD_MARKER + "': " +
             this.wa + ")";
   }

   /**
    * Wrap components.
    *
    * @param fieldName the field name
    * @param components the components
    * @return the token stream components
    * @see org.apache.lucene.analysis.AnalyzerWrapper#wrapComponents(java.lang.String, org.apache.lucene.analysis.Analyzer.TokenStreamComponents)
    */
   @Override
   protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
      if (fieldName.endsWith(WHITE_SPACE_FIELD_MARKER)) {
         return new TokenStreamComponents(components.getTokenizer(), new LowerCaseFilter(components.getTokenStream()));
      } else {
         return components;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the wrapped analyzer.
    *
    * @param fieldName the field name
    * @return the wrapped analyzer
    * @see org.apache.lucene.analysis.AnalyzerWrapper#getWrappedAnalyzer(java.lang.String)
    */
   @Override
   protected Analyzer getWrappedAnalyzer(String fieldName) {
      if (fieldName.endsWith(WHITE_SPACE_FIELD_MARKER)) {
         return this.wa;
      } else {
         return this.sa;
      }
   }
}

