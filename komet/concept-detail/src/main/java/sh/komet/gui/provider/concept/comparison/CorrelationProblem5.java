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

/**
 * New isomorphic record: 5010 Isomorphic Analysis for:Primary open reduction of
 * fracture dislocation alone (procedure) 0477ab39-7c10-3ba7-bfca-c9dc98dbe27c
 *
Reference expression:

 Root[0r]➞[186r]
    Necessary[186r]➞[185r]
        And[185r]➞[4r, 8r, 12r, 16r, 20r, 24r, 28r, 32r, 38r, 42r, 50r, 60r, 68r, 78r, 88r, 94r, 102r, 112r, 124r, 136r, 144r, 152r, 162r, 172r, 182r, 183r, 184r]
            Some[4r] Role group (SOLOR) <-2147483593>➞[3r]
                And[3r]➞[2r]
                    Some[2r] Access (attribute) <-2147315914>➞[1r]
                        Concept[1r] Open approach - access (qualifier value) <-2146941428>
            Some[8r] Role group (SOLOR) <-2147483593>➞[7r]
                And[7r]➞[6r]
                    Some[6r] Direct morphology (attribute) <-2147378241>➞[5r]
                        Concept[5r] Dislocation (morphologic abnormality) <-2147448026>
            Some[12r] Role group (SOLOR) <-2147483593>➞[11r]
                And[11r]➞[10r]
                    Some[10r] Revision status (attribute) <-2146315099>➞[9r]
                        Concept[9r] Principal (qualifier value) <-2146603744>
            Some[16r] Role group (SOLOR) <-2147483593>➞[15r]
                And[15r]➞[14r]
                    Some[14r] Method (attribute) <-2147314116>➞[13r]
                        Concept[13r] Surgical repair - action (qualifier value) <-2146939778>
            Some[20r] Role group (SOLOR) <-2147483593>➞[19r]
                And[19r]➞[18r]
                    Some[18r] Direct morphology (attribute) <-2147378241>➞[17r]
                        Concept[17r] Dislocation of joint (disorder) <-2147196846>
            Some[24r] Role group (SOLOR) <-2147483593>➞[23r]
                And[23r]➞[22r]
                    Some[22r] Revision status (attribute) <-2146315099>➞[21r]
                        Concept[21r] Primary operation (qualifier value) <-2147302589>
            Some[28r] Role group (SOLOR) <-2147483593>➞[27r]
                And[27r]➞[26r]
                    Some[26r] Direct morphology (attribute) <-2147378241>➞[25r]
                        Concept[25r] Traumatic dislocation (morphologic abnormality) <-2146977295>
            Some[32r] Role group (SOLOR) <-2147483593>➞[31r]
                And[31r]➞[30r]
                    Some[30r] Method (attribute) <-2147314116>➞[29r]
                        Concept[29r] Surgical action (qualifier value) <-2146940928>
            Some[38r] Role group (SOLOR) <-2147483593>➞[37r]
                And[37r]➞[34r, 36r]
                    Some[34r] Procedure site (attribute) <-2147378082>➞[33r]
                        Concept[33r] Joint structure (body structure) <-2146932341>
                    Some[36r] Method (attribute) <-2147314116>➞[35r]
                        Concept[35r] Reduction - action (qualifier value) <-2146938668>
            Some[42r] Role group (SOLOR) <-2147483593>➞[41r]
                And[41r]➞[40r]
                    Some[40r] Direct morphology (attribute) <-2147378241>➞[39r]
                        Concept[39r] Fracture-dislocation (morphologic abnormality) <-2147403668>
            Some[50r] Role group (SOLOR) <-2147483593>➞[49r]
                And[49r]➞[44r, 46r, 48r]
                    Some[44r] Direct morphology (attribute) <-2147378241>➞[43r]
                        Concept[43r] Dislocation (morphologic abnormality) <-2147448026>
                    Some[46r] Method (attribute) <-2147314116>➞[45r]
                        Concept[45r] Reduction - action (qualifier value) <-2146938668>
                    Some[48r] Procedure site - Indirect (attribute) <-2146878264>➞[47r]
                        Concept[47r] Joint structure (body structure) <-2146932341>
            Some[60r] Role group (SOLOR) <-2147483593>➞[59r]
                And[59r]➞[52r, 54r, 56r, 58r]
                    Some[52r] Direct morphology (attribute) <-2147378241>➞[51r]
                        Concept[51r] Fracture (morphologic abnormality) <-2146461022>
                    Some[54r] Access (attribute) <-2147315914>➞[53r]
                        Concept[53r] Open approach - access (qualifier value) <-2146941428>
                    Some[56r] Method (attribute) <-2147314116>➞[55r]
                        Concept[55r] Reduction - action (qualifier value) <-2146938668>
                    Some[58r] Procedure site - Indirect (attribute) <-2146878264>➞[57r]
                        Concept[57r] Bone structure (body structure) <-2147146938>
            Some[68r] Role group (SOLOR) <-2147483593>➞[67r]
                And[67r]➞[62r, 64r, 66r]
                    Some[62r] Direct morphology (attribute) <-2147378241>➞[61r]
                        Concept[61r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[64r] Method (attribute) <-2147314116>➞[63r]
                        Concept[63r] Reduction - action (qualifier value) <-2146938668>
                    Some[66r] Procedure site - Direct (attribute) <-2146878287>➞[65r]
                        Concept[65r] Bone structure (body structure) <-2147146938>
            Some[78r] Role group (SOLOR) <-2147483593>➞[77r]
                And[77r]➞[70r, 72r, 74r, 76r]
                    Some[70r] Direct morphology (attribute) <-2147378241>➞[69r]
                        Concept[69r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[72r] Access (attribute) <-2147315914>➞[71r]
                        Concept[71r] Open approach - access (qualifier value) <-2146941428>
                    Some[74r] Method (attribute) <-2147314116>➞[73r]
                        Concept[73r] Reduction - action (qualifier value) <-2146938668>
                    Some[76r] Procedure site - Direct (attribute) <-2146878287>➞[75r]
                        Concept[75r] Joint structure (body structure) <-2146932341>
            Some[88r] Role group (SOLOR) <-2147483593>➞[87r]
                And[87r]➞[80r, 82r, 84r, 86r]
                    Some[80r] Direct morphology (attribute) <-2147378241>➞[79r]
                        Concept[79r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[82r] Access (attribute) <-2147315914>➞[81r]
                        Concept[81r] Open approach - access (qualifier value) <-2146941428>
                    Some[84r] Method (attribute) <-2147314116>➞[83r]
                        Concept[83r] Reduction - action (qualifier value) <-2146938668>
                    Some[86r] Procedure site - Indirect (attribute) <-2146878264>➞[85r]
                        Concept[85r] Bone structure (body structure) <-2147146938>
            Some[94r] Role group (SOLOR) <-2147483593>➞[93r]
                And[93r]➞[90r, 92r]
                    Some[90r] Direct morphology (attribute) <-2147378241>➞[89r]
                        Concept[89r] Fracture (morphologic abnormality) <-2146461022>
                    Some[92r] Method (attribute) <-2147314116>➞[91r]
                        Concept[91r] Open reduction - action (qualifier value) <-2146585859>
            Some[102r] Role group (SOLOR) <-2147483593>➞[101r]
                And[101r]➞[96r, 98r, 100r]
                    Some[96r] Direct morphology (attribute) <-2147378241>➞[95r]
                        Concept[95r] Dislocation (morphologic abnormality) <-2147448026>
                    Some[98r] Method (attribute) <-2147314116>➞[97r]
                        Concept[97r] Open reduction - action (qualifier value) <-2146585859>
                    Some[100r] Procedure site - Direct (attribute) <-2146878287>➞[99r]
                        Concept[99r] Joint structure (body structure) <-2146932341>
            Some[112r] Role group (SOLOR) <-2147483593>➞[111r]
                And[111r]➞[104r, 106r, 108r, 110r]
                    Some[104r] Direct morphology (attribute) <-2147378241>➞[103r]
                        Concept[103r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[106r] Method (attribute) <-2147314116>➞[105r]
                        Concept[105r] Reduction - action (qualifier value) <-2146938668>
                    Some[108r] Procedure site - Direct (attribute) <-2146878287>➞[107r]
                        Concept[107r] Bone structure (body structure) <-2147146938>
                    Some[110r] Revision status (attribute) <-2146315099>➞[109r]
                        Concept[109r] Primary operation (qualifier value) <-2147302589>
            Some[124r] Role group (SOLOR) <-2147483593>➞[123r]
                And[123r]➞[114r, 116r, 118r, 120r, 122r]
                    Some[114r] Direct morphology (attribute) <-2147378241>➞[113r]
                        Concept[113r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[116r] Access (attribute) <-2147315914>➞[115r]
                        Concept[115r] Open approach - access (qualifier value) <-2146941428>
                    Some[118r] Method (attribute) <-2147314116>➞[117r]
                        Concept[117r] Reduction - action (qualifier value) <-2146938668>
                    Some[120r] Procedure site - Direct (attribute) <-2146878287>➞[119r]
                        Concept[119r] Joint structure (body structure) <-2146932341>
                    Some[122r] Revision status (attribute) <-2146315099>➞[121r]
                        Concept[121r] Primary operation (qualifier value) <-2147302589>
            Some[136r] Role group (SOLOR) <-2147483593>➞[135r]
                And[135r]➞[126r, 128r, 130r, 132r, 134r]
                    Some[126r] Direct morphology (attribute) <-2147378241>➞[125r]
                        Concept[125r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[128r] Access (attribute) <-2147315914>➞[127r]
                        Concept[127r] Open approach - access (qualifier value) <-2146941428>
                    Some[130r] Method (attribute) <-2147314116>➞[129r]
                        Concept[129r] Reduction - action (qualifier value) <-2146938668>
                    Some[132r] Procedure site - Indirect (attribute) <-2146878264>➞[131r]
                        Concept[131r] Bone structure (body structure) <-2147146938>
                    Some[134r] Revision status (attribute) <-2146315099>➞[133r]
                        Concept[133r] Primary operation (qualifier value) <-2147302589>
            Some[144r] Role group (SOLOR) <-2147483593>➞[143r]
                And[143r]➞[138r, 140r, 142r]
                    Some[138r] Direct morphology (attribute) <-2147378241>➞[137r]
                        Concept[137r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[140r] Method (attribute) <-2147314116>➞[139r]
                        Concept[139r] Open reduction - action (qualifier value) <-2146585859>
                    Some[142r] Procedure site - Direct (attribute) <-2146878287>➞[141r]
                        Concept[141r] Joint structure (body structure) <-2146932341>
            Some[152r] Role group (SOLOR) <-2147483593>➞[151r]
                And[151r]➞[146r, 148r, 150r]
                    Some[146r] Direct morphology (attribute) <-2147378241>➞[145r]
                        Concept[145r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[148r] Method (attribute) <-2147314116>➞[147r]
                        Concept[147r] Reduction - action (qualifier value) <-2146938668>
                    Some[150r] Procedure site - Direct (attribute) <-2146878287>➞[149r]
                        Concept[149r] Joint structure (body structure) <-2146932341>
            Some[162r] Role group (SOLOR) <-2147483593>➞[161r]
                And[161r]➞[154r, 156r, 158r, 160r]
                    Some[154r] Direct morphology (attribute) <-2147378241>➞[153r]
                        Concept[153r] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[156r] Method (attribute) <-2147314116>➞[155r]
                        Concept[155r] Open reduction - action (qualifier value) <-2146585859>
                    Some[158r] Procedure site - Direct (attribute) <-2146878287>➞[157r]
                        Concept[157r] Bone structure (body structure) <-2147146938>
                    Some[160r] Revision status (attribute) <-2146315099>➞[159r]
                        Concept[159r] Primary operation (qualifier value) <-2147302589>
            Some[172r] Role group (SOLOR) <-2147483593>➞[171r]
                And[171r]➞[164r, 166r, 168r, 170r]
                    Some[164r] Direct morphology (attribute) <-2147378241>➞[163r]
                        Concept[163r] Dislocation (morphologic abnormality) <-2147448026>
                    Some[166r] Method (attribute) <-2147314116>➞[165r]
                        Concept[165r] Open reduction - action (qualifier value) <-2146585859>
                    Some[168r] Procedure site - Direct (attribute) <-2146878287>➞[167r]
                        Concept[167r] Joint structure (body structure) <-2146932341>
                    Some[170r] Revision status (attribute) <-2146315099>➞[169r]
                        Concept[169r] Primary operation (qualifier value) <-2147302589>
            Some[182r] Role group (SOLOR) <-2147483593>➞[181r]
                And[181r]➞[174r, 176r, 178r, 180r]
                    Some[174r] Direct morphology (attribute) <-2147378241>➞[173r]
                        Concept[173r] Fracture (morphologic abnormality) <-2146461022>
                    Some[176r] Method (attribute) <-2147314116>➞[175r]
                        Concept[175r] Open reduction - action (qualifier value) <-2146585859>
                    Some[178r] Procedure site - Direct (attribute) <-2146878287>➞[177r]
                        Concept[177r] Bone structure (body structure) <-2147146938>
                    Some[180r] Revision status (attribute) <-2146315099>➞[179r]
                        Concept[179r] Primary operation (qualifier value) <-2147302589>
            Concept[183r] Reduction of fracture (procedure) <-2147008610>
            Concept[184r] Primary open reduction of fracture dislocation (procedure) <-2146647907>

Comparison expression:

 Root[0c]➞[166c]
    Necessary[166c]➞[165c]
        And[165c]➞[4c, 8c, 12c, 16c, 20c, 24c, 28c, 32c, 38c, 42c, 50c, 60c, 68c, 78c, 88c, 94c, 102c, 112c, 124c, 136c, 144c, 152c, 162c, 163c, 164c]
            Some[4c] Role group (SOLOR) <-2147483593>➞[3c]
                And[3c]➞[2c]
                    Some[2c] Access (attribute) <-2147315914>➞[1c]
                        Concept[1c] Open approach - access (qualifier value) <-2146941428>
            Some[8c] Role group (SOLOR) <-2147483593>➞[7c]
                And[7c]➞[6c]
                    Some[6c] Direct morphology (attribute) <-2147378241>➞[5c]
                        Concept[5c] Dislocation (morphologic abnormality) <-2147448026>
            Some[12c] Role group (SOLOR) <-2147483593>➞[11c]
                And[11c]➞[10c]
                    Some[10c] Revision status (attribute) <-2146315099>➞[9c]
                        Concept[9c] Principal (qualifier value) <-2146603744>
            Some[16c] Role group (SOLOR) <-2147483593>➞[15c]
                And[15c]➞[14c]
                    Some[14c] Method (attribute) <-2147314116>➞[13c]
                        Concept[13c] Surgical repair - action (qualifier value) <-2146939778>
            Some[20c] Role group (SOLOR) <-2147483593>➞[19c]
                And[19c]➞[18c]
                    Some[18c] Direct morphology (attribute) <-2147378241>➞[17c]
                        Concept[17c] Dislocation of joint (disorder) <-2147196846>
            Some[24c] Role group (SOLOR) <-2147483593>➞[23c]
                And[23c]➞[22c]
                    Some[22c] Revision status (attribute) <-2146315099>➞[21c]
                        Concept[21c] Primary operation (qualifier value) <-2147302589>
            Some[28c] Role group (SOLOR) <-2147483593>➞[27c]
                And[27c]➞[26c]
                    Some[26c] Direct morphology (attribute) <-2147378241>➞[25c]
                        Concept[25c] Traumatic dislocation (morphologic abnormality) <-2146977295>
            Some[32c] Role group (SOLOR) <-2147483593>➞[31c]
                And[31c]➞[30c]
                    Some[30c] Method (attribute) <-2147314116>➞[29c]
                        Concept[29c] Surgical action (qualifier value) <-2146940928>
            Some[38c] Role group (SOLOR) <-2147483593>➞[37c]
                And[37c]➞[34c, 36c]
                    Some[34c] Procedure site (attribute) <-2147378082>➞[33c]
                        Concept[33c] Joint structure (body structure) <-2146932341>
                    Some[36c] Method (attribute) <-2147314116>➞[35c]
                        Concept[35c] Reduction - action (qualifier value) <-2146938668>
            Some[42c] Role group (SOLOR) <-2147483593>➞[41c]
                And[41c]➞[40c]
                    Some[40c] Direct morphology (attribute) <-2147378241>➞[39c]
                        Concept[39c] Fracture-dislocation (morphologic abnormality) <-2147403668>
            Some[50c] Role group (SOLOR) <-2147483593>➞[49c]
                And[49c]➞[44c, 46c, 48c]
                    Some[44c] Direct morphology (attribute) <-2147378241>➞[43c]
                        Concept[43c] Dislocation (morphologic abnormality) <-2147448026>
                    Some[46c] Method (attribute) <-2147314116>➞[45c]
                        Concept[45c] Reduction - action (qualifier value) <-2146938668>
                    Some[48c] Procedure site - Indirect (attribute) <-2146878264>➞[47c]
                        Concept[47c] Joint structure (body structure) <-2146932341>
            Some[60c] Role group (SOLOR) <-2147483593>➞[59c]
                And[59c]➞[52c, 54c, 56c, 58c]
                    Some[52c] Direct morphology (attribute) <-2147378241>➞[51c]
                        Concept[51c] Fracture (morphologic abnormality) <-2146461022>
                    Some[54c] Access (attribute) <-2147315914>➞[53c]
                        Concept[53c] Open approach - access (qualifier value) <-2146941428>
                    Some[56c] Method (attribute) <-2147314116>➞[55c]
                        Concept[55c] Reduction - action (qualifier value) <-2146938668>
                    Some[58c] Procedure site - Indirect (attribute) <-2146878264>➞[57c]
                        Concept[57c] Bone structure (body structure) <-2147146938>
            Some[68c] Role group (SOLOR) <-2147483593>➞[67c]
                And[67c]➞[62c, 64c, 66c]
                    Some[62c] Direct morphology (attribute) <-2147378241>➞[61c]
                        Concept[61c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[64c] Method (attribute) <-2147314116>➞[63c]
                        Concept[63c] Reduction - action (qualifier value) <-2146938668>
                    Some[66c] Procedure site - Direct (attribute) <-2146878287>➞[65c]
                        Concept[65c] Bone structure (body structure) <-2147146938>
            Some[78c] Role group (SOLOR) <-2147483593>➞[77c]
                And[77c]➞[70c, 72c, 74c, 76c]
                    Some[70c] Direct morphology (attribute) <-2147378241>➞[69c]
                        Concept[69c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[72c] Access (attribute) <-2147315914>➞[71c]
                        Concept[71c] Open approach - access (qualifier value) <-2146941428>
                    Some[74c] Method (attribute) <-2147314116>➞[73c]
                        Concept[73c] Reduction - action (qualifier value) <-2146938668>
                    Some[76c] Procedure site - Direct (attribute) <-2146878287>➞[75c]
                        Concept[75c] Joint structure (body structure) <-2146932341>
            Some[88c] Role group (SOLOR) <-2147483593>➞[87c]
                And[87c]➞[80c, 82c, 84c, 86c]
                    Some[80c] Direct morphology (attribute) <-2147378241>➞[79c]
                        Concept[79c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[82c] Access (attribute) <-2147315914>➞[81c]
                        Concept[81c] Open approach - access (qualifier value) <-2146941428>
                    Some[84c] Method (attribute) <-2147314116>➞[83c]
                        Concept[83c] Reduction - action (qualifier value) <-2146938668>
                    Some[86c] Procedure site - Indirect (attribute) <-2146878264>➞[85c]
                        Concept[85c] Bone structure (body structure) <-2147146938>
            Some[94c] Role group (SOLOR) <-2147483593>➞[93c]
                And[93c]➞[90c, 92c]
                    Some[90c] Direct morphology (attribute) <-2147378241>➞[89c]
                        Concept[89c] Fracture (morphologic abnormality) <-2146461022>
                    Some[92c] Method (attribute) <-2147314116>➞[91c]
                        Concept[91c] Open reduction - action (qualifier value) <-2146585859>
            Some[102c] Role group (SOLOR) <-2147483593>➞[101c]
                And[101c]➞[96c, 98c, 100c]
                    Some[96c] Direct morphology (attribute) <-2147378241>➞[95c]
                        Concept[95c] Dislocation (morphologic abnormality) <-2147448026>
                    Some[98c] Method (attribute) <-2147314116>➞[97c]
                        Concept[97c] Open reduction - action (qualifier value) <-2146585859>
                    Some[100c] Procedure site - Direct (attribute) <-2146878287>➞[99c]
                        Concept[99c] Joint structure (body structure) <-2146932341>
            Some[112c] Role group (SOLOR) <-2147483593>➞[111c]
                And[111c]➞[104c, 106c, 108c, 110c]
                    Some[104c] Direct morphology (attribute) <-2147378241>➞[103c]
                        Concept[103c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[106c] Method (attribute) <-2147314116>➞[105c]
                        Concept[105c] Reduction - action (qualifier value) <-2146938668>
                    Some[108c] Procedure site - Direct (attribute) <-2146878287>➞[107c]
                        Concept[107c] Bone structure (body structure) <-2147146938>
                    Some[110c] Revision status (attribute) <-2146315099>➞[109c]
                        Concept[109c] Primary operation (qualifier value) <-2147302589>
            Some[124c] Role group (SOLOR) <-2147483593>➞[123c]
                And[123c]➞[114c, 116c, 118c, 120c, 122c]
                    Some[114c] Direct morphology (attribute) <-2147378241>➞[113c]
                        Concept[113c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[116c] Access (attribute) <-2147315914>➞[115c]
                        Concept[115c] Open approach - access (qualifier value) <-2146941428>
                    Some[118c] Method (attribute) <-2147314116>➞[117c]
                        Concept[117c] Reduction - action (qualifier value) <-2146938668>
                    Some[120c] Procedure site - Direct (attribute) <-2146878287>➞[119c]
                        Concept[119c] Joint structure (body structure) <-2146932341>
                    Some[122c] Revision status (attribute) <-2146315099>➞[121c]
                        Concept[121c] Primary operation (qualifier value) <-2147302589>
            Some[136c] Role group (SOLOR) <-2147483593>➞[135c]
                And[135c]➞[126c, 128c, 130c, 132c, 134c]
                    Some[126c] Direct morphology (attribute) <-2147378241>➞[125c]
                        Concept[125c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[128c] Access (attribute) <-2147315914>➞[127c]
                        Concept[127c] Open approach - access (qualifier value) <-2146941428>
                    Some[130c] Method (attribute) <-2147314116>➞[129c]
                        Concept[129c] Reduction - action (qualifier value) <-2146938668>
                    Some[132c] Procedure site - Indirect (attribute) <-2146878264>➞[131c]
                        Concept[131c] Bone structure (body structure) <-2147146938>
                    Some[134c] Revision status (attribute) <-2146315099>➞[133c]
                        Concept[133c] Primary operation (qualifier value) <-2147302589>
            Some[144c] Role group (SOLOR) <-2147483593>➞[143c]
                And[143c]➞[138c, 140c, 142c]
                    Some[138c] Direct morphology (attribute) <-2147378241>➞[137c]
                        Concept[137c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[140c] Method (attribute) <-2147314116>➞[139c]
                        Concept[139c] Open reduction - action (qualifier value) <-2146585859>
                    Some[142c] Procedure site - Direct (attribute) <-2146878287>➞[141c]
                        Concept[141c] Joint structure (body structure) <-2146932341>
            Some[152c] Role group (SOLOR) <-2147483593>➞[151c]
                And[151c]➞[146c, 148c, 150c]
                    Some[146c] Direct morphology (attribute) <-2147378241>➞[145c]
                        Concept[145c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[148c] Method (attribute) <-2147314116>➞[147c]
                        Concept[147c] Reduction - action (qualifier value) <-2146938668>
                    Some[150c] Procedure site - Direct (attribute) <-2146878287>➞[149c]
                        Concept[149c] Joint structure (body structure) <-2146932341>
            Some[162c] Role group (SOLOR) <-2147483593>➞[161c]
                And[161c]➞[154c, 156c, 158c, 160c]
                    Some[154c] Direct morphology (attribute) <-2147378241>➞[153c]
                        Concept[153c] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[156c] Method (attribute) <-2147314116>➞[155c]
                        Concept[155c] Open reduction - action (qualifier value) <-2146585859>
                    Some[158c] Procedure site - Direct (attribute) <-2146878287>➞[157c]
                        Concept[157c] Bone structure (body structure) <-2147146938>
                    Some[160c] Revision status (attribute) <-2146315099>➞[159c]
                        Concept[159c] Primary operation (qualifier value) <-2147302589>
            Concept[163c] Reduction of fracture (procedure) <-2147008610>
            Concept[164c] Primary open reduction of fracture dislocation (procedure) <-2146647907>

Isomorphic expression:

 Root[166i]➞[165i]
    Necessary[165i]➞[164i]
        And[164i]➞[3i, 7i, 11i, 15i, 19i, 23i, 27i, 31i, 37i, 41i, 49i, 59i, 67i, 77i, 87i, 93i, 101i, 111i, 123i, 135i, 143i, 151i, 161i, 162i, 163i]
            Some[3i] Role group (SOLOR) <-2147483593>➞[2i]
                And[2i]➞[1i]
                    Some[1i] Access (attribute) <-2147315914>➞[0i]
                        Concept[0i] Open approach - access (qualifier value) <-2146941428>
            Some[7i] Role group (SOLOR) <-2147483593>➞[6i]
                And[6i]➞[5i]
                    Some[5i] Direct morphology (attribute) <-2147378241>➞[4i]
                        Concept[4i] Dislocation (morphologic abnormality) <-2147448026>
            Some[11i] Role group (SOLOR) <-2147483593>➞[10i]
                And[10i]➞[9i]
                    Some[9i] Revision status (attribute) <-2146315099>➞[8i]
                        Concept[8i] Principal (qualifier value) <-2146603744>
            Some[15i] Role group (SOLOR) <-2147483593>➞[14i]
                And[14i]➞[13i]
                    Some[13i] Method (attribute) <-2147314116>➞[12i]
                        Concept[12i] Surgical repair - action (qualifier value) <-2146939778>
            Some[19i] Role group (SOLOR) <-2147483593>➞[18i]
                And[18i]➞[17i]
                    Some[17i] Direct morphology (attribute) <-2147378241>➞[16i]
                        Concept[16i] Dislocation of joint (disorder) <-2147196846>
            Some[23i] Role group (SOLOR) <-2147483593>➞[22i]
                And[22i]➞[21i]
                    Some[21i] Revision status (attribute) <-2146315099>➞[20i]
                        Concept[20i] Primary operation (qualifier value) <-2147302589>
            Some[27i] Role group (SOLOR) <-2147483593>➞[26i]
                And[26i]➞[25i]
                    Some[25i] Direct morphology (attribute) <-2147378241>➞[24i]
                        Concept[24i] Traumatic dislocation (morphologic abnormality) <-2146977295>
            Some[31i] Role group (SOLOR) <-2147483593>➞[30i]
                And[30i]➞[29i]
                    Some[29i] Method (attribute) <-2147314116>➞[28i]
                        Concept[28i] Surgical action (qualifier value) <-2146940928>
            Some[37i] Role group (SOLOR) <-2147483593>➞[36i]
                And[36i]➞[33i, 35i]
                    Some[33i] Procedure site (attribute) <-2147378082>➞[32i]
                        Concept[32i] Joint structure (body structure) <-2146932341>
                    Some[35i] Method (attribute) <-2147314116>➞[34i]
                        Concept[34i] Reduction - action (qualifier value) <-2146938668>
            Some[41i] Role group (SOLOR) <-2147483593>➞[40i]
                And[40i]➞[39i]
                    Some[39i] Direct morphology (attribute) <-2147378241>➞[38i]
                        Concept[38i] Fracture-dislocation (morphologic abnormality) <-2147403668>
            Some[49i] Role group (SOLOR) <-2147483593>➞[48i]
                And[48i]➞[43i, 45i, 47i]
                    Some[43i] Direct morphology (attribute) <-2147378241>➞[42i]
                        Concept[42i] Dislocation (morphologic abnormality) <-2147448026>
                    Some[45i] Method (attribute) <-2147314116>➞[44i]
                        Concept[44i] Reduction - action (qualifier value) <-2146938668>
                    Some[47i] Procedure site - Indirect (attribute) <-2146878264>➞[46i]
                        Concept[46i] Joint structure (body structure) <-2146932341>
            Some[59i] Role group (SOLOR) <-2147483593>➞[58i]
                And[58i]➞[51i, 53i, 55i, 57i]
                    Some[51i] Direct morphology (attribute) <-2147378241>➞[50i]
                        Concept[50i] Fracture (morphologic abnormality) <-2146461022>
                    Some[53i] Access (attribute) <-2147315914>➞[52i]
                        Concept[52i] Open approach - access (qualifier value) <-2146941428>
                    Some[55i] Method (attribute) <-2147314116>➞[54i]
                        Concept[54i] Reduction - action (qualifier value) <-2146938668>
                    Some[57i] Procedure site - Indirect (attribute) <-2146878264>➞[56i]
                        Concept[56i] Bone structure (body structure) <-2147146938>
            Some[67i] Role group (SOLOR) <-2147483593>➞[66i]
                And[66i]➞[61i, 63i, 65i]
                    Some[61i] Direct morphology (attribute) <-2147378241>➞[60i]
                        Concept[60i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[63i] Method (attribute) <-2147314116>➞[62i]
                        Concept[62i] Reduction - action (qualifier value) <-2146938668>
                    Some[65i] Procedure site - Direct (attribute) <-2146878287>➞[64i]
                        Concept[64i] Bone structure (body structure) <-2147146938>
            Some[77i] Role group (SOLOR) <-2147483593>➞[76i]
                And[76i]➞[69i, 71i, 73i, 75i]
                    Some[69i] Direct morphology (attribute) <-2147378241>➞[68i]
                        Concept[68i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[71i] Access (attribute) <-2147315914>➞[70i]
                        Concept[70i] Open approach - access (qualifier value) <-2146941428>
                    Some[73i] Method (attribute) <-2147314116>➞[72i]
                        Concept[72i] Reduction - action (qualifier value) <-2146938668>
                    Some[75i] Procedure site - Direct (attribute) <-2146878287>➞[74i]
                        Concept[74i] Joint structure (body structure) <-2146932341>
            Some[87i] Role group (SOLOR) <-2147483593>➞[86i]
                And[86i]➞[79i, 81i, 83i, 85i]
                    Some[79i] Direct morphology (attribute) <-2147378241>➞[78i]
                        Concept[78i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[81i] Access (attribute) <-2147315914>➞[80i]
                        Concept[80i] Open approach - access (qualifier value) <-2146941428>
                    Some[83i] Method (attribute) <-2147314116>➞[82i]
                        Concept[82i] Reduction - action (qualifier value) <-2146938668>
                    Some[85i] Procedure site - Indirect (attribute) <-2146878264>➞[84i]
                        Concept[84i] Bone structure (body structure) <-2147146938>
            Some[93i] Role group (SOLOR) <-2147483593>➞[92i]
                And[92i]➞[89i, 91i]
                    Some[89i] Direct morphology (attribute) <-2147378241>➞[88i]
                        Concept[88i] Fracture (morphologic abnormality) <-2146461022>
                    Some[91i] Method (attribute) <-2147314116>➞[90i]
                        Concept[90i] Open reduction - action (qualifier value) <-2146585859>
            Some[101i] Role group (SOLOR) <-2147483593>➞[100i]
                And[100i]➞[95i, 97i, 99i]
                    Some[95i] Direct morphology (attribute) <-2147378241>➞[94i]
                        Concept[94i] Dislocation (morphologic abnormality) <-2147448026>
                    Some[97i] Method (attribute) <-2147314116>➞[96i]
                        Concept[96i] Open reduction - action (qualifier value) <-2146585859>
                    Some[99i] Procedure site - Direct (attribute) <-2146878287>➞[98i]
                        Concept[98i] Joint structure (body structure) <-2146932341>
            Some[111i] Role group (SOLOR) <-2147483593>➞[110i]
                And[110i]➞[103i, 105i, 107i, 109i]
                    Some[103i] Direct morphology (attribute) <-2147378241>➞[102i]
                        Concept[102i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[105i] Method (attribute) <-2147314116>➞[104i]
                        Concept[104i] Reduction - action (qualifier value) <-2146938668>
                    Some[107i] Procedure site - Direct (attribute) <-2146878287>➞[106i]
                        Concept[106i] Bone structure (body structure) <-2147146938>
                    Some[109i] Revision status (attribute) <-2146315099>➞[108i]
                        Concept[108i] Primary operation (qualifier value) <-2147302589>
            Some[123i] Role group (SOLOR) <-2147483593>➞[122i]
                And[122i]➞[113i, 115i, 117i, 119i, 121i]
                    Some[113i] Direct morphology (attribute) <-2147378241>➞[112i]
                        Concept[112i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[115i] Access (attribute) <-2147315914>➞[114i]
                        Concept[114i] Open approach - access (qualifier value) <-2146941428>
                    Some[117i] Method (attribute) <-2147314116>➞[116i]
                        Concept[116i] Reduction - action (qualifier value) <-2146938668>
                    Some[119i] Procedure site - Direct (attribute) <-2146878287>➞[118i]
                        Concept[118i] Joint structure (body structure) <-2146932341>
                    Some[121i] Revision status (attribute) <-2146315099>➞[120i]
                        Concept[120i] Primary operation (qualifier value) <-2147302589>
            Some[135i] Role group (SOLOR) <-2147483593>➞[134i]
                And[134i]➞[125i, 127i, 129i, 131i, 133i]
                    Some[125i] Direct morphology (attribute) <-2147378241>➞[124i]
                        Concept[124i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[127i] Access (attribute) <-2147315914>➞[126i]
                        Concept[126i] Open approach - access (qualifier value) <-2146941428>
                    Some[129i] Method (attribute) <-2147314116>➞[128i]
                        Concept[128i] Reduction - action (qualifier value) <-2146938668>
                    Some[131i] Procedure site - Indirect (attribute) <-2146878264>➞[130i]
                        Concept[130i] Bone structure (body structure) <-2147146938>
                    Some[133i] Revision status (attribute) <-2146315099>➞[132i]
                        Concept[132i] Primary operation (qualifier value) <-2147302589>
            Some[143i] Role group (SOLOR) <-2147483593>➞[142i]
                And[142i]➞[137i, 139i, 141i]
                    Some[137i] Direct morphology (attribute) <-2147378241>➞[136i]
                        Concept[136i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[139i] Method (attribute) <-2147314116>➞[138i]
                        Concept[138i] Open reduction - action (qualifier value) <-2146585859>
                    Some[141i] Procedure site - Direct (attribute) <-2146878287>➞[140i]
                        Concept[140i] Joint structure (body structure) <-2146932341>
            Some[151i] Role group (SOLOR) <-2147483593>➞[150i]
                And[150i]➞[145i, 147i, 149i]
                    Some[145i] Direct morphology (attribute) <-2147378241>➞[144i]
                        Concept[144i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[147i] Method (attribute) <-2147314116>➞[146i]
                        Concept[146i] Reduction - action (qualifier value) <-2146938668>
                    Some[149i] Procedure site - Direct (attribute) <-2146878287>➞[148i]
                        Concept[148i] Joint structure (body structure) <-2146932341>
            Some[161i] Role group (SOLOR) <-2147483593>➞[160i]
                And[160i]➞[153i, 155i, 157i, 159i]
                    Some[153i] Direct morphology (attribute) <-2147378241>➞[152i]
                        Concept[152i] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[155i] Method (attribute) <-2147314116>➞[154i]
                        Concept[154i] Open reduction - action (qualifier value) <-2146585859>
                    Some[157i] Procedure site - Direct (attribute) <-2146878287>➞[156i]
                        Concept[156i] Bone structure (body structure) <-2147146938>
                    Some[159i] Revision status (attribute) <-2146315099>➞[158i]
                        Concept[158i] Primary operation (qualifier value) <-2147302589>
            Concept[162i] Reduction of fracture (procedure) <-2147008610>
            Concept[163i] Primary open reduction of fracture dislocation (procedure) <-2146647907>

Reference Expression To MergedNodeId Map:

 [0r:186m, 1r:0m, 2r:1m, 3r:2m, 4r:3m, 5r:4m, 6r:5m, 7r:6m, 8r:7m, 9r:8m, 10r:9m, 11r:10m, 12r:11m, 13r:12m, 14r:13m, 15r:14m, 16r:15m, 17r:16m, 18r:17m, 19r:18m, 20r:19m, 21r:20m, 22r:21m, 23r:22m, 24r:23m, 25r:24m, 26r:25m, 27r:26m, 28r:27m, 29r:28m, 30r:29m, 31r:30m, 32r:31m, 33r:32m, 34r:33m, 35r:34m, 36r:35m, 37r:36m, 38r:37m, 39r:38m, 40r:39m, 41r:40m, 42r:41m, 43r:42m, 44r:43m, 45r:44m, 46r:45m, 47r:46m, 48r:47m, 49r:48m, 50r:49m, 51r:50m, 52r:51m, 53r:52m, 54r:53m, 55r:54m, 56r:55m, 57r:56m, 58r:57m, 59r:58m, 60r:59m, 61r:60m, 62r:61m, 63r:62m, 64r:63m, 65r:64m, 66r:65m, 67r:66m, 68r:67m, 69r:68m, 70r:69m, 71r:70m, 72r:71m, 73r:72m, 74r:73m, 75r:74m, 76r:75m, 77r:76m, 78r:77m, 79r:78m, 80r:79m, 81r:80m, 82r:81m, 83r:82m, 84r:83m, 85r:84m, 86r:85m, 87r:86m, 88r:87m, 89r:88m, 90r:89m, 91r:90m, 92r:91m, 93r:92m, 94r:93m, 95r:94m, 96r:95m, 97r:96m, 98r:97m, 99r:98m, 100r:99m, 101r:100m, 102r:101m, 103r:102m, 104r:103m, 105r:104m, 106r:105m, 107r:106m, 108r:107m, 109r:108m, 110r:109m, 111r:110m, 112r:111m, 113r:112m, 114r:113m, 115r:114m, 116r:115m, 117r:116m, 118r:117m, 119r:118m, 120r:119m, 121r:120m, 122r:121m, 123r:122m, 124r:123m, 125r:124m, 126r:125m, 127r:126m, 128r:127m, 129r:128m, 130r:129m, 131r:130m, 132r:131m, 133r:132m, 134r:133m, 135r:134m, 136r:135m, 137r:136m, 138r:137m, 139r:138m, 140r:139m, 141r:140m, 142r:141m, 143r:142m, 144r:143m, 145r:144m, 146r:145m, 147r:146m, 148r:147m, 149r:148m, 150r:149m, 151r:150m, 152r:151m, 153r:152m, 154r:153m, 155r:154m, 156r:155m, 157r:156m, 158r:157m, 159r:158m, 160r:159m, 161r:160m, 162r:161m, 163r:162m, 164r:163m, 165r:164m, 166r:165m, 167r:166m, 168r:167m, 169r:168m, 170r:169m, 171r:170m, 172r:171m, 173r:172m, 174r:173m, 175r:174m, 176r:175m, 177r:176m, 178r:177m, 179r:178m, 180r:179m, 181r:180m, 182r:181m, 183r:182m, 184r:183m, 185r:184m, 186r:185m]

Reference Expression To ComparisonNodeId Map:

 [0r:0c, 1r:1c, 2r:2c, 3r:3c, 4r:4c, 5r:5c, 6r:6c, 7r:7c, 8r:8c, 9r:9c, 10r:10c, 11r:11c, 12r:12c, 13r:13c, 14r:14c, 15r:15c, 16r:16c, 17r:17c, 18r:18c, 19r:19c, 20r:20c, 21r:21c, 22r:22c, 23r:23c, 24r:24c, 25r:25c, 26r:26c, 27r:27c, 28r:28c, 29r:29c, 30r:30c, 31r:31c, 32r:32c, 33r:33c, 34r:34c, 35r:35c, 36r:36c, 37r:37c, 38r:38c, 39r:39c, 40r:40c, 41r:41c, 42r:42c, 43r:43c, 44r:44c, 45r:45c, 46r:46c, 47r:47c, 48r:48c, 49r:49c, 50r:50c, 51r:51c, 52r:52c, 53r:53c, 54r:54c, 55r:55c, 56r:56c, 57r:57c, 58r:58c, 59r:59c, 60r:60c, 61r:61c, 62r:62c, 63r:63c, 64r:64c, 65r:65c, 66r:66c, 67r:67c, 68r:68c, 69r:69c, 70r:70c, 71r:71c, 72r:72c, 73r:73c, 74r:74c, 75r:75c, 76r:76c, 77r:77c, 78r:78c, 79r:79c, 80r:80c, 81r:81c, 82r:82c, 83r:83c, 84r:84c, 85r:85c, 86r:86c, 87r:87c, 88r:88c, 89r:89c, 90r:90c, 91r:91c, 92r:92c, 93r:93c, 94r:94c, 95r:95c, 96r:96c, 97r:97c, 98r:98c, 99r:99c, 100r:100c, 101r:101c, 102r:102c, 103r:103c, 104r:104c, 105r:105c, 106r:106c, 107r:107c, 108r:108c, 109r:109c, 110r:110c, 111r:111c, 112r:112c, 113r:113c, 114r:114c, 115r:115c, 116r:116c, 117r:117c, 118r:118c, 119r:119c, 120r:120c, 121r:121c, 122r:122c, 123r:123c, 124r:124c, 125r:125c, 126r:126c, 127r:127c, 128r:128c, 129r:129c, 130r:130c, 131r:131c, 132r:132c, 133r:133c, 134r:134c, 135r:135c, 136r:136c, 137r:137c, 138r:138c, 139r:139c, 140r:140c, 141r:141c, 142r:142c, 143r:143c, 144r:144c, 145r:145c, 146r:146c, 147r:147c, 148r:148c, 149r:149c, 150r:150c, 151r:151c, 152r:152c, 153r:153c, 154r:154c, 155r:155c, 156r:156c, 157r:157c, 158r:158c, 159r:159c, 160r:160c, 161r:161c, 162r:162c, 163r:-1c, 164r:-1c, 165r:-1c, 166r:-1c, 167r:-1c, 168r:-1c, 169r:-1c, 170r:-1c, 171r:-1c, 172r:-1c, 173r:-1c, 174r:-1c, 175r:-1c, 176r:-1c, 177r:-1c, 178r:-1c, 179r:-1c, 180r:-1c, 181r:-1c, 182r:-1c, 183r:163c, 184r:164c, 185r:165c, 186r:166c]

Comparison Expression To ReferenceNodeId Map:

 [0c:0r, 1c:1r, 2c:2r, 3c:3r, 4c:4r, 5c:5r, 6c:6r, 7c:7r, 8c:8r, 9c:9r, 10c:10r, 11c:11r, 12c:12r, 13c:13r, 14c:14r, 15c:15r, 16c:16r, 17c:17r, 18c:18r, 19c:19r, 20c:20r, 21c:21r, 22c:22r, 23c:23r, 24c:24r, 25c:25r, 26c:26r, 27c:27r, 28c:28r, 29c:29r, 30c:30r, 31c:31r, 32c:32r, 33c:33r, 34c:34r, 35c:35r, 36c:36r, 37c:37r, 38c:38r, 39c:39r, 40c:40r, 41c:41r, 42c:42r, 43c:43r, 44c:44r, 45c:45r, 46c:46r, 47c:47r, 48c:48r, 49c:49r, 50c:50r, 51c:51r, 52c:52r, 53c:53r, 54c:54r, 55c:55r, 56c:56r, 57c:57r, 58c:58r, 59c:59r, 60c:60r, 61c:61r, 62c:62r, 63c:63r, 64c:64r, 65c:65r, 66c:66r, 67c:67r, 68c:68r, 69c:69r, 70c:70r, 71c:71r, 72c:72r, 73c:73r, 74c:74r, 75c:75r, 76c:76r, 77c:77r, 78c:78r, 79c:79r, 80c:80r, 81c:81r, 82c:82r, 83c:83r, 84c:84r, 85c:85r, 86c:86r, 87c:87r, 88c:88r, 89c:89r, 90c:90r, 91c:91r, 92c:92r, 93c:93r, 94c:94r, 95c:95r, 96c:96r, 97c:97r, 98c:98r, 99c:99r, 100c:100r, 101c:101r, 102c:102r, 103c:103r, 104c:104r, 105c:105r, 106c:106r, 107c:107r, 108c:108r, 109c:109r, 110c:110r, 111c:111r, 112c:112r, 113c:113r, 114c:114r, 115c:115r, 116c:116r, 117c:117r, 118c:118r, 119c:119r, 120c:120r, 121c:121r, 122c:122r, 123c:123r, 124c:124r, 125c:125r, 126c:126r, 127c:127r, 128c:128r, 129c:129r, 130c:130r, 131c:131r, 132c:132r, 133c:133r, 134c:134r, 135c:135r, 136c:136r, 137c:137r, 138c:138r, 139c:139r, 140c:140r, 141c:141r, 142c:142r, 143c:143r, 144c:144r, 145c:145r, 146c:146r, 147c:147r, 148c:148r, 149c:149r, 150c:150r, 151c:151r, 152c:152r, 153c:153r, 154c:154r, 155c:155r, 156c:156r, 157c:157r, 158c:158r, 159c:159r, 160c:160r, 161c:161r, 162c:162r, 163c:183r, 164c:184r, 165c:185r, 166c:186r]

Isomorphic solution: 
  [  0r] ➞ [  0c]  Root[0r]➞[186r]
  [  1r] ➞ [  1c]  Concept[1r] Open approach - access (qualifier value) <-2146941428>
  [  2r] ➞ [  2c]  Some[2r] Access (attribute) <-2147315914>➞[1r]
  [  3r] ➞ [  3c]  And[3r]➞[2r]
  [  4r] ➞ [  4c]  Some[4r] Role group (SOLOR) <-2147483593>➞[3r]
  [  5r] ➞ [  5c]  Concept[5r] Dislocation (morphologic abnormality) <-2147448026>
  [  6r] ➞ [  6c]  Some[6r] Direct morphology (attribute) <-2147378241>➞[5r]
  [  7r] ➞ [  7c]  And[7r]➞[6r]
  [  8r] ➞ [  8c]  Some[8r] Role group (SOLOR) <-2147483593>➞[7r]
  [  9r] ➞ [  9c]  Concept[9r] Principal (qualifier value) <-2146603744>
  [ 10r] ➞ [ 10c]  Some[10r] Revision status (attribute) <-2146315099>➞[9r]
  [ 11r] ➞ [ 11c]  And[11r]➞[10r]
  [ 12r] ➞ [ 12c]  Some[12r] Role group (SOLOR) <-2147483593>➞[11r]
  [ 13r] ➞ [ 13c]  Concept[13r] Surgical repair - action (qualifier value) <-2146939778>
  [ 14r] ➞ [ 14c]  Some[14r] Method (attribute) <-2147314116>➞[13r]
  [ 15r] ➞ [ 15c]  And[15r]➞[14r]
  [ 16r] ➞ [ 16c]  Some[16r] Role group (SOLOR) <-2147483593>➞[15r]
  [ 17r] ➞ [ 17c]  Concept[17r] Dislocation of joint (disorder) <-2147196846>
  [ 18r] ➞ [ 18c]  Some[18r] Direct morphology (attribute) <-2147378241>➞[17r]
  [ 19r] ➞ [ 19c]  And[19r]➞[18r]
  [ 20r] ➞ [ 20c]  Some[20r] Role group (SOLOR) <-2147483593>➞[19r]
  [ 21r] ➞ [ 21c]  Concept[21r] Primary operation (qualifier value) <-2147302589>
  [ 22r] ➞ [ 22c]  Some[22r] Revision status (attribute) <-2146315099>➞[21r]
  [ 23r] ➞ [ 23c]  And[23r]➞[22r]
  [ 24r] ➞ [ 24c]  Some[24r] Role group (SOLOR) <-2147483593>➞[23r]
  [ 25r] ➞ [ 25c]  Concept[25r] Traumatic dislocation (morphologic abnormality) <-2146977295>
  [ 26r] ➞ [ 26c]  Some[26r] Direct morphology (attribute) <-2147378241>➞[25r]
  [ 27r] ➞ [ 27c]  And[27r]➞[26r]
  [ 28r] ➞ [ 28c]  Some[28r] Role group (SOLOR) <-2147483593>➞[27r]
  [ 29r] ➞ [ 29c]  Concept[29r] Surgical action (qualifier value) <-2146940928>
  [ 30r] ➞ [ 30c]  Some[30r] Method (attribute) <-2147314116>➞[29r]
  [ 31r] ➞ [ 31c]  And[31r]➞[30r]
  [ 32r] ➞ [ 32c]  Some[32r] Role group (SOLOR) <-2147483593>➞[31r]
  [ 33r] ➞ [ 33c]  Concept[33r] Joint structure (body structure) <-2146932341>
  [ 34r] ➞ [ 34c]  Some[34r] Procedure site (attribute) <-2147378082>➞[33r]
  [ 35r] ➞ [ 35c]  Concept[35r] Reduction - action (qualifier value) <-2146938668>
  [ 36r] ➞ [ 36c]  Some[36r] Method (attribute) <-2147314116>➞[35r]
  [ 37r] ➞ [ 37c]  And[37r]➞[34r, 36r]
  [ 38r] ➞ [ 38c]  Some[38r] Role group (SOLOR) <-2147483593>➞[37r]
  [ 39r] ➞ [ 39c]  Concept[39r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [ 40r] ➞ [ 40c]  Some[40r] Direct morphology (attribute) <-2147378241>➞[39r]
  [ 41r] ➞ [ 41c]  And[41r]➞[40r]
  [ 42r] ➞ [ 42c]  Some[42r] Role group (SOLOR) <-2147483593>➞[41r]
  [ 43r] ➞ [ 43c]  Concept[43r] Dislocation (morphologic abnormality) <-2147448026>
  [ 44r] ➞ [ 44c]  Some[44r] Direct morphology (attribute) <-2147378241>➞[43r]
  [ 45r] ➞ [ 45c]  Concept[45r] Reduction - action (qualifier value) <-2146938668>
  [ 46r] ➞ [ 46c]  Some[46r] Method (attribute) <-2147314116>➞[45r]
  [ 47r] ➞ [ 47c]  Concept[47r] Joint structure (body structure) <-2146932341>
  [ 48r] ➞ [ 48c]  Some[48r] Procedure site - Indirect (attribute) <-2146878264>➞[47r]
  [ 49r] ➞ [ 49c]  And[49r]➞[44r, 46r, 48r]
  [ 50r] ➞ [ 50c]  Some[50r] Role group (SOLOR) <-2147483593>➞[49r]
  [ 51r] ➞ [ 51c]  Concept[51r] Fracture (morphologic abnormality) <-2146461022>
  [ 52r] ➞ [ 52c]  Some[52r] Direct morphology (attribute) <-2147378241>➞[51r]
  [ 53r] ➞ [ 53c]  Concept[53r] Open approach - access (qualifier value) <-2146941428>
  [ 54r] ➞ [ 54c]  Some[54r] Access (attribute) <-2147315914>➞[53r]
  [ 55r] ➞ [ 55c]  Concept[55r] Reduction - action (qualifier value) <-2146938668>
  [ 56r] ➞ [ 56c]  Some[56r] Method (attribute) <-2147314116>➞[55r]
  [ 57r] ➞ [ 57c]  Concept[57r] Bone structure (body structure) <-2147146938>
  [ 58r] ➞ [ 58c]  Some[58r] Procedure site - Indirect (attribute) <-2146878264>➞[57r]
  [ 59r] ➞ [ 59c]  And[59r]➞[52r, 54r, 56r, 58r]
  [ 60r] ➞ [ 60c]  Some[60r] Role group (SOLOR) <-2147483593>➞[59r]
  [ 61r] ➞ [ 61c]  Concept[61r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [ 62r] ➞ [ 62c]  Some[62r] Direct morphology (attribute) <-2147378241>➞[61r]
  [ 63r] ➞ [ 63c]  Concept[63r] Reduction - action (qualifier value) <-2146938668>
  [ 64r] ➞ [ 64c]  Some[64r] Method (attribute) <-2147314116>➞[63r]
  [ 65r] ➞ [ 65c]  Concept[65r] Bone structure (body structure) <-2147146938>
  [ 66r] ➞ [ 66c]  Some[66r] Procedure site - Direct (attribute) <-2146878287>➞[65r]
  [ 67r] ➞ [ 67c]  And[67r]➞[62r, 64r, 66r]
  [ 68r] ➞ [ 68c]  Some[68r] Role group (SOLOR) <-2147483593>➞[67r]
  [ 69r] ➞ [ 69c]  Concept[69r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [ 70r] ➞ [ 70c]  Some[70r] Direct morphology (attribute) <-2147378241>➞[69r]
  [ 71r] ➞ [ 71c]  Concept[71r] Open approach - access (qualifier value) <-2146941428>
  [ 72r] ➞ [ 72c]  Some[72r] Access (attribute) <-2147315914>➞[71r]
  [ 73r] ➞ [ 73c]  Concept[73r] Reduction - action (qualifier value) <-2146938668>
  [ 74r] ➞ [ 74c]  Some[74r] Method (attribute) <-2147314116>➞[73r]
  [ 75r] ➞ [ 75c]  Concept[75r] Joint structure (body structure) <-2146932341>
  [ 76r] ➞ [ 76c]  Some[76r] Procedure site - Direct (attribute) <-2146878287>➞[75r]
  [ 77r] ➞ [ 77c]  And[77r]➞[70r, 72r, 74r, 76r]
  [ 78r] ➞ [ 78c]  Some[78r] Role group (SOLOR) <-2147483593>➞[77r]
  [ 79r] ➞ [ 79c]  Concept[79r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [ 80r] ➞ [ 80c]  Some[80r] Direct morphology (attribute) <-2147378241>➞[79r]
  [ 81r] ➞ [ 81c]  Concept[81r] Open approach - access (qualifier value) <-2146941428>
  [ 82r] ➞ [ 82c]  Some[82r] Access (attribute) <-2147315914>➞[81r]
  [ 83r] ➞ [ 83c]  Concept[83r] Reduction - action (qualifier value) <-2146938668>
  [ 84r] ➞ [ 84c]  Some[84r] Method (attribute) <-2147314116>➞[83r]
  [ 85r] ➞ [ 85c]  Concept[85r] Bone structure (body structure) <-2147146938>
  [ 86r] ➞ [ 86c]  Some[86r] Procedure site - Indirect (attribute) <-2146878264>➞[85r]
  [ 87r] ➞ [ 87c]  And[87r]➞[80r, 82r, 84r, 86r]
  [ 88r] ➞ [ 88c]  Some[88r] Role group (SOLOR) <-2147483593>➞[87r]
  [ 89r] ➞ [ 89c]  Concept[89r] Fracture (morphologic abnormality) <-2146461022>
  [ 90r] ➞ [ 90c]  Some[90r] Direct morphology (attribute) <-2147378241>➞[89r]
  [ 91r] ➞ [ 91c]  Concept[91r] Open reduction - action (qualifier value) <-2146585859>
  [ 92r] ➞ [ 92c]  Some[92r] Method (attribute) <-2147314116>➞[91r]
  [ 93r] ➞ [ 93c]  And[93r]➞[90r, 92r]
  [ 94r] ➞ [ 94c]  Some[94r] Role group (SOLOR) <-2147483593>➞[93r]
  [ 95r] ➞ [ 95c]  Concept[95r] Dislocation (morphologic abnormality) <-2147448026>
  [ 96r] ➞ [ 96c]  Some[96r] Direct morphology (attribute) <-2147378241>➞[95r]
  [ 97r] ➞ [ 97c]  Concept[97r] Open reduction - action (qualifier value) <-2146585859>
  [ 98r] ➞ [ 98c]  Some[98r] Method (attribute) <-2147314116>➞[97r]
  [ 99r] ➞ [ 99c]  Concept[99r] Joint structure (body structure) <-2146932341>
  [100r] ➞ [100c]  Some[100r] Procedure site - Direct (attribute) <-2146878287>➞[99r]
  [101r] ➞ [101c]  And[101r]➞[96r, 98r, 100r]
  [102r] ➞ [102c]  Some[102r] Role group (SOLOR) <-2147483593>➞[101r]
  [103r] ➞ [103c]  Concept[103r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [104r] ➞ [104c]  Some[104r] Direct morphology (attribute) <-2147378241>➞[103r]
  [105r] ➞ [105c]  Concept[105r] Reduction - action (qualifier value) <-2146938668>
  [106r] ➞ [106c]  Some[106r] Method (attribute) <-2147314116>➞[105r]
  [107r] ➞ [107c]  Concept[107r] Bone structure (body structure) <-2147146938>
  [108r] ➞ [108c]  Some[108r] Procedure site - Direct (attribute) <-2146878287>➞[107r]
  [109r] ➞ [109c]  Concept[109r] Primary operation (qualifier value) <-2147302589>
  [110r] ➞ [110c]  Some[110r] Revision status (attribute) <-2146315099>➞[109r]
  [111r] ➞ [111c]  And[111r]➞[104r, 106r, 108r, 110r]
  [112r] ➞ [112c]  Some[112r] Role group (SOLOR) <-2147483593>➞[111r]
  [113r] ➞ [113c]  Concept[113r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [114r] ➞ [114c]  Some[114r] Direct morphology (attribute) <-2147378241>➞[113r]
  [115r] ➞ [115c]  Concept[115r] Open approach - access (qualifier value) <-2146941428>
  [116r] ➞ [116c]  Some[116r] Access (attribute) <-2147315914>➞[115r]
  [117r] ➞ [117c]  Concept[117r] Reduction - action (qualifier value) <-2146938668>
  [118r] ➞ [118c]  Some[118r] Method (attribute) <-2147314116>➞[117r]
  [119r] ➞ [119c]  Concept[119r] Joint structure (body structure) <-2146932341>
  [120r] ➞ [120c]  Some[120r] Procedure site - Direct (attribute) <-2146878287>➞[119r]
  [121r] ➞ [121c]  Concept[121r] Primary operation (qualifier value) <-2147302589>
  [122r] ➞ [122c]  Some[122r] Revision status (attribute) <-2146315099>➞[121r]
  [123r] ➞ [123c]  And[123r]➞[114r, 116r, 118r, 120r, 122r]
  [124r] ➞ [124c]  Some[124r] Role group (SOLOR) <-2147483593>➞[123r]
  [125r] ➞ [125c]  Concept[125r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [126r] ➞ [126c]  Some[126r] Direct morphology (attribute) <-2147378241>➞[125r]
  [127r] ➞ [127c]  Concept[127r] Open approach - access (qualifier value) <-2146941428>
  [128r] ➞ [128c]  Some[128r] Access (attribute) <-2147315914>➞[127r]
  [129r] ➞ [129c]  Concept[129r] Reduction - action (qualifier value) <-2146938668>
  [130r] ➞ [130c]  Some[130r] Method (attribute) <-2147314116>➞[129r]
  [131r] ➞ [131c]  Concept[131r] Bone structure (body structure) <-2147146938>
  [132r] ➞ [132c]  Some[132r] Procedure site - Indirect (attribute) <-2146878264>➞[131r]
  [133r] ➞ [133c]  Concept[133r] Primary operation (qualifier value) <-2147302589>
  [134r] ➞ [134c]  Some[134r] Revision status (attribute) <-2146315099>➞[133r]
  [135r] ➞ [135c]  And[135r]➞[126r, 128r, 130r, 132r, 134r]
  [136r] ➞ [136c]  Some[136r] Role group (SOLOR) <-2147483593>➞[135r]
  [137r] ➞ [137c]  Concept[137r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [138r] ➞ [138c]  Some[138r] Direct morphology (attribute) <-2147378241>➞[137r]
  [139r] ➞ [139c]  Concept[139r] Open reduction - action (qualifier value) <-2146585859>
  [140r] ➞ [140c]  Some[140r] Method (attribute) <-2147314116>➞[139r]
  [141r] ➞ [141c]  Concept[141r] Joint structure (body structure) <-2146932341>
  [142r] ➞ [142c]  Some[142r] Procedure site - Direct (attribute) <-2146878287>➞[141r]
  [143r] ➞ [143c]  And[143r]➞[138r, 140r, 142r]
  [144r] ➞ [144c]  Some[144r] Role group (SOLOR) <-2147483593>➞[143r]
  [145r] ➞ [145c]  Concept[145r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [146r] ➞ [146c]  Some[146r] Direct morphology (attribute) <-2147378241>➞[145r]
  [147r] ➞ [147c]  Concept[147r] Reduction - action (qualifier value) <-2146938668>
  [148r] ➞ [148c]  Some[148r] Method (attribute) <-2147314116>➞[147r]
  [149r] ➞ [149c]  Concept[149r] Joint structure (body structure) <-2146932341>
  [150r] ➞ [150c]  Some[150r] Procedure site - Direct (attribute) <-2146878287>➞[149r]
  [151r] ➞ [151c]  And[151r]➞[146r, 148r, 150r]
  [152r] ➞ [152c]  Some[152r] Role group (SOLOR) <-2147483593>➞[151r]
  [153r] ➞ [153c]  Concept[153r] Fracture-dislocation (morphologic abnormality) <-2147403668>
  [154r] ➞ [154c]  Some[154r] Direct morphology (attribute) <-2147378241>➞[153r]
  [155r] ➞ [155c]  Concept[155r] Open reduction - action (qualifier value) <-2146585859>
  [156r] ➞ [156c]  Some[156r] Method (attribute) <-2147314116>➞[155r]
  [157r] ➞ [157c]  Concept[157r] Bone structure (body structure) <-2147146938>
  [158r] ➞ [158c]  Some[158r] Procedure site - Direct (attribute) <-2146878287>➞[157r]
  [159r] ➞ [159c]  Concept[159r] Primary operation (qualifier value) <-2147302589>
  [160r] ➞ [160c]  Some[160r] Revision status (attribute) <-2146315099>➞[159r]
  [161r] ➞ [161c]  And[161r]➞[154r, 156r, 158r, 160r]
  [162r] ➞ [162c]  Some[162r] Role group (SOLOR) <-2147483593>➞[161r]
  [163r] ➞  ∅ 
  [164r] ➞  ∅ 
  [165r] ➞  ∅ 
  [166r] ➞  ∅ 
  [167r] ➞  ∅ 
  [168r] ➞  ∅ 
  [169r] ➞  ∅ 
  [170r] ➞  ∅ 
  [171r] ➞  ∅ 
  [172r] ➞  ∅ 
  [173r] ➞  ∅ 
  [174r] ➞  ∅ 
  [175r] ➞  ∅ 
  [176r] ➞  ∅ 
  [177r] ➞  ∅ 
  [178r] ➞  ∅ 
  [179r] ➞  ∅ 
  [180r] ➞  ∅ 
  [181r] ➞  ∅ 
  [182r] ➞  ∅ 
  [183r] ➞ [163c]* Concept[183r] Reduction of fracture (procedure) <-2147008610>
  [184r] ➞ [164c]* Concept[184r] Primary open reduction of fracture dislocation (procedure) <-2146647907>
  [185r] ➞ [165c]* And[185r]➞[4r, 8r, 12r, 16r, 20r, 24r, 28r, 32r, 38r, 42r, 50r, 60r, 68r, 78r, 88r, 94r, 102r, 112r, 124r, 136r, 144r, 152r, 162r, 172r, 182r, 183r, 184r]
  [186r] ➞ [166c]* Necessary[186r]➞[185r]

Additions: 

  Some[172r] Role group (SOLOR) <-2147483593>➞[171r]
    And[171r]➞[164r, 166r, 168r, 170r]
        Some[164r] Direct morphology (attribute) <-2147378241>➞[163r]
            Concept[163r] Dislocation (morphologic abnormality) <-2147448026>
        Some[166r] Method (attribute) <-2147314116>➞[165r]
            Concept[165r] Open reduction - action (qualifier value) <-2146585859>
        Some[168r] Procedure site - Direct (attribute) <-2146878287>➞[167r]
            Concept[167r] Joint structure (body structure) <-2146932341>
        Some[170r] Revision status (attribute) <-2146315099>➞[169r]
            Concept[169r] Primary operation (qualifier value) <-2147302589>

  Some[182r] Role group (SOLOR) <-2147483593>➞[181r]
    And[181r]➞[174r, 176r, 178r, 180r]
        Some[174r] Direct morphology (attribute) <-2147378241>➞[173r]
            Concept[173r] Fracture (morphologic abnormality) <-2146461022>
        Some[176r] Method (attribute) <-2147314116>➞[175r]
            Concept[175r] Open reduction - action (qualifier value) <-2146585859>
        Some[178r] Procedure site - Direct (attribute) <-2146878287>➞[177r]
            Concept[177r] Bone structure (body structure) <-2147146938>
        Some[180r] Revision status (attribute) <-2146315099>➞[179r]
            Concept[179r] Primary operation (qualifier value) <-2147302589>


Deletions: 


Shared relationship roots: 

  Concept[163] Reduction of fracture (procedure) <-2147008610>

  Concept[164] Primary open reduction of fracture dislocation (procedure) <-2146647907>

  Some[8] Role group (SOLOR) <-2147483593>➞[7]
    And[7]➞[6]
        Some[6] Direct morphology (attribute) <-2147378241>➞[5]
            Concept[5] Dislocation (morphologic abnormality) <-2147448026>

  Some[42] Role group (SOLOR) <-2147483593>➞[41]
    And[41]➞[40]
        Some[40] Direct morphology (attribute) <-2147378241>➞[39]
            Concept[39] Fracture-dislocation (morphologic abnormality) <-2147403668>

  Some[20] Role group (SOLOR) <-2147483593>➞[19]
    And[19]➞[18]
        Some[18] Direct morphology (attribute) <-2147378241>➞[17]
            Concept[17] Dislocation of joint (disorder) <-2147196846>

  Some[4] Role group (SOLOR) <-2147483593>➞[3]
    And[3]➞[2]
        Some[2] Access (attribute) <-2147315914>➞[1]
            Concept[1] Open approach - access (qualifier value) <-2146941428>

  Some[24] Role group (SOLOR) <-2147483593>➞[23]
    And[23]➞[22]
        Some[22] Revision status (attribute) <-2146315099>➞[21]
            Concept[21] Primary operation (qualifier value) <-2147302589>

  Some[28] Role group (SOLOR) <-2147483593>➞[27]
    And[27]➞[26]
        Some[26] Direct morphology (attribute) <-2147378241>➞[25]
            Concept[25] Traumatic dislocation (morphologic abnormality) <-2146977295>

  Some[32] Role group (SOLOR) <-2147483593>➞[31]
    And[31]➞[30]
        Some[30] Method (attribute) <-2147314116>➞[29]
            Concept[29] Surgical action (qualifier value) <-2146940928>

  Some[16] Role group (SOLOR) <-2147483593>➞[15]
    And[15]➞[14]
        Some[14] Method (attribute) <-2147314116>➞[13]
            Concept[13] Surgical repair - action (qualifier value) <-2146939778>

  Some[12] Role group (SOLOR) <-2147483593>➞[11]
    And[11]➞[10]
        Some[10] Revision status (attribute) <-2146315099>➞[9]
            Concept[9] Principal (qualifier value) <-2146603744>

  Some[38] Role group (SOLOR) <-2147483593>➞[37]
    And[37]➞[34, 36]
        Some[34] Procedure site (attribute) <-2147378082>➞[33]
            Concept[33] Joint structure (body structure) <-2146932341>
        Some[36] Method (attribute) <-2147314116>➞[35]
            Concept[35] Reduction - action (qualifier value) <-2146938668>

  Some[94] Role group (SOLOR) <-2147483593>➞[93]
    And[93]➞[90, 92]
        Some[90] Direct morphology (attribute) <-2147378241>➞[89]
            Concept[89] Fracture (morphologic abnormality) <-2146461022>
        Some[92] Method (attribute) <-2147314116>➞[91]
            Concept[91] Open reduction - action (qualifier value) <-2146585859>

  Some[102] Role group (SOLOR) <-2147483593>➞[101]
    And[101]➞[96, 98, 100]
        Some[96] Direct morphology (attribute) <-2147378241>➞[95]
            Concept[95] Dislocation (morphologic abnormality) <-2147448026>
        Some[98] Method (attribute) <-2147314116>➞[97]
            Concept[97] Open reduction - action (qualifier value) <-2146585859>
        Some[100] Procedure site - Direct (attribute) <-2146878287>➞[99]
            Concept[99] Joint structure (body structure) <-2146932341>

  Some[50] Role group (SOLOR) <-2147483593>➞[49]
    And[49]➞[44, 46, 48]
        Some[44] Direct morphology (attribute) <-2147378241>➞[43]
            Concept[43] Dislocation (morphologic abnormality) <-2147448026>
        Some[46] Method (attribute) <-2147314116>➞[45]
            Concept[45] Reduction - action (qualifier value) <-2146938668>
        Some[48] Procedure site - Indirect (attribute) <-2146878264>➞[47]
            Concept[47] Joint structure (body structure) <-2146932341>

  Some[152] Role group (SOLOR) <-2147483593>➞[151]
    And[151]➞[146, 148, 150]
        Some[146] Direct morphology (attribute) <-2147378241>➞[145]
            Concept[145] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[148] Method (attribute) <-2147314116>➞[147]
            Concept[147] Reduction - action (qualifier value) <-2146938668>
        Some[150] Procedure site - Direct (attribute) <-2146878287>➞[149]
            Concept[149] Joint structure (body structure) <-2146932341>

  Some[144] Role group (SOLOR) <-2147483593>➞[143]
    And[143]➞[138, 140, 142]
        Some[138] Direct morphology (attribute) <-2147378241>➞[137]
            Concept[137] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[140] Method (attribute) <-2147314116>➞[139]
            Concept[139] Open reduction - action (qualifier value) <-2146585859>
        Some[142] Procedure site - Direct (attribute) <-2146878287>➞[141]
            Concept[141] Joint structure (body structure) <-2146932341>

  Some[68] Role group (SOLOR) <-2147483593>➞[67]
    And[67]➞[62, 64, 66]
        Some[62] Direct morphology (attribute) <-2147378241>➞[61]
            Concept[61] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[64] Method (attribute) <-2147314116>➞[63]
            Concept[63] Reduction - action (qualifier value) <-2146938668>
        Some[66] Procedure site - Direct (attribute) <-2146878287>➞[65]
            Concept[65] Bone structure (body structure) <-2147146938>

  Some[78] Role group (SOLOR) <-2147483593>➞[77]
    And[77]➞[70, 72, 74, 76]
        Some[70] Direct morphology (attribute) <-2147378241>➞[69]
            Concept[69] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[72] Access (attribute) <-2147315914>➞[71]
            Concept[71] Open approach - access (qualifier value) <-2146941428>
        Some[74] Method (attribute) <-2147314116>➞[73]
            Concept[73] Reduction - action (qualifier value) <-2146938668>
        Some[76] Procedure site - Direct (attribute) <-2146878287>➞[75]
            Concept[75] Joint structure (body structure) <-2146932341>

  Some[112] Role group (SOLOR) <-2147483593>➞[111]
    And[111]➞[104, 106, 108, 110]
        Some[104] Direct morphology (attribute) <-2147378241>➞[103]
            Concept[103] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[106] Method (attribute) <-2147314116>➞[105]
            Concept[105] Reduction - action (qualifier value) <-2146938668>
        Some[108] Procedure site - Direct (attribute) <-2146878287>➞[107]
            Concept[107] Bone structure (body structure) <-2147146938>
        Some[110] Revision status (attribute) <-2146315099>➞[109]
            Concept[109] Primary operation (qualifier value) <-2147302589>

  Some[162] Role group (SOLOR) <-2147483593>➞[161]
    And[161]➞[154, 156, 158, 160]
        Some[154] Direct morphology (attribute) <-2147378241>➞[153]
            Concept[153] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[156] Method (attribute) <-2147314116>➞[155]
            Concept[155] Open reduction - action (qualifier value) <-2146585859>
        Some[158] Procedure site - Direct (attribute) <-2146878287>➞[157]
            Concept[157] Bone structure (body structure) <-2147146938>
        Some[160] Revision status (attribute) <-2146315099>➞[159]
            Concept[159] Primary operation (qualifier value) <-2147302589>

  Some[88] Role group (SOLOR) <-2147483593>➞[87]
    And[87]➞[80, 82, 84, 86]
        Some[80] Direct morphology (attribute) <-2147378241>➞[79]
            Concept[79] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[82] Access (attribute) <-2147315914>➞[81]
            Concept[81] Open approach - access (qualifier value) <-2146941428>
        Some[84] Method (attribute) <-2147314116>➞[83]
            Concept[83] Reduction - action (qualifier value) <-2146938668>
        Some[86] Procedure site - Indirect (attribute) <-2146878264>➞[85]
            Concept[85] Bone structure (body structure) <-2147146938>

  Some[60] Role group (SOLOR) <-2147483593>➞[59]
    And[59]➞[52, 54, 56, 58]
        Some[52] Direct morphology (attribute) <-2147378241>➞[51]
            Concept[51] Fracture (morphologic abnormality) <-2146461022>
        Some[54] Access (attribute) <-2147315914>➞[53]
            Concept[53] Open approach - access (qualifier value) <-2146941428>
        Some[56] Method (attribute) <-2147314116>➞[55]
            Concept[55] Reduction - action (qualifier value) <-2146938668>
        Some[58] Procedure site - Indirect (attribute) <-2146878264>➞[57]
            Concept[57] Bone structure (body structure) <-2147146938>

  Some[124] Role group (SOLOR) <-2147483593>➞[123]
    And[123]➞[114, 116, 118, 120, 122]
        Some[114] Direct morphology (attribute) <-2147378241>➞[113]
            Concept[113] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[116] Access (attribute) <-2147315914>➞[115]
            Concept[115] Open approach - access (qualifier value) <-2146941428>
        Some[118] Method (attribute) <-2147314116>➞[117]
            Concept[117] Reduction - action (qualifier value) <-2146938668>
        Some[120] Procedure site - Direct (attribute) <-2146878287>➞[119]
            Concept[119] Joint structure (body structure) <-2146932341>
        Some[122] Revision status (attribute) <-2146315099>➞[121]
            Concept[121] Primary operation (qualifier value) <-2147302589>

  Some[136] Role group (SOLOR) <-2147483593>➞[135]
    And[135]➞[126, 128, 130, 132, 134]
        Some[126] Direct morphology (attribute) <-2147378241>➞[125]
            Concept[125] Fracture-dislocation (morphologic abnormality) <-2147403668>
        Some[128] Access (attribute) <-2147315914>➞[127]
            Concept[127] Open approach - access (qualifier value) <-2146941428>
        Some[130] Method (attribute) <-2147314116>➞[129]
            Concept[129] Reduction - action (qualifier value) <-2146938668>
        Some[132] Procedure site - Indirect (attribute) <-2146878264>➞[131]
            Concept[131] Bone structure (body structure) <-2147146938>
        Some[134] Revision status (attribute) <-2146315099>➞[133]
            Concept[133] Primary operation (qualifier value) <-2147302589>


New relationship roots: 

  Some[172] Role group (SOLOR) <-2147483593>➞[171]
    And[171]➞[164, 166, 168, 170]
        Some[164] Direct morphology (attribute) <-2147378241>➞[163]
            Concept[163] Dislocation (morphologic abnormality) <-2147448026>
        Some[166] Method (attribute) <-2147314116>➞[165]
            Concept[165] Open reduction - action (qualifier value) <-2146585859>
        Some[168] Procedure site - Direct (attribute) <-2146878287>➞[167]
            Concept[167] Joint structure (body structure) <-2146932341>
        Some[170] Revision status (attribute) <-2146315099>➞[169]
            Concept[169] Primary operation (qualifier value) <-2147302589>

  Some[182] Role group (SOLOR) <-2147483593>➞[181]
    And[181]➞[174, 176, 178, 180]
        Some[174] Direct morphology (attribute) <-2147378241>➞[173]
            Concept[173] Fracture (morphologic abnormality) <-2146461022>
        Some[176] Method (attribute) <-2147314116>➞[175]
            Concept[175] Open reduction - action (qualifier value) <-2146585859>
        Some[178] Procedure site - Direct (attribute) <-2146878287>➞[177]
            Concept[177] Bone structure (body structure) <-2147146938>
        Some[180] Revision status (attribute) <-2146315099>➞[179]
            Concept[179] Primary operation (qualifier value) <-2147302589>


Deleted relationship roots: 


Merged expression: 

Root[186m]➞[185m]
    Necessary[185m]➞[184m]
        And[184m]➞[3m, 7m, 11m, 15m, 19m, 23m, 27m, 31m, 37m, 41m, 49m, 59m, 67m, 77m, 87m, 93m, 101m, 111m, 123m, 135m, 143m, 151m, 161m, 171m, 181m, 182m, 183m]
            Some[3m] Role group (SOLOR) <-2147483593>➞[2m]
                And[2m]➞[1m]
                    Some[1m] Access (attribute) <-2147315914>➞[0m]
                        Concept[0m] Open approach - access (qualifier value) <-2146941428>
            Some[7m] Role group (SOLOR) <-2147483593>➞[6m]
                And[6m]➞[5m]
                    Some[5m] Direct morphology (attribute) <-2147378241>➞[4m]
                        Concept[4m] Dislocation (morphologic abnormality) <-2147448026>
            Some[11m] Role group (SOLOR) <-2147483593>➞[10m]
                And[10m]➞[9m]
                    Some[9m] Revision status (attribute) <-2146315099>➞[8m]
                        Concept[8m] Principal (qualifier value) <-2146603744>
            Some[15m] Role group (SOLOR) <-2147483593>➞[14m]
                And[14m]➞[13m]
                    Some[13m] Method (attribute) <-2147314116>➞[12m]
                        Concept[12m] Surgical repair - action (qualifier value) <-2146939778>
            Some[19m] Role group (SOLOR) <-2147483593>➞[18m]
                And[18m]➞[17m]
                    Some[17m] Direct morphology (attribute) <-2147378241>➞[16m]
                        Concept[16m] Dislocation of joint (disorder) <-2147196846>
            Some[23m] Role group (SOLOR) <-2147483593>➞[22m]
                And[22m]➞[21m]
                    Some[21m] Revision status (attribute) <-2146315099>➞[20m]
                        Concept[20m] Primary operation (qualifier value) <-2147302589>
            Some[27m] Role group (SOLOR) <-2147483593>➞[26m]
                And[26m]➞[25m]
                    Some[25m] Direct morphology (attribute) <-2147378241>➞[24m]
                        Concept[24m] Traumatic dislocation (morphologic abnormality) <-2146977295>
            Some[31m] Role group (SOLOR) <-2147483593>➞[30m]
                And[30m]➞[29m]
                    Some[29m] Method (attribute) <-2147314116>➞[28m]
                        Concept[28m] Surgical action (qualifier value) <-2146940928>
            Some[37m] Role group (SOLOR) <-2147483593>➞[36m]
                And[36m]➞[33m, 35m]
                    Some[33m] Procedure site (attribute) <-2147378082>➞[32m]
                        Concept[32m] Joint structure (body structure) <-2146932341>
                    Some[35m] Method (attribute) <-2147314116>➞[34m]
                        Concept[34m] Reduction - action (qualifier value) <-2146938668>
            Some[41m] Role group (SOLOR) <-2147483593>➞[40m]
                And[40m]➞[39m]
                    Some[39m] Direct morphology (attribute) <-2147378241>➞[38m]
                        Concept[38m] Fracture-dislocation (morphologic abnormality) <-2147403668>
            Some[49m] Role group (SOLOR) <-2147483593>➞[48m]
                And[48m]➞[43m, 45m, 47m]
                    Some[43m] Direct morphology (attribute) <-2147378241>➞[42m]
                        Concept[42m] Dislocation (morphologic abnormality) <-2147448026>
                    Some[45m] Method (attribute) <-2147314116>➞[44m]
                        Concept[44m] Reduction - action (qualifier value) <-2146938668>
                    Some[47m] Procedure site - Indirect (attribute) <-2146878264>➞[46m]
                        Concept[46m] Joint structure (body structure) <-2146932341>
            Some[59m] Role group (SOLOR) <-2147483593>➞[58m]
                And[58m]➞[51m, 53m, 55m, 57m]
                    Some[51m] Direct morphology (attribute) <-2147378241>➞[50m]
                        Concept[50m] Fracture (morphologic abnormality) <-2146461022>
                    Some[53m] Access (attribute) <-2147315914>➞[52m]
                        Concept[52m] Open approach - access (qualifier value) <-2146941428>
                    Some[55m] Method (attribute) <-2147314116>➞[54m]
                        Concept[54m] Reduction - action (qualifier value) <-2146938668>
                    Some[57m] Procedure site - Indirect (attribute) <-2146878264>➞[56m]
                        Concept[56m] Bone structure (body structure) <-2147146938>
            Some[67m] Role group (SOLOR) <-2147483593>➞[66m]
                And[66m]➞[61m, 63m, 65m]
                    Some[61m] Direct morphology (attribute) <-2147378241>➞[60m]
                        Concept[60m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[63m] Method (attribute) <-2147314116>➞[62m]
                        Concept[62m] Reduction - action (qualifier value) <-2146938668>
                    Some[65m] Procedure site - Direct (attribute) <-2146878287>➞[64m]
                        Concept[64m] Bone structure (body structure) <-2147146938>
            Some[77m] Role group (SOLOR) <-2147483593>➞[76m]
                And[76m]➞[69m, 71m, 73m, 75m]
                    Some[69m] Direct morphology (attribute) <-2147378241>➞[68m]
                        Concept[68m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[71m] Access (attribute) <-2147315914>➞[70m]
                        Concept[70m] Open approach - access (qualifier value) <-2146941428>
                    Some[73m] Method (attribute) <-2147314116>➞[72m]
                        Concept[72m] Reduction - action (qualifier value) <-2146938668>
                    Some[75m] Procedure site - Direct (attribute) <-2146878287>➞[74m]
                        Concept[74m] Joint structure (body structure) <-2146932341>
            Some[87m] Role group (SOLOR) <-2147483593>➞[86m]
                And[86m]➞[79m, 81m, 83m, 85m]
                    Some[79m] Direct morphology (attribute) <-2147378241>➞[78m]
                        Concept[78m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[81m] Access (attribute) <-2147315914>➞[80m]
                        Concept[80m] Open approach - access (qualifier value) <-2146941428>
                    Some[83m] Method (attribute) <-2147314116>➞[82m]
                        Concept[82m] Reduction - action (qualifier value) <-2146938668>
                    Some[85m] Procedure site - Indirect (attribute) <-2146878264>➞[84m]
                        Concept[84m] Bone structure (body structure) <-2147146938>
            Some[93m] Role group (SOLOR) <-2147483593>➞[92m]
                And[92m]➞[89m, 91m]
                    Some[89m] Direct morphology (attribute) <-2147378241>➞[88m]
                        Concept[88m] Fracture (morphologic abnormality) <-2146461022>
                    Some[91m] Method (attribute) <-2147314116>➞[90m]
                        Concept[90m] Open reduction - action (qualifier value) <-2146585859>
            Some[101m] Role group (SOLOR) <-2147483593>➞[100m]
                And[100m]➞[95m, 97m, 99m]
                    Some[95m] Direct morphology (attribute) <-2147378241>➞[94m]
                        Concept[94m] Dislocation (morphologic abnormality) <-2147448026>
                    Some[97m] Method (attribute) <-2147314116>➞[96m]
                        Concept[96m] Open reduction - action (qualifier value) <-2146585859>
                    Some[99m] Procedure site - Direct (attribute) <-2146878287>➞[98m]
                        Concept[98m] Joint structure (body structure) <-2146932341>
            Some[111m] Role group (SOLOR) <-2147483593>➞[110m]
                And[110m]➞[103m, 105m, 107m, 109m]
                    Some[103m] Direct morphology (attribute) <-2147378241>➞[102m]
                        Concept[102m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[105m] Method (attribute) <-2147314116>➞[104m]
                        Concept[104m] Reduction - action (qualifier value) <-2146938668>
                    Some[107m] Procedure site - Direct (attribute) <-2146878287>➞[106m]
                        Concept[106m] Bone structure (body structure) <-2147146938>
                    Some[109m] Revision status (attribute) <-2146315099>➞[108m]
                        Concept[108m] Primary operation (qualifier value) <-2147302589>
            Some[123m] Role group (SOLOR) <-2147483593>➞[122m]
                And[122m]➞[113m, 115m, 117m, 119m, 121m]
                    Some[113m] Direct morphology (attribute) <-2147378241>➞[112m]
                        Concept[112m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[115m] Access (attribute) <-2147315914>➞[114m]
                        Concept[114m] Open approach - access (qualifier value) <-2146941428>
                    Some[117m] Method (attribute) <-2147314116>➞[116m]
                        Concept[116m] Reduction - action (qualifier value) <-2146938668>
                    Some[119m] Procedure site - Direct (attribute) <-2146878287>➞[118m]
                        Concept[118m] Joint structure (body structure) <-2146932341>
                    Some[121m] Revision status (attribute) <-2146315099>➞[120m]
                        Concept[120m] Primary operation (qualifier value) <-2147302589>
            Some[135m] Role group (SOLOR) <-2147483593>➞[134m]
                And[134m]➞[125m, 127m, 129m, 131m, 133m]
                    Some[125m] Direct morphology (attribute) <-2147378241>➞[124m]
                        Concept[124m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[127m] Access (attribute) <-2147315914>➞[126m]
                        Concept[126m] Open approach - access (qualifier value) <-2146941428>
                    Some[129m] Method (attribute) <-2147314116>➞[128m]
                        Concept[128m] Reduction - action (qualifier value) <-2146938668>
                    Some[131m] Procedure site - Indirect (attribute) <-2146878264>➞[130m]
                        Concept[130m] Bone structure (body structure) <-2147146938>
                    Some[133m] Revision status (attribute) <-2146315099>➞[132m]
                        Concept[132m] Primary operation (qualifier value) <-2147302589>
            Some[143m] Role group (SOLOR) <-2147483593>➞[142m]
                And[142m]➞[137m, 139m, 141m]
                    Some[137m] Direct morphology (attribute) <-2147378241>➞[136m]
                        Concept[136m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[139m] Method (attribute) <-2147314116>➞[138m]
                        Concept[138m] Open reduction - action (qualifier value) <-2146585859>
                    Some[141m] Procedure site - Direct (attribute) <-2146878287>➞[140m]
                        Concept[140m] Joint structure (body structure) <-2146932341>
            Some[151m] Role group (SOLOR) <-2147483593>➞[150m]
                And[150m]➞[145m, 147m, 149m]
                    Some[145m] Direct morphology (attribute) <-2147378241>➞[144m]
                        Concept[144m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[147m] Method (attribute) <-2147314116>➞[146m]
                        Concept[146m] Reduction - action (qualifier value) <-2146938668>
                    Some[149m] Procedure site - Direct (attribute) <-2146878287>➞[148m]
                        Concept[148m] Joint structure (body structure) <-2146932341>
            Some[161m] Role group (SOLOR) <-2147483593>➞[160m]
                And[160m]➞[153m, 155m, 157m, 159m]
                    Some[153m] Direct morphology (attribute) <-2147378241>➞[152m]
                        Concept[152m] Fracture-dislocation (morphologic abnormality) <-2147403668>
                    Some[155m] Method (attribute) <-2147314116>➞[154m]
                        Concept[154m] Open reduction - action (qualifier value) <-2146585859>
                    Some[157m] Procedure site - Direct (attribute) <-2146878287>➞[156m]
                        Concept[156m] Bone structure (body structure) <-2147146938>
                    Some[159m] Revision status (attribute) <-2146315099>➞[158m]
                        Concept[158m] Primary operation (qualifier value) <-2147302589>
            Some[171m] Role group (SOLOR) <-2147483593>➞[170m]
                And[170m]➞[163m, 165m, 167m, 169m]
                    Some[163m] Direct morphology (attribute) <-2147378241>➞[162m]
                        Concept[162m] Dislocation (morphologic abnormality) <-2147448026>
                    Some[165m] Method (attribute) <-2147314116>➞[164m]
                        Concept[164m] Open reduction - action (qualifier value) <-2146585859>
                    Some[167m] Procedure site - Direct (attribute) <-2146878287>➞[166m]
                        Concept[166m] Joint structure (body structure) <-2146932341>
                    Some[169m] Revision status (attribute) <-2146315099>➞[168m]
                        Concept[168m] Primary operation (qualifier value) <-2147302589>
            Some[181m] Role group (SOLOR) <-2147483593>➞[180m]
                And[180m]➞[173m, 175m, 177m, 179m]
                    Some[173m] Direct morphology (attribute) <-2147378241>➞[172m]
                        Concept[172m] Fracture (morphologic abnormality) <-2146461022>
                    Some[175m] Method (attribute) <-2147314116>➞[174m]
                        Concept[174m] Open reduction - action (qualifier value) <-2146585859>
                    Some[177m] Procedure site - Direct (attribute) <-2146878287>➞[176m]
                        Concept[176m] Bone structure (body structure) <-2147146938>
                    Some[179m] Revision status (attribute) <-2146315099>➞[178m]
                        Concept[178m] Primary operation (qualifier value) <-2147302589>
            Concept[182m] Reduction of fracture (procedure) <-2147008610>
            Concept[183m] Primary open reduction of fracture dislocation (procedure) <-2146647907>


solution{true  s:226, [0:0, 1:1, 2:2, 3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9, 10:10, 11:11, 12:12, 13:13, 14:14, 15:15, 16:16, 17:17, 18:18, 19:19, 20:20, 21:21, 22:22, 23:23, 24:24, 25:25, 26:26, 27:27, 28:28, 29:29, 30:30, 31:31, 32:32, 33:33, 34:34, 35:35, 36:36, 37:37, 38:38, 39:39, 40:40, 41:41, 42:42, 43:43, 44:44, 45:45, 46:46, 47:47, 48:48, 49:49, 50:50, 51:51, 52:52, 53:53, 54:54, 55:55, 56:56, 57:57, 58:58, 59:59, 60:60, 61:61, 62:62, 63:63, 64:64, 65:65, 66:66, 67:67, 68:68, 69:69, 70:70, 71:71, 72:72, 73:73, 74:74, 75:75, 76:76, 77:77, 78:78, 79:79, 80:80, 81:81, 82:82, 83:83, 84:84, 85:85, 86:86, 87:87, 88:88, 89:89, 90:90, 91:91, 92:92, 93:93, 94:94, 95:95, 96:96, 97:97, 98:98, 99:99, 100:100, 101:101, 102:102, 103:103, 104:104, 105:105, 106:106, 107:107, 108:108, 109:109, 110:110, 111:111, 112:112, 113:113, 114:114, 115:115, 116:116, 117:117, 118:118, 119:119, 120:120, 121:121, 122:122, 123:123, 124:124, 125:125, 126:126, 127:127, 128:128, 129:129, 130:130, 131:131, 132:132, 133:133, 134:134, 135:135, 136:136, 137:137, 138:138, 139:139, 140:140, 141:141, 142:142, 143:143, 144:144, 145:145, 146:146, 147:147, 148:148, 149:149, 150:150, 151:151, 152:152, 153:153, 154:154, 155:155, 156:156, 157:157, 158:158, 159:159, 160:160, 161:161, 162:162, 163:-1, 164:-1, 165:-1, 166:-1, 167:-1, 168:-1, 169:-1, 170:-1, 171:-1, 172:-1, 173:-1, 174:-1, 175:-1, 176:-1, 177:-1, 178:-1, 179:-1, 180:-1, 181:-1, 182:-1, 183:163, 184:164, 185:165, 186:166]}
 
 *
 */
public class CorrelationProblem5 {

    static LogicalExpression getReferenceExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();

        NecessarySet(And(
                SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("a1f2189b-678b-317c-b2ff-d2c42de59bc4"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("c8d13afd-3282-34c9-9ff8-fa39a464784f"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("2ea59409-1d4e-32e0-907c-3e31c67a8af6"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("39dfcda1-7381-3d64-9d6f-408cb2b46a1e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("c9230856-a645-31d4-bcbb-1d69e8bccbac"), leb)),
                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("c9230856-a645-31d4-bcbb-1d69e8bccbac"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"), And(
                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                ConceptAssertion(Get.concept("c9230856-a645-31d4-bcbb-1d69e8bccbac"), leb)),
                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)),
                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)),
                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb))
                )
                ),
                 ConceptAssertion(Get.concept("6757bbb3-b2b1-327f-bc09-96ae3578b2cc"), leb),
                ConceptAssertion(Get.concept("8be65040-3e28-397a-a31e-bbb2ba27fce2"), leb)
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
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("a1f2189b-678b-317c-b2ff-d2c42de59bc4"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("c8d13afd-3282-34c9-9ff8-fa39a464784f"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("2ea59409-1d4e-32e0-907c-3e31c67a8af6"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("39dfcda1-7381-3d64-9d6f-408cb2b46a1e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("723458e1-9c77-3b58-88e4-f3e4701c7466"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("78dd0334-4b9e-3c26-9266-356f8c5c43ed"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("c9230856-a645-31d4-bcbb-1d69e8bccbac"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("c9230856-a645-31d4-bcbb-1d69e8bccbac"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        ),
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("3f5a4b8c-923b-3df5-9362-67881b729394"),
                                                ConceptAssertion(Get.concept("59943708-d6cf-3bfc-b2f5-325a47b40c84"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("ac38de9e-2c97-37ed-a3e2-365a87ba1730"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("d391e844-de89-3010-929e-73d030ff1bed"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("5bf6d9bf-89ae-3e58-a7c1-45436b43268e"), leb)
                                        )
                                )
                        ),
                        SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("f28dd2fb-7573-3c53-b42a-c8212c946738"),
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fc8344cd-a2e6-3ccd-942f-e8d852816139"), leb)
                                        ),
                                        SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                        SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                        ConceptAssertion(Get.concept("6757bbb3-b2b1-327f-bc09-96ae3578b2cc"), leb),
                        ConceptAssertion(Get.concept("8be65040-3e28-397a-a31e-bbb2ba27fce2"), leb)
                )
        );
        return leb.build();
    }

}
