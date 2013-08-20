/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

import java.io.IOException;
import java.util.ArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()
public class QueryTest {

    public QueryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRegexQuery() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());
        try {
            Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
                @Override
                protected NativeIdSetBI For() throws IOException {
                    NativeIdSetBI forSet = new ConcurrentBitSet();
                    forSet.add(Snomed.MOTION.getNid());
                    forSet.add(Snomed.ACCELERATION.getNid());
                    forSet.add(Snomed.CENTRIFUGAL_FORCE.getNid());
                    forSet.add(Snomed.CONTINUED_MOVEMENT.getNid());
                    forSet.add(Snomed.DECELERATION.getNid());
                    forSet.add((Snomed.MOMENTUM.getNid()));
                    forSet.add(Snomed.VIBRATION.getNid());
                    return forSet;
                }

                @Override
                protected void Let() throws IOException {
                    let("regex", "[Vv]ibration");
                }

                @Override
                protected Clause Where() {
                    return Or(ConceptForComponent(DescriptionRegexMatch("regex")));
                }
            };

            NativeIdSetBI results = q.compute();
            System.out.println("Regex query result count: " + results.size());
            Assert.assertEquals(1, results.size());


        } catch (IOException ex) {
            Assert.fail(ex.toString());
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
    }

    /*@Test
    public void testDescriptionLuceneMatch() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());

        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("momentum", "momentum");
            }

            @Override
            protected Clause Where() {
                return Or(DescriptionLuceneMatch("momentum"));
            }
        };
        NativeIdSetBI results = q.compute();
        System.out.println(results.size());
        Assert.assertEquals(2, results.size());

    }*/

    /*@Test
    public void testDescriptionLuceneMatch2() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());

        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("Specimen source", "\"Specimen source\"");
            }

            @Override
            protected Clause Where() {
                return Or(DescriptionLuceneMatch("Specimen source"));
            }
        };
        NativeIdSetBI results = q.compute();
        System.out.println(results.size());
        Assert.assertEquals(49, results.size());

    }*/

    @Test
    public void testXor() throws IOException, Exception {

        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("Acceleration", Snomed.ACCELERATION);
                let("Motion", Snomed.MOTION);
            }

            @Override
            protected Clause Where() {
                return Xor(ConceptIsChildOf("Acceleration"), ConceptIsChildOf("Motion"));
            }
        };

        NativeIdSetBI results = q.compute();
        System.out.println("Xor result size: " + results.size());
        Assert.assertEquals(6, results.size());


    }

    @Test
    public void testPreferredTerm() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());

        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                NativeIdSetBI forSet = new ConcurrentBitSet();
                forSet.add(Snomed.STATUS.getNid());
                return forSet;
            }

            @Override
            protected void Let() throws IOException {
                let("status", Snomed.STATUS);
            }

            @Override
            protected Clause Where() {
                return PreferredNameForConcept(ConceptIsKindOf("status"));
            }
        };
        NativeIdSetBI results = q.compute();
        System.out.println("Preferred query result count: " + results.size());
        System.out.println("Preferred nids: " + results.getMin());
        Assert.assertEquals(1, results.size());
    }

    @Ignore
    @Test
    public void testRefsetLuceneMatch() throws IOException, Exception {
        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();

            }

            @Override
            protected void Let() throws IOException {
                let("Body", "Body");
            }

            @Override
            protected Clause Where() {
                return Or(RefsetLuceneMatch("Body"));

            }
        };

        NativeIdSetBI results = q.compute();
        System.out.println("Refset lucene search test: " + results.size());
        Assert.assertEquals(31145, results.size());
    }

    @Test
    public void testFullySpecifiedName() throws IOException, Exception {
        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();

            }

            @Override
            protected void Let() throws IOException {
                let("motion", Snomed.MOTION);
            }

            @Override
            protected Clause Where() {
                return FullySpecifiedNameForConcept(ConceptIsKindOf("motion"));

            }
        };

        NativeIdSetBI results = q.compute();
        System.out.println("Fully specified name test: " + results.size());
        Assert.assertEquals(7, results.size());

    }

    /*@Test
    public void TestRelType() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());
        
        Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {

            @Override
            protected NativeIdSetBI For() throws IOException {
                return Ts.get().getAllConceptNids();
            }

            @Override
            protected void Let() throws IOException {
                let("Associated with", Snomed.ASSOCIATED_WITH);
                let("Diabetes metillus", Snomed.DIABETES_MELLITUS);
            }

            @Override
            protected Clause Where() {
                return Or(RelType("Associated with", "Diabetes metillus"));
            }
        };
        NativeIdSetBI results = q.compute();
        System.out.println("Rel type query result size: " + results.size());
    }*/
    
    @Test
    public void testQuery() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());
        try {
            Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
                @Override
                protected NativeIdSetBI For() throws IOException {
                    return Ts.get().getAllConceptNids();
                }

                @Override
                protected void Let() throws IOException {
                    let("allergic-asthma", Snomed.ALLERGIC_ASTHMA);
                }

                @Override
                protected Clause Where() {
                    return And(ConceptIsKindOf("allergic-asthma"));
                }
            };

            NativeIdSetBI results = q.compute();
            System.out.println("Query result count: " + results.size());
            Assert.assertEquals(11, results.size());


        } catch (IOException ex) {
            Assert.fail(ex.toString());
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
        /*
         System.out.println("Sequence: " + Ts.get().getSequence());
         OrTest orTest = new OrTest();
         Query q2 = orTest.getOrTest();
         NativeIdSetBI results2 = q2.compute();
         System.out.println("Query result count:" + results2.size());
         Assert.assertEquals(11 + 427, results2.size());
         */

        //System.out.println("Sequence: " + Ts.get().getSequence());

        /*
         NotTest notTest = new NotTest();
         Query notQuery = notTest.getQuery();
         NativeIdSetBI notResults = notQuery.compute();
         System.out.println("Query result count: " + notResults.size());
         Assert.assertEquals(5, notResults.size());
         */

        /*
         ChangedFromPreviousVersionTest versionTest = new ChangedFromPreviousVersionTest();
         Query versionQuery = versionTest.getQuery();
         NativeIdSetBI versionResults = versionQuery.compute();
         System.out.println("Changed from previous version result count: " + versionResults.size());
         Assert.assertEquals(5, versionResults.size());
         */

        /*        
         MemberOfRefsetTest refsetTest = new MemberOfRefsetTest();
         Query refsetQuery = refsetTest.getQuery();
         NativeIdSetBI refsetResults = refsetQuery.compute();
         System.out.println("Refset result count: " + refsetResults.size());
         Assert.assertEquals(4, refsetResults.size());
         */
        IsChildOfTest isChildOfTest = new IsChildOfTest();
        Query q3 = isChildOfTest.getQuery();
        NativeIdSetBI results3 = q3.compute();
        System.out.println("Query result count " + results3.size());
        Assert.assertEquals(21, results3.size());

        IsDescendentOfTest isDescendent = new IsDescendentOfTest();
        Query q4 = isDescendent.getQuery();
        NativeIdSetBI results4 = q4.compute();
        System.out.println("Query result count " + results4.size());
        Assert.assertEquals(6, results4.size());

        IsKindOfTest kindOf = new IsKindOfTest();
        Query kindOfQuery = kindOf.getQuery();
        NativeIdSetBI kindOfResults = kindOfQuery.compute();
        System.out.println("Kind of results: " + kindOfResults.size());
        Assert.assertEquals(171, kindOfResults.size());
        /*
         ChangedFromPreviousVersionTest previous = new ChangedFromPreviousVersionTest();
         Query changesQuery = previous.getQuery();
         NativeIdSetBI changesResults = changesQuery.compute();
         System.out.println("Query result count " + changesResults.size());
         Assert.assertEquals(6, changesResults.size());
         */

        /*DescriptionRegexMatchTest regexMatch = new DescriptionRegexMatchTest();
         Query regexMatchQuery = regexMatch.getQuery();
         NativeIdSetBI regexResults = regexMatchQuery.compute();
         System.out.println("Regex query result count " + regexResults.size());
         Assert.assertEquals(2, regexResults.size());
         */
        /*
         ConceptForComponentTest componentTest = new ConceptForComponentTest();
         Query componentQuery = componentTest.getQuery();
         NativeIdSetBI componentResults = componentQuery.compute();
         System.out.println("\n\nQuery result count: " + componentResults.size());
         Assert.assertEquals(1, componentResults.size());
         */
    }
}