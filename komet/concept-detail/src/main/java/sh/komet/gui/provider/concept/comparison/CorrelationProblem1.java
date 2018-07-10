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
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;

/*

New isomorphic record: 929668
Isomorphic Analysis for:Cystourethroscopy with ureteroscopy and pyeloscopy (navigational concept)
     bde051a6-dc00-3a73-b85a-b1aea31890f7

Reference expression:

 Root[185r]➞[184r]
    Necessary[184r]➞[183r]
        And[183r]➞[3r, 7r, 11r, 15r, 19r, 23r, 27r, 33r, 37r, 41r, 45r, 51r, 63r, 67r, 73r, 79r, 85r, 91r, 99r, 107r, 117r, 127r, 137r, 157r, 169r, 170r, 171r, 172r, 173r, 174r, 175r, 176r, 177r, 178r, 179r, 180r, 181r, 182r]
            Some[3r] Role group (SOLOR) <-2147483593>➞[2r]
                And[2r]➞[1r]
                    Some[1r] Access (attribute) <-2147315914>➞[0r]
                        Concept[0r] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[7r] Role group (SOLOR) <-2147483593>➞[6r]
                And[6r]➞[5r]
                    Some[5r] Approach (attribute) <-2147314305>➞[4r]
                        Concept[4r] Transurethral approach (qualifier value) <-2146691909>
            Some[11r] Role group (SOLOR) <-2147483593>➞[10r]
                And[10r]➞[9r]
                    Some[9r] Procedure site (attribute) <-2147378082>➞[8r]
                        Concept[8r] Urinary bladder structure (body structure) <-2147419211>
            Some[15r] Role group (SOLOR) <-2147483593>➞[14r]
                And[14r]➞[13r]
                    Some[13r] Instrumentation (attribute) <-2146514480>➞[12r]
                        Concept[12r] Ureteroscope (physical object) <-2146503035>
            Some[19r] Role group (SOLOR) <-2147483593>➞[18r]
                And[18r]➞[17r]
                    Some[17r] Instrumentation (attribute) <-2146514480>➞[16r]
                        Concept[16r] Cystoscope, device (physical object) <-2147196166>
            Some[23r] Role group (SOLOR) <-2147483593>➞[22r]
                And[22r]➞[21r]
                    Some[21r] Procedure site (attribute) <-2147378082>➞[20r]
                        Concept[20r] Renal pelvis structure (body structure) <-2147120521>
            Some[27r] Role group (SOLOR) <-2147483593>➞[26r]
                And[26r]➞[25r]
                    Some[25r] Procedure site (attribute) <-2147378082>➞[24r]
                        Concept[24r] Ureteric structure (body structure) <-2147443660>
            Some[33r] Role group (SOLOR) <-2147483593>➞[32r]
                And[32r]➞[29r, 31r]
                    Some[29r] Procedure site (attribute) <-2147378082>➞[28r]
                        Concept[28r] Kidney structure (body structure) <-2146589772>
                    Some[31r] Method (attribute) <-2147314116>➞[30r]
                        Concept[30r] Examination - action (qualifier value) <-2146625815>
            Some[37r] Role group (SOLOR) <-2147483593>➞[36r]
                And[36r]➞[35r]
                    Some[35r] Access instrument (attribute) <-2147283909>➞[34r]
                        Concept[34r] Ureteroscope (physical object) <-2146503035>
            Some[41r] Role group (SOLOR) <-2147483593>➞[40r]
                And[40r]➞[39r]
                    Some[39r] Access instrument (attribute) <-2147283909>➞[38r]
                        Concept[38r] Cystoscope, device (physical object) <-2147196166>
            Some[45r] Role group (SOLOR) <-2147483593>➞[44r]
                And[44r]➞[43r]
                    Some[43r] Using (attribute) <-2147300466>➞[42r]
                        Concept[42r] Endoscope, device (physical object) <-2146961395>
            Some[51r] Role group (SOLOR) <-2147483593>➞[50r]
                And[50r]➞[47r, 49r]
                    Some[47r] Procedure site (attribute) <-2147378082>➞[46r]
                        Concept[46r] Ureteric structure (body structure) <-2147443660>
                    Some[49r] Method (attribute) <-2147314116>➞[48r]
                        Concept[48r] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63r] Role group (SOLOR) <-2147483593>➞[62r]
                And[62r]➞[53r, 55r, 57r, 59r, 61r]
                    Some[53r] Procedure site (attribute) <-2147378082>➞[52r]
                        Concept[52r] Urinary bladder structure (body structure) <-2147419211>
                    Some[55r] Procedure site (attribute) <-2147378082>➞[54r]
                        Concept[54r] Kidney structure (body structure) <-2146589772>
                    Some[57r] Procedure site (attribute) <-2147378082>➞[56r]
                        Concept[56r] Ureteric structure (body structure) <-2147443660>
                    Some[59r] Method (attribute) <-2147314116>➞[58r]
                        Concept[58r] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61r] Method (attribute) <-2147314116>➞[60r]
                        Concept[60r] Surgical action (qualifier value) <-2146940928>
            Some[67r] Role group (SOLOR) <-2147483593>➞[66r]
                And[66r]➞[65r]
                    Some[65r] Direct device (attribute) <-2147378264>➞[64r]
                        Concept[64r] Endoscope, device (physical object) <-2146961395>
            Some[73r] Role group (SOLOR) <-2147483593>➞[72r]
                And[72r]➞[69r, 71r]
                    Some[69r] Procedure site (attribute) <-2147378082>➞[68r]
                        Concept[68r] Urethral structure (body structure) <-2147284236>
                    Some[71r] Method (attribute) <-2147314116>➞[70r]
                        Concept[70r] Inspection - action (qualifier value) <-2146938586>
            Some[79r] Role group (SOLOR) <-2147483593>➞[78r]
                And[78r]➞[75r, 77r]
                    Some[75r] Procedure site (attribute) <-2147378082>➞[74r]
                        Concept[74r] Renal pelvis structure (body structure) <-2147120521>
                    Some[77r] Method (attribute) <-2147314116>➞[76r]
                        Concept[76r] Inspection - action (qualifier value) <-2146938586>
            Some[85r] Role group (SOLOR) <-2147483593>➞[84r]
                And[84r]➞[81r, 83r]
                    Some[81r] Procedure site (attribute) <-2147378082>➞[80r]
                        Concept[80r] Ureteric structure (body structure) <-2147443660>
                    Some[83r] Method (attribute) <-2147314116>➞[82r]
                        Concept[82r] Inspection - action (qualifier value) <-2146938586>
            Some[91r] Role group (SOLOR) <-2147483593>➞[90r]
                And[90r]➞[87r, 89r]
                    Some[87r] Procedure site (attribute) <-2147378082>➞[86r]
                        Concept[86r] Urinary bladder structure (body structure) <-2147419211>
                    Some[89r] Method (attribute) <-2147314116>➞[88r]
                        Concept[88r] Inspection - action (qualifier value) <-2146938586>
            Some[99r] Role group (SOLOR) <-2147483593>➞[98r]
                And[98r]➞[93r, 95r, 97r]
                    Some[93r] Procedure site (attribute) <-2147378082>➞[92r]
                        Concept[92r] Urinary bladder structure (body structure) <-2147419211>
                    Some[95r] Procedure site (attribute) <-2147378082>➞[94r]
                        Concept[94r] Renal pelvis structure (body structure) <-2147120521>
                    Some[97r] Method (attribute) <-2147314116>➞[96r]
                        Concept[96r] Surgical action (qualifier value) <-2146940928>
            Some[107r] Role group (SOLOR) <-2147483593>➞[106r]
                And[106r]➞[101r, 103r, 105r]
                    Some[101r] Access (attribute) <-2147315914>➞[100r]
                        Concept[100r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[103r] Approach (attribute) <-2147314305>➞[102r]
                        Concept[102r] Transurethral approach (qualifier value) <-2146691909>
                    Some[105r] Access instrument (attribute) <-2147283909>➞[104r]
                        Concept[104r] Cystoscope, device (physical object) <-2147196166>
            Some[117r] Role group (SOLOR) <-2147483593>➞[116r]
                And[116r]➞[109r, 111r, 113r, 115r]
                    Some[109r] Procedure site (attribute) <-2147378082>➞[108r]
                        Concept[108r] Urethral structure (body structure) <-2147284236>
                    Some[111r] Access (attribute) <-2147315914>➞[110r]
                        Concept[110r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[113r] Method (attribute) <-2147314116>➞[112r]
                        Concept[112r] Inspection - action (qualifier value) <-2146938586>
                    Some[115r] Access instrument (attribute) <-2147283909>➞[114r]
                        Concept[114r] Endoscope, device (physical object) <-2146961395>
            Some[127r] Role group (SOLOR) <-2147483593>➞[126r]
                And[126r]➞[119r, 121r, 123r, 125r]
                    Some[119r] Procedure site (attribute) <-2147378082>➞[118r]
                        Concept[118r] Urinary bladder structure (body structure) <-2147419211>
                    Some[121r] Access (attribute) <-2147315914>➞[120r]
                        Concept[120r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[123r] Method (attribute) <-2147314116>➞[122r]
                        Concept[122r] Inspection - action (qualifier value) <-2146938586>
                    Some[125r] Access instrument (attribute) <-2147283909>➞[124r]
                        Concept[124r] Cystoscope, device (physical object) <-2147196166>
            Some[137r] Role group (SOLOR) <-2147483593>➞[136r]
                And[136r]➞[129r, 131r, 133r, 135r]
                    Some[129r] Procedure site (attribute) <-2147378082>➞[128r]
                        Concept[128r] Ureteric structure (body structure) <-2147443660>
                    Some[131r] Access (attribute) <-2147315914>➞[130r]
                        Concept[130r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[133r] Method (attribute) <-2147314116>➞[132r]
                        Concept[132r] Inspection - action (qualifier value) <-2146938586>
                    Some[135r] Access instrument (attribute) <-2147283909>➞[134r]
                        Concept[134r] Ureteroscope (physical object) <-2146503035>
            Some[157r] Role group (SOLOR) <-2147483593>➞[156r]
                And[156r]➞[139r, 141r, 143r, 145r, 147r, 149r, 151r, 153r, 155r]
                    Some[139r] Procedure site (attribute) <-2147378082>➞[138r]
                        Concept[138r] Urethral structure (body structure) <-2147284236>
                    Some[141r] Procedure site (attribute) <-2147378082>➞[140r]
                        Concept[140r] Renal pelvis structure (body structure) <-2147120521>
                    Some[143r] Procedure site (attribute) <-2147378082>➞[142r]
                        Concept[142r] Urinary bladder structure (body structure) <-2147419211>
                    Some[145r] Procedure site (attribute) <-2147378082>➞[144r]
                        Concept[144r] Ureteric structure (body structure) <-2147443660>
                    Some[147r] Access (attribute) <-2147315914>➞[146r]
                        Concept[146r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[149r] Approach (attribute) <-2147314305>➞[148r]
                        Concept[148r] Transurethral approach (qualifier value) <-2146691909>
                    Some[151r] Method (attribute) <-2147314116>➞[150r]
                        Concept[150r] Surgical action (qualifier value) <-2146940928>
                    Some[153r] Method (attribute) <-2147314116>➞[152r]
                        Concept[152r] Inspection - action (qualifier value) <-2146938586>
                    Some[155r] Access instrument (attribute) <-2147283909>➞[154r]
                        Concept[154r] Cystoscope, device (physical object) <-2147196166>
            Some[169r] Role group (SOLOR) <-2147483593>➞[168r]
                And[168r]➞[159r, 161r, 163r, 165r, 167r]
                    Some[159r] Access (attribute) <-2147315914>➞[158r]
                        Concept[158r] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[161r] Approach (attribute) <-2147314305>➞[160r]
                        Concept[160r] Transurethral approach (qualifier value) <-2146691909>
                    Some[163r] Method (attribute) <-2147314116>➞[162r]
                        Concept[162r] Inspection - action (qualifier value) <-2146938586>
                    Some[165r] Access instrument (attribute) <-2147283909>➞[164r]
                        Concept[164r] Cystoscope, device (physical object) <-2147196166>
                    Some[167r] Procedure site - Direct (attribute) <-2146878287>➞[166r]
                        Concept[166r] Urinary bladder structure (body structure) <-2147419211>
            Concept[170r] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[171r] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[172r] Endoscopy of renal pelvis (procedure) <-2147408011>
            Concept[173r] Endoscopy of pelvic cavity (procedure) <-2147273796>
            Concept[174r] Operation on bladder (procedure) <-2147270677>
            Concept[175r] Operation on urethra (procedure) <-2147266709>
            Concept[176r] Endoscopy of urethra (procedure) <-2147137204>
            Concept[177r] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178r] Ureteroscopy (procedure) <-2147119375>
            Concept[179r] Operation on retroperitoneum (procedure) <-2146990006>
            Concept[180r] Urethral removal of ureteric clot (procedure) <-2146907625>
            Concept[181r] Ureteroscopic operation (procedure) <-2146478846>
            Concept[182r] Kidney operation (procedure) <-2146307173>

Comparison expression:

 Root[172c]➞[171c]
    Necessary[171c]➞[170c]
        And[170c]➞[3c, 7c, 11c, 15c, 19c, 23c, 27c, 33c, 37c, 41c, 45c, 51c, 63c, 67c, 73c, 79c, 85c, 91c, 99c, 107c, 117c, 127c, 137c, 157c, 185c, 158c, 159c, 160c, 161c, 162c, 163c, 173c, 164c, 165c, 166c, 167c, 168c, 169c]
            Some[3c] Role group (SOLOR) <-2147483593>➞[2c]
                And[2c]➞[1c]
                    Some[1c] Access (attribute) <-2147315914>➞[0c]
                        Concept[0c] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[7c] Role group (SOLOR) <-2147483593>➞[6c]
                And[6c]➞[5c]
                    Some[5c] Approach (attribute) <-2147314305>➞[4c]
                        Concept[4c] Transurethral approach (qualifier value) <-2146691909>
            Some[11c] Role group (SOLOR) <-2147483593>➞[10c]
                And[10c]➞[9c]
                    Some[9c] Procedure site (attribute) <-2147378082>➞[8c]
                        Concept[8c] Urinary bladder structure (body structure) <-2147419211>
            Some[15c] Role group (SOLOR) <-2147483593>➞[14c]
                And[14c]➞[13c]
                    Some[13c] Instrumentation (attribute) <-2146514480>➞[12c]
                        Concept[12c] Ureteroscope (physical object) <-2146503035>
            Some[19c] Role group (SOLOR) <-2147483593>➞[18c]
                And[18c]➞[17c]
                    Some[17c] Instrumentation (attribute) <-2146514480>➞[16c]
                        Concept[16c] Cystoscope, device (physical object) <-2147196166>
            Some[23c] Role group (SOLOR) <-2147483593>➞[22c]
                And[22c]➞[21c]
                    Some[21c] Procedure site (attribute) <-2147378082>➞[20c]
                        Concept[20c] Renal pelvis structure (body structure) <-2147120521>
            Some[27c] Role group (SOLOR) <-2147483593>➞[26c]
                And[26c]➞[25c]
                    Some[25c] Procedure site (attribute) <-2147378082>➞[24c]
                        Concept[24c] Ureteric structure (body structure) <-2147443660>
            Some[33c] Role group (SOLOR) <-2147483593>➞[32c]
                And[32c]➞[29c, 31c]
                    Some[29c] Procedure site (attribute) <-2147378082>➞[28c]
                        Concept[28c] Kidney structure (body structure) <-2146589772>
                    Some[31c] Method (attribute) <-2147314116>➞[30c]
                        Concept[30c] Examination - action (qualifier value) <-2146625815>
            Some[37c] Role group (SOLOR) <-2147483593>➞[36c]
                And[36c]➞[35c]
                    Some[35c] Access instrument (attribute) <-2147283909>➞[34c]
                        Concept[34c] Ureteroscope (physical object) <-2146503035>
            Some[41c] Role group (SOLOR) <-2147483593>➞[40c]
                And[40c]➞[39c]
                    Some[39c] Access instrument (attribute) <-2147283909>➞[38c]
                        Concept[38c] Cystoscope, device (physical object) <-2147196166>
            Some[45c] Role group (SOLOR) <-2147483593>➞[44c]
                And[44c]➞[43c]
                    Some[43c] Using (attribute) <-2147300466>➞[42c]
                        Concept[42c] Endoscope, device (physical object) <-2146961395>
            Some[51c] Role group (SOLOR) <-2147483593>➞[50c]
                And[50c]➞[47c, 49c]
                    Some[47c] Procedure site (attribute) <-2147378082>➞[46c]
                        Concept[46c] Ureteric structure (body structure) <-2147443660>
                    Some[49c] Method (attribute) <-2147314116>➞[48c]
                        Concept[48c] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63c] Role group (SOLOR) <-2147483593>➞[62c]
                And[62c]➞[53c, 55c, 57c, 59c, 61c]
                    Some[53c] Procedure site (attribute) <-2147378082>➞[52c]
                        Concept[52c] Urinary bladder structure (body structure) <-2147419211>
                    Some[55c] Procedure site (attribute) <-2147378082>➞[54c]
                        Concept[54c] Kidney structure (body structure) <-2146589772>
                    Some[57c] Procedure site (attribute) <-2147378082>➞[56c]
                        Concept[56c] Ureteric structure (body structure) <-2147443660>
                    Some[59c] Method (attribute) <-2147314116>➞[58c]
                        Concept[58c] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61c] Method (attribute) <-2147314116>➞[60c]
                        Concept[60c] Surgical action (qualifier value) <-2146940928>
            Some[67c] Role group (SOLOR) <-2147483593>➞[66c]
                And[66c]➞[65c]
                    Some[65c] Direct device (attribute) <-2147378264>➞[64c]
                        Concept[64c] Endoscope, device (physical object) <-2146961395>
            Some[73c] Role group (SOLOR) <-2147483593>➞[72c]
                And[72c]➞[69c, 71c]
                    Some[69c] Procedure site (attribute) <-2147378082>➞[68c]
                        Concept[68c] Urethral structure (body structure) <-2147284236>
                    Some[71c] Method (attribute) <-2147314116>➞[70c]
                        Concept[70c] Inspection - action (qualifier value) <-2146938586>
            Some[79c] Role group (SOLOR) <-2147483593>➞[78c]
                And[78c]➞[75c, 77c]
                    Some[75c] Procedure site (attribute) <-2147378082>➞[74c]
                        Concept[74c] Renal pelvis structure (body structure) <-2147120521>
                    Some[77c] Method (attribute) <-2147314116>➞[76c]
                        Concept[76c] Inspection - action (qualifier value) <-2146938586>
            Some[85c] Role group (SOLOR) <-2147483593>➞[84c]
                And[84c]➞[81c, 83c]
                    Some[81c] Procedure site (attribute) <-2147378082>➞[80c]
                        Concept[80c] Ureteric structure (body structure) <-2147443660>
                    Some[83c] Method (attribute) <-2147314116>➞[82c]
                        Concept[82c] Inspection - action (qualifier value) <-2146938586>
            Some[91c] Role group (SOLOR) <-2147483593>➞[90c]
                And[90c]➞[87c, 89c]
                    Some[87c] Procedure site (attribute) <-2147378082>➞[86c]
                        Concept[86c] Urinary bladder structure (body structure) <-2147419211>
                    Some[89c] Method (attribute) <-2147314116>➞[88c]
                        Concept[88c] Inspection - action (qualifier value) <-2146938586>
            Some[99c] Role group (SOLOR) <-2147483593>➞[98c]
                And[98c]➞[93c, 95c, 97c]
                    Some[93c] Procedure site (attribute) <-2147378082>➞[92c]
                        Concept[92c] Urinary bladder structure (body structure) <-2147419211>
                    Some[95c] Procedure site (attribute) <-2147378082>➞[94c]
                        Concept[94c] Renal pelvis structure (body structure) <-2147120521>
                    Some[97c] Method (attribute) <-2147314116>➞[96c]
                        Concept[96c] Surgical action (qualifier value) <-2146940928>
            Some[107c] Role group (SOLOR) <-2147483593>➞[106c]
                And[106c]➞[101c, 103c, 105c]
                    Some[101c] Access (attribute) <-2147315914>➞[100c]
                        Concept[100c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[103c] Approach (attribute) <-2147314305>➞[102c]
                        Concept[102c] Transurethral approach (qualifier value) <-2146691909>
                    Some[105c] Access instrument (attribute) <-2147283909>➞[104c]
                        Concept[104c] Cystoscope, device (physical object) <-2147196166>
            Some[117c] Role group (SOLOR) <-2147483593>➞[116c]
                And[116c]➞[109c, 111c, 113c, 115c]
                    Some[109c] Procedure site (attribute) <-2147378082>➞[108c]
                        Concept[108c] Urethral structure (body structure) <-2147284236>
                    Some[111c] Access (attribute) <-2147315914>➞[110c]
                        Concept[110c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[113c] Method (attribute) <-2147314116>➞[112c]
                        Concept[112c] Inspection - action (qualifier value) <-2146938586>
                    Some[115c] Access instrument (attribute) <-2147283909>➞[114c]
                        Concept[114c] Endoscope, device (physical object) <-2146961395>
            Some[127c] Role group (SOLOR) <-2147483593>➞[126c]
                And[126c]➞[119c, 121c, 123c, 125c]
                    Some[119c] Procedure site (attribute) <-2147378082>➞[118c]
                        Concept[118c] Urinary bladder structure (body structure) <-2147419211>
                    Some[121c] Access (attribute) <-2147315914>➞[120c]
                        Concept[120c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[123c] Method (attribute) <-2147314116>➞[122c]
                        Concept[122c] Inspection - action (qualifier value) <-2146938586>
                    Some[125c] Access instrument (attribute) <-2147283909>➞[124c]
                        Concept[124c] Cystoscope, device (physical object) <-2147196166>
            Some[137c] Role group (SOLOR) <-2147483593>➞[136c]
                And[136c]➞[129c, 131c, 133c, 135c]
                    Some[129c] Procedure site (attribute) <-2147378082>➞[128c]
                        Concept[128c] Ureteric structure (body structure) <-2147443660>
                    Some[131c] Access (attribute) <-2147315914>➞[130c]
                        Concept[130c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[133c] Method (attribute) <-2147314116>➞[132c]
                        Concept[132c] Inspection - action (qualifier value) <-2146938586>
                    Some[135c] Access instrument (attribute) <-2147283909>➞[134c]
                        Concept[134c] Ureteroscope (physical object) <-2146503035>
            Some[157c] Role group (SOLOR) <-2147483593>➞[156c]
                And[156c]➞[139c, 141c, 143c, 145c, 147c, 149c, 151c, 153c, 155c]
                    Some[139c] Procedure site (attribute) <-2147378082>➞[138c]
                        Concept[138c] Urethral structure (body structure) <-2147284236>
                    Some[141c] Procedure site (attribute) <-2147378082>➞[140c]
                        Concept[140c] Renal pelvis structure (body structure) <-2147120521>
                    Some[143c] Procedure site (attribute) <-2147378082>➞[142c]
                        Concept[142c] Urinary bladder structure (body structure) <-2147419211>
                    Some[145c] Procedure site (attribute) <-2147378082>➞[144c]
                        Concept[144c] Ureteric structure (body structure) <-2147443660>
                    Some[147c] Access (attribute) <-2147315914>➞[146c]
                        Concept[146c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[149c] Approach (attribute) <-2147314305>➞[148c]
                        Concept[148c] Transurethral approach (qualifier value) <-2146691909>
                    Some[151c] Method (attribute) <-2147314116>➞[150c]
                        Concept[150c] Surgical action (qualifier value) <-2146940928>
                    Some[153c] Method (attribute) <-2147314116>➞[152c]
                        Concept[152c] Inspection - action (qualifier value) <-2146938586>
                    Some[155c] Access instrument (attribute) <-2147283909>➞[154c]
                        Concept[154c] Cystoscope, device (physical object) <-2147196166>
            Some[185c] Role group (SOLOR) <-2147483593>➞[184c]
                And[184c]➞[175c, 177c, 179c, 181c, 183c]
                    Some[175c] Access (attribute) <-2147315914>➞[174c]
                        Concept[174c] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[177c] Approach (attribute) <-2147314305>➞[176c]
                        Concept[176c] Transurethral approach (qualifier value) <-2146691909>
                    Some[179c] Method (attribute) <-2147314116>➞[178c]
                        Concept[178c] Inspection - action (qualifier value) <-2146938586>
                    Some[181c] Access instrument (attribute) <-2147283909>➞[180c]
                        Concept[180c] Cystoscope, device (physical object) <-2147196166>
                    Some[183c] Procedure site - Direct (attribute) <-2146878287>➞[182c]
                        Concept[182c] Urinary bladder structure (body structure) <-2147419211>
            Concept[158c] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[159c] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[160c] Endoscopy of renal pelvis (procedure) <-2147408011>
            Concept[161c] Endoscopy of pelvic cavity (procedure) <-2147273796>
            Concept[162c] Operation on bladder (procedure) <-2147270677>
            Concept[163c] Operation on urethra (procedure) <-2147266709>
            Concept[173c] Endoscopy of urethra (procedure) <-2147137204>
            Concept[164c] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[165c] Ureteroscopy (procedure) <-2147119375>
            Concept[166c] Operation on retroperitoneum (procedure) <-2146990006>
            Concept[167c] Urethral removal of ureteric clot (procedure) <-2146907625>
            Concept[168c] Ureteroscopic operation (procedure) <-2146478846>
            Concept[169c] Kidney operation (procedure) <-2146307173>

Isomorphic expression:

 Root[185i]➞[184i]
    Necessary[184i]➞[183i]
        And[183i]➞[3i, 7i, 11i, 15i, 19i, 23i, 27i, 33i, 37i, 41i, 45i, 51i, 63i, 67i, 73i, 79i, 85i, 91i, 99i, 107i, 117i, 127i, 137i, 157i, 169i, 170i, 171i, 172i, 173i, 174i, 175i, 176i, 177i, 178i, 179i, 180i, 181i, 182i]
            Some[3i] Role group (SOLOR) <-2147483593>➞[2i]
                And[2i]➞[1i]
                    Some[1i] Access (attribute) <-2147315914>➞[0i]
                        Concept[0i] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[7i] Role group (SOLOR) <-2147483593>➞[6i]
                And[6i]➞[5i]
                    Some[5i] Approach (attribute) <-2147314305>➞[4i]
                        Concept[4i] Transurethral approach (qualifier value) <-2146691909>
            Some[11i] Role group (SOLOR) <-2147483593>➞[10i]
                And[10i]➞[9i]
                    Some[9i] Procedure site (attribute) <-2147378082>➞[8i]
                        Concept[8i] Urinary bladder structure (body structure) <-2147419211>
            Some[15i] Role group (SOLOR) <-2147483593>➞[14i]
                And[14i]➞[13i]
                    Some[13i] Instrumentation (attribute) <-2146514480>➞[12i]
                        Concept[12i] Ureteroscope (physical object) <-2146503035>
            Some[19i] Role group (SOLOR) <-2147483593>➞[18i]
                And[18i]➞[17i]
                    Some[17i] Instrumentation (attribute) <-2146514480>➞[16i]
                        Concept[16i] Cystoscope, device (physical object) <-2147196166>
            Some[23i] Role group (SOLOR) <-2147483593>➞[22i]
                And[22i]➞[21i]
                    Some[21i] Procedure site (attribute) <-2147378082>➞[20i]
                        Concept[20i] Renal pelvis structure (body structure) <-2147120521>
            Some[27i] Role group (SOLOR) <-2147483593>➞[26i]
                And[26i]➞[25i]
                    Some[25i] Procedure site (attribute) <-2147378082>➞[24i]
                        Concept[24i] Ureteric structure (body structure) <-2147443660>
            Some[33i] Role group (SOLOR) <-2147483593>➞[32i]
                And[32i]➞[29i, 31i]
                    Some[29i] Procedure site (attribute) <-2147378082>➞[28i]
                        Concept[28i] Kidney structure (body structure) <-2146589772>
                    Some[31i] Method (attribute) <-2147314116>➞[30i]
                        Concept[30i] Examination - action (qualifier value) <-2146625815>
            Some[37i] Role group (SOLOR) <-2147483593>➞[36i]
                And[36i]➞[35i]
                    Some[35i] Access instrument (attribute) <-2147283909>➞[34i]
                        Concept[34i] Ureteroscope (physical object) <-2146503035>
            Some[41i] Role group (SOLOR) <-2147483593>➞[40i]
                And[40i]➞[39i]
                    Some[39i] Access instrument (attribute) <-2147283909>➞[38i]
                        Concept[38i] Cystoscope, device (physical object) <-2147196166>
            Some[45i] Role group (SOLOR) <-2147483593>➞[44i]
                And[44i]➞[43i]
                    Some[43i] Using (attribute) <-2147300466>➞[42i]
                        Concept[42i] Endoscope, device (physical object) <-2146961395>
            Some[51i] Role group (SOLOR) <-2147483593>➞[50i]
                And[50i]➞[47i, 49i]
                    Some[47i] Procedure site (attribute) <-2147378082>➞[46i]
                        Concept[46i] Ureteric structure (body structure) <-2147443660>
                    Some[49i] Method (attribute) <-2147314116>➞[48i]
                        Concept[48i] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63i] Role group (SOLOR) <-2147483593>➞[62i]
                And[62i]➞[53i, 55i, 57i, 59i, 61i]
                    Some[53i] Procedure site (attribute) <-2147378082>➞[52i]
                        Concept[52i] Urinary bladder structure (body structure) <-2147419211>
                    Some[55i] Procedure site (attribute) <-2147378082>➞[54i]
                        Concept[54i] Kidney structure (body structure) <-2146589772>
                    Some[57i] Procedure site (attribute) <-2147378082>➞[56i]
                        Concept[56i] Ureteric structure (body structure) <-2147443660>
                    Some[59i] Method (attribute) <-2147314116>➞[58i]
                        Concept[58i] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61i] Method (attribute) <-2147314116>➞[60i]
                        Concept[60i] Surgical action (qualifier value) <-2146940928>
            Some[67i] Role group (SOLOR) <-2147483593>➞[66i]
                And[66i]➞[65i]
                    Some[65i] Direct device (attribute) <-2147378264>➞[64i]
                        Concept[64i] Endoscope, device (physical object) <-2146961395>
            Some[73i] Role group (SOLOR) <-2147483593>➞[72i]
                And[72i]➞[69i, 71i]
                    Some[69i] Procedure site (attribute) <-2147378082>➞[68i]
                        Concept[68i] Urethral structure (body structure) <-2147284236>
                    Some[71i] Method (attribute) <-2147314116>➞[70i]
                        Concept[70i] Inspection - action (qualifier value) <-2146938586>
            Some[79i] Role group (SOLOR) <-2147483593>➞[78i]
                And[78i]➞[75i, 77i]
                    Some[75i] Procedure site (attribute) <-2147378082>➞[74i]
                        Concept[74i] Renal pelvis structure (body structure) <-2147120521>
                    Some[77i] Method (attribute) <-2147314116>➞[76i]
                        Concept[76i] Inspection - action (qualifier value) <-2146938586>
            Some[85i] Role group (SOLOR) <-2147483593>➞[84i]
                And[84i]➞[81i, 83i]
                    Some[81i] Procedure site (attribute) <-2147378082>➞[80i]
                        Concept[80i] Ureteric structure (body structure) <-2147443660>
                    Some[83i] Method (attribute) <-2147314116>➞[82i]
                        Concept[82i] Inspection - action (qualifier value) <-2146938586>
            Some[91i] Role group (SOLOR) <-2147483593>➞[90i]
                And[90i]➞[87i, 89i]
                    Some[87i] Procedure site (attribute) <-2147378082>➞[86i]
                        Concept[86i] Urinary bladder structure (body structure) <-2147419211>
                    Some[89i] Method (attribute) <-2147314116>➞[88i]
                        Concept[88i] Inspection - action (qualifier value) <-2146938586>
            Some[99i] Role group (SOLOR) <-2147483593>➞[98i]
                And[98i]➞[93i, 95i, 97i]
                    Some[93i] Procedure site (attribute) <-2147378082>➞[92i]
                        Concept[92i] Urinary bladder structure (body structure) <-2147419211>
                    Some[95i] Procedure site (attribute) <-2147378082>➞[94i]
                        Concept[94i] Renal pelvis structure (body structure) <-2147120521>
                    Some[97i] Method (attribute) <-2147314116>➞[96i]
                        Concept[96i] Surgical action (qualifier value) <-2146940928>
            Some[107i] Role group (SOLOR) <-2147483593>➞[106i]
                And[106i]➞[101i, 103i, 105i]
                    Some[101i] Access (attribute) <-2147315914>➞[100i]
                        Concept[100i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[103i] Approach (attribute) <-2147314305>➞[102i]
                        Concept[102i] Transurethral approach (qualifier value) <-2146691909>
                    Some[105i] Access instrument (attribute) <-2147283909>➞[104i]
                        Concept[104i] Cystoscope, device (physical object) <-2147196166>
            Some[117i] Role group (SOLOR) <-2147483593>➞[116i]
                And[116i]➞[109i, 111i, 113i, 115i]
                    Some[109i] Procedure site (attribute) <-2147378082>➞[108i]
                        Concept[108i] Urethral structure (body structure) <-2147284236>
                    Some[111i] Access (attribute) <-2147315914>➞[110i]
                        Concept[110i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[113i] Method (attribute) <-2147314116>➞[112i]
                        Concept[112i] Inspection - action (qualifier value) <-2146938586>
                    Some[115i] Access instrument (attribute) <-2147283909>➞[114i]
                        Concept[114i] Endoscope, device (physical object) <-2146961395>
            Some[127i] Role group (SOLOR) <-2147483593>➞[126i]
                And[126i]➞[119i, 121i, 123i, 125i]
                    Some[119i] Procedure site (attribute) <-2147378082>➞[118i]
                        Concept[118i] Urinary bladder structure (body structure) <-2147419211>
                    Some[121i] Access (attribute) <-2147315914>➞[120i]
                        Concept[120i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[123i] Method (attribute) <-2147314116>➞[122i]
                        Concept[122i] Inspection - action (qualifier value) <-2146938586>
                    Some[125i] Access instrument (attribute) <-2147283909>➞[124i]
                        Concept[124i] Cystoscope, device (physical object) <-2147196166>
            Some[137i] Role group (SOLOR) <-2147483593>➞[136i]
                And[136i]➞[129i, 131i, 133i, 135i]
                    Some[129i] Procedure site (attribute) <-2147378082>➞[128i]
                        Concept[128i] Ureteric structure (body structure) <-2147443660>
                    Some[131i] Access (attribute) <-2147315914>➞[130i]
                        Concept[130i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[133i] Method (attribute) <-2147314116>➞[132i]
                        Concept[132i] Inspection - action (qualifier value) <-2146938586>
                    Some[135i] Access instrument (attribute) <-2147283909>➞[134i]
                        Concept[134i] Ureteroscope (physical object) <-2146503035>
            Some[157i] Role group (SOLOR) <-2147483593>➞[156i]
                And[156i]➞[139i, 141i, 143i, 145i, 147i, 149i, 151i, 153i, 155i]
                    Some[139i] Procedure site (attribute) <-2147378082>➞[138i]
                        Concept[138i] Urethral structure (body structure) <-2147284236>
                    Some[141i] Procedure site (attribute) <-2147378082>➞[140i]
                        Concept[140i] Renal pelvis structure (body structure) <-2147120521>
                    Some[143i] Procedure site (attribute) <-2147378082>➞[142i]
                        Concept[142i] Urinary bladder structure (body structure) <-2147419211>
                    Some[145i] Procedure site (attribute) <-2147378082>➞[144i]
                        Concept[144i] Ureteric structure (body structure) <-2147443660>
                    Some[147i] Access (attribute) <-2147315914>➞[146i]
                        Concept[146i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[149i] Approach (attribute) <-2147314305>➞[148i]
                        Concept[148i] Transurethral approach (qualifier value) <-2146691909>
                    Some[151i] Method (attribute) <-2147314116>➞[150i]
                        Concept[150i] Surgical action (qualifier value) <-2146940928>
                    Some[153i] Method (attribute) <-2147314116>➞[152i]
                        Concept[152i] Inspection - action (qualifier value) <-2146938586>
                    Some[155i] Access instrument (attribute) <-2147283909>➞[154i]
                        Concept[154i] Cystoscope, device (physical object) <-2147196166>
            Some[169i] Role group (SOLOR) <-2147483593>➞[168i]
                And[168i]➞[159i, 161i, 163i, 165i, 167i]
                    Some[159i] Access (attribute) <-2147315914>➞[158i]
                        Concept[158i] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[161i] Approach (attribute) <-2147314305>➞[160i]
                        Concept[160i] Transurethral approach (qualifier value) <-2146691909>
                    Some[163i] Method (attribute) <-2147314116>➞[162i]
                        Concept[162i] Inspection - action (qualifier value) <-2146938586>
                    Some[165i] Access instrument (attribute) <-2147283909>➞[164i]
                        Concept[164i] Cystoscope, device (physical object) <-2147196166>
                    Some[167i] Procedure site - Direct (attribute) <-2146878287>➞[166i]
                        Concept[166i] Urinary bladder structure (body structure) <-2147419211>
            Concept[170i] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[171i] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[172i] Endoscopy of renal pelvis (procedure) <-2147408011>
            Concept[173i] Endoscopy of pelvic cavity (procedure) <-2147273796>
            Concept[174i] Operation on bladder (procedure) <-2147270677>
            Concept[175i] Operation on urethra (procedure) <-2147266709>
            Concept[176i] Endoscopy of urethra (procedure) <-2147137204>
            Concept[177i] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178i] Ureteroscopy (procedure) <-2147119375>
            Concept[179i] Operation on retroperitoneum (procedure) <-2146990006>
            Concept[180i] Urethral removal of ureteric clot (procedure) <-2146907625>
            Concept[181i] Ureteroscopic operation (procedure) <-2146478846>
            Concept[182i] Kidney operation (procedure) <-2146307173>

Reference Expression To MergedNodeId Map:

 [0r:0m, 1r:1m, 2r:2m, 3r:3m, 4r:4m, 5r:5m, 6r:6m, 7r:7m, 8r:8m, 9r:9m, 10r:10m, 11r:11m, 12r:12m, 13r:13m, 14r:14m, 15r:15m, 16r:16m, 17r:17m, 18r:18m, 19r:19m, 20r:20m, 21r:21m, 22r:22m, 23r:23m, 24r:24m, 25r:25m, 26r:26m, 27r:27m, 28r:28m, 29r:29m, 30r:30m, 31r:31m, 32r:32m, 33r:33m, 34r:34m, 35r:35m, 36r:36m, 37r:37m, 38r:38m, 39r:39m, 40r:40m, 41r:41m, 42r:42m, 43r:43m, 44r:44m, 45r:45m, 46r:46m, 47r:47m, 48r:48m, 49r:49m, 50r:50m, 51r:51m, 52r:52m, 53r:53m, 54r:54m, 55r:55m, 56r:56m, 57r:57m, 58r:58m, 59r:59m, 60r:60m, 61r:61m, 62r:62m, 63r:63m, 64r:64m, 65r:65m, 66r:66m, 67r:67m, 68r:68m, 69r:69m, 70r:70m, 71r:71m, 72r:72m, 73r:73m, 74r:74m, 75r:75m, 76r:76m, 77r:77m, 78r:78m, 79r:79m, 80r:80m, 81r:81m, 82r:82m, 83r:83m, 84r:84m, 85r:85m, 86r:86m, 87r:87m, 88r:88m, 89r:89m, 90r:90m, 91r:91m, 92r:92m, 93r:93m, 94r:94m, 95r:95m, 96r:96m, 97r:97m, 98r:98m, 99r:99m, 100r:100m, 101r:101m, 102r:102m, 103r:103m, 104r:104m, 105r:105m, 106r:106m, 107r:107m, 108r:108m, 109r:109m, 110r:110m, 111r:111m, 112r:112m, 113r:113m, 114r:114m, 115r:115m, 116r:116m, 117r:117m, 118r:118m, 119r:119m, 120r:120m, 121r:121m, 122r:122m, 123r:123m, 124r:124m, 125r:125m, 126r:126m, 127r:127m, 128r:128m, 129r:129m, 130r:130m, 131r:131m, 132r:132m, 133r:133m, 134r:134m, 135r:135m, 136r:136m, 137r:137m, 138r:138m, 139r:139m, 140r:140m, 141r:141m, 142r:142m, 143r:143m, 144r:144m, 145r:145m, 146r:146m, 147r:147m, 148r:148m, 149r:149m, 150r:150m, 151r:151m, 152r:152m, 153r:153m, 154r:154m, 155r:155m, 156r:156m, 157r:157m, 158r:158m, 159r:159m, 160r:160m, 161r:161m, 162r:162m, 163r:163m, 164r:164m, 165r:165m, 166r:166m, 167r:167m, 168r:168m, 169r:169m, 170r:170m, 171r:171m, 172r:172m, 173r:173m, 174r:174m, 175r:175m, 176r:176m, 177r:177m, 178r:178m, 179r:179m, 180r:180m, 181r:181m, 182r:182m, 183r:183m, 184r:184m, 185r:185m]

Reference Expression To ComparisonNodeId Map:

 [0r:0c, 1r:1c, 2r:2c, 3r:3c, 4r:4c, 5r:5c, 6r:6c, 7r:7c, 8r:8c, 9r:9c, 10r:10c, 11r:11c, 12r:12c, 13r:13c, 14r:14c, 15r:15c, 16r:16c, 17r:17c, 18r:18c, 19r:19c, 20r:20c, 21r:21c, 22r:22c, 23r:23c, 24r:24c, 25r:25c, 26r:26c, 27r:27c, 28r:28c, 29r:29c, 30r:30c, 31r:31c, 32r:32c, 33r:33c, 34r:34c, 35r:35c, 36r:36c, 37r:37c, 38r:38c, 39r:39c, 40r:40c, 41r:41c, 42r:42c, 43r:43c, 44r:44c, 45r:45c, 46r:46c, 47r:47c, 48r:48c, 49r:49c, 50r:50c, 51r:51c, 52r:52c, 53r:53c, 54r:54c, 55r:55c, 56r:56c, 57r:57c, 58r:58c, 59r:59c, 60r:60c, 61r:61c, 62r:62c, 63r:63c, 64r:64c, 65r:65c, 66r:66c, 67r:67c, 68r:68c, 69r:69c, 70r:70c, 71r:71c, 72r:72c, 73r:73c, 74r:74c, 75r:75c, 76r:76c, 77r:77c, 78r:78c, 79r:79c, 80r:80c, 81r:81c, 82r:82c, 83r:83c, 84r:84c, 85r:85c, 86r:86c, 87r:87c, 88r:88c, 89r:89c, 90r:90c, 91r:91c, 92r:92c, 93r:93c, 94r:94c, 95r:95c, 96r:96c, 97r:97c, 98r:98c, 99r:99c, 100r:100c, 101r:101c, 102r:102c, 103r:103c, 104r:104c, 105r:105c, 106r:106c, 107r:107c, 108r:108c, 109r:109c, 110r:110c, 111r:111c, 112r:112c, 113r:113c, 114r:114c, 115r:115c, 116r:116c, 117r:117c, 118r:118c, 119r:119c, 120r:120c, 121r:121c, 122r:122c, 123r:123c, 124r:124c, 125r:125c, 126r:126c, 127r:127c, 128r:128c, 129r:129c, 130r:130c, 131r:131c, 132r:132c, 133r:133c, 134r:134c, 135r:135c, 136r:136c, 137r:137c, 138r:138c, 139r:139c, 140r:140c, 141r:141c, 142r:142c, 143r:143c, 144r:144c, 145r:145c, 146r:146c, 147r:147c, 148r:148c, 149r:149c, 150r:150c, 151r:151c, 152r:152c, 153r:153c, 154r:154c, 155r:155c, 156r:156c, 157r:157c, 158r:174c, 159r:175c, 160r:176c, 161r:177c, 162r:178c, 163r:179c, 164r:180c, 165r:181c, 166r:182c, 167r:183c, 168r:184c, 169r:185c, 170r:158c, 171r:159c, 172r:160c, 173r:161c, 174r:162c, 175r:163c, 176r:173c, 177r:164c, 178r:165c, 179r:166c, 180r:167c, 181r:168c, 182r:169c, 183r:170c, 184r:171c, 185r:172c]

Comparison Expression To ReferenceNodeId Map:

 [0c:0r, 1c:1r, 2c:2r, 3c:3r, 4c:4r, 5c:5r, 6c:6r, 7c:7r, 8c:8r, 9c:9r, 10c:10r, 11c:11r, 12c:12r, 13c:13r, 14c:14r, 15c:15r, 16c:16r, 17c:17r, 18c:18r, 19c:19r, 20c:20r, 21c:21r, 22c:22r, 23c:23r, 24c:24r, 25c:25r, 26c:26r, 27c:27r, 28c:28r, 29c:29r, 30c:30r, 31c:31r, 32c:32r, 33c:33r, 34c:34r, 35c:35r, 36c:36r, 37c:37r, 38c:38r, 39c:39r, 40c:40r, 41c:41r, 42c:42r, 43c:43r, 44c:44r, 45c:45r, 46c:46r, 47c:47r, 48c:48r, 49c:49r, 50c:50r, 51c:51r, 52c:52r, 53c:53r, 54c:54r, 55c:55r, 56c:56r, 57c:57r, 58c:58r, 59c:59r, 60c:60r, 61c:61r, 62c:62r, 63c:63r, 64c:64r, 65c:65r, 66c:66r, 67c:67r, 68c:68r, 69c:69r, 70c:70r, 71c:71r, 72c:72r, 73c:73r, 74c:74r, 75c:75r, 76c:76r, 77c:77r, 78c:78r, 79c:79r, 80c:80r, 81c:81r, 82c:82r, 83c:83r, 84c:84r, 85c:85r, 86c:86r, 87c:87r, 88c:88r, 89c:89r, 90c:90r, 91c:91r, 92c:92r, 93c:93r, 94c:94r, 95c:95r, 96c:96r, 97c:97r, 98c:98r, 99c:99r, 100c:100r, 101c:101r, 102c:102r, 103c:103r, 104c:104r, 105c:105r, 106c:106r, 107c:107r, 108c:108r, 109c:109r, 110c:110r, 111c:111r, 112c:112r, 113c:113r, 114c:114r, 115c:115r, 116c:116r, 117c:117r, 118c:118r, 119c:119r, 120c:120r, 121c:121r, 122c:122r, 123c:123r, 124c:124r, 125c:125r, 126c:126r, 127c:127r, 128c:128r, 129c:129r, 130c:130r, 131c:131r, 132c:132r, 133c:133r, 134c:134r, 135c:135r, 136c:136r, 137c:137r, 138c:138r, 139c:139r, 140c:140r, 141c:141r, 142c:142r, 143c:143r, 144c:144r, 145c:145r, 146c:146r, 147c:147r, 148c:148r, 149c:149r, 150c:150r, 151c:151r, 152c:152r, 153c:153r, 154c:154r, 155c:155r, 156c:156r, 157c:157r, 158c:170r, 159c:171r, 160c:172r, 161c:173r, 162c:174r, 163c:175r, 164c:177r, 165c:178r, 166c:179r, 167c:180r, 168c:181r, 169c:182r, 170c:183r, 171c:184r, 172c:185r, 173c:176r, 174c:158r, 175c:159r, 176c:160r, 177c:161r, 178c:162r, 179c:163r, 180c:164r, 181c:165r, 182c:166r, 183c:167r, 184c:168r, 185c:169r]

Isomorphic solution: 
  [  0r] ➞ [  0c]  Concept[0r] Endoscopic approach - access (qualifier value) <-2146941410>
  [  1r] ➞ [  1c]  Some[1r] Access (attribute) <-2147315914>➞[0r]
  [  2r] ➞ [  2c]  And[2r]➞[1r]
  [  3r] ➞ [  3c]  Some[3r] Role group (SOLOR) <-2147483593>➞[2r]
  [  4r] ➞ [  4c]  Concept[4r] Transurethral approach (qualifier value) <-2146691909>
  [  5r] ➞ [  5c]  Some[5r] Approach (attribute) <-2147314305>➞[4r]
  [  6r] ➞ [  6c]  And[6r]➞[5r]
  [  7r] ➞ [  7c]  Some[7r] Role group (SOLOR) <-2147483593>➞[6r]
  [  8r] ➞ [  8c]  Concept[8r] Urinary bladder structure (body structure) <-2147419211>
  [  9r] ➞ [  9c]  Some[9r] Procedure site (attribute) <-2147378082>➞[8r]
  [ 10r] ➞ [ 10c]  And[10r]➞[9r]
  [ 11r] ➞ [ 11c]  Some[11r] Role group (SOLOR) <-2147483593>➞[10r]
  [ 12r] ➞ [ 12c]  Concept[12r] Ureteroscope (physical object) <-2146503035>
  [ 13r] ➞ [ 13c]  Some[13r] Instrumentation (attribute) <-2146514480>➞[12r]
  [ 14r] ➞ [ 14c]  And[14r]➞[13r]
  [ 15r] ➞ [ 15c]  Some[15r] Role group (SOLOR) <-2147483593>➞[14r]
  [ 16r] ➞ [ 16c]  Concept[16r] Cystoscope, device (physical object) <-2147196166>
  [ 17r] ➞ [ 17c]  Some[17r] Instrumentation (attribute) <-2146514480>➞[16r]
  [ 18r] ➞ [ 18c]  And[18r]➞[17r]
  [ 19r] ➞ [ 19c]  Some[19r] Role group (SOLOR) <-2147483593>➞[18r]
  [ 20r] ➞ [ 20c]  Concept[20r] Renal pelvis structure (body structure) <-2147120521>
  [ 21r] ➞ [ 21c]  Some[21r] Procedure site (attribute) <-2147378082>➞[20r]
  [ 22r] ➞ [ 22c]  And[22r]➞[21r]
  [ 23r] ➞ [ 23c]  Some[23r] Role group (SOLOR) <-2147483593>➞[22r]
  [ 24r] ➞ [ 24c]  Concept[24r] Ureteric structure (body structure) <-2147443660>
  [ 25r] ➞ [ 25c]  Some[25r] Procedure site (attribute) <-2147378082>➞[24r]
  [ 26r] ➞ [ 26c]  And[26r]➞[25r]
  [ 27r] ➞ [ 27c]  Some[27r] Role group (SOLOR) <-2147483593>➞[26r]
  [ 28r] ➞ [ 28c]  Concept[28r] Kidney structure (body structure) <-2146589772>
  [ 29r] ➞ [ 29c]  Some[29r] Procedure site (attribute) <-2147378082>➞[28r]
  [ 30r] ➞ [ 30c]  Concept[30r] Examination - action (qualifier value) <-2146625815>
  [ 31r] ➞ [ 31c]  Some[31r] Method (attribute) <-2147314116>➞[30r]
  [ 32r] ➞ [ 32c]  And[32r]➞[29r, 31r]
  [ 33r] ➞ [ 33c]  Some[33r] Role group (SOLOR) <-2147483593>➞[32r]
  [ 34r] ➞ [ 34c]  Concept[34r] Ureteroscope (physical object) <-2146503035>
  [ 35r] ➞ [ 35c]  Some[35r] Access instrument (attribute) <-2147283909>➞[34r]
  [ 36r] ➞ [ 36c]  And[36r]➞[35r]
  [ 37r] ➞ [ 37c]  Some[37r] Role group (SOLOR) <-2147483593>➞[36r]
  [ 38r] ➞ [ 38c]  Concept[38r] Cystoscope, device (physical object) <-2147196166>
  [ 39r] ➞ [ 39c]  Some[39r] Access instrument (attribute) <-2147283909>➞[38r]
  [ 40r] ➞ [ 40c]  And[40r]➞[39r]
  [ 41r] ➞ [ 41c]  Some[41r] Role group (SOLOR) <-2147483593>➞[40r]
  [ 42r] ➞ [ 42c]  Concept[42r] Endoscope, device (physical object) <-2146961395>
  [ 43r] ➞ [ 43c]  Some[43r] Using (attribute) <-2147300466>➞[42r]
  [ 44r] ➞ [ 44c]  And[44r]➞[43r]
  [ 45r] ➞ [ 45c]  Some[45r] Role group (SOLOR) <-2147483593>➞[44r]
  [ 46r] ➞ [ 46c]  Concept[46r] Ureteric structure (body structure) <-2147443660>
  [ 47r] ➞ [ 47c]  Some[47r] Procedure site (attribute) <-2147378082>➞[46r]
  [ 48r] ➞ [ 48c]  Concept[48r] Endoscopic inspection - action (qualifier value) <-2146940226>
  [ 49r] ➞ [ 49c]  Some[49r] Method (attribute) <-2147314116>➞[48r]
  [ 50r] ➞ [ 50c]  And[50r]➞[47r, 49r]
  [ 51r] ➞ [ 51c]  Some[51r] Role group (SOLOR) <-2147483593>➞[50r]
  [ 52r] ➞ [ 52c]  Concept[52r] Urinary bladder structure (body structure) <-2147419211>
  [ 53r] ➞ [ 53c]  Some[53r] Procedure site (attribute) <-2147378082>➞[52r]
  [ 54r] ➞ [ 54c]  Concept[54r] Kidney structure (body structure) <-2146589772>
  [ 55r] ➞ [ 55c]  Some[55r] Procedure site (attribute) <-2147378082>➞[54r]
  [ 56r] ➞ [ 56c]  Concept[56r] Ureteric structure (body structure) <-2147443660>
  [ 57r] ➞ [ 57c]  Some[57r] Procedure site (attribute) <-2147378082>➞[56r]
  [ 58r] ➞ [ 58c]  Concept[58r] Endoscopic inspection - action (qualifier value) <-2146940226>
  [ 59r] ➞ [ 59c]  Some[59r] Method (attribute) <-2147314116>➞[58r]
  [ 60r] ➞ [ 60c]  Concept[60r] Surgical action (qualifier value) <-2146940928>
  [ 61r] ➞ [ 61c]  Some[61r] Method (attribute) <-2147314116>➞[60r]
  [ 62r] ➞ [ 62c]  And[62r]➞[53r, 55r, 57r, 59r, 61r]
  [ 63r] ➞ [ 63c]  Some[63r] Role group (SOLOR) <-2147483593>➞[62r]
  [ 64r] ➞ [ 64c]  Concept[64r] Endoscope, device (physical object) <-2146961395>
  [ 65r] ➞ [ 65c]  Some[65r] Direct device (attribute) <-2147378264>➞[64r]
  [ 66r] ➞ [ 66c]  And[66r]➞[65r]
  [ 67r] ➞ [ 67c]  Some[67r] Role group (SOLOR) <-2147483593>➞[66r]
  [ 68r] ➞ [ 68c]  Concept[68r] Urethral structure (body structure) <-2147284236>
  [ 69r] ➞ [ 69c]  Some[69r] Procedure site (attribute) <-2147378082>➞[68r]
  [ 70r] ➞ [ 70c]  Concept[70r] Inspection - action (qualifier value) <-2146938586>
  [ 71r] ➞ [ 71c]  Some[71r] Method (attribute) <-2147314116>➞[70r]
  [ 72r] ➞ [ 72c]  And[72r]➞[69r, 71r]
  [ 73r] ➞ [ 73c]  Some[73r] Role group (SOLOR) <-2147483593>➞[72r]
  [ 74r] ➞ [ 74c]  Concept[74r] Renal pelvis structure (body structure) <-2147120521>
  [ 75r] ➞ [ 75c]  Some[75r] Procedure site (attribute) <-2147378082>➞[74r]
  [ 76r] ➞ [ 76c]  Concept[76r] Inspection - action (qualifier value) <-2146938586>
  [ 77r] ➞ [ 77c]  Some[77r] Method (attribute) <-2147314116>➞[76r]
  [ 78r] ➞ [ 78c]  And[78r]➞[75r, 77r]
  [ 79r] ➞ [ 79c]  Some[79r] Role group (SOLOR) <-2147483593>➞[78r]
  [ 80r] ➞ [ 80c]  Concept[80r] Ureteric structure (body structure) <-2147443660>
  [ 81r] ➞ [ 81c]  Some[81r] Procedure site (attribute) <-2147378082>➞[80r]
  [ 82r] ➞ [ 82c]  Concept[82r] Inspection - action (qualifier value) <-2146938586>
  [ 83r] ➞ [ 83c]  Some[83r] Method (attribute) <-2147314116>➞[82r]
  [ 84r] ➞ [ 84c]  And[84r]➞[81r, 83r]
  [ 85r] ➞ [ 85c]  Some[85r] Role group (SOLOR) <-2147483593>➞[84r]
  [ 86r] ➞ [ 86c]  Concept[86r] Urinary bladder structure (body structure) <-2147419211>
  [ 87r] ➞ [ 87c]  Some[87r] Procedure site (attribute) <-2147378082>➞[86r]
  [ 88r] ➞ [ 88c]  Concept[88r] Inspection - action (qualifier value) <-2146938586>
  [ 89r] ➞ [ 89c]  Some[89r] Method (attribute) <-2147314116>➞[88r]
  [ 90r] ➞ [ 90c]  And[90r]➞[87r, 89r]
  [ 91r] ➞ [ 91c]  Some[91r] Role group (SOLOR) <-2147483593>➞[90r]
  [ 92r] ➞ [ 92c]  Concept[92r] Urinary bladder structure (body structure) <-2147419211>
  [ 93r] ➞ [ 93c]  Some[93r] Procedure site (attribute) <-2147378082>➞[92r]
  [ 94r] ➞ [ 94c]  Concept[94r] Renal pelvis structure (body structure) <-2147120521>
  [ 95r] ➞ [ 95c]  Some[95r] Procedure site (attribute) <-2147378082>➞[94r]
  [ 96r] ➞ [ 96c]  Concept[96r] Surgical action (qualifier value) <-2146940928>
  [ 97r] ➞ [ 97c]  Some[97r] Method (attribute) <-2147314116>➞[96r]
  [ 98r] ➞ [ 98c]  And[98r]➞[93r, 95r, 97r]
  [ 99r] ➞ [ 99c]  Some[99r] Role group (SOLOR) <-2147483593>➞[98r]
  [100r] ➞ [100c]  Concept[100r] Endoscopic approach - access (qualifier value) <-2146941410>
  [101r] ➞ [101c]  Some[101r] Access (attribute) <-2147315914>➞[100r]
  [102r] ➞ [102c]  Concept[102r] Transurethral approach (qualifier value) <-2146691909>
  [103r] ➞ [103c]  Some[103r] Approach (attribute) <-2147314305>➞[102r]
  [104r] ➞ [104c]  Concept[104r] Cystoscope, device (physical object) <-2147196166>
  [105r] ➞ [105c]  Some[105r] Access instrument (attribute) <-2147283909>➞[104r]
  [106r] ➞ [106c]  And[106r]➞[101r, 103r, 105r]
  [107r] ➞ [107c]  Some[107r] Role group (SOLOR) <-2147483593>➞[106r]
  [108r] ➞ [108c]  Concept[108r] Urethral structure (body structure) <-2147284236>
  [109r] ➞ [109c]  Some[109r] Procedure site (attribute) <-2147378082>➞[108r]
  [110r] ➞ [110c]  Concept[110r] Endoscopic approach - access (qualifier value) <-2146941410>
  [111r] ➞ [111c]  Some[111r] Access (attribute) <-2147315914>➞[110r]
  [112r] ➞ [112c]  Concept[112r] Inspection - action (qualifier value) <-2146938586>
  [113r] ➞ [113c]  Some[113r] Method (attribute) <-2147314116>➞[112r]
  [114r] ➞ [114c]  Concept[114r] Endoscope, device (physical object) <-2146961395>
  [115r] ➞ [115c]  Some[115r] Access instrument (attribute) <-2147283909>➞[114r]
  [116r] ➞ [116c]  And[116r]➞[109r, 111r, 113r, 115r]
  [117r] ➞ [117c]  Some[117r] Role group (SOLOR) <-2147483593>➞[116r]
  [118r] ➞ [118c]  Concept[118r] Urinary bladder structure (body structure) <-2147419211>
  [119r] ➞ [119c]  Some[119r] Procedure site (attribute) <-2147378082>➞[118r]
  [120r] ➞ [120c]  Concept[120r] Endoscopic approach - access (qualifier value) <-2146941410>
  [121r] ➞ [121c]  Some[121r] Access (attribute) <-2147315914>➞[120r]
  [122r] ➞ [122c]  Concept[122r] Inspection - action (qualifier value) <-2146938586>
  [123r] ➞ [123c]  Some[123r] Method (attribute) <-2147314116>➞[122r]
  [124r] ➞ [124c]  Concept[124r] Cystoscope, device (physical object) <-2147196166>
  [125r] ➞ [125c]  Some[125r] Access instrument (attribute) <-2147283909>➞[124r]
  [126r] ➞ [126c]  And[126r]➞[119r, 121r, 123r, 125r]
  [127r] ➞ [127c]  Some[127r] Role group (SOLOR) <-2147483593>➞[126r]
  [128r] ➞ [128c]  Concept[128r] Ureteric structure (body structure) <-2147443660>
  [129r] ➞ [129c]  Some[129r] Procedure site (attribute) <-2147378082>➞[128r]
  [130r] ➞ [130c]  Concept[130r] Endoscopic approach - access (qualifier value) <-2146941410>
  [131r] ➞ [131c]  Some[131r] Access (attribute) <-2147315914>➞[130r]
  [132r] ➞ [132c]  Concept[132r] Inspection - action (qualifier value) <-2146938586>
  [133r] ➞ [133c]  Some[133r] Method (attribute) <-2147314116>➞[132r]
  [134r] ➞ [134c]  Concept[134r] Ureteroscope (physical object) <-2146503035>
  [135r] ➞ [135c]  Some[135r] Access instrument (attribute) <-2147283909>➞[134r]
  [136r] ➞ [136c]  And[136r]➞[129r, 131r, 133r, 135r]
  [137r] ➞ [137c]  Some[137r] Role group (SOLOR) <-2147483593>➞[136r]
  [138r] ➞ [138c]  Concept[138r] Urethral structure (body structure) <-2147284236>
  [139r] ➞ [139c]  Some[139r] Procedure site (attribute) <-2147378082>➞[138r]
  [140r] ➞ [140c]  Concept[140r] Renal pelvis structure (body structure) <-2147120521>
  [141r] ➞ [141c]  Some[141r] Procedure site (attribute) <-2147378082>➞[140r]
  [142r] ➞ [142c]  Concept[142r] Urinary bladder structure (body structure) <-2147419211>
  [143r] ➞ [143c]  Some[143r] Procedure site (attribute) <-2147378082>➞[142r]
  [144r] ➞ [144c]  Concept[144r] Ureteric structure (body structure) <-2147443660>
  [145r] ➞ [145c]  Some[145r] Procedure site (attribute) <-2147378082>➞[144r]
  [146r] ➞ [146c]  Concept[146r] Endoscopic approach - access (qualifier value) <-2146941410>
  [147r] ➞ [147c]  Some[147r] Access (attribute) <-2147315914>➞[146r]
  [148r] ➞ [148c]  Concept[148r] Transurethral approach (qualifier value) <-2146691909>
  [149r] ➞ [149c]  Some[149r] Approach (attribute) <-2147314305>➞[148r]
  [150r] ➞ [150c]  Concept[150r] Surgical action (qualifier value) <-2146940928>
  [151r] ➞ [151c]  Some[151r] Method (attribute) <-2147314116>➞[150r]
  [152r] ➞ [152c]  Concept[152r] Inspection - action (qualifier value) <-2146938586>
  [153r] ➞ [153c]  Some[153r] Method (attribute) <-2147314116>➞[152r]
  [154r] ➞ [154c]  Concept[154r] Cystoscope, device (physical object) <-2147196166>
  [155r] ➞ [155c]  Some[155r] Access instrument (attribute) <-2147283909>➞[154r]
  [156r] ➞ [156c]  And[156r]➞[139r, 141r, 143r, 145r, 147r, 149r, 151r, 153r, 155r]
  [157r] ➞ [157c]  Some[157r] Role group (SOLOR) <-2147483593>➞[156r]
  [158r] ➞ [174c]* Concept[158r] Endoscopic approach - access (qualifier value) <-2146941410>
  [159r] ➞ [175c]* Some[159r] Access (attribute) <-2147315914>➞[158r]
  [160r] ➞ [176c]* Concept[160r] Transurethral approach (qualifier value) <-2146691909>
  [161r] ➞ [177c]* Some[161r] Approach (attribute) <-2147314305>➞[160r]
  [162r] ➞ [178c]* Concept[162r] Inspection - action (qualifier value) <-2146938586>
  [163r] ➞ [179c]* Some[163r] Method (attribute) <-2147314116>➞[162r]
  [164r] ➞ [180c]* Concept[164r] Cystoscope, device (physical object) <-2147196166>
  [165r] ➞ [181c]* Some[165r] Access instrument (attribute) <-2147283909>➞[164r]
  [166r] ➞ [182c]* Concept[166r] Urinary bladder structure (body structure) <-2147419211>
  [167r] ➞ [183c]* Some[167r] Procedure site - Direct (attribute) <-2146878287>➞[166r]
  [168r] ➞ [184c]* And[168r]➞[159r, 161r, 163r, 165r, 167r]
  [169r] ➞ [185c]* Some[169r] Role group (SOLOR) <-2147483593>➞[168r]
  [170r] ➞ [158c]* Concept[170r] Endoscopy with surgical procedure (procedure) <-2147474859>
  [171r] ➞ [159c]* Concept[171r] Transurethral cystoscopy (procedure) <-2147462038>
  [172r] ➞ [160c]* Concept[172r] Endoscopy of renal pelvis (procedure) <-2147408011>
  [173r] ➞ [161c]* Concept[173r] Endoscopy of pelvic cavity (procedure) <-2147273796>
  [174r] ➞ [162c]* Concept[174r] Operation on bladder (procedure) <-2147270677>
  [175r] ➞ [163c]* Concept[175r] Operation on urethra (procedure) <-2147266709>
  [176r] ➞ [173c]* Concept[176r] Endoscopy of urethra (procedure) <-2147137204>
  [177r] ➞ [164c]* Concept[177r] Transurethral cystoscopy (procedure) <-2147133743>
  [178r] ➞ [165c]* Concept[178r] Ureteroscopy (procedure) <-2147119375>
  [179r] ➞ [166c]* Concept[179r] Operation on retroperitoneum (procedure) <-2146990006>
  [180r] ➞ [167c]* Concept[180r] Urethral removal of ureteric clot (procedure) <-2146907625>
  [181r] ➞ [168c]* Concept[181r] Ureteroscopic operation (procedure) <-2146478846>
  [182r] ➞ [169c]* Concept[182r] Kidney operation (procedure) <-2146307173>
  [183r] ➞ [170c]* And[183r]➞[3r, 7r, 11r, 15r, 19r, 23r, 27r, 33r, 37r, 41r, 45r, 51r, 63r, 67r, 73r, 79r, 85r, 91r, 99r, 107r, 117r, 127r, 137r, 157r, 169r, 170r, 171r, 172r, 173r, 174r, 175r, 176r, 177r, 178r, 179r, 180r, 181r, 182r]
  [184r] ➞ [171c]* Necessary[184r]➞[183r]
  [185r] ➞ [172c]* Root[185r]➞[184r]

Additions: 


Deletions: 


Shared relationship roots: 

  Concept[159] Transurethral cystoscopy (procedure) <-2147462038>

  Concept[158] Endoscopy with surgical procedure (procedure) <-2147474859>

  Concept[160] Endoscopy of renal pelvis (procedure) <-2147408011>

  Concept[161] Endoscopy of pelvic cavity (procedure) <-2147273796>

  Concept[162] Operation on bladder (procedure) <-2147270677>

  Concept[163] Operation on urethra (procedure) <-2147266709>

  Concept[173] Endoscopy of urethra (procedure) <-2147137204>

  Concept[164] Transurethral cystoscopy (procedure) <-2147133743>

  Concept[165] Ureteroscopy (procedure) <-2147119375>

  Concept[166] Operation on retroperitoneum (procedure) <-2146990006>

  Concept[167] Urethral removal of ureteric clot (procedure) <-2146907625>

  Concept[168] Ureteroscopic operation (procedure) <-2146478846>

  Concept[169] Kidney operation (procedure) <-2146307173>

  Some[37] Role group (SOLOR) <-2147483593>➞[36]
    And[36]➞[35]
        Some[35] Access instrument (attribute) <-2147283909>➞[34]
            Concept[34] Ureteroscope (physical object) <-2146503035>

  Some[27] Role group (SOLOR) <-2147483593>➞[26]
    And[26]➞[25]
        Some[25] Procedure site (attribute) <-2147378082>➞[24]
            Concept[24] Ureteric structure (body structure) <-2147443660>

  Some[11] Role group (SOLOR) <-2147483593>➞[10]
    And[10]➞[9]
        Some[9] Procedure site (attribute) <-2147378082>➞[8]
            Concept[8] Urinary bladder structure (body structure) <-2147419211>

  Some[67] Role group (SOLOR) <-2147483593>➞[66]
    And[66]➞[65]
        Some[65] Direct device (attribute) <-2147378264>➞[64]
            Concept[64] Endoscope, device (physical object) <-2146961395>

  Some[7] Role group (SOLOR) <-2147483593>➞[6]
    And[6]➞[5]
        Some[5] Approach (attribute) <-2147314305>➞[4]
            Concept[4] Transurethral approach (qualifier value) <-2146691909>

  Some[41] Role group (SOLOR) <-2147483593>➞[40]
    And[40]➞[39]
        Some[39] Access instrument (attribute) <-2147283909>➞[38]
            Concept[38] Cystoscope, device (physical object) <-2147196166>

  Some[19] Role group (SOLOR) <-2147483593>➞[18]
    And[18]➞[17]
        Some[17] Instrumentation (attribute) <-2146514480>➞[16]
            Concept[16] Cystoscope, device (physical object) <-2147196166>

  Some[23] Role group (SOLOR) <-2147483593>➞[22]
    And[22]➞[21]
        Some[21] Procedure site (attribute) <-2147378082>➞[20]
            Concept[20] Renal pelvis structure (body structure) <-2147120521>

  Some[45] Role group (SOLOR) <-2147483593>➞[44]
    And[44]➞[43]
        Some[43] Using (attribute) <-2147300466>➞[42]
            Concept[42] Endoscope, device (physical object) <-2146961395>

  Some[3] Role group (SOLOR) <-2147483593>➞[2]
    And[2]➞[1]
        Some[1] Access (attribute) <-2147315914>➞[0]
            Concept[0] Endoscopic approach - access (qualifier value) <-2146941410>

  Some[15] Role group (SOLOR) <-2147483593>➞[14]
    And[14]➞[13]
        Some[13] Instrumentation (attribute) <-2146514480>➞[12]
            Concept[12] Ureteroscope (physical object) <-2146503035>

  Some[73] Role group (SOLOR) <-2147483593>➞[72]
    And[72]➞[69, 71]
        Some[69] Procedure site (attribute) <-2147378082>➞[68]
            Concept[68] Urethral structure (body structure) <-2147284236>
        Some[71] Method (attribute) <-2147314116>➞[70]
            Concept[70] Inspection - action (qualifier value) <-2146938586>

  Some[79] Role group (SOLOR) <-2147483593>➞[78]
    And[78]➞[75, 77]
        Some[75] Procedure site (attribute) <-2147378082>➞[74]
            Concept[74] Renal pelvis structure (body structure) <-2147120521>
        Some[77] Method (attribute) <-2147314116>➞[76]
            Concept[76] Inspection - action (qualifier value) <-2146938586>

  Some[51] Role group (SOLOR) <-2147483593>➞[50]
    And[50]➞[47, 49]
        Some[47] Procedure site (attribute) <-2147378082>➞[46]
            Concept[46] Ureteric structure (body structure) <-2147443660>
        Some[49] Method (attribute) <-2147314116>➞[48]
            Concept[48] Endoscopic inspection - action (qualifier value) <-2146940226>

  Some[85] Role group (SOLOR) <-2147483593>➞[84]
    And[84]➞[81, 83]
        Some[81] Procedure site (attribute) <-2147378082>➞[80]
            Concept[80] Ureteric structure (body structure) <-2147443660>
        Some[83] Method (attribute) <-2147314116>➞[82]
            Concept[82] Inspection - action (qualifier value) <-2146938586>

  Some[91] Role group (SOLOR) <-2147483593>➞[90]
    And[90]➞[87, 89]
        Some[87] Procedure site (attribute) <-2147378082>➞[86]
            Concept[86] Urinary bladder structure (body structure) <-2147419211>
        Some[89] Method (attribute) <-2147314116>➞[88]
            Concept[88] Inspection - action (qualifier value) <-2146938586>

  Some[33] Role group (SOLOR) <-2147483593>➞[32]
    And[32]➞[29, 31]
        Some[29] Procedure site (attribute) <-2147378082>➞[28]
            Concept[28] Kidney structure (body structure) <-2146589772>
        Some[31] Method (attribute) <-2147314116>➞[30]
            Concept[30] Examination - action (qualifier value) <-2146625815>

  Some[99] Role group (SOLOR) <-2147483593>➞[98]
    And[98]➞[93, 95, 97]
        Some[93] Procedure site (attribute) <-2147378082>➞[92]
            Concept[92] Urinary bladder structure (body structure) <-2147419211>
        Some[95] Procedure site (attribute) <-2147378082>➞[94]
            Concept[94] Renal pelvis structure (body structure) <-2147120521>
        Some[97] Method (attribute) <-2147314116>➞[96]
            Concept[96] Surgical action (qualifier value) <-2146940928>

  Some[107] Role group (SOLOR) <-2147483593>➞[106]
    And[106]➞[101, 103, 105]
        Some[101] Access (attribute) <-2147315914>➞[100]
            Concept[100] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[103] Approach (attribute) <-2147314305>➞[102]
            Concept[102] Transurethral approach (qualifier value) <-2146691909>
        Some[105] Access instrument (attribute) <-2147283909>➞[104]
            Concept[104] Cystoscope, device (physical object) <-2147196166>

  Some[63] Role group (SOLOR) <-2147483593>➞[62]
    And[62]➞[53, 55, 57, 59, 61]
        Some[53] Procedure site (attribute) <-2147378082>➞[52]
            Concept[52] Urinary bladder structure (body structure) <-2147419211>
        Some[55] Procedure site (attribute) <-2147378082>➞[54]
            Concept[54] Kidney structure (body structure) <-2146589772>
        Some[57] Procedure site (attribute) <-2147378082>➞[56]
            Concept[56] Ureteric structure (body structure) <-2147443660>
        Some[59] Method (attribute) <-2147314116>➞[58]
            Concept[58] Endoscopic inspection - action (qualifier value) <-2146940226>
        Some[61] Method (attribute) <-2147314116>➞[60]
            Concept[60] Surgical action (qualifier value) <-2146940928>

  Some[117] Role group (SOLOR) <-2147483593>➞[116]
    And[116]➞[109, 111, 113, 115]
        Some[109] Procedure site (attribute) <-2147378082>➞[108]
            Concept[108] Urethral structure (body structure) <-2147284236>
        Some[111] Access (attribute) <-2147315914>➞[110]
            Concept[110] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[113] Method (attribute) <-2147314116>➞[112]
            Concept[112] Inspection - action (qualifier value) <-2146938586>
        Some[115] Access instrument (attribute) <-2147283909>➞[114]
            Concept[114] Endoscope, device (physical object) <-2146961395>

  Some[127] Role group (SOLOR) <-2147483593>➞[126]
    And[126]➞[119, 121, 123, 125]
        Some[119] Procedure site (attribute) <-2147378082>➞[118]
            Concept[118] Urinary bladder structure (body structure) <-2147419211>
        Some[121] Access (attribute) <-2147315914>➞[120]
            Concept[120] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[123] Method (attribute) <-2147314116>➞[122]
            Concept[122] Inspection - action (qualifier value) <-2146938586>
        Some[125] Access instrument (attribute) <-2147283909>➞[124]
            Concept[124] Cystoscope, device (physical object) <-2147196166>

  Some[137] Role group (SOLOR) <-2147483593>➞[136]
    And[136]➞[129, 131, 133, 135]
        Some[129] Procedure site (attribute) <-2147378082>➞[128]
            Concept[128] Ureteric structure (body structure) <-2147443660>
        Some[131] Access (attribute) <-2147315914>➞[130]
            Concept[130] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[133] Method (attribute) <-2147314116>➞[132]
            Concept[132] Inspection - action (qualifier value) <-2146938586>
        Some[135] Access instrument (attribute) <-2147283909>➞[134]
            Concept[134] Ureteroscope (physical object) <-2146503035>

  Some[185] Role group (SOLOR) <-2147483593>➞[184]
    And[184]➞[175, 177, 179, 181, 183]
        Some[175] Access (attribute) <-2147315914>➞[174]
            Concept[174] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[177] Approach (attribute) <-2147314305>➞[176]
            Concept[176] Transurethral approach (qualifier value) <-2146691909>
        Some[179] Method (attribute) <-2147314116>➞[178]
            Concept[178] Inspection - action (qualifier value) <-2146938586>
        Some[181] Access instrument (attribute) <-2147283909>➞[180]
            Concept[180] Cystoscope, device (physical object) <-2147196166>
        Some[183] Procedure site - Direct (attribute) <-2146878287>➞[182]
            Concept[182] Urinary bladder structure (body structure) <-2147419211>

  Some[157] Role group (SOLOR) <-2147483593>➞[156]
    And[156]➞[139, 141, 143, 145, 147, 149, 151, 153, 155]
        Some[139] Procedure site (attribute) <-2147378082>➞[138]
            Concept[138] Urethral structure (body structure) <-2147284236>
        Some[141] Procedure site (attribute) <-2147378082>➞[140]
            Concept[140] Renal pelvis structure (body structure) <-2147120521>
        Some[143] Procedure site (attribute) <-2147378082>➞[142]
            Concept[142] Urinary bladder structure (body structure) <-2147419211>
        Some[145] Procedure site (attribute) <-2147378082>➞[144]
            Concept[144] Ureteric structure (body structure) <-2147443660>
        Some[147] Access (attribute) <-2147315914>➞[146]
            Concept[146] Endoscopic approach - access (qualifier value) <-2146941410>
        Some[149] Approach (attribute) <-2147314305>➞[148]
            Concept[148] Transurethral approach (qualifier value) <-2146691909>
        Some[151] Method (attribute) <-2147314116>➞[150]
            Concept[150] Surgical action (qualifier value) <-2146940928>
        Some[153] Method (attribute) <-2147314116>➞[152]
            Concept[152] Inspection - action (qualifier value) <-2146938586>
        Some[155] Access instrument (attribute) <-2147283909>➞[154]
            Concept[154] Cystoscope, device (physical object) <-2147196166>


New relationship roots: 


Deleted relationship roots: 


Merged expression: 

Root[185m]➞[184m]
    Necessary[184m]➞[183m]
        And[183m]➞[3m, 7m, 11m, 15m, 19m, 23m, 27m, 33m, 37m, 41m, 45m, 51m, 63m, 67m, 73m, 79m, 85m, 91m, 99m, 107m, 117m, 127m, 137m, 157m, 169m, 170m, 171m, 172m, 173m, 174m, 175m, 176m, 177m, 178m, 179m, 180m, 181m, 182m]
            Some[3m] Role group (SOLOR) <-2147483593>➞[2m]
                And[2m]➞[1m]
                    Some[1m] Access (attribute) <-2147315914>➞[0m]
                        Concept[0m] Endoscopic approach - access (qualifier value) <-2146941410>
            Some[7m] Role group (SOLOR) <-2147483593>➞[6m]
                And[6m]➞[5m]
                    Some[5m] Approach (attribute) <-2147314305>➞[4m]
                        Concept[4m] Transurethral approach (qualifier value) <-2146691909>
            Some[11m] Role group (SOLOR) <-2147483593>➞[10m]
                And[10m]➞[9m]
                    Some[9m] Procedure site (attribute) <-2147378082>➞[8m]
                        Concept[8m] Urinary bladder structure (body structure) <-2147419211>
            Some[15m] Role group (SOLOR) <-2147483593>➞[14m]
                And[14m]➞[13m]
                    Some[13m] Instrumentation (attribute) <-2146514480>➞[12m]
                        Concept[12m] Ureteroscope (physical object) <-2146503035>
            Some[19m] Role group (SOLOR) <-2147483593>➞[18m]
                And[18m]➞[17m]
                    Some[17m] Instrumentation (attribute) <-2146514480>➞[16m]
                        Concept[16m] Cystoscope, device (physical object) <-2147196166>
            Some[23m] Role group (SOLOR) <-2147483593>➞[22m]
                And[22m]➞[21m]
                    Some[21m] Procedure site (attribute) <-2147378082>➞[20m]
                        Concept[20m] Renal pelvis structure (body structure) <-2147120521>
            Some[27m] Role group (SOLOR) <-2147483593>➞[26m]
                And[26m]➞[25m]
                    Some[25m] Procedure site (attribute) <-2147378082>➞[24m]
                        Concept[24m] Ureteric structure (body structure) <-2147443660>
            Some[33m] Role group (SOLOR) <-2147483593>➞[32m]
                And[32m]➞[29m, 31m]
                    Some[29m] Procedure site (attribute) <-2147378082>➞[28m]
                        Concept[28m] Kidney structure (body structure) <-2146589772>
                    Some[31m] Method (attribute) <-2147314116>➞[30m]
                        Concept[30m] Examination - action (qualifier value) <-2146625815>
            Some[37m] Role group (SOLOR) <-2147483593>➞[36m]
                And[36m]➞[35m]
                    Some[35m] Access instrument (attribute) <-2147283909>➞[34m]
                        Concept[34m] Ureteroscope (physical object) <-2146503035>
            Some[41m] Role group (SOLOR) <-2147483593>➞[40m]
                And[40m]➞[39m]
                    Some[39m] Access instrument (attribute) <-2147283909>➞[38m]
                        Concept[38m] Cystoscope, device (physical object) <-2147196166>
            Some[45m] Role group (SOLOR) <-2147483593>➞[44m]
                And[44m]➞[43m]
                    Some[43m] Using (attribute) <-2147300466>➞[42m]
                        Concept[42m] Endoscope, device (physical object) <-2146961395>
            Some[51m] Role group (SOLOR) <-2147483593>➞[50m]
                And[50m]➞[47m, 49m]
                    Some[47m] Procedure site (attribute) <-2147378082>➞[46m]
                        Concept[46m] Ureteric structure (body structure) <-2147443660>
                    Some[49m] Method (attribute) <-2147314116>➞[48m]
                        Concept[48m] Endoscopic inspection - action (qualifier value) <-2146940226>
            Some[63m] Role group (SOLOR) <-2147483593>➞[62m]
                And[62m]➞[53m, 55m, 57m, 59m, 61m]
                    Some[53m] Procedure site (attribute) <-2147378082>➞[52m]
                        Concept[52m] Urinary bladder structure (body structure) <-2147419211>
                    Some[55m] Procedure site (attribute) <-2147378082>➞[54m]
                        Concept[54m] Kidney structure (body structure) <-2146589772>
                    Some[57m] Procedure site (attribute) <-2147378082>➞[56m]
                        Concept[56m] Ureteric structure (body structure) <-2147443660>
                    Some[59m] Method (attribute) <-2147314116>➞[58m]
                        Concept[58m] Endoscopic inspection - action (qualifier value) <-2146940226>
                    Some[61m] Method (attribute) <-2147314116>➞[60m]
                        Concept[60m] Surgical action (qualifier value) <-2146940928>
            Some[67m] Role group (SOLOR) <-2147483593>➞[66m]
                And[66m]➞[65m]
                    Some[65m] Direct device (attribute) <-2147378264>➞[64m]
                        Concept[64m] Endoscope, device (physical object) <-2146961395>
            Some[73m] Role group (SOLOR) <-2147483593>➞[72m]
                And[72m]➞[69m, 71m]
                    Some[69m] Procedure site (attribute) <-2147378082>➞[68m]
                        Concept[68m] Urethral structure (body structure) <-2147284236>
                    Some[71m] Method (attribute) <-2147314116>➞[70m]
                        Concept[70m] Inspection - action (qualifier value) <-2146938586>
            Some[79m] Role group (SOLOR) <-2147483593>➞[78m]
                And[78m]➞[75m, 77m]
                    Some[75m] Procedure site (attribute) <-2147378082>➞[74m]
                        Concept[74m] Renal pelvis structure (body structure) <-2147120521>
                    Some[77m] Method (attribute) <-2147314116>➞[76m]
                        Concept[76m] Inspection - action (qualifier value) <-2146938586>
            Some[85m] Role group (SOLOR) <-2147483593>➞[84m]
                And[84m]➞[81m, 83m]
                    Some[81m] Procedure site (attribute) <-2147378082>➞[80m]
                        Concept[80m] Ureteric structure (body structure) <-2147443660>
                    Some[83m] Method (attribute) <-2147314116>➞[82m]
                        Concept[82m] Inspection - action (qualifier value) <-2146938586>
            Some[91m] Role group (SOLOR) <-2147483593>➞[90m]
                And[90m]➞[87m, 89m]
                    Some[87m] Procedure site (attribute) <-2147378082>➞[86m]
                        Concept[86m] Urinary bladder structure (body structure) <-2147419211>
                    Some[89m] Method (attribute) <-2147314116>➞[88m]
                        Concept[88m] Inspection - action (qualifier value) <-2146938586>
            Some[99m] Role group (SOLOR) <-2147483593>➞[98m]
                And[98m]➞[93m, 95m, 97m]
                    Some[93m] Procedure site (attribute) <-2147378082>➞[92m]
                        Concept[92m] Urinary bladder structure (body structure) <-2147419211>
                    Some[95m] Procedure site (attribute) <-2147378082>➞[94m]
                        Concept[94m] Renal pelvis structure (body structure) <-2147120521>
                    Some[97m] Method (attribute) <-2147314116>➞[96m]
                        Concept[96m] Surgical action (qualifier value) <-2146940928>
            Some[107m] Role group (SOLOR) <-2147483593>➞[106m]
                And[106m]➞[101m, 103m, 105m]
                    Some[101m] Access (attribute) <-2147315914>➞[100m]
                        Concept[100m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[103m] Approach (attribute) <-2147314305>➞[102m]
                        Concept[102m] Transurethral approach (qualifier value) <-2146691909>
                    Some[105m] Access instrument (attribute) <-2147283909>➞[104m]
                        Concept[104m] Cystoscope, device (physical object) <-2147196166>
            Some[117m] Role group (SOLOR) <-2147483593>➞[116m]
                And[116m]➞[109m, 111m, 113m, 115m]
                    Some[109m] Procedure site (attribute) <-2147378082>➞[108m]
                        Concept[108m] Urethral structure (body structure) <-2147284236>
                    Some[111m] Access (attribute) <-2147315914>➞[110m]
                        Concept[110m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[113m] Method (attribute) <-2147314116>➞[112m]
                        Concept[112m] Inspection - action (qualifier value) <-2146938586>
                    Some[115m] Access instrument (attribute) <-2147283909>➞[114m]
                        Concept[114m] Endoscope, device (physical object) <-2146961395>
            Some[127m] Role group (SOLOR) <-2147483593>➞[126m]
                And[126m]➞[119m, 121m, 123m, 125m]
                    Some[119m] Procedure site (attribute) <-2147378082>➞[118m]
                        Concept[118m] Urinary bladder structure (body structure) <-2147419211>
                    Some[121m] Access (attribute) <-2147315914>➞[120m]
                        Concept[120m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[123m] Method (attribute) <-2147314116>➞[122m]
                        Concept[122m] Inspection - action (qualifier value) <-2146938586>
                    Some[125m] Access instrument (attribute) <-2147283909>➞[124m]
                        Concept[124m] Cystoscope, device (physical object) <-2147196166>
            Some[137m] Role group (SOLOR) <-2147483593>➞[136m]
                And[136m]➞[129m, 131m, 133m, 135m]
                    Some[129m] Procedure site (attribute) <-2147378082>➞[128m]
                        Concept[128m] Ureteric structure (body structure) <-2147443660>
                    Some[131m] Access (attribute) <-2147315914>➞[130m]
                        Concept[130m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[133m] Method (attribute) <-2147314116>➞[132m]
                        Concept[132m] Inspection - action (qualifier value) <-2146938586>
                    Some[135m] Access instrument (attribute) <-2147283909>➞[134m]
                        Concept[134m] Ureteroscope (physical object) <-2146503035>
            Some[157m] Role group (SOLOR) <-2147483593>➞[156m]
                And[156m]➞[139m, 141m, 143m, 145m, 147m, 149m, 151m, 153m, 155m]
                    Some[139m] Procedure site (attribute) <-2147378082>➞[138m]
                        Concept[138m] Urethral structure (body structure) <-2147284236>
                    Some[141m] Procedure site (attribute) <-2147378082>➞[140m]
                        Concept[140m] Renal pelvis structure (body structure) <-2147120521>
                    Some[143m] Procedure site (attribute) <-2147378082>➞[142m]
                        Concept[142m] Urinary bladder structure (body structure) <-2147419211>
                    Some[145m] Procedure site (attribute) <-2147378082>➞[144m]
                        Concept[144m] Ureteric structure (body structure) <-2147443660>
                    Some[147m] Access (attribute) <-2147315914>➞[146m]
                        Concept[146m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[149m] Approach (attribute) <-2147314305>➞[148m]
                        Concept[148m] Transurethral approach (qualifier value) <-2146691909>
                    Some[151m] Method (attribute) <-2147314116>➞[150m]
                        Concept[150m] Surgical action (qualifier value) <-2146940928>
                    Some[153m] Method (attribute) <-2147314116>➞[152m]
                        Concept[152m] Inspection - action (qualifier value) <-2146938586>
                    Some[155m] Access instrument (attribute) <-2147283909>➞[154m]
                        Concept[154m] Cystoscope, device (physical object) <-2147196166>
            Some[169m] Role group (SOLOR) <-2147483593>➞[168m]
                And[168m]➞[159m, 161m, 163m, 165m, 167m]
                    Some[159m] Access (attribute) <-2147315914>➞[158m]
                        Concept[158m] Endoscopic approach - access (qualifier value) <-2146941410>
                    Some[161m] Approach (attribute) <-2147314305>➞[160m]
                        Concept[160m] Transurethral approach (qualifier value) <-2146691909>
                    Some[163m] Method (attribute) <-2147314116>➞[162m]
                        Concept[162m] Inspection - action (qualifier value) <-2146938586>
                    Some[165m] Access instrument (attribute) <-2147283909>➞[164m]
                        Concept[164m] Cystoscope, device (physical object) <-2147196166>
                    Some[167m] Procedure site - Direct (attribute) <-2146878287>➞[166m]
                        Concept[166m] Urinary bladder structure (body structure) <-2147419211>
            Concept[170m] Endoscopy with surgical procedure (procedure) <-2147474859>
            Concept[171m] Transurethral cystoscopy (procedure) <-2147462038>
            Concept[172m] Endoscopy of renal pelvis (procedure) <-2147408011>
            Concept[173m] Endoscopy of pelvic cavity (procedure) <-2147273796>
            Concept[174m] Operation on bladder (procedure) <-2147270677>
            Concept[175m] Operation on urethra (procedure) <-2147266709>
            Concept[176m] Endoscopy of urethra (procedure) <-2147137204>
            Concept[177m] Transurethral cystoscopy (procedure) <-2147133743>
            Concept[178m] Ureteroscopy (procedure) <-2147119375>
            Concept[179m] Operation on retroperitoneum (procedure) <-2146990006>
            Concept[180m] Urethral removal of ureteric clot (procedure) <-2146907625>
            Concept[181m] Ureteroscopic operation (procedure) <-2146478846>
            Concept[182m] Kidney operation (procedure) <-2146307173>


 */
public class CorrelationProblem1 {
static LogicalExpression getReferenceExpression() {
       LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();


       NecessarySet(
       And(
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"), 
       ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("191f6333-8c5d-32b0-870f-16889ed417f9"), 
       ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("191f6333-8c5d-32b0-870f-16889ed417f9"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("ed9ed6f3-2f1f-38c0-966e-fa43f7ff89e3"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("8f7cb92d-5530-3b75-91ef-9f5e63106e9b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("6860552d-437e-34aa-8027-59070eddce17"), 
       ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("ed9ed6f3-2f1f-38c0-966e-fa43f7ff89e3"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("102422d3-6b68-3d16-a756-1df791d91e7f"), 
       ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"), 
       ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
)
, 
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("11bd0c80-dbb9-3908-9d49-fccc8d2cd97d"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)
, 
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)
, 
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)
, 
       SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"), 
       ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
)
, 
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"), 
       ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)

          )
)
, 
       SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), 
       And(
       SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"), 
       ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
)
, 
       SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"), 
       ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
)
, 
       SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"), 
       ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
)
, 
       SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"), 
       ConceptAssertion(Get.concept("c32c9441-5298-342b-9ae4-e1d5a909bb7b"), leb)
)
, 
       SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"), 
       ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
)

          )
)
, 
       ConceptAssertion(Get.concept("76f21b2e-4f6d-3875-a063-1a7550137dc6"), leb)
, 
       ConceptAssertion(Get.concept("829b7ab2-fb5c-3783-b7ef-1e343e84f659"), leb)
, 
       ConceptAssertion(Get.concept("8300a6bf-cd5e-327a-96d4-aa2976e6e6ec"), leb)
, 
       ConceptAssertion(Get.concept("12f0d949-576e-3f3e-a09d-c186ee2b97ad"), leb)
, 
       ConceptAssertion(Get.concept("7767fb43-7739-38f5-86f3-eb393bd454bd"), leb)
, 
       ConceptAssertion(Get.concept("4697f71d-9655-37be-a347-5fa24a937f50"), leb)
, 
       ConceptAssertion(Get.concept("79fc871c-9f6a-3871-9e16-69ea55ffbceb"), leb)
, 
       ConceptAssertion(Get.concept("546ca61f-5714-3b48-8899-00d60a9769ca"), leb)
, 
       ConceptAssertion(Get.concept("745620f0-e195-347c-9c8b-2e144ef4cc67"), leb)
, 
       ConceptAssertion(Get.concept("b9ea1de7-e37e-34e1-a85f-f218149a73e7"), leb)
, 
       ConceptAssertion(Get.concept("cba17d3a-cea3-3a8b-9538-c4755a56d508"), leb)
, 
       ConceptAssertion(Get.concept("67891570-7604-3b9c-84f0-4f4f9acd0591"), leb)
, 
       ConceptAssertion(Get.concept("796a2607-68b8-31f6-a643-16693fff0561"), leb)

          )

          )
;
        return leb.build();
}



    static LogicalExpression getComparisonExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        NecessarySet(
                And(
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
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("191f6333-8c5d-32b0-870f-16889ed417f9"),
                                                ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
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
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("ed9ed6f3-2f1f-38c0-966e-fa43f7ff89e3"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("8f7cb92d-5530-3b75-91ef-9f5e63106e9b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
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
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("1119fc96-c2ca-3ada-bd90-60cd481d4516"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("ed9ed6f3-2f1f-38c0-966e-fa43f7ff89e3"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
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
                                                ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
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
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
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
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("f1f816ed-e681-3c06-980c-5378ff406bd3"), leb)
                                        ),
                                         SomeRole(Get.concept("d32e27d0-b0bc-34e0-ade6-2d352eab5c63"),
                                                ConceptAssertion(Get.concept("22754acb-a209-3908-89e1-4d6988390c8b"), leb)
                                        )
                                )
                        ),
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("43a88d0f-a7fa-3090-bcc2-6b05f1a9e0e6"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("8de12b8d-ef13-3a8d-b020-417836f75448"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("6520ef22-2ebf-3b29-ac94-640ec30572c9"), leb)
                                        ),
                                         SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("28d3fbe2-50b2-3c4e-832d-77402f770f6c"), leb)
                                        ),
                                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("cfd3d56a-347e-35e2-a6c0-918dd85c72f0"), leb)
                                        ),
                                         SomeRole(Get.concept("3bce0a6e-3d9b-3a1a-9b57-e22ff126e368"),
                                                ConceptAssertion(Get.concept("4ec68202-c9d1-3c0e-b80f-28524a3f646a"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
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
                         ConceptAssertion(Get.concept("8300a6bf-cd5e-327a-96d4-aa2976e6e6ec"), leb),
                         ConceptAssertion(Get.concept("12f0d949-576e-3f3e-a09d-c186ee2b97ad"), leb),
                         ConceptAssertion(Get.concept("7767fb43-7739-38f5-86f3-eb393bd454bd"), leb),
                         ConceptAssertion(Get.concept("4697f71d-9655-37be-a347-5fa24a937f50"), leb),
                         ConceptAssertion(Get.concept("79fc871c-9f6a-3871-9e16-69ea55ffbceb"), leb),
                         ConceptAssertion(Get.concept("546ca61f-5714-3b48-8899-00d60a9769ca"), leb),
                         ConceptAssertion(Get.concept("745620f0-e195-347c-9c8b-2e144ef4cc67"), leb),
                         ConceptAssertion(Get.concept("b9ea1de7-e37e-34e1-a85f-f218149a73e7"), leb),
                         ConceptAssertion(Get.concept("cba17d3a-cea3-3a8b-9538-c4755a56d508"), leb),
                         ConceptAssertion(Get.concept("67891570-7604-3b9c-84f0-4f4f9acd0591"), leb),
                         ConceptAssertion(Get.concept("796a2607-68b8-31f6-a643-16693fff0561"), leb)
                )
        );
        return leb.build();
    }

}
