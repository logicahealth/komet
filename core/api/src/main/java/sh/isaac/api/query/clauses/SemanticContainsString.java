/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.query.clauses;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.*;
import sh.isaac.api.query.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author kec
 */
public class SemanticContainsString extends QueryStringAbstract {

    public SemanticContainsString() {
    }

    public SemanticContainsString(Query enclosingQuery) {
        super(enclosingQuery);
    }

    public SemanticContainsString(Query enclosingQuery, LetItemKey queryStringKey) {
        super(enclosingQuery, queryStringKey);
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.SEMANTIC_CONTAINS_TEXT;
    }

    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        String queryString = this.getQueryText();

        Pattern pattern;
        if (isRegex()) {
            pattern = Pattern.compile(queryString);
        } else {
            pattern = Pattern.compile(Pattern.quote(queryString));
        }

        NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());
        for (int nid : possibleComponents.asArray()) {
            SemanticChronology sc = Get.assemblageService().getSemanticChronology(nid);
            if (!regexMatch(sc, pattern)) {
                possibleComponents.remove(nid);
            }
        }
        return incomingPossibleComponents;
    }

    protected boolean regexMatch(SemanticChronology sc, Pattern pattern) {
        List<String> stringsToMatch = new ArrayList<>();
        for (Version v : sc.getVersionList()) {
            switch (sc.getVersionType()) {
                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:

                    stringsToMatch.add(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version) v).getStr3());
                    stringsToMatch.add(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version) v).getStr4());
                    stringsToMatch.add(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version) v).getStr5());
                    break;
                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                    stringsToMatch.add(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version) v).getStr3());
                    stringsToMatch.add(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version) v).getStr4());
                    break;
                case Nid1_Nid2_Str3:
                    stringsToMatch.add(((Nid1_Nid2_Str3_Version) v).getStr3());
                    break;
                case Nid1_Str2:
                    stringsToMatch.add(((Nid1_Str2_Version) v).getStr2());
                    break;
                case STRING:
                    stringsToMatch.add(((StringVersion) v).getString());
                    break;
                case Str1_Nid2_Nid3_Nid4:
                    stringsToMatch.add(((Str1_Nid2_Nid3_Nid4_Version) v).getStr1());
                    break;
                case Str1_Str2:
                    stringsToMatch.add(((Str1_Str2_Version) v).getStr1());
                    stringsToMatch.add(((Str1_Str2_Version) v).getStr2());
                    break;
                case Str1_Str2_Nid3_Nid4:
                    stringsToMatch.add(((Str1_Str2_Nid3_Nid4_Version) v).getStr1());
                    stringsToMatch.add(((Str1_Str2_Nid3_Nid4_Version) v).getStr2());
                    break;
                case Str1_Str2_Nid3_Nid4_Nid5:
                    stringsToMatch.add(((Str1_Str2_Nid3_Nid4_Nid5_Version) v).getStr1());
                    stringsToMatch.add(((Str1_Str2_Nid3_Nid4_Nid5_Version) v).getStr2());
                    break;
                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr1());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr2());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr3());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr4());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr5());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr6());
                    stringsToMatch.add(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) v).getStr7());
                    break;
                default:
                    throw new UnsupportedOperationException("Can't match strings on: " + sc.getVersionType());
            }
        }
        for (String str : stringsToMatch) {
            if (pattern.matcher(str).find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return ITERATION;
    }

    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.SEMANTIC_CONTAINS_TEXT);
        whereClause.getLetKeys()
                .add(this.getQueryStringKey());
        return whereClause;
    }

}
