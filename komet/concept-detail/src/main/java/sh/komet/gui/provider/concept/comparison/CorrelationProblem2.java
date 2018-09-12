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
package sh.komet.gui.provider.concept.comparison;

import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;

import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;

import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;

/*


New isomorphic record: 490281
Isomorphic Analysis for:Cystourethroscopy with insertion of radioactive substance (procedure)
     4d9f8b51-d288-33c4-8599-cee19cc3349f

Reference expression:

 Root[184r]➞[183r]
    Necessary[183r]➞[182r]
        And[182r]➞[3r, 7r, 11r, 15r, 19r, 23r, 27r, 31r, 35r, 39r, 43r, 47r, 53r, 63r, 67r, 71r, 75r, 81r, 87r, 93r, 97r, 101r, 109r, 119r, 129r, 143r, 159r, 171r, 172r, 173r, 174r, 175r, 176r, 177r, 178r, 179r, 180r, 181r]
            Some[3r] Role group (SOLOR) <-2147483593>➞[2r]
                And[2r]➞[1r]
                    Some[1r] Procedure site (attribute) <-2147378082>➞[0r]
                        Concept[0r] Urinary bladder structure (body structure) <-2147419211>
            Some[7r] Role group (SOLOR) <-2147483593>➞[6r]
                And[6r]➞[5r]
                    Some[5r] Method (attribute) <-2147314116>➞[4r]
                        Concept[4r] Radioactivity (physical force) <-2147018351>
            Some[11r] Role group (SOLOR) <-2147483593>➞[10r]
                And[10r]➞[9r]
                    Some[9r] Access (attribute) <-2147315914>➞[8r]
                        Concept[8r] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[15r] Role group (SOLOR) <-2147483593>➞[14r]
                And[14r]➞[13r]
                    Some[13r] Approach (attribute) <-2147314305>➞[12r]
                        Concept[12r] Transurethral approach (qualifier value) <-2146691909>
            Some[19r] Role group (SOLOR) <-2147483593>➞[18r]
                And[18r]➞[17r]
                    Some[17r] Method (attribute) <-2147314116>➞[16r]
                        Concept[16r] Brachytherapy - action (qualifier value) <-2146464707>
            Some[23r] Role group (SOLOR) <-2147483593>➞[22r]
                And[22r]➞[21r]
                    Some[21r] Procedure site (attribute) <-2147378082>➞[20r]
                        Concept[20r] Urethral structure (body structure) <-2147284236>
            Some[27r] Role group (SOLOR) <-2147483593>➞[26r]
                And[26r]➞[25r]
                    Some[25r] Instrumentation (attribute) <-2146514480>➞[24r]
                        Concept[24r] Cystoscope, device (physical object) <-2147196166>
            Some[31r] Role group (SOLOR) <-2147483593>➞[30r]
                And[30r]➞[29r]
                    Some[29r] Method (attribute) <-2147314116>➞[28r]
                        Concept[28r] Implantation - action (qualifier value) <-2146940339>
            Some[35r] Role group (SOLOR) <-2147483593>➞[34r]
                And[34r]➞[33r]
                    Some[33r] Using (attribute) <-2147300466>➞[32r]
                        Concept[32r] Radioactivity (physical force) <-2147018351>
            Some[39r] Role group (SOLOR) <-2147483593>➞[38r]
                And[38r]➞[37r]
                    Some[37r] Using (attribute) <-2147300466>➞[36r]
                        Concept[36r] Ionizing radiation (physical force) <-2146978384>
            Some[43r] Role group (SOLOR) <-2147483593>➞[42r]
                And[42r]➞[41r]
                    Some[41r] Access instrument (attribute) <-2147283909>➞[40r]
                        Concept[40r] Cystoscope, device (physical object) <-2147196166>
            Some[47r] Role group (SOLOR) <-2147483593>➞[46r]
                And[46r]➞[45r]
                    Some[45r] Using (attribute) <-2147300466>➞[44r]
                        Concept[44r] Endoscope, device (physical object) <-2146961395>
            Some[53r] Role group (SOLOR) <-2147483593>➞[52r]
                And[52r]➞[49r, 51r]
                    Some[49r] Procedure site (attribute) <-2147378082>➞[48r]
                        Concept[48r] Urinary bladder structure (body structure) <-2147419211>
                    Some[51r] Method (attribute) <-2147314116>➞[50r]
                        Concept[50r] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63r] Role group (SOLOR) <-2147483593>➞[62r]
                And[62r]➞[55r, 57r, 59r, 61r]
                    Some[55r] Procedure site (attribute) <-2147378082>➞[54r]
                        Concept[54r] Urethral structure (body structure) <-2147284236>
                    Some[57r] Method (attribute) <-2147314116>➞[56r]
                        Concept[56r] Implantation - action (qualifier value) <-2146940339>
                    Some[59r] Method (attribute) <-2147314116>➞[58r]
                        Concept[58r] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61r] Method (attribute) <-2147314116>➞[60r]
                        Concept[60r] Surgical action (qualifier value) <-2146940928>
            Some[67r] Role group (SOLOR) <-2147483593>➞[66r]
                And[66r]➞[65r]
                    Some[65r] Direct device (attribute) <-2147378264>➞[64r]
                        Concept[64r] Endoscope, device (physical object) <-2146961395>
            Some[71r] Role group (SOLOR) <-2147483593>➞[70r]
                And[70r]➞[69r]
                    Some[69r] Method (attribute) <-2147314116>➞[68r]
                        Concept[68r] Insertion - action (qualifier value) <-2147349375>
            Some[75r] Role group (SOLOR) <-2147483593>➞[74r]
                And[74r]➞[73r]
                    Some[73r] Has intent (attribute) <-2147378135>➞[72r]
                        Concept[72r] Therapeutic intent (qualifier value) <-2147292522>
            Some[81r] Role group (SOLOR) <-2147483593>➞[80r]
                And[80r]➞[77r, 79r]
                    Some[77r] Procedure site (attribute) <-2147378082>➞[76r]
                        Concept[76r] Urethral structure (body structure) <-2147284236>
                    Some[79r] Method (attribute) <-2147314116>➞[78r]
                        Concept[78r] Inspection - action (qualifier value) <-2146938586>
            Some[87r] Role group (SOLOR) <-2147483593>➞[86r]
                And[86r]➞[83r, 85r]
                    Some[83r] Procedure site (attribute) <-2147378082>➞[82r]
                        Concept[82r] Urinary tract structure (body structure) <-2146984124>
                    Some[85r] Method (attribute) <-2147314116>➞[84r]
                        Concept[84r] Surgical action (qualifier value) <-2146940928>
            Some[93r] Role group (SOLOR) <-2147483593>➞[92r]
                And[92r]➞[89r, 91r]
                    Some[89r] Procedure site (attribute) <-2147378082>➞[88r]
                        Concept[88r] Urinary bladder structure (body structure) <-2147419211>
                    Some[91r] Method (attribute) <-2147314116>➞[90r]
                        Concept[90r] Inspection - action (qualifier value) <-2146938586>
            Some[97r] Role group (SOLOR) <-2147483593>➞[96r]
                And[96r]➞[95r]
                    Some[95r] Direct substance (attribute) <-2147378192>➞[94r]
                        Concept[94r] Radioactive isotope (substance) <-2147423787>
            Some[101r] Role group (SOLOR) <-2147483593>➞[100r]
                And[100r]➞[99r]
                    Some[99r] Direct device (attribute) <-2147378264>➞[98r]
                        Concept[98r] Implant, device (physical object) <-2146919385>
            Some[109r] Role group (SOLOR) <-2147483593>➞[108r]
                And[108r]➞[103r, 105r, 107r]
                    Some[103r] Access (attribute) <-2147315914>➞[102r]
                        Concept[102r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[105r] Approach (attribute) <-2147314305>➞[104r]
                        Concept[104r] Transurethral approach (qualifier value) <-2146691909>
                    Some[107r] Access instrument (attribute) <-2147283909>➞[106r]
                        Concept[106r] Cystoscope, device (physical object) <-2147196166>
            Some[119r] Role group (SOLOR) <-2147483593>➞[118r]
                And[118r]➞[111r, 113r, 115r, 117r]
                    Some[111r] Procedure site (attribute) <-2147378082>➞[110r]
                        Concept[110r] Urethral structure (body structure) <-2147284236>
                    Some[113r] Access (attribute) <-2147315914>➞[112r]
                        Concept[112r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[115r] Method (attribute) <-2147314116>➞[114r]
                        Concept[114r] Inspection - action (qualifier value) <-2146938586>
                    Some[117r] Access instrument (attribute) <-2147283909>➞[116r]
                        Concept[116r] Endoscope, device (physical object) <-2146961395>
            Some[129r] Role group (SOLOR) <-2147483593>➞[128r]
                And[128r]➞[121r, 123r, 125r, 127r]
                    Some[121r] Procedure site (attribute) <-2147378082>➞[120r]
                        Concept[120r] Urinary bladder structure (body structure) <-2147419211>
                    Some[123r] Access (attribute) <-2147315914>➞[122r]
                        Concept[122r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[125r] Method (attribute) <-2147314116>➞[124r]
                        Concept[124r] Inspection - action (qualifier value) <-2146938586>
                    Some[127r] Access instrument (attribute) <-2147283909>➞[126r]
                        Concept[126r] Cystoscope, device (physical object) <-2147196166>
            Some[143r] Role group (SOLOR) <-2147483593>➞[142r]
                And[142r]➞[131r, 133r, 135r, 137r, 139r, 141r]
                    Some[131r] Procedure site (attribute) <-2147378082>➞[130r]
                        Concept[130r] Urethral structure (body structure) <-2147284236>
                    Some[133r] Procedure site (attribute) <-2147378082>➞[132r]
                        Concept[132r] Urinary bladder structure (body structure) <-2147419211>
                    Some[135r] Access (attribute) <-2147315914>➞[134r]
                        Concept[134r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[137r] Approach (attribute) <-2147314305>➞[136r]
                        Concept[136r] Transurethral approach (qualifier value) <-2146691909>
                    Some[139r] Method (attribute) <-2147314116>➞[138r]
                        Concept[138r] Inspection - action (qualifier value) <-2146938586>
                    Some[141r] Access instrument (attribute) <-2147283909>➞[140r]
                        Concept[140r] Cystoscope, device (physical object) <-2147196166>
            Some[159r] Role group (SOLOR) <-2147483593>➞[158r]
                And[158r]➞[145r, 147r, 149r, 151r, 153r, 155r, 157r]
                    Some[145r] Direct substance (attribute) <-2147378192>➞[144r]
                        Concept[144r] Radioactive isotope (substance) <-2147423787>
                    Some[147r] Procedure site (attribute) <-2147378082>➞[146r]
                        Concept[146r] Urinary bladder structure (body structure) <-2147419211>
                    Some[149r] Access (attribute) <-2147315914>➞[148r]
                        Concept[148r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[151r] Approach (attribute) <-2147314305>➞[150r]
                        Concept[150r] Transurethral approach (qualifier value) <-2146691909>
                    Some[153r] Method (attribute) <-2147314116>➞[152r]
                        Concept[152r] Insertion - action (qualifier value) <-2147349375>
                    Some[155r] Method (attribute) <-2147314116>➞[154r]
                        Concept[154r] Inspection - action (qualifier value) <-2146938586>
                    Some[157r] Access instrument (attribute) <-2147283909>➞[156r]
                        Concept[156r] Cystoscope, device (physical object) <-2147196166>
            Some[171r] Role group (SOLOR) <-2147483593>➞[170r]
                And[170r]➞[161r, 163r, 165r, 167r, 169r]
                    Some[161r] Access (attribute) <-2147315914>➞[160r]
                        Concept[160r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[163r] Approach (attribute) <-2147314305>➞[162r]
                        Concept[162r] Transurethral approach (qualifier value) <-2146691909>
                    Some[165r] Method (attribute) <-2147314116>➞[164r]
                        Concept[164r] Inspection - action (qualifier value) <-2146938586>
                    Some[167r] Access instrument (attribute) <-2147283909>➞[166r]
                        Concept[166r] Cystoscope, device (physical object) <-2147196166>
                    Some[169r] Procedure site - Direct (attribute) <-2146878287>➞[168r]
                        Concept[168r] Urinary bladder structure (body structure) <-2147419211>
            Concept[172r] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[173r] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[174r] Endoscopy of urethra (procedure) <-2147319097>
            Concept[175r] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>
            Concept[176r] Therapeutic endoscopic procedure (procedure) <-2147153232>
            Concept[177r] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178r] Operation on urinary tract proper (procedure) <-2147061512>
            Concept[179r] Introduction to urinary tract (procedure) <-2147055307>
            Concept[180r] Bladder implantation (procedure) <-2147044478>
            Concept[181r] Insertion of radioactive isotope (procedure) <-2146915764>

Comparison expression:

 Root[184c]➞[183c]
    Necessary[183c]➞[182c]
        And[182c]➞[3c, 7c, 11c, 15c, 19c, 23c, 27c, 31c, 35c, 39c, 43c, 47c, 53c, 63c, 67c, 71c, 75c, 81c, 87c, 93c, 97c, 101c, 109c, 119c, 129c, 143c, 159c, 171c, 172c, 173c, 174c, 175c, 176c, 177c, 178c, 179c, 180c, 181c]
            Some[3c] Role group (SOLOR) <-2147483593>➞[2c]
                And[2c]➞[1c]
                    Some[1c] Procedure site (attribute) <-2147378082>➞[0c]
                        Concept[0c] Urinary bladder structure (body structure) <-2147419211>
            Some[7c] Role group (SOLOR) <-2147483593>➞[6c]
                And[6c]➞[5c]
                    Some[5c] Method (attribute) <-2147314116>➞[4c]
                        Concept[4c] Radioactivity (physical force) <-2147018351>
            Some[11c] Role group (SOLOR) <-2147483593>➞[10c]
                And[10c]➞[9c]
                    Some[9c] Access (attribute) <-2147315914>➞[8c]
                        Concept[8c] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[15c] Role group (SOLOR) <-2147483593>➞[14c]
                And[14c]➞[13c]
                    Some[13c] Approach (attribute) <-2147314305>➞[12c]
                        Concept[12c] Transurethral approach (qualifier value) <-2146691909>
            Some[19c] Role group (SOLOR) <-2147483593>➞[18c]
                And[18c]➞[17c]
                    Some[17c] Method (attribute) <-2147314116>➞[16c]
                        Concept[16c] Brachytherapy - action (qualifier value) <-2146464707>
            Some[23c] Role group (SOLOR) <-2147483593>➞[22c]
                And[22c]➞[21c]
                    Some[21c] Procedure site (attribute) <-2147378082>➞[20c]
                        Concept[20c] Urethral structure (body structure) <-2147284236>
            Some[27c] Role group (SOLOR) <-2147483593>➞[26c]
                And[26c]➞[25c]
                    Some[25c] Instrumentation (attribute) <-2146514480>➞[24c]
                        Concept[24c] Cystoscope, device (physical object) <-2147196166>
            Some[31c] Role group (SOLOR) <-2147483593>➞[30c]
                And[30c]➞[29c]
                    Some[29c] Method (attribute) <-2147314116>➞[28c]
                        Concept[28c] Implantation - action (qualifier value) <-2146940339>
            Some[35c] Role group (SOLOR) <-2147483593>➞[34c]
                And[34c]➞[33c]
                    Some[33c] Using (attribute) <-2147300466>➞[32c]
                        Concept[32c] Radioactivity (physical force) <-2147018351>
            Some[39c] Role group (SOLOR) <-2147483593>➞[38c]
                And[38c]➞[37c]
                    Some[37c] Using (attribute) <-2147300466>➞[36c]
                        Concept[36c] Ionizing radiation (physical force) <-2146978384>
            Some[43c] Role group (SOLOR) <-2147483593>➞[42c]
                And[42c]➞[41c]
                    Some[41c] Access instrument (attribute) <-2147283909>➞[40c]
                        Concept[40c] Cystoscope, device (physical object) <-2147196166>
            Some[47c] Role group (SOLOR) <-2147483593>➞[46c]
                And[46c]➞[45c]
                    Some[45c] Using (attribute) <-2147300466>➞[44c]
                        Concept[44c] Endoscope, device (physical object) <-2146961395>
            Some[53c] Role group (SOLOR) <-2147483593>➞[52c]
                And[52c]➞[49c, 51c]
                    Some[49c] Procedure site (attribute) <-2147378082>➞[48c]
                        Concept[48c] Urinary bladder structure (body structure) <-2147419211>
                    Some[51c] Method (attribute) <-2147314116>➞[50c]
                        Concept[50c] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63c] Role group (SOLOR) <-2147483593>➞[62c]
                And[62c]➞[55c, 57c, 59c, 61c]
                    Some[55c] Procedure site (attribute) <-2147378082>➞[54c]
                        Concept[54c] Urethral structure (body structure) <-2147284236>
                    Some[57c] Method (attribute) <-2147314116>➞[56c]
                        Concept[56c] Implantation - action (qualifier value) <-2146940339>
                    Some[59c] Method (attribute) <-2147314116>➞[58c]
                        Concept[58c] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61c] Method (attribute) <-2147314116>➞[60c]
                        Concept[60c] Surgical action (qualifier value) <-2146940928>
            Some[67c] Role group (SOLOR) <-2147483593>➞[66c]
                And[66c]➞[65c]
                    Some[65c] Direct device (attribute) <-2147378264>➞[64c]
                        Concept[64c] Endoscope, device (physical object) <-2146961395>
            Some[71c] Role group (SOLOR) <-2147483593>➞[70c]
                And[70c]➞[69c]
                    Some[69c] Method (attribute) <-2147314116>➞[68c]
                        Concept[68c] Insertion - action (qualifier value) <-2147349375>
            Some[75c] Role group (SOLOR) <-2147483593>➞[74c]
                And[74c]➞[73c]
                    Some[73c] Has intent (attribute) <-2147378135>➞[72c]
                        Concept[72c] Therapeutic intent (qualifier value) <-2147292522>
            Some[81c] Role group (SOLOR) <-2147483593>➞[80c]
                And[80c]➞[77c, 79c]
                    Some[77c] Procedure site (attribute) <-2147378082>➞[76c]
                        Concept[76c] Urethral structure (body structure) <-2147284236>
                    Some[79c] Method (attribute) <-2147314116>➞[78c]
                        Concept[78c] Inspection - action (qualifier value) <-2146938586>
            Some[87c] Role group (SOLOR) <-2147483593>➞[86c]
                And[86c]➞[83c, 85c]
                    Some[83c] Procedure site (attribute) <-2147378082>➞[82c]
                        Concept[82c] Urinary tract structure (body structure) <-2146984124>
                    Some[85c] Method (attribute) <-2147314116>➞[84c]
                        Concept[84c] Surgical action (qualifier value) <-2146940928>
            Some[93c] Role group (SOLOR) <-2147483593>➞[92c]
                And[92c]➞[89c, 91c]
                    Some[89c] Procedure site (attribute) <-2147378082>➞[88c]
                        Concept[88c] Urinary bladder structure (body structure) <-2147419211>
                    Some[91c] Method (attribute) <-2147314116>➞[90c]
                        Concept[90c] Inspection - action (qualifier value) <-2146938586>
            Some[97c] Role group (SOLOR) <-2147483593>➞[96c]
                And[96c]➞[95c]
                    Some[95c] Direct substance (attribute) <-2147378192>➞[94c]
                        Concept[94c] Radioactive isotope (substance) <-2147423787>
            Some[101c] Role group (SOLOR) <-2147483593>➞[100c]
                And[100c]➞[99c]
                    Some[99c] Direct device (attribute) <-2147378264>➞[98c]
                        Concept[98c] Implant, device (physical object) <-2146919385>
            Some[109c] Role group (SOLOR) <-2147483593>➞[108c]
                And[108c]➞[103c, 105c, 107c]
                    Some[103c] Access (attribute) <-2147315914>➞[102c]
                        Concept[102c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[105c] Approach (attribute) <-2147314305>➞[104c]
                        Concept[104c] Transurethral approach (qualifier value) <-2146691909>
                    Some[107c] Access instrument (attribute) <-2147283909>➞[106c]
                        Concept[106c] Cystoscope, device (physical object) <-2147196166>
            Some[119c] Role group (SOLOR) <-2147483593>➞[118c]
                And[118c]➞[111c, 113c, 115c, 117c]
                    Some[111c] Procedure site (attribute) <-2147378082>➞[110c]
                        Concept[110c] Urethral structure (body structure) <-2147284236>
                    Some[113c] Access (attribute) <-2147315914>➞[112c]
                        Concept[112c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[115c] Method (attribute) <-2147314116>➞[114c]
                        Concept[114c] Inspection - action (qualifier value) <-2146938586>
                    Some[117c] Access instrument (attribute) <-2147283909>➞[116c]
                        Concept[116c] Endoscope, device (physical object) <-2146961395>
            Some[129c] Role group (SOLOR) <-2147483593>➞[128c]
                And[128c]➞[121c, 123c, 125c, 127c]
                    Some[121c] Procedure site (attribute) <-2147378082>➞[120c]
                        Concept[120c] Urinary bladder structure (body structure) <-2147419211>
                    Some[123c] Access (attribute) <-2147315914>➞[122c]
                        Concept[122c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[125c] Method (attribute) <-2147314116>➞[124c]
                        Concept[124c] Inspection - action (qualifier value) <-2146938586>
                    Some[127c] Access instrument (attribute) <-2147283909>➞[126c]
                        Concept[126c] Cystoscope, device (physical object) <-2147196166>
            Some[143c] Role group (SOLOR) <-2147483593>➞[142c]
                And[142c]➞[131c, 133c, 135c, 137c, 139c, 141c]
                    Some[131c] Procedure site (attribute) <-2147378082>➞[130c]
                        Concept[130c] Urethral structure (body structure) <-2147284236>
                    Some[133c] Procedure site (attribute) <-2147378082>➞[132c]
                        Concept[132c] Urinary bladder structure (body structure) <-2147419211>
                    Some[135c] Access (attribute) <-2147315914>➞[134c]
                        Concept[134c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[137c] Approach (attribute) <-2147314305>➞[136c]
                        Concept[136c] Transurethral approach (qualifier value) <-2146691909>
                    Some[139c] Method (attribute) <-2147314116>➞[138c]
                        Concept[138c] Inspection - action (qualifier value) <-2146938586>
                    Some[141c] Access instrument (attribute) <-2147283909>➞[140c]
                        Concept[140c] Cystoscope, device (physical object) <-2147196166>
            Some[159c] Role group (SOLOR) <-2147483593>➞[158c]
                And[158c]➞[145c, 147c, 149c, 151c, 153c, 155c, 157c]
                    Some[145c] Direct substance (attribute) <-2147378192>➞[144c]
                        Concept[144c] Radioactive isotope (substance) <-2147423787>
                    Some[147c] Procedure site (attribute) <-2147378082>➞[146c]
                        Concept[146c] Urinary bladder structure (body structure) <-2147419211>
                    Some[149c] Access (attribute) <-2147315914>➞[148c]
                        Concept[148c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[151c] Approach (attribute) <-2147314305>➞[150c]
                        Concept[150c] Transurethral approach (qualifier value) <-2146691909>
                    Some[153c] Method (attribute) <-2147314116>➞[152c]
                        Concept[152c] Insertion - action (qualifier value) <-2147349375>
                    Some[155c] Method (attribute) <-2147314116>➞[154c]
                        Concept[154c] Inspection - action (qualifier value) <-2146938586>
                    Some[157c] Access instrument (attribute) <-2147283909>➞[156c]
                        Concept[156c] Cystoscope, device (physical object) <-2147196166>
            Some[171c] Role group (SOLOR) <-2147483593>➞[170c]
                And[170c]➞[161c, 163c, 165c, 167c, 169c]
                    Some[161c] Access (attribute) <-2147315914>➞[160c]
                        Concept[160c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[163c] Approach (attribute) <-2147314305>➞[162c]
                        Concept[162c] Transurethral approach (qualifier value) <-2146691909>
                    Some[165c] Method (attribute) <-2147314116>➞[164c]
                        Concept[164c] Inspection - action (qualifier value) <-2146938586>
                    Some[167c] Access instrument (attribute) <-2147283909>➞[166c]
                        Concept[166c] Cystoscope, device (physical object) <-2147196166>
                    Some[169c] Procedure site - Direct (attribute) <-2146878287>➞[168c]
                        Concept[168c] Urinary bladder structure (body structure) <-2147419211>
            Concept[172c] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[173c] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[174c] Endoscopy of urethra (procedure) <-2147319097>
            Concept[175c] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>
            Concept[176c] Therapeutic endoscopic procedure (procedure) <-2147153232>
            Concept[177c] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178c] Operation on urinary tract proper (procedure) <-2147061512>
            Concept[179c] Introduction to urinary tract (procedure) <-2147055307>
            Concept[180c] Bladder implantation (procedure) <-2147044478>
            Concept[181c] Insertion of radioactive isotope (procedure) <-2146915764>

Isomorphic expression:

 Root[184i]➞[183i]
    Necessary[183i]➞[182i]
        And[182i]➞[3i, 7i, 11i, 15i, 19i, 23i, 27i, 31i, 35i, 39i, 43i, 47i, 53i, 63i, 67i, 71i, 75i, 81i, 87i, 93i, 97i, 101i, 109i, 119i, 129i, 143i, 159i, 171i, 172i, 173i, 174i, 175i, 176i, 177i, 178i, 179i, 180i, 181i]
            Some[3i] Role group (SOLOR) <-2147483593>➞[2i]
                And[2i]➞[1i]
                    Some[1i] Procedure site (attribute) <-2147378082>➞[0i]
                        Concept[0i] Urinary bladder structure (body structure) <-2147419211>
            Some[7i] Role group (SOLOR) <-2147483593>➞[6i]
                And[6i]➞[5i]
                    Some[5i] Method (attribute) <-2147314116>➞[4i]
                        Concept[4i] Radioactivity (physical force) <-2147018351>
            Some[11i] Role group (SOLOR) <-2147483593>➞[10i]
                And[10i]➞[9i]
                    Some[9i] Access (attribute) <-2147315914>➞[8i]
                        Concept[8i] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[15i] Role group (SOLOR) <-2147483593>➞[14i]
                And[14i]➞[13i]
                    Some[13i] Approach (attribute) <-2147314305>➞[12i]
                        Concept[12i] Transurethral approach (qualifier value) <-2146691909>
            Some[19i] Role group (SOLOR) <-2147483593>➞[18i]
                And[18i]➞[17i]
                    Some[17i] Method (attribute) <-2147314116>➞[16i]
                        Concept[16i] Brachytherapy - action (qualifier value) <-2146464707>
            Some[23i] Role group (SOLOR) <-2147483593>➞[22i]
                And[22i]➞[21i]
                    Some[21i] Procedure site (attribute) <-2147378082>➞[20i]
                        Concept[20i] Urethral structure (body structure) <-2147284236>
            Some[27i] Role group (SOLOR) <-2147483593>➞[26i]
                And[26i]➞[25i]
                    Some[25i] Instrumentation (attribute) <-2146514480>➞[24i]
                        Concept[24i] Cystoscope, device (physical object) <-2147196166>
            Some[31i] Role group (SOLOR) <-2147483593>➞[30i]
                And[30i]➞[29i]
                    Some[29i] Method (attribute) <-2147314116>➞[28i]
                        Concept[28i] Implantation - action (qualifier value) <-2146940339>
            Some[35i] Role group (SOLOR) <-2147483593>➞[34i]
                And[34i]➞[33i]
                    Some[33i] Using (attribute) <-2147300466>➞[32i]
                        Concept[32i] Radioactivity (physical force) <-2147018351>
            Some[39i] Role group (SOLOR) <-2147483593>➞[38i]
                And[38i]➞[37i]
                    Some[37i] Using (attribute) <-2147300466>➞[36i]
                        Concept[36i] Ionizing radiation (physical force) <-2146978384>
            Some[43i] Role group (SOLOR) <-2147483593>➞[42i]
                And[42i]➞[41i]
                    Some[41i] Access instrument (attribute) <-2147283909>➞[40i]
                        Concept[40i] Cystoscope, device (physical object) <-2147196166>
            Some[47i] Role group (SOLOR) <-2147483593>➞[46i]
                And[46i]➞[45i]
                    Some[45i] Using (attribute) <-2147300466>➞[44i]
                        Concept[44i] Endoscope, device (physical object) <-2146961395>
            Some[53i] Role group (SOLOR) <-2147483593>➞[52i]
                And[52i]➞[49i, 51i]
                    Some[49i] Procedure site (attribute) <-2147378082>➞[48i]
                        Concept[48i] Urinary bladder structure (body structure) <-2147419211>
                    Some[51i] Method (attribute) <-2147314116>➞[50i]
                        Concept[50i] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63i] Role group (SOLOR) <-2147483593>➞[62i]
                And[62i]➞[55i, 57i, 59i, 61i]
                    Some[55i] Procedure site (attribute) <-2147378082>➞[54i]
                        Concept[54i] Urethral structure (body structure) <-2147284236>
                    Some[57i] Method (attribute) <-2147314116>➞[56i]
                        Concept[56i] Implantation - action (qualifier value) <-2146940339>
                    Some[59i] Method (attribute) <-2147314116>➞[58i]
                        Concept[58i] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61i] Method (attribute) <-2147314116>➞[60i]
                        Concept[60i] Surgical action (qualifier value) <-2146940928>
            Some[67i] Role group (SOLOR) <-2147483593>➞[66i]
                And[66i]➞[65i]
                    Some[65i] Direct device (attribute) <-2147378264>➞[64i]
                        Concept[64i] Endoscope, device (physical object) <-2146961395>
            Some[71i] Role group (SOLOR) <-2147483593>➞[70i]
                And[70i]➞[69i]
                    Some[69i] Method (attribute) <-2147314116>➞[68i]
                        Concept[68i] Insertion - action (qualifier value) <-2147349375>
            Some[75i] Role group (SOLOR) <-2147483593>➞[74i]
                And[74i]➞[73i]
                    Some[73i] Has intent (attribute) <-2147378135>➞[72i]
                        Concept[72i] Therapeutic intent (qualifier value) <-2147292522>
            Some[81i] Role group (SOLOR) <-2147483593>➞[80i]
                And[80i]➞[77i, 79i]
                    Some[77i] Procedure site (attribute) <-2147378082>➞[76i]
                        Concept[76i] Urethral structure (body structure) <-2147284236>
                    Some[79i] Method (attribute) <-2147314116>➞[78i]
                        Concept[78i] Inspection - action (qualifier value) <-2146938586>
            Some[87i] Role group (SOLOR) <-2147483593>➞[86i]
                And[86i]➞[83i, 85i]
                    Some[83i] Procedure site (attribute) <-2147378082>➞[82i]
                        Concept[82i] Urinary tract structure (body structure) <-2146984124>
                    Some[85i] Method (attribute) <-2147314116>➞[84i]
                        Concept[84i] Surgical action (qualifier value) <-2146940928>
            Some[93i] Role group (SOLOR) <-2147483593>➞[92i]
                And[92i]➞[89i, 91i]
                    Some[89i] Procedure site (attribute) <-2147378082>➞[88i]
                        Concept[88i] Urinary bladder structure (body structure) <-2147419211>
                    Some[91i] Method (attribute) <-2147314116>➞[90i]
                        Concept[90i] Inspection - action (qualifier value) <-2146938586>
            Some[97i] Role group (SOLOR) <-2147483593>➞[96i]
                And[96i]➞[95i]
                    Some[95i] Direct substance (attribute) <-2147378192>➞[94i]
                        Concept[94i] Radioactive isotope (substance) <-2147423787>
            Some[101i] Role group (SOLOR) <-2147483593>➞[100i]
                And[100i]➞[99i]
                    Some[99i] Direct device (attribute) <-2147378264>➞[98i]
                        Concept[98i] Implant, device (physical object) <-2146919385>
            Some[109i] Role group (SOLOR) <-2147483593>➞[108i]
                And[108i]➞[103i, 105i, 107i]
                    Some[103i] Access (attribute) <-2147315914>➞[102i]
                        Concept[102i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[105i] Approach (attribute) <-2147314305>➞[104i]
                        Concept[104i] Transurethral approach (qualifier value) <-2146691909>
                    Some[107i] Access instrument (attribute) <-2147283909>➞[106i]
                        Concept[106i] Cystoscope, device (physical object) <-2147196166>
            Some[119i] Role group (SOLOR) <-2147483593>➞[118i]
                And[118i]➞[111i, 113i, 115i, 117i]
                    Some[111i] Procedure site (attribute) <-2147378082>➞[110i]
                        Concept[110i] Urethral structure (body structure) <-2147284236>
                    Some[113i] Access (attribute) <-2147315914>➞[112i]
                        Concept[112i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[115i] Method (attribute) <-2147314116>➞[114i]
                        Concept[114i] Inspection - action (qualifier value) <-2146938586>
                    Some[117i] Access instrument (attribute) <-2147283909>➞[116i]
                        Concept[116i] Endoscope, device (physical object) <-2146961395>
            Some[129i] Role group (SOLOR) <-2147483593>➞[128i]
                And[128i]➞[121i, 123i, 125i, 127i]
                    Some[121i] Procedure site (attribute) <-2147378082>➞[120i]
                        Concept[120i] Urinary bladder structure (body structure) <-2147419211>
                    Some[123i] Access (attribute) <-2147315914>➞[122i]
                        Concept[122i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[125i] Method (attribute) <-2147314116>➞[124i]
                        Concept[124i] Inspection - action (qualifier value) <-2146938586>
                    Some[127i] Access instrument (attribute) <-2147283909>➞[126i]
                        Concept[126i] Cystoscope, device (physical object) <-2147196166>
            Some[143i] Role group (SOLOR) <-2147483593>➞[142i]
                And[142i]➞[131i, 133i, 135i, 137i, 139i, 141i]
                    Some[131i] Procedure site (attribute) <-2147378082>➞[130i]
                        Concept[130i] Urethral structure (body structure) <-2147284236>
                    Some[133i] Procedure site (attribute) <-2147378082>➞[132i]
                        Concept[132i] Urinary bladder structure (body structure) <-2147419211>
                    Some[135i] Access (attribute) <-2147315914>➞[134i]
                        Concept[134i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[137i] Approach (attribute) <-2147314305>➞[136i]
                        Concept[136i] Transurethral approach (qualifier value) <-2146691909>
                    Some[139i] Method (attribute) <-2147314116>➞[138i]
                        Concept[138i] Inspection - action (qualifier value) <-2146938586>
                    Some[141i] Access instrument (attribute) <-2147283909>➞[140i]
                        Concept[140i] Cystoscope, device (physical object) <-2147196166>
            Some[159i] Role group (SOLOR) <-2147483593>➞[158i]
                And[158i]➞[145i, 147i, 149i, 151i, 153i, 155i, 157i]
                    Some[145i] Direct substance (attribute) <-2147378192>➞[144i]
                        Concept[144i] Radioactive isotope (substance) <-2147423787>
                    Some[147i] Procedure site (attribute) <-2147378082>➞[146i]
                        Concept[146i] Urinary bladder structure (body structure) <-2147419211>
                    Some[149i] Access (attribute) <-2147315914>➞[148i]
                        Concept[148i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[151i] Approach (attribute) <-2147314305>➞[150i]
                        Concept[150i] Transurethral approach (qualifier value) <-2146691909>
                    Some[153i] Method (attribute) <-2147314116>➞[152i]
                        Concept[152i] Insertion - action (qualifier value) <-2147349375>
                    Some[155i] Method (attribute) <-2147314116>➞[154i]
                        Concept[154i] Inspection - action (qualifier value) <-2146938586>
                    Some[157i] Access instrument (attribute) <-2147283909>➞[156i]
                        Concept[156i] Cystoscope, device (physical object) <-2147196166>
            Some[171i] Role group (SOLOR) <-2147483593>➞[170i]
                And[170i]➞[161i, 163i, 165i, 167i, 169i]
                    Some[161i] Access (attribute) <-2147315914>➞[160i]
                        Concept[160i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[163i] Approach (attribute) <-2147314305>➞[162i]
                        Concept[162i] Transurethral approach (qualifier value) <-2146691909>
                    Some[165i] Method (attribute) <-2147314116>➞[164i]
                        Concept[164i] Inspection - action (qualifier value) <-2146938586>
                    Some[167i] Access instrument (attribute) <-2147283909>➞[166i]
                        Concept[166i] Cystoscope, device (physical object) <-2147196166>
                    Some[169i] Procedure site - Direct (attribute) <-2146878287>➞[168i]
                        Concept[168i] Urinary bladder structure (body structure) <-2147419211>
            Concept[172i] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[173i] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[174i] Endoscopy of urethra (procedure) <-2147319097>
            Concept[175i] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>
            Concept[176i] Therapeutic endoscopic procedure (procedure) <-2147153232>
            Concept[177i] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178i] Operation on urinary tract proper (procedure) <-2147061512>
            Concept[179i] Introduction to urinary tract (procedure) <-2147055307>
            Concept[180i] Bladder implantation (procedure) <-2147044478>
            Concept[181i] Insertion of radioactive isotope (procedure) <-2146915764>

Reference Expression To MergedNodeId Map:

 [0r:0m, 1r:1m, 2r:2m, 3r:3m, 4r:4m, 5r:5m, 6r:6m, 7r:7m, 8r:8m, 9r:9m, 10r:10m, 11r:11m, 12r:12m, 13r:13m, 14r:14m, 15r:15m, 16r:16m, 17r:17m, 18r:18m, 19r:19m, 20r:20m, 21r:21m, 22r:22m, 23r:23m, 24r:24m, 25r:25m, 26r:26m, 27r:27m, 28r:28m, 29r:29m, 30r:30m, 31r:31m, 32r:32m, 33r:33m, 34r:34m, 35r:35m, 36r:36m, 37r:37m, 38r:38m, 39r:39m, 40r:40m, 41r:41m, 42r:42m, 43r:43m, 44r:44m, 45r:45m, 46r:46m, 47r:47m, 48r:48m, 49r:49m, 50r:50m, 51r:51m, 52r:52m, 53r:53m, 54r:54m, 55r:55m, 56r:56m, 57r:57m, 58r:58m, 59r:59m, 60r:60m, 61r:61m, 62r:62m, 63r:63m, 64r:64m, 65r:65m, 66r:66m, 67r:67m, 68r:68m, 69r:69m, 70r:70m, 71r:71m, 72r:72m, 73r:73m, 74r:74m, 75r:75m, 76r:76m, 77r:77m, 78r:78m, 79r:79m, 80r:80m, 81r:81m, 82r:82m, 83r:83m, 84r:84m, 85r:85m, 86r:86m, 87r:87m, 88r:88m, 89r:89m, 90r:90m, 91r:91m, 92r:92m, 93r:93m, 94r:94m, 95r:95m, 96r:96m, 97r:97m, 98r:98m, 99r:99m, 100r:100m, 101r:101m, 102r:102m, 103r:103m, 104r:104m, 105r:105m, 106r:106m, 107r:107m, 108r:108m, 109r:109m, 110r:110m, 111r:111m, 112r:112m, 113r:113m, 114r:114m, 115r:115m, 116r:116m, 117r:117m, 118r:118m, 119r:119m, 120r:120m, 121r:121m, 122r:122m, 123r:123m, 124r:124m, 125r:125m, 126r:126m, 127r:127m, 128r:128m, 129r:129m, 130r:130m, 131r:131m, 132r:132m, 133r:133m, 134r:134m, 135r:135m, 136r:136m, 137r:137m, 138r:138m, 139r:139m, 140r:140m, 141r:141m, 142r:142m, 143r:143m, 144r:144m, 145r:145m, 146r:146m, 147r:147m, 148r:148m, 149r:149m, 150r:150m, 151r:151m, 152r:152m, 153r:153m, 154r:154m, 155r:155m, 156r:156m, 157r:157m, 158r:158m, 159r:159m, 160r:160m, 161r:161m, 162r:162m, 163r:163m, 164r:164m, 165r:165m, 166r:166m, 167r:167m, 168r:168m, 169r:169m, 170r:170m, 171r:171m, 172r:172m, 173r:173m, 174r:174m, 175r:175m, 176r:176m, 177r:177m, 178r:178m, 179r:179m, 180r:180m, 181r:181m, 182r:182m, 183r:183m, 184r:184m]

Reference Expression To ComparisonNodeId Map:

 [0r:0c, 1r:1c, 2r:2c, 3r:3c, 4r:4c, 5r:5c, 6r:6c, 7r:7c, 8r:8c, 9r:9c, 10r:10c, 11r:11c, 12r:12c, 13r:13c, 14r:14c, 15r:15c, 16r:16c, 17r:17c, 18r:18c, 19r:19c, 20r:20c, 21r:21c, 22r:22c, 23r:23c, 24r:24c, 25r:25c, 26r:26c, 27r:27c, 28r:28c, 29r:29c, 30r:30c, 31r:31c, 32r:32c, 33r:33c, 34r:34c, 35r:35c, 36r:36c, 37r:37c, 38r:38c, 39r:39c, 40r:40c, 41r:41c, 42r:42c, 43r:43c, 44r:44c, 45r:45c, 46r:46c, 47r:47c, 48r:48c, 49r:49c, 50r:50c, 51r:51c, 52r:52c, 53r:53c, 54r:54c, 55r:55c, 56r:56c, 57r:57c, 58r:58c, 59r:59c, 60r:60c, 61r:61c, 62r:62c, 63r:63c, 64r:64c, 65r:65c, 66r:66c, 67r:67c, 68r:68c, 69r:69c, 70r:70c, 71r:71c, 72r:72c, 73r:73c, 74r:74c, 75r:75c, 76r:76c, 77r:77c, 78r:78c, 79r:79c, 80r:80c, 81r:81c, 82r:82c, 83r:83c, 84r:84c, 85r:85c, 86r:86c, 87r:87c, 88r:88c, 89r:89c, 90r:90c, 91r:91c, 92r:92c, 93r:93c, 94r:94c, 95r:95c, 96r:96c, 97r:97c, 98r:98c, 99r:99c, 100r:100c, 101r:101c, 102r:102c, 103r:103c, 104r:104c, 105r:105c, 106r:106c, 107r:107c, 108r:108c, 109r:109c, 110r:110c, 111r:111c, 112r:112c, 113r:113c, 114r:114c, 115r:115c, 116r:116c, 117r:117c, 118r:118c, 119r:119c, 120r:120c, 121r:121c, 122r:122c, 123r:123c, 124r:124c, 125r:125c, 126r:126c, 127r:127c, 128r:128c, 129r:129c, 130r:130c, 131r:131c, 132r:132c, 133r:133c, 134r:134c, 135r:135c, 136r:136c, 137r:137c, 138r:138c, 139r:139c, 140r:140c, 141r:141c, 142r:142c, 143r:143c, 144r:144c, 145r:145c, 146r:146c, 147r:147c, 148r:148c, 149r:149c, 150r:150c, 151r:151c, 152r:152c, 153r:153c, 154r:154c, 155r:155c, 156r:156c, 157r:157c, 158r:158c, 159r:159c, 160r:160c, 161r:161c, 162r:162c, 163r:163c, 164r:164c, 165r:165c, 166r:166c, 167r:167c, 168r:168c, 169r:169c, 170r:170c, 171r:171c, 172r:172c, 173r:173c, 174r:174c, 175r:175c, 176r:176c, 177r:177c, 178r:178c, 179r:179c, 180r:180c, 181r:181c, 182r:182c, 183r:183c, 184r:184c]

Comparison Expression To ReferenceNodeId Map:

 [0c:0r, 1c:1r, 2c:2r, 3c:3r, 4c:4r, 5c:5r, 6c:6r, 7c:7r, 8c:8r, 9c:9r, 10c:10r, 11c:11r, 12c:12r, 13c:13r, 14c:14r, 15c:15r, 16c:16r, 17c:17r, 18c:18r, 19c:19r, 20c:20r, 21c:21r, 22c:22r, 23c:23r, 24c:24r, 25c:25r, 26c:26r, 27c:27r, 28c:28r, 29c:29r, 30c:30r, 31c:31r, 32c:32r, 33c:33r, 34c:34r, 35c:35r, 36c:36r, 37c:37r, 38c:38r, 39c:39r, 40c:40r, 41c:41r, 42c:42r, 43c:43r, 44c:44r, 45c:45r, 46c:46r, 47c:47r, 48c:48r, 49c:49r, 50c:50r, 51c:51r, 52c:52r, 53c:53r, 54c:54r, 55c:55r, 56c:56r, 57c:57r, 58c:58r, 59c:59r, 60c:60r, 61c:61r, 62c:62r, 63c:63r, 64c:64r, 65c:65r, 66c:66r, 67c:67r, 68c:68r, 69c:69r, 70c:70r, 71c:71r, 72c:72r, 73c:73r, 74c:74r, 75c:75r, 76c:76r, 77c:77r, 78c:78r, 79c:79r, 80c:80r, 81c:81r, 82c:82r, 83c:83r, 84c:84r, 85c:85r, 86c:86r, 87c:87r, 88c:88r, 89c:89r, 90c:90r, 91c:91r, 92c:92r, 93c:93r, 94c:94r, 95c:95r, 96c:96r, 97c:97r, 98c:98r, 99c:99r, 100c:100r, 101c:101r, 102c:102r, 103c:103r, 104c:104r, 105c:105r, 106c:106r, 107c:107r, 108c:108r, 109c:109r, 110c:110r, 111c:111r, 112c:112r, 113c:113r, 114c:114r, 115c:115r, 116c:116r, 117c:117r, 118c:118r, 119c:119r, 120c:120r, 121c:121r, 122c:122r, 123c:123r, 124c:124r, 125c:125r, 126c:126r, 127c:127r, 128c:128r, 129c:129r, 130c:130r, 131c:131r, 132c:132r, 133c:133r, 134c:134r, 135c:135r, 136c:136r, 137c:137r, 138c:138r, 139c:139r, 140c:140r, 141c:141r, 142c:142r, 143c:143r, 144c:144r, 145c:145r, 146c:146r, 147c:147r, 148c:148r, 149c:149r, 150c:150r, 151c:151r, 152c:152r, 153c:153r, 154c:154r, 155c:155r, 156c:156r, 157c:157r, 158c:158r, 159c:159r, 160c:160r, 161c:161r, 162c:162r, 163c:163r, 164c:164r, 165c:165r, 166c:166r, 167c:167r, 168c:168r, 169c:169r, 170c:170r, 171c:171r, 172c:172r, 173c:173r, 174c:174r, 175c:175r, 176c:176r, 177c:177r, 178c:178r, 179c:179r, 180c:180r, 181c:181r, 182c:182r, 183c:183r, 184c:184r]

Isomorphic solution: 
  [  0r] ➞ [  0c]  Concept[0r] Urinary bladder structure (body structure) <-2147419211>
  [  1r] ➞ [  1c]  Some[1r] Procedure site (attribute) <-2147378082>➞[0r]
  [  2r] ➞ [  2c]  And[2r]➞[1r]
  [  3r] ➞ [  3c]  Some[3r] Role group (SOLOR) <-2147483593>➞[2r]
  [  4r] ➞ [  4c]  Concept[4r] Radioactivity (physical force) <-2147018351>
  [  5r] ➞ [  5c]  Some[5r] Method (attribute) <-2147314116>➞[4r]
  [  6r] ➞ [  6c]  And[6r]➞[5r]
  [  7r] ➞ [  7c]  Some[7r] Role group (SOLOR) <-2147483593>➞[6r]
  [  8r] ➞ [  8c]  Concept[8r] Endoscopic approach - access (qualifier value) <-2146941410>
  [  9r] ➞ [  9c]  Some[9r] Access (attribute) <-2147315914>➞[8r]
  [ 10r] ➞ [ 10c]  And[10r]➞[9r]
  [ 11r] ➞ [ 11c]  Some[11r] Role group (SOLOR) <-2147483593>➞[10r]
  [ 12r] ➞ [ 12c]  Concept[12r] Transurethral approach (qualifier value) <-2146691909>
  [ 13r] ➞ [ 13c]  Some[13r] Approach (attribute) <-2147314305>➞[12r]
  [ 14r] ➞ [ 14c]  And[14r]➞[13r]
  [ 15r] ➞ [ 15c]  Some[15r] Role group (SOLOR) <-2147483593>➞[14r]
  [ 16r] ➞ [ 16c]  Concept[16r] Brachytherapy - action (qualifier value) <-2146464707>
  [ 17r] ➞ [ 17c]  Some[17r] Method (attribute) <-2147314116>➞[16r]
  [ 18r] ➞ [ 18c]  And[18r]➞[17r]
  [ 19r] ➞ [ 19c]  Some[19r] Role group (SOLOR) <-2147483593>➞[18r]
  [ 20r] ➞ [ 20c]  Concept[20r] Urethral structure (body structure) <-2147284236>
  [ 21r] ➞ [ 21c]  Some[21r] Procedure site (attribute) <-2147378082>➞[20r]
  [ 22r] ➞ [ 22c]  And[22r]➞[21r]
  [ 23r] ➞ [ 23c]  Some[23r] Role group (SOLOR) <-2147483593>➞[22r]
  [ 24r] ➞ [ 24c]  Concept[24r] Cystoscope, device (physical object) <-2147196166>
  [ 25r] ➞ [ 25c]  Some[25r] Instrumentation (attribute) <-2146514480>➞[24r]
  [ 26r] ➞ [ 26c]  And[26r]➞[25r]
  [ 27r] ➞ [ 27c]  Some[27r] Role group (SOLOR) <-2147483593>➞[26r]
  [ 28r] ➞ [ 28c]  Concept[28r] Implantation - action (qualifier value) <-2146940339>
  [ 29r] ➞ [ 29c]  Some[29r] Method (attribute) <-2147314116>➞[28r]
  [ 30r] ➞ [ 30c]  And[30r]➞[29r]
  [ 31r] ➞ [ 31c]  Some[31r] Role group (SOLOR) <-2147483593>➞[30r]
  [ 32r] ➞ [ 32c]  Concept[32r] Radioactivity (physical force) <-2147018351>
  [ 33r] ➞ [ 33c]  Some[33r] Using (attribute) <-2147300466>➞[32r]
  [ 34r] ➞ [ 34c]  And[34r]➞[33r]
  [ 35r] ➞ [ 35c]  Some[35r] Role group (SOLOR) <-2147483593>➞[34r]
  [ 36r] ➞ [ 36c]  Concept[36r] Ionizing radiation (physical force) <-2146978384>
  [ 37r] ➞ [ 37c]  Some[37r] Using (attribute) <-2147300466>➞[36r]
  [ 38r] ➞ [ 38c]  And[38r]➞[37r]
  [ 39r] ➞ [ 39c]  Some[39r] Role group (SOLOR) <-2147483593>➞[38r]
  [ 40r] ➞ [ 40c]  Concept[40r] Cystoscope, device (physical object) <-2147196166>
  [ 41r] ➞ [ 41c]  Some[41r] Access instrument (attribute) <-2147283909>➞[40r]
  [ 42r] ➞ [ 42c]  And[42r]➞[41r]
  [ 43r] ➞ [ 43c]  Some[43r] Role group (SOLOR) <-2147483593>➞[42r]
  [ 44r] ➞ [ 44c]  Concept[44r] Endoscope, device (physical object) <-2146961395>
  [ 45r] ➞ [ 45c]  Some[45r] Using (attribute) <-2147300466>➞[44r]
  [ 46r] ➞ [ 46c]  And[46r]➞[45r]
  [ 47r] ➞ [ 47c]  Some[47r] Role group (SOLOR) <-2147483593>➞[46r]
  [ 48r] ➞ [ 48c]  Concept[48r] Urinary bladder structure (body structure) <-2147419211>
  [ 49r] ➞ [ 49c]  Some[49r] Procedure site (attribute) <-2147378082>➞[48r]
  [ 50r] ➞ [ 50c]  Concept[50r] Endoscopic inspection - action (qualifier value) <-2146940226>
  [ 51r] ➞ [ 51c]  Some[51r] Method (attribute) <-2147314116>➞[50r]
  [ 52r] ➞ [ 52c]  And[52r]➞[49r, 51r]
  [ 53r] ➞ [ 53c]  Some[53r] Role group (SOLOR) <-2147483593>➞[52r]
  [ 54r] ➞ [ 54c]  Concept[54r] Urethral structure (body structure) <-2147284236>
  [ 55r] ➞ [ 55c]  Some[55r] Procedure site (attribute) <-2147378082>➞[54r]
  [ 56r] ➞ [ 56c]  Concept[56r] Implantation - action (qualifier value) <-2146940339>
  [ 57r] ➞ [ 57c]  Some[57r] Method (attribute) <-2147314116>➞[56r]
  [ 58r] ➞ [ 58c]  Concept[58r] Endoscopic inspection - action (qualifier value) <-2146940226>
  [ 59r] ➞ [ 59c]  Some[59r] Method (attribute) <-2147314116>➞[58r]
  [ 60r] ➞ [ 60c]  Concept[60r] Surgical action (qualifier value) <-2146940928>
  [ 61r] ➞ [ 61c]  Some[61r] Method (attribute) <-2147314116>➞[60r]
  [ 62r] ➞ [ 62c]  And[62r]➞[55r, 57r, 59r, 61r]
  [ 63r] ➞ [ 63c]  Some[63r] Role group (SOLOR) <-2147483593>➞[62r]
  [ 64r] ➞ [ 64c]  Concept[64r] Endoscope, device (physical object) <-2146961395>
  [ 65r] ➞ [ 65c]  Some[65r] Direct device (attribute) <-2147378264>➞[64r]
  [ 66r] ➞ [ 66c]  And[66r]➞[65r]
  [ 67r] ➞ [ 67c]  Some[67r] Role group (SOLOR) <-2147483593>➞[66r]
  [ 68r] ➞ [ 68c]  Concept[68r] Insertion - action (qualifier value) <-2147349375>
  [ 69r] ➞ [ 69c]  Some[69r] Method (attribute) <-2147314116>➞[68r]
  [ 70r] ➞ [ 70c]  And[70r]➞[69r]
  [ 71r] ➞ [ 71c]  Some[71r] Role group (SOLOR) <-2147483593>➞[70r]
  [ 72r] ➞ [ 72c]  Concept[72r] Therapeutic intent (qualifier value) <-2147292522>
  [ 73r] ➞ [ 73c]  Some[73r] Has intent (attribute) <-2147378135>➞[72r]
  [ 74r] ➞ [ 74c]  And[74r]➞[73r]
  [ 75r] ➞ [ 75c]  Some[75r] Role group (SOLOR) <-2147483593>➞[74r]
  [ 76r] ➞ [ 76c]  Concept[76r] Urethral structure (body structure) <-2147284236>
  [ 77r] ➞ [ 77c]  Some[77r] Procedure site (attribute) <-2147378082>➞[76r]
  [ 78r] ➞ [ 78c]  Concept[78r] Inspection - action (qualifier value) <-2146938586>
  [ 79r] ➞ [ 79c]  Some[79r] Method (attribute) <-2147314116>➞[78r]
  [ 80r] ➞ [ 80c]  And[80r]➞[77r, 79r]
  [ 81r] ➞ [ 81c]  Some[81r] Role group (SOLOR) <-2147483593>➞[80r]
  [ 82r] ➞ [ 82c]  Concept[82r] Urinary tract structure (body structure) <-2146984124>
  [ 83r] ➞ [ 83c]  Some[83r] Procedure site (attribute) <-2147378082>➞[82r]
  [ 84r] ➞ [ 84c]  Concept[84r] Surgical action (qualifier value) <-2146940928>
  [ 85r] ➞ [ 85c]  Some[85r] Method (attribute) <-2147314116>➞[84r]
  [ 86r] ➞ [ 86c]  And[86r]➞[83r, 85r]
  [ 87r] ➞ [ 87c]  Some[87r] Role group (SOLOR) <-2147483593>➞[86r]
  [ 88r] ➞ [ 88c]  Concept[88r] Urinary bladder structure (body structure) <-2147419211>
  [ 89r] ➞ [ 89c]  Some[89r] Procedure site (attribute) <-2147378082>➞[88r]
  [ 90r] ➞ [ 90c]  Concept[90r] Inspection - action (qualifier value) <-2146938586>
  [ 91r] ➞ [ 91c]  Some[91r] Method (attribute) <-2147314116>➞[90r]
  [ 92r] ➞ [ 92c]  And[92r]➞[89r, 91r]
  [ 93r] ➞ [ 93c]  Some[93r] Role group (SOLOR) <-2147483593>➞[92r]
  [ 94r] ➞ [ 94c]  Concept[94r] Radioactive isotope (substance) <-2147423787>
  [ 95r] ➞ [ 95c]  Some[95r] Direct substance (attribute) <-2147378192>➞[94r]
  [ 96r] ➞ [ 96c]  And[96r]➞[95r]
  [ 97r] ➞ [ 97c]  Some[97r] Role group (SOLOR) <-2147483593>➞[96r]
  [ 98r] ➞ [ 98c]  Concept[98r] Implant, device (physical object) <-2146919385>
  [ 99r] ➞ [ 99c]  Some[99r] Direct device (attribute) <-2147378264>➞[98r]
  [100r] ➞ [100c]  And[100r]➞[99r]
  [101r] ➞ [101c]  Some[101r] Role group (SOLOR) <-2147483593>➞[100r]
  [102r] ➞ [102c]  Concept[102r] Endoscopic approach - access (qualifier value) <-2146941410>
  [103r] ➞ [103c]  Some[103r] Access (attribute) <-2147315914>➞[102r]
  [104r] ➞ [104c]  Concept[104r] Transurethral approach (qualifier value) <-2146691909>
  [105r] ➞ [105c]  Some[105r] Approach (attribute) <-2147314305>➞[104r]
  [106r] ➞ [106c]  Concept[106r] Cystoscope, device (physical object) <-2147196166>
  [107r] ➞ [107c]  Some[107r] Access instrument (attribute) <-2147283909>➞[106r]
  [108r] ➞ [108c]  And[108r]➞[103r, 105r, 107r]
  [109r] ➞ [109c]  Some[109r] Role group (SOLOR) <-2147483593>➞[108r]
  [110r] ➞ [110c]  Concept[110r] Urethral structure (body structure) <-2147284236>
  [111r] ➞ [111c]  Some[111r] Procedure site (attribute) <-2147378082>➞[110r]
  [112r] ➞ [112c]  Concept[112r] Endoscopic approach - access (qualifier value) <-2146941410>
  [113r] ➞ [113c]  Some[113r] Access (attribute) <-2147315914>➞[112r]
  [114r] ➞ [114c]  Concept[114r] Inspection - action (qualifier value) <-2146938586>
  [115r] ➞ [115c]  Some[115r] Method (attribute) <-2147314116>➞[114r]
  [116r] ➞ [116c]  Concept[116r] Endoscope, device (physical object) <-2146961395>
  [117r] ➞ [117c]  Some[117r] Access instrument (attribute) <-2147283909>➞[116r]
  [118r] ➞ [118c]  And[118r]➞[111r, 113r, 115r, 117r]
  [119r] ➞ [119c]  Some[119r] Role group (SOLOR) <-2147483593>➞[118r]
  [120r] ➞ [120c]  Concept[120r] Urinary bladder structure (body structure) <-2147419211>
  [121r] ➞ [121c]  Some[121r] Procedure site (attribute) <-2147378082>➞[120r]
  [122r] ➞ [122c]  Concept[122r] Endoscopic approach - access (qualifier value) <-2146941410>
  [123r] ➞ [123c]  Some[123r] Access (attribute) <-2147315914>➞[122r]
  [124r] ➞ [124c]  Concept[124r] Inspection - action (qualifier value) <-2146938586>
  [125r] ➞ [125c]  Some[125r] Method (attribute) <-2147314116>➞[124r]
  [126r] ➞ [126c]  Concept[126r] Cystoscope, device (physical object) <-2147196166>
  [127r] ➞ [127c]  Some[127r] Access instrument (attribute) <-2147283909>➞[126r]
  [128r] ➞ [128c]  And[128r]➞[121r, 123r, 125r, 127r]
  [129r] ➞ [129c]  Some[129r] Role group (SOLOR) <-2147483593>➞[128r]
  [130r] ➞ [130c]  Concept[130r] Urethral structure (body structure) <-2147284236>
  [131r] ➞ [131c]  Some[131r] Procedure site (attribute) <-2147378082>➞[130r]
  [132r] ➞ [132c]  Concept[132r] Urinary bladder structure (body structure) <-2147419211>
  [133r] ➞ [133c]  Some[133r] Procedure site (attribute) <-2147378082>➞[132r]
  [134r] ➞ [134c]  Concept[134r] Endoscopic approach - access (qualifier value) <-2146941410>
  [135r] ➞ [135c]  Some[135r] Access (attribute) <-2147315914>➞[134r]
  [136r] ➞ [136c]  Concept[136r] Transurethral approach (qualifier value) <-2146691909>
  [137r] ➞ [137c]  Some[137r] Approach (attribute) <-2147314305>➞[136r]
  [138r] ➞ [138c]  Concept[138r] Inspection - action (qualifier value) <-2146938586>
  [139r] ➞ [139c]  Some[139r] Method (attribute) <-2147314116>➞[138r]
  [140r] ➞ [140c]  Concept[140r] Cystoscope, device (physical object) <-2147196166>
  [141r] ➞ [141c]  Some[141r] Access instrument (attribute) <-2147283909>➞[140r]
  [142r] ➞ [142c]  And[142r]➞[131r, 133r, 135r, 137r, 139r, 141r]
  [143r] ➞ [143c]  Some[143r] Role group (SOLOR) <-2147483593>➞[142r]
  [144r] ➞ [144c]  Concept[144r] Radioactive isotope (substance) <-2147423787>
  [145r] ➞ [145c]  Some[145r] Direct substance (attribute) <-2147378192>➞[144r]
  [146r] ➞ [146c]  Concept[146r] Urinary bladder structure (body structure) <-2147419211>
  [147r] ➞ [147c]  Some[147r] Procedure site (attribute) <-2147378082>➞[146r]
  [148r] ➞ [148c]  Concept[148r] Endoscopic approach - access (qualifier value) <-2146941410>
  [149r] ➞ [149c]  Some[149r] Access (attribute) <-2147315914>➞[148r]
  [150r] ➞ [150c]  Concept[150r] Transurethral approach (qualifier value) <-2146691909>
  [151r] ➞ [151c]  Some[151r] Approach (attribute) <-2147314305>➞[150r]
  [152r] ➞ [152c]  Concept[152r] Insertion - action (qualifier value) <-2147349375>
  [153r] ➞ [153c]  Some[153r] Method (attribute) <-2147314116>➞[152r]
  [154r] ➞ [154c]  Concept[154r] Inspection - action (qualifier value) <-2146938586>
  [155r] ➞ [155c]  Some[155r] Method (attribute) <-2147314116>➞[154r]
  [156r] ➞ [156c]  Concept[156r] Cystoscope, device (physical object) <-2147196166>
  [157r] ➞ [157c]  Some[157r] Access instrument (attribute) <-2147283909>➞[156r]
  [158r] ➞ [158c]  And[158r]➞[145r, 147r, 149r, 151r, 153r, 155r, 157r]
  [159r] ➞ [159c]  Some[159r] Role group (SOLOR) <-2147483593>➞[158r]
  [160r] ➞ [160c]  Concept[160r] Endoscopic approach - access (qualifier value) <-2146941410>
  [161r] ➞ [161c]  Some[161r] Access (attribute) <-2147315914>➞[160r]
  [162r] ➞ [162c]  Concept[162r] Transurethral approach (qualifier value) <-2146691909>
  [163r] ➞ [163c]  Some[163r] Approach (attribute) <-2147314305>➞[162r]
  [164r] ➞ [164c]  Concept[164r] Inspection - action (qualifier value) <-2146938586>
  [165r] ➞ [165c]  Some[165r] Method (attribute) <-2147314116>➞[164r]
  [166r] ➞ [166c]  Concept[166r] Cystoscope, device (physical object) <-2147196166>
  [167r] ➞ [167c]  Some[167r] Access instrument (attribute) <-2147283909>➞[166r]
  [168r] ➞ [168c]  Concept[168r] Urinary bladder structure (body structure) <-2147419211>
  [169r] ➞ [169c]  Some[169r] Procedure site - Direct (attribute) <-2146878287>➞[168r]
  [170r] ➞ [170c]  And[170r]➞[161r, 163r, 165r, 167r, 169r]
  [171r] ➞ [171c]  Some[171r] Role group (SOLOR) <-2147483593>➞[170r]
  [172r] ➞ [172c]  Concept[172r] Endoscopy with surgical procedure (procedure) <-2147474859>
  [173r] ➞ [173c]  Concept[173r] Transurethral cystoscopy (procedure) <-2147462038>
  [174r] ➞ [174c]  Concept[174r] Endoscopy of urethra (procedure) <-2147319097>
  [175r] ➞ [175c]  Concept[175r] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>
  [176r] ➞ [176c]  Concept[176r] Therapeutic endoscopic procedure (procedure) <-2147153232>
  [177r] ➞ [177c]  Concept[177r] Transurethral cystoscopy (procedure) <-2147133743>
  [178r] ➞ [178c]  Concept[178r] Operation on urinary tract proper (procedure) <-2147061512>
  [179r] ➞ [179c]  Concept[179r] Introduction to urinary tract (procedure) <-2147055307>
  [180r] ➞ [180c]  Concept[180r] Bladder implantation (procedure) <-2147044478>
  [181r] ➞ [181c]  Concept[181r] Insertion of radioactive isotope (procedure) <-2146915764>
  [182r] ➞ [182c]  And[182r]➞[3r, 7r, 11r, 15r, 19r, 23r, 27r, 31r, 35r, 39r, 43r, 47r, 53r, 63r, 67r, 71r, 75r, 81r, 87r, 93r, 97r, 101r, 109r, 119r, 129r, 143r, 159r, 171r, 172r, 173r, 174r, 175r, 176r, 177r, 178r, 179r, 180r, 181r]
  [183r] ➞ [183c]  Necessary[183r]➞[182r]
  [184r] ➞ [184c]  Root[184r]➞[183r]

Additions: 


Deletions: 


Shared relationship roots: 

  Concept[173] Transurethral cystoscopy (procedure) <-2147462038>

  Concept[174] Endoscopy of urethra (procedure) <-2147319097>

  Concept[172] Endoscopy with surgical procedure (procedure) <-2147474859>

  Concept[175] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>

  Concept[176] Therapeutic endoscopic procedure (procedure) <-2147153232>

  Concept[177] Transurethral cystoscopy (procedure) <-2147133743>

  Concept[178] Operation on urinary tract proper (procedure) <-2147061512>

  Concept[179] Introduction to urinary tract (procedure) <-2147055307>

  Concept[180] Bladder implantation (procedure) <-2147044478>

  Concept[181] Insertion of radioactive isotope (procedure) <-2146915764>

  Some[3] Role group (SOLOR) <-2147483593>➞[2]
    And[2]➞[1]
        Some[1] Procedure site (attribute) <-2147378082>➞[0]
            Concept[0] Urinary bladder structure (body structure) <-2147419211>

  Some[101] Role group (SOLOR) <-2147483593>➞[100]
    And[100]➞[99]
        Some[99] Direct device (attribute) <-2147378264>➞[98]
            Concept[98] Implant, device (physical object) <-2146919385>

  Some[67] Role group (SOLOR) <-2147483593>➞[66]
    And[66]➞[65]
        Some[65] Direct device (attribute) <-2147378264>➞[64]
            Concept[64] Endoscope, device (physical object) <-2146961395>

  Some[97] Role group (SOLOR) <-2147483593>➞[96]
    And[96]➞[95]
        Some[95] Direct substance (attribute) <-2147378192>➞[94]
            Concept[94] Radioactive isotope (substance) <-2147423787>

  Some[71] Role group (SOLOR) <-2147483593>➞[70]
    And[70]➞[69]
        Some[69] Method (attribute) <-2147314116>➞[68]
            Concept[68] Insertion - action (qualifier value) <-2147349375>

  Some[15] Role group (SOLOR) <-2147483593>➞[14]
    And[14]➞[13]
        Some[13] Approach (attribute) <-2147314305>➞[12]
            Concept[12] Transurethral approach (qualifier value) <-2146691909>

  Some[39] Role group (SOLOR) <-2147483593>➞[38]
    And[38]➞[37]
        Some[37] Using (attribute) <-2147300466>➞[36]
            Concept[36] Ionizing radiation (physical force) <-2146978384>

  Some[75] Role group (SOLOR) <-2147483593>➞[74]
    And[74]➞[73]
        Some[73] Has intent (attribute) <-2147378135>➞[72]
            Concept[72] Therapeutic intent (qualifier value) <-2147292522>

  Some[23] Role group (SOLOR) <-2147483593>➞[22]
    And[22]➞[21]
        Some[21] Procedure site (attribute) <-2147378082>➞[20]
            Concept[20] Urethral structure (body structure) <-2147284236>

  Some[43] Role group (SOLOR) <-2147483593>➞[42]
    And[42]➞[41]
        Some[41] Access instrument (attribute) <-2147283909>➞[40]
            Concept[40] Cystoscope, device (physical object) <-2147196166>

  Some[27] Role group (SOLOR) <-2147483593>➞[26]
    And[26]➞[25]
        Some[25] Instrumentation (attribute) <-2146514480>➞[24]
            Concept[24] Cystoscope, device (physical object) <-2147196166>

  Some[7] Role group (SOLOR) <-2147483593>➞[6]
    And[6]➞[5]
        Some[5] Method (attribute) <-2147314116>➞[4]
            Concept[4] Radioactivity (physical force) <-2147018351>

  Some[35] Role group (SOLOR) <-2147483593>➞[34]
    And[34]➞[33]
        Some[33] Using (attribute) <-2147300466>➞[32]
            Concept[32] Radioactivity (physical force) <-2147018351>

  Some[47] Role group (SOLOR) <-2147483593>➞[46]
    And[46]➞[45]
        Some[45] Using (attribute) <-2147300466>➞[44]
            Concept[44] Endoscope, device (physical object) <-2146961395>

  Some[11] Role group (SOLOR) <-2147483593>➞[10]
    And[10]➞[9]
        Some[9] Access (attribute) <-2147315914>➞[8]
            Concept[8] Endoscopic approach - access (qualifier value) <-2146941410>

  Some[31] Role group (SOLOR) <-2147483593>➞[30]
    And[30]➞[29]
        Some[29] Method (attribute) <-2147314116>➞[28]
            Concept[28] Implantation - action (qualifier value) <-2146940339>

  Some[19] Role group (SOLOR) <-2147483593>➞[18]
    And[18]➞[17]
        Some[17] Method (attribute) <-2147314116>➞[16]
            Concept[16] Brachytherapy - action (qualifier value) <-2146464707>

  Some[81] Role group (SOLOR) <-2147483593>➞[80]
    And[80]➞[77, 79]
        Some[77] Procedure site (attribute) <-2147378082>➞[76]
            Concept[76] Urethral structure (body structure) <-2147284236>
        Some[79] Method (attribute) <-2147314116>➞[78]
            Concept[78] Inspection - action (qualifier value) <-2146938586>

  Some[87] Role group (SOLOR) <-2147483593>➞[86]
    And[86]➞[83, 85]
        Some[83] Procedure site (attribute) <-2147378082>➞[82]
            Concept[82] Urinary tract structure (body structure) <-2146984124>
        Some[85] Method (attribute) <-2147314116>➞[84]
            Concept[84] Surgical action (qualifier value) <-2146940928>

  Some[53] Role group (SOLOR) <-2147483593>➞[52]
    And[52]➞[49, 51]
        Some[49] Procedure site (attribute) <-2147378082>➞[48]
            Concept[48] Urinary bladder structure (body structure) <-2147419211>
        Some[51] Method (attribute) <-2147314116>➞[50]
            Concept[50] Endoscopic inspection - action (qualifier value) <-2146940226>

  Some[93] Role group (SOLOR) <-2147483593>➞[92]
    And[92]➞[89, 91]
        Some[89] Procedure site (attribute) <-2147378082>➞[88]
            Concept[88] Urinary bladder structure (body structure) <-2147419211>
        Some[91] Method (attribute) <-2147314116>➞[90]
            Concept[90] Inspection - action (qualifier value) <-2146938586>

  Some[63] Role group (SOLOR) <-2147483593>➞[62]
    And[62]➞[55, 57, 59, 61]
        Some[55] Procedure site (attribute) <-2147378082>➞[54]
            Concept[54] Urethral structure (body structure) <-2147284236>
        Some[57] Method (attribute) <-2147314116>➞[56]
            Concept[56] Implantation - action (qualifier value) <-2146940339>
        Some[59] Method (attribute) <-2147314116>➞[58]
            Concept[58] Endoscopic inspection - action (qualifier value) <-2146940226>
        Some[61] Method (attribute) <-2147314116>➞[60]
            Concept[60] Surgical action (qualifier value) <-2146940928>

  Some[109] Role group (SOLOR) <-2147483593>➞[108]
    And[108]➞[103, 105, 107]
        Some[103] Access (attribute) <-2147315914>➞[102]
            Concept[102] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[105] Approach (attribute) <-2147314305>➞[104]
            Concept[104] Transurethral approach (qualifier value) <-2146691909>
        Some[107] Access instrument (attribute) <-2147283909>➞[106]
            Concept[106] Cystoscope, device (physical object) <-2147196166>

  Some[119] Role group (SOLOR) <-2147483593>➞[118]
    And[118]➞[111, 113, 115, 117]
        Some[111] Procedure site (attribute) <-2147378082>➞[110]
            Concept[110] Urethral structure (body structure) <-2147284236>
        Some[113] Access (attribute) <-2147315914>➞[112]
            Concept[112] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[115] Method (attribute) <-2147314116>➞[114]
            Concept[114] Inspection - action (qualifier value) <-2146938586>
        Some[117] Access instrument (attribute) <-2147283909>➞[116]
            Concept[116] Endoscope, device (physical object) <-2146961395>

  Some[129] Role group (SOLOR) <-2147483593>➞[128]
    And[128]➞[121, 123, 125, 127]
        Some[121] Procedure site (attribute) <-2147378082>➞[120]
            Concept[120] Urinary bladder structure (body structure) <-2147419211>
        Some[123] Access (attribute) <-2147315914>➞[122]
            Concept[122] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[125] Method (attribute) <-2147314116>➞[124]
            Concept[124] Inspection - action (qualifier value) <-2146938586>
        Some[127] Access instrument (attribute) <-2147283909>➞[126]
            Concept[126] Cystoscope, device (physical object) <-2147196166>

  Some[171] Role group (SOLOR) <-2147483593>➞[170]
    And[170]➞[161, 163, 165, 167, 169]
        Some[161] Access (attribute) <-2147315914>➞[160]
            Concept[160] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[163] Approach (attribute) <-2147314305>➞[162]
            Concept[162] Transurethral approach (qualifier value) <-2146691909>
        Some[165] Method (attribute) <-2147314116>➞[164]
            Concept[164] Inspection - action (qualifier value) <-2146938586>
        Some[167] Access instrument (attribute) <-2147283909>➞[166]
            Concept[166] Cystoscope, device (physical object) <-2147196166>
        Some[169] Procedure site - Direct (attribute) <-2146878287>➞[168]
            Concept[168] Urinary bladder structure (body structure) <-2147419211>

  Some[143] Role group (SOLOR) <-2147483593>➞[142]
    And[142]➞[131, 133, 135, 137, 139, 141]
        Some[131] Procedure site (attribute) <-2147378082>➞[130]
            Concept[130] Urethral structure (body structure) <-2147284236>
        Some[133] Procedure site (attribute) <-2147378082>➞[132]
            Concept[132] Urinary bladder structure (body structure) <-2147419211>
        Some[135] Access (attribute) <-2147315914>➞[134]
            Concept[134] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[137] Approach (attribute) <-2147314305>➞[136]
            Concept[136] Transurethral approach (qualifier value) <-2146691909>
        Some[139] Method (attribute) <-2147314116>➞[138]
            Concept[138] Inspection - action (qualifier value) <-2146938586>
        Some[141] Access instrument (attribute) <-2147283909>➞[140]
            Concept[140] Cystoscope, device (physical object) <-2147196166>

  Some[159] Role group (SOLOR) <-2147483593>➞[158]
    And[158]➞[145, 147, 149, 151, 153, 155, 157]
        Some[145] Direct substance (attribute) <-2147378192>➞[144]
            Concept[144] Radioactive isotope (substance) <-2147423787>
        Some[147] Procedure site (attribute) <-2147378082>➞[146]
            Concept[146] Urinary bladder structure (body structure) <-2147419211>
        Some[149] Access (attribute) <-2147315914>➞[148]
            Concept[148] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[151] Approach (attribute) <-2147314305>➞[150]
            Concept[150] Transurethral approach (qualifier value) <-2146691909>
        Some[153] Method (attribute) <-2147314116>➞[152]
            Concept[152] Insertion - action (qualifier value) <-2147349375>
        Some[155] Method (attribute) <-2147314116>➞[154]
            Concept[154] Inspection - action (qualifier value) <-2146938586>
        Some[157] Access instrument (attribute) <-2147283909>➞[156]
            Concept[156] Cystoscope, device (physical object) <-2147196166>


New relationship roots: 


Deleted relationship roots: 


Merged expression: 

Root[184m]➞[183m]
    Necessary[183m]➞[182m]
        And[182m]➞[3m, 7m, 11m, 15m, 19m, 23m, 27m, 31m, 35m, 39m, 43m, 47m, 53m, 63m, 67m, 71m, 75m, 81m, 87m, 93m, 97m, 101m, 109m, 119m, 129m, 143m, 159m, 171m, 172m, 173m, 174m, 175m, 176m, 177m, 178m, 179m, 180m, 181m]
            Some[3m] Role group (SOLOR) <-2147483593>➞[2m]
                And[2m]➞[1m]
                    Some[1m] Procedure site (attribute) <-2147378082>➞[0m]
                        Concept[0m] Urinary bladder structure (body structure) <-2147419211>
            Some[7m] Role group (SOLOR) <-2147483593>➞[6m]
                And[6m]➞[5m]
                    Some[5m] Method (attribute) <-2147314116>➞[4m]
                        Concept[4m] Radioactivity (physical force) <-2147018351>
            Some[11m] Role group (SOLOR) <-2147483593>➞[10m]
                And[10m]➞[9m]
                    Some[9m] Access (attribute) <-2147315914>➞[8m]
                        Concept[8m] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[15m] Role group (SOLOR) <-2147483593>➞[14m]
                And[14m]➞[13m]
                    Some[13m] Approach (attribute) <-2147314305>➞[12m]
                        Concept[12m] Transurethral approach (qualifier value) <-2146691909>
            Some[19m] Role group (SOLOR) <-2147483593>➞[18m]
                And[18m]➞[17m]
                    Some[17m] Method (attribute) <-2147314116>➞[16m]
                        Concept[16m] Brachytherapy - action (qualifier value) <-2146464707>
            Some[23m] Role group (SOLOR) <-2147483593>➞[22m]
                And[22m]➞[21m]
                    Some[21m] Procedure site (attribute) <-2147378082>➞[20m]
                        Concept[20m] Urethral structure (body structure) <-2147284236>
            Some[27m] Role group (SOLOR) <-2147483593>➞[26m]
                And[26m]➞[25m]
                    Some[25m] Instrumentation (attribute) <-2146514480>➞[24m]
                        Concept[24m] Cystoscope, device (physical object) <-2147196166>
            Some[31m] Role group (SOLOR) <-2147483593>➞[30m]
                And[30m]➞[29m]
                    Some[29m] Method (attribute) <-2147314116>➞[28m]
                        Concept[28m] Implantation - action (qualifier value) <-2146940339>
            Some[35m] Role group (SOLOR) <-2147483593>➞[34m]
                And[34m]➞[33m]
                    Some[33m] Using (attribute) <-2147300466>➞[32m]
                        Concept[32m] Radioactivity (physical force) <-2147018351>
            Some[39m] Role group (SOLOR) <-2147483593>➞[38m]
                And[38m]➞[37m]
                    Some[37m] Using (attribute) <-2147300466>➞[36m]
                        Concept[36m] Ionizing radiation (physical force) <-2146978384>
            Some[43m] Role group (SOLOR) <-2147483593>➞[42m]
                And[42m]➞[41m]
                    Some[41m] Access instrument (attribute) <-2147283909>➞[40m]
                        Concept[40m] Cystoscope, device (physical object) <-2147196166>
            Some[47m] Role group (SOLOR) <-2147483593>➞[46m]
                And[46m]➞[45m]
                    Some[45m] Using (attribute) <-2147300466>➞[44m]
                        Concept[44m] Endoscope, device (physical object) <-2146961395>
            Some[53m] Role group (SOLOR) <-2147483593>➞[52m]
                And[52m]➞[49m, 51m]
                    Some[49m] Procedure site (attribute) <-2147378082>➞[48m]
                        Concept[48m] Urinary bladder structure (body structure) <-2147419211>
                    Some[51m] Method (attribute) <-2147314116>➞[50m]
                        Concept[50m] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63m] Role group (SOLOR) <-2147483593>➞[62m]
                And[62m]➞[55m, 57m, 59m, 61m]
                    Some[55m] Procedure site (attribute) <-2147378082>➞[54m]
                        Concept[54m] Urethral structure (body structure) <-2147284236>
                    Some[57m] Method (attribute) <-2147314116>➞[56m]
                        Concept[56m] Implantation - action (qualifier value) <-2146940339>
                    Some[59m] Method (attribute) <-2147314116>➞[58m]
                        Concept[58m] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61m] Method (attribute) <-2147314116>➞[60m]
                        Concept[60m] Surgical action (qualifier value) <-2146940928>
            Some[67m] Role group (SOLOR) <-2147483593>➞[66m]
                And[66m]➞[65m]
                    Some[65m] Direct device (attribute) <-2147378264>➞[64m]
                        Concept[64m] Endoscope, device (physical object) <-2146961395>
            Some[71m] Role group (SOLOR) <-2147483593>➞[70m]
                And[70m]➞[69m]
                    Some[69m] Method (attribute) <-2147314116>➞[68m]
                        Concept[68m] Insertion - action (qualifier value) <-2147349375>
            Some[75m] Role group (SOLOR) <-2147483593>➞[74m]
                And[74m]➞[73m]
                    Some[73m] Has intent (attribute) <-2147378135>➞[72m]
                        Concept[72m] Therapeutic intent (qualifier value) <-2147292522>
            Some[81m] Role group (SOLOR) <-2147483593>➞[80m]
                And[80m]➞[77m, 79m]
                    Some[77m] Procedure site (attribute) <-2147378082>➞[76m]
                        Concept[76m] Urethral structure (body structure) <-2147284236>
                    Some[79m] Method (attribute) <-2147314116>➞[78m]
                        Concept[78m] Inspection - action (qualifier value) <-2146938586>
            Some[87m] Role group (SOLOR) <-2147483593>➞[86m]
                And[86m]➞[83m, 85m]
                    Some[83m] Procedure site (attribute) <-2147378082>➞[82m]
                        Concept[82m] Urinary tract structure (body structure) <-2146984124>
                    Some[85m] Method (attribute) <-2147314116>➞[84m]
                        Concept[84m] Surgical action (qualifier value) <-2146940928>
            Some[93m] Role group (SOLOR) <-2147483593>➞[92m]
                And[92m]➞[89m, 91m]
                    Some[89m] Procedure site (attribute) <-2147378082>➞[88m]
                        Concept[88m] Urinary bladder structure (body structure) <-2147419211>
                    Some[91m] Method (attribute) <-2147314116>➞[90m]
                        Concept[90m] Inspection - action (qualifier value) <-2146938586>
            Some[97m] Role group (SOLOR) <-2147483593>➞[96m]
                And[96m]➞[95m]
                    Some[95m] Direct substance (attribute) <-2147378192>➞[94m]
                        Concept[94m] Radioactive isotope (substance) <-2147423787>
            Some[101m] Role group (SOLOR) <-2147483593>➞[100m]
                And[100m]➞[99m]
                    Some[99m] Direct device (attribute) <-2147378264>➞[98m]
                        Concept[98m] Implant, device (physical object) <-2146919385>
            Some[109m] Role group (SOLOR) <-2147483593>➞[108m]
                And[108m]➞[103m, 105m, 107m]
                    Some[103m] Access (attribute) <-2147315914>➞[102m]
                        Concept[102m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[105m] Approach (attribute) <-2147314305>➞[104m]
                        Concept[104m] Transurethral approach (qualifier value) <-2146691909>
                    Some[107m] Access instrument (attribute) <-2147283909>➞[106m]
                        Concept[106m] Cystoscope, device (physical object) <-2147196166>
            Some[119m] Role group (SOLOR) <-2147483593>➞[118m]
                And[118m]➞[111m, 113m, 115m, 117m]
                    Some[111m] Procedure site (attribute) <-2147378082>➞[110m]
                        Concept[110m] Urethral structure (body structure) <-2147284236>
                    Some[113m] Access (attribute) <-2147315914>➞[112m]
                        Concept[112m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[115m] Method (attribute) <-2147314116>➞[114m]
                        Concept[114m] Inspection - action (qualifier value) <-2146938586>
                    Some[117m] Access instrument (attribute) <-2147283909>➞[116m]
                        Concept[116m] Endoscope, device (physical object) <-2146961395>
            Some[129m] Role group (SOLOR) <-2147483593>➞[128m]
                And[128m]➞[121m, 123m, 125m, 127m]
                    Some[121m] Procedure site (attribute) <-2147378082>➞[120m]
                        Concept[120m] Urinary bladder structure (body structure) <-2147419211>
                    Some[123m] Access (attribute) <-2147315914>➞[122m]
                        Concept[122m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[125m] Method (attribute) <-2147314116>➞[124m]
                        Concept[124m] Inspection - action (qualifier value) <-2146938586>
                    Some[127m] Access instrument (attribute) <-2147283909>➞[126m]
                        Concept[126m] Cystoscope, device (physical object) <-2147196166>
            Some[143m] Role group (SOLOR) <-2147483593>➞[142m]
                And[142m]➞[131m, 133m, 135m, 137m, 139m, 141m]
                    Some[131m] Procedure site (attribute) <-2147378082>➞[130m]
                        Concept[130m] Urethral structure (body structure) <-2147284236>
                    Some[133m] Procedure site (attribute) <-2147378082>➞[132m]
                        Concept[132m] Urinary bladder structure (body structure) <-2147419211>
                    Some[135m] Access (attribute) <-2147315914>➞[134m]
                        Concept[134m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[137m] Approach (attribute) <-2147314305>➞[136m]
                        Concept[136m] Transurethral approach (qualifier value) <-2146691909>
                    Some[139m] Method (attribute) <-2147314116>➞[138m]
                        Concept[138m] Inspection - action (qualifier value) <-2146938586>
                    Some[141m] Access instrument (attribute) <-2147283909>➞[140m]
                        Concept[140m] Cystoscope, device (physical object) <-2147196166>
            Some[159m] Role group (SOLOR) <-2147483593>➞[158m]
                And[158m]➞[145m, 147m, 149m, 151m, 153m, 155m, 157m]
                    Some[145m] Direct substance (attribute) <-2147378192>➞[144m]
                        Concept[144m] Radioactive isotope (substance) <-2147423787>
                    Some[147m] Procedure site (attribute) <-2147378082>➞[146m]
                        Concept[146m] Urinary bladder structure (body structure) <-2147419211>
                    Some[149m] Access (attribute) <-2147315914>➞[148m]
                        Concept[148m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[151m] Approach (attribute) <-2147314305>➞[150m]
                        Concept[150m] Transurethral approach (qualifier value) <-2146691909>
                    Some[153m] Method (attribute) <-2147314116>➞[152m]
                        Concept[152m] Insertion - action (qualifier value) <-2147349375>
                    Some[155m] Method (attribute) <-2147314116>➞[154m]
                        Concept[154m] Inspection - action (qualifier value) <-2146938586>
                    Some[157m] Access instrument (attribute) <-2147283909>➞[156m]
                        Concept[156m] Cystoscope, device (physical object) <-2147196166>
            Some[171m] Role group (SOLOR) <-2147483593>➞[170m]
                And[170m]➞[161m, 163m, 165m, 167m, 169m]
                    Some[161m] Access (attribute) <-2147315914>➞[160m]
                        Concept[160m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[163m] Approach (attribute) <-2147314305>➞[162m]
                        Concept[162m] Transurethral approach (qualifier value) <-2146691909>
                    Some[165m] Method (attribute) <-2147314116>➞[164m]
                        Concept[164m] Inspection - action (qualifier value) <-2146938586>
                    Some[167m] Access instrument (attribute) <-2147283909>➞[166m]
                        Concept[166m] Cystoscope, device (physical object) <-2147196166>
                    Some[169m] Procedure site - Direct (attribute) <-2146878287>➞[168m]
                        Concept[168m] Urinary bladder structure (body structure) <-2147419211>
            Concept[172m] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[173m] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[174m] Endoscopy of urethra (procedure) <-2147319097>
            Concept[175m] Nuclear medicine diagnostic procedure on genitourinary system (procedure) <-2147197236>
            Concept[176m] Therapeutic endoscopic procedure (procedure) <-2147153232>
            Concept[177m] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178m] Operation on urinary tract proper (procedure) <-2147061512>
            Concept[179m] Introduction to urinary tract (procedure) <-2147055307>
            Concept[180m] Bladder implantation (procedure) <-2147044478>
            Concept[181m] Insertion of radioactive isotope (procedure) <-2146915764>

 */
public class CorrelationProblem2 {

    static LogicalExpression getReferenceExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        NecessarySet(
                And(
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("78a80c03-d47c-35d4-8fe3-88b54fbb8c6d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("36c747c7-7108-3ec4-9a9e-fa48285841c9"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("191f6333-8c5d-32b0-870f-16889ed417f9"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("872afa01-6da3-37c5-8981-274b63e9d99d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("78a80c03-d47c-35d4-8fe3-88b54fbb8c6d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("eaff07d1-391f-3b98-9d47-8dfc187ae2fc"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("872afa01-6da3-37c5-8981-274b63e9d99d"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("102422d3-6b68-3d16-a756-1df791d91e7f"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("55877954-8897-3249-a77d-45f53810a5f4"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("4e504dc1-c971-3e20-a4f9-b86d0c0490af"),
                                                ConceptAssertion(Get.concept("4a7b9b43-dd6c-384f-8474-6b80c232eef4"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("44888eda-db16-3868-8e44-72ee3886b5bc"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("49ee3912-abb7-325c-88ba-a98824b4c47d"),
                                                ConceptAssertion(Get.concept("cca20417-4603-3200-9429-4778d0039dca"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("102422d3-6b68-3d16-a756-1df791d91e7f"),
                                                ConceptAssertion(Get.concept("569704c9-4e1f-3cfb-a475-0955a0afa135"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("49ee3912-abb7-325c-88ba-a98824b4c47d"),
                                                ConceptAssertion(Get.concept("cca20417-4603-3200-9429-4778d0039dca"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("55877954-8897-3249-a77d-45f53810a5f4"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        )
                                )
                        ),
                         ConceptAssertion(Get.concept("76f21b2e-4f6d-3875-a063-1a7550137dc6"), leb),
                         ConceptAssertion(Get.concept("829b7ab2-fb5c-3783-b7ef-1e343e84f659"), leb),
                         ConceptAssertion(Get.concept("d12bf138-35ac-31a2-9239-26ee07a04e9b"), leb),
                         ConceptAssertion(Get.concept("13db7f79-bd89-352d-a98a-987ff36e5531"), leb),
                         ConceptAssertion(Get.concept("e58b6288-a07b-3763-ab8c-3d2f850eacdb"), leb),
                         ConceptAssertion(Get.concept("546ca61f-5714-3b48-8899-00d60a9769ca"), leb),
                         ConceptAssertion(Get.concept("75b49851-9e93-3032-826b-f573348fae4b"), leb),
                         ConceptAssertion(Get.concept("a21dab6a-7ead-3d35-9b73-60d0178c5a67"), leb),
                         ConceptAssertion(Get.concept("066db292-bcd1-3be6-a575-37aa777d6b49"), leb),
                         ConceptAssertion(Get.concept("08e44f29-902d-30e9-8279-b18d6369487f"), leb)
                )
        );
        return leb.build();
    }

    static LogicalExpression getComparisonExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        NecessarySet(
                And(
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("78a80c03-d47c-35d4-8fe3-88b54fbb8c6d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("36c747c7-7108-3ec4-9a9e-fa48285841c9"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("191f6333-8c5d-32b0-870f-16889ed417f9"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("872afa01-6da3-37c5-8981-274b63e9d99d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("78a80c03-d47c-35d4-8fe3-88b54fbb8c6d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("eaff07d1-391f-3b98-9d47-8dfc187ae2fc"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("872afa01-6da3-37c5-8981-274b63e9d99d"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("102422d3-6b68-3d16-a756-1df791d91e7f"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("55877954-8897-3249-a77d-45f53810a5f4"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("4e504dc1-c971-3e20-a4f9-b86d0c0490af"),
                                                ConceptAssertion(Get.concept("4a7b9b43-dd6c-384f-8474-6b80c232eef4"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("44888eda-db16-3868-8e44-72ee3886b5bc"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("49ee3912-abb7-325c-88ba-a98824b4c47d"),
                                                ConceptAssertion(Get.concept("cca20417-4603-3200-9429-4778d0039dca"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("102422d3-6b68-3d16-a756-1df791d91e7f"),
                                                ConceptAssertion(Get.concept("569704c9-4e1f-3cfb-a475-0955a0afa135"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("49ee3912-abb7-325c-88ba-a98824b4c47d"),
                                                ConceptAssertion(Get.concept("cca20417-4603-3200-9429-4778d0039dca"), leb)
                                        ),
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("55877954-8897-3249-a77d-45f53810a5f4"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                        SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        )
                                )
                        ),
                        ConceptAssertion(Get.concept("76f21b2e-4f6d-3875-a063-1a7550137dc6"), leb),
                        ConceptAssertion(Get.concept("829b7ab2-fb5c-3783-b7ef-1e343e84f659"), leb),
                        ConceptAssertion(Get.concept("d12bf138-35ac-31a2-9239-26ee07a04e9b"), leb),
                        ConceptAssertion(Get.concept("13db7f79-bd89-352d-a98a-987ff36e5531"), leb),
                        ConceptAssertion(Get.concept("e58b6288-a07b-3763-ab8c-3d2f850eacdb"), leb),
                        ConceptAssertion(Get.concept("546ca61f-5714-3b48-8899-00d60a9769ca"), leb),
                        ConceptAssertion(Get.concept("75b49851-9e93-3032-826b-f573348fae4b"), leb),
                        ConceptAssertion(Get.concept("a21dab6a-7ead-3d35-9b73-60d0178c5a67"), leb),
                        ConceptAssertion(Get.concept("066db292-bcd1-3be6-a575-37aa777d6b49"), leb),
                        ConceptAssertion(Get.concept("08e44f29-902d-30e9-8279-b18d6369487f"), leb)
                )
        );
        return leb.build();
    }

}
