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
 *
 *
 *
 * New isomorphic record: 21792 Isomorphic Analysis for:Primary open reduction
 * of fracture dislocation and functional bracing (procedure)
 * eedd5957-f508-3105-bf54-57f06c650f48
 *
 * Reference expression:
 *
 * Root[210r]➞[209r] Necessary[209r]➞[208r] And[208r]➞[3r, 7r, 11r, 15r, 19r,
 * 23r, 27r, 31r, 37r, 41r, 49r, 59r, 67r, 77r, 87r, 97r, 103r, 111r, 119r,
 * 129r, 141r, 153r, 161r, 171r, 179r, 189r, 199r, 211r, 200r, 201r, 202r, 203r,
 * 204r, 205r, 206r, 207r] Some[3r] Role group (SOLOR) <-2147483593>➞[2r]
 * And[2r]➞[1r] Some[1r] Access (attribute) <-2147315914>➞[0r] Concept[0r] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[7r] Role group (SOLOR) <-2147483593>➞[6r] And[6r]➞[5r] Some[5r] Direct
 * morphology (attribute) <-2147378241>➞[4r] Concept[4r] Dislocation
 * (morphologic abnormality) <-2147448026>
 * Some[11r] Role group (SOLOR) <-2147483593>➞[10r] And[10r]➞[9r] Some[9r]
 * Revision status (attribute) <-2146315099>➞[8r] Concept[8r] Principal
 * (qualifier value) <-2146603744>
 * Some[15r] Role group (SOLOR) <-2147483593>➞[14r] And[14r]➞[13r] Some[13r]
 * Method (attribute) <-2147314116>➞[12r] Concept[12r] Surgical repair - action
 * (qualifier value) <-2146939778>
 * Some[19r] Role group (SOLOR) <-2147483593>➞[18r] And[18r]➞[17r] Some[17r]
 * Direct morphology (attribute) <-2147378241>➞[16r] Concept[16r] Dislocation of
 * joint (disorder) <-2147196846>
 * Some[23r] Role group (SOLOR) <-2147483593>➞[22r] And[22r]➞[21r] Some[21r]
 * Revision status (attribute) <-2146315099>➞[20r] Concept[20r] Primary
 * operation (qualifier value) <-2147302589>
 * Some[27r] Role group (SOLOR) <-2147483593>➞[26r] And[26r]➞[25r] Some[25r]
 * Direct morphology (attribute) <-2147378241>➞[24r] Concept[24r] Traumatic
 * dislocation (morphologic abnormality) <-2146977295>
 * Some[31r] Role group (SOLOR) <-2147483593>➞[30r] And[30r]➞[29r] Some[29r]
 * Method (attribute) <-2147314116>➞[28r] Concept[28r] Surgical action
 * (qualifier value) <-2146940928>
 * Some[37r] Role group (SOLOR) <-2147483593>➞[36r] And[36r]➞[33r, 35r]
 * Some[33r] Procedure site (attribute) <-2147378082>➞[32r] Concept[32r] Joint
 * structure (body structure) <-2146932341>
 * Some[35r] Method (attribute) <-2147314116>➞[34r] Concept[34r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[41r] Role group (SOLOR) <-2147483593>➞[40r] And[40r]➞[39r] Some[39r]
 * Direct morphology (attribute) <-2147378241>➞[38r] Concept[38r]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[49r] Role group (SOLOR) <-2147483593>➞[48r] And[48r]➞[43r, 45r, 47r]
 * Some[43r] Direct morphology (attribute) <-2147378241>➞[42r] Concept[42r]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[45r] Method (attribute) <-2147314116>➞[44r] Concept[44r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[47r] Procedure site - Indirect (attribute) <-2146878264>➞[46r]
 * Concept[46r] Joint structure (body structure) <-2146932341>
 * Some[59r] Role group (SOLOR) <-2147483593>➞[58r] And[58r]➞[51r, 53r, 55r,
 * 57r] Some[51r] Direct morphology (attribute) <-2147378241>➞[50r] Concept[50r]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[53r] Access (attribute) <-2147315914>➞[52r] Concept[52r] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[55r] Method (attribute) <-2147314116>➞[54r] Concept[54r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[57r] Procedure site - Indirect (attribute) <-2146878264>➞[56r]
 * Concept[56r] Bone structure (body structure) <-2147146938>
 * Some[67r] Role group (SOLOR) <-2147483593>➞[66r] And[66r]➞[61r, 63r, 65r]
 * Some[61r] Direct morphology (attribute) <-2147378241>➞[60r] Concept[60r]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[63r] Method (attribute) <-2147314116>➞[62r] Concept[62r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[65r] Procedure site - Direct (attribute) <-2146878287>➞[64r]
 * Concept[64r] Bone structure (body structure) <-2147146938>
 * Some[77r] Role group (SOLOR) <-2147483593>➞[76r] And[76r]➞[69r, 71r, 73r,
 * 75r] Some[69r] Direct morphology (attribute) <-2147378241>➞[68r] Concept[68r]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[71r] Access (attribute) <-2147315914>➞[70r] Concept[70r] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[73r] Method (attribute) <-2147314116>➞[72r] Concept[72r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[75r] Procedure site - Direct (attribute) <-2146878287>➞[74r]
 * Concept[74r] Joint structure (body structure) <-2146932341>
 * Some[87r] Role group (SOLOR) <-2147483593>➞[86r] And[86r]➞[79r, 81r, 83r,
 * 85r] Some[79r] Direct morphology (attribute) <-2147378241>➞[78r] Concept[78r]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[81r] Access (attribute) <-2147315914>➞[80r] Concept[80r] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[83r] Method (attribute) <-2147314116>➞[82r] Concept[82r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[85r] Procedure site - Indirect (attribute) <-2146878264>➞[84r]
 * Concept[84r] Bone structure (body structure) <-2147146938>
 * Some[97r] Role group (SOLOR) <-2147483593>➞[96r] And[96r]➞[89r, 91r, 93r,
 * 95r] Some[89r] Direct morphology (attribute) <-2147378241>➞[88r] Concept[88r]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[91r] Method (attribute) <-2147314116>➞[90r] Concept[90r] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[93r] Procedure site - Direct (attribute) <-2146878287>➞[92r]
 * Concept[92r] Bone structure (body structure) <-2147146938>
 * Some[95r] Using device (attribute) <-2146621201>➞[94r] Concept[94r]
 * Functional brace (physical object) <-2147091701>
 * Some[103r] Role group (SOLOR) <-2147483593>➞[102r] And[102r]➞[99r, 101r]
 * Some[99r] Direct morphology (attribute) <-2147378241>➞[98r] Concept[98r]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[101r] Method (attribute) <-2147314116>➞[100r] Concept[100r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[111r] Role group (SOLOR) <-2147483593>➞[110r] And[110r]➞[105r, 107r,
 * 109r] Some[105r] Direct morphology (attribute) <-2147378241>➞[104r]
 * Concept[104r] Dislocation (morphologic abnormality) <-2147448026>
 * Some[107r] Method (attribute) <-2147314116>➞[106r] Concept[106r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[109r] Procedure site - Direct (attribute) <-2146878287>➞[108r]
 * Concept[108r] Joint structure (body structure) <-2146932341>
 * Some[119r] Role group (SOLOR) <-2147483593>➞[118r] And[118r]➞[113r, 115r,
 * 117r] Some[113r] Direct morphology (attribute) <-2147378241>➞[112r]
 * Concept[112r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[115r] Method (attribute) <-2147314116>➞[114r] Concept[114r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[117r] Procedure site - Direct (attribute) <-2146878287>➞[116r]
 * Concept[116r] Joint structure (body structure) <-2146932341>
 * Some[129r] Role group (SOLOR) <-2147483593>➞[128r] And[128r]➞[121r, 123r,
 * 125r, 127r] Some[121r] Direct morphology (attribute) <-2147378241>➞[120r]
 * Concept[120r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[123r] Method (attribute) <-2147314116>➞[122r] Concept[122r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[125r] Procedure site - Direct (attribute) <-2146878287>➞[124r]
 * Concept[124r] Bone structure (body structure) <-2147146938>
 * Some[127r] Revision status (attribute) <-2146315099>➞[126r] Concept[126r]
 * Primary operation (qualifier value) <-2147302589>
 * Some[141r] Role group (SOLOR) <-2147483593>➞[140r] And[140r]➞[131r, 133r,
 * 135r, 137r, 139r] Some[131r] Direct morphology (attribute)
 * <-2147378241>➞[130r] Concept[130r] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[133r] Access (attribute) <-2147315914>➞[132r] Concept[132r] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[135r] Method (attribute) <-2147314116>➞[134r] Concept[134r] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[137r] Procedure site - Indirect (attribute) <-2146878264>➞[136r]
 * Concept[136r] Bone structure (body structure) <-2147146938>
 * Some[139r] Revision status (attribute) <-2146315099>➞[138r] Concept[138r]
 * Primary operation (qualifier value) <-2147302589>
 * Some[153r] Role group (SOLOR) <-2147483593>➞[152r] And[152r]➞[143r, 145r,
 * 147r, 149r, 151r] Some[143r] Direct morphology (attribute)
 * <-2147378241>➞[142r] Concept[142r] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[145r] Method (attribute) <-2147314116>➞[144r] Concept[144r] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[147r] Procedure site - Direct (attribute) <-2146878287>➞[146r]
 * Concept[146r] Bone structure (body structure) <-2147146938>
 * Some[149r] Using device (attribute) <-2146621201>➞[148r] Concept[148r]
 * Functional brace (physical object) <-2147091701>
 * Some[151r] Revision status (attribute) <-2146315099>➞[150r] Concept[150r]
 * Primary operation (qualifier value) <-2147302589>
 * Some[161r] Role group (SOLOR) <-2147483593>➞[160r] And[160r]➞[155r, 157r,
 * 159r] Some[155r] Direct morphology (attribute) <-2147378241>➞[154r]
 * Concept[154r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[157r] Method (attribute) <-2147314116>➞[156r] Concept[156r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[159r] Procedure site - Direct (attribute) <-2146878287>➞[158r]
 * Concept[158r] Joint structure (body structure) <-2146932341>
 * Some[171r] Role group (SOLOR) <-2147483593>➞[170r] And[170r]➞[163r, 165r,
 * 167r, 169r] Some[163r] Direct morphology (attribute) <-2147378241>➞[162r]
 * Concept[162r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[165r] Method (attribute) <-2147314116>➞[164r] Concept[164r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[167r] Procedure site - Direct (attribute) <-2146878287>➞[166r]
 * Concept[166r] Bone structure (body structure) <-2147146938>
 * Some[169r] Revision status (attribute) <-2146315099>➞[168r] Concept[168r]
 * Primary operation (qualifier value) <-2147302589>
 * Some[179r] Role group (SOLOR) <-2147483593>➞[178r] And[178r]➞[173r, 175r,
 * 177r] Some[173r] Method (attribute) <-2147314116>➞[172r] Concept[172r]
 * Fixation - action (qualifier value) <-2146939443>
 * Some[175r] Procedure site - Direct (attribute) <-2146878287>➞[174r]
 * Concept[174r] Bone and/or joint structure (body structure) <-2146558726>
 * Some[177r] Using device (attribute) <-2146621201>➞[176r] Concept[176r]
 * Functional brace (physical object) <-2147091701>
 * Some[189r] Role group (SOLOR) <-2147483593>➞[188r] And[188r]➞[181r, 183r,
 * 185r, 187r] Some[181r] Direct morphology (attribute) <-2147378241>➞[180r]
 * Concept[180r] Dislocation (morphologic abnormality) <-2147448026>
 * Some[183r] Method (attribute) <-2147314116>➞[182r] Concept[182r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[185r] Procedure site - Direct (attribute) <-2146878287>➞[184r]
 * Concept[184r] Joint structure (body structure) <-2146932341>
 * Some[187r] Revision status (attribute) <-2146315099>➞[186r] Concept[186r]
 * Primary operation (qualifier value) <-2147302589>
 * Some[199r] Role group (SOLOR) <-2147483593>➞[198r] And[198r]➞[191r, 193r,
 * 195r, 197r] Some[191r] Direct morphology (attribute) <-2147378241>➞[190r]
 * Concept[190r] Fracture (morphologic abnormality) <-2146461022>
 * Some[193r] Method (attribute) <-2147314116>➞[192r] Concept[192r] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[195r] Procedure site - Direct (attribute) <-2146878287>➞[194r]
 * Concept[194r] Bone structure (body structure) <-2147146938>
 * Some[197r] Revision status (attribute) <-2146315099>➞[196r] Concept[196r]
 * Primary operation (qualifier value) <-2147302589>
 * Concept[211r] Fixation (procedure) <-2147467999>
 * Concept[200r] Procedure categorized by device involved (procedure)
 * <-2147378462>
 * Concept[201r] Open reduction of fracture with fixation (procedure)
 * <-2147062793>
 * Concept[202r] Skeletal fixation procedure (procedure) <-2147013539>
 * Concept[203r] Reduction of fracture (procedure) <-2147008610>
 * Concept[204r] Primary open reduction of fracture dislocation (procedure)
 * <-2146647907>
 * Concept[205r] Procedure involving splint (procedure) <-2146632066>
 * Concept[206r] Operation on fracture (procedure) <-2146429781>
 * Concept[207r] Fixation of fracture (procedure) <-2146429760>
 *
Comparison expression:
 *
 * Root[182c]➞[181c] Necessary[181c]➞[180c] And[180c]➞[3c, 7c, 11c, 15c, 19c,
 * 23c, 27c, 31c, 37c, 41c, 49c, 59c, 67c, 77c, 87c, 97c, 103c, 111c, 119c,
 * 129c, 141c, 153c, 161c, 171c, 190c, 200c, 210c, 172c, 173c, 174c, 175c, 176c,
 * 177c, 178c, 179c] Some[3c] Role group (SOLOR) <-2147483593>➞[2c] And[2c]➞[1c]
 * Some[1c] Access (attribute) <-2147315914>➞[0c] Concept[0c] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[7c] Role group (SOLOR) <-2147483593>➞[6c] And[6c]➞[5c] Some[5c] Direct
 * morphology (attribute) <-2147378241>➞[4c] Concept[4c] Dislocation
 * (morphologic abnormality) <-2147448026>
 * Some[11c] Role group (SOLOR) <-2147483593>➞[10c] And[10c]➞[9c] Some[9c]
 * Revision status (attribute) <-2146315099>➞[8c] Concept[8c] Principal
 * (qualifier value) <-2146603744>
 * Some[15c] Role group (SOLOR) <-2147483593>➞[14c] And[14c]➞[13c] Some[13c]
 * Method (attribute) <-2147314116>➞[12c] Concept[12c] Surgical repair - action
 * (qualifier value) <-2146939778>
 * Some[19c] Role group (SOLOR) <-2147483593>➞[18c] And[18c]➞[17c] Some[17c]
 * Direct morphology (attribute) <-2147378241>➞[16c] Concept[16c] Dislocation of
 * joint (disorder) <-2147196846>
 * Some[23c] Role group (SOLOR) <-2147483593>➞[22c] And[22c]➞[21c] Some[21c]
 * Revision status (attribute) <-2146315099>➞[20c] Concept[20c] Primary
 * operation (qualifier value) <-2147302589>
 * Some[27c] Role group (SOLOR) <-2147483593>➞[26c] And[26c]➞[25c] Some[25c]
 * Direct morphology (attribute) <-2147378241>➞[24c] Concept[24c] Traumatic
 * dislocation (morphologic abnormality) <-2146977295>
 * Some[31c] Role group (SOLOR) <-2147483593>➞[30c] And[30c]➞[29c] Some[29c]
 * Method (attribute) <-2147314116>➞[28c] Concept[28c] Surgical action
 * (qualifier value) <-2146940928>
 * Some[37c] Role group (SOLOR) <-2147483593>➞[36c] And[36c]➞[33c, 35c]
 * Some[33c] Procedure site (attribute) <-2147378082>➞[32c] Concept[32c] Joint
 * structure (body structure) <-2146932341>
 * Some[35c] Method (attribute) <-2147314116>➞[34c] Concept[34c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[41c] Role group (SOLOR) <-2147483593>➞[40c] And[40c]➞[39c] Some[39c]
 * Direct morphology (attribute) <-2147378241>➞[38c] Concept[38c]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[49c] Role group (SOLOR) <-2147483593>➞[48c] And[48c]➞[43c, 45c, 47c]
 * Some[43c] Direct morphology (attribute) <-2147378241>➞[42c] Concept[42c]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[45c] Method (attribute) <-2147314116>➞[44c] Concept[44c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[47c] Procedure site - Indirect (attribute) <-2146878264>➞[46c]
 * Concept[46c] Joint structure (body structure) <-2146932341>
 * Some[59c] Role group (SOLOR) <-2147483593>➞[58c] And[58c]➞[51c, 53c, 55c,
 * 57c] Some[51c] Direct morphology (attribute) <-2147378241>➞[50c] Concept[50c]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[53c] Access (attribute) <-2147315914>➞[52c] Concept[52c] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[55c] Method (attribute) <-2147314116>➞[54c] Concept[54c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[57c] Procedure site - Indirect (attribute) <-2146878264>➞[56c]
 * Concept[56c] Bone structure (body structure) <-2147146938>
 * Some[67c] Role group (SOLOR) <-2147483593>➞[66c] And[66c]➞[61c, 63c, 65c]
 * Some[61c] Direct morphology (attribute) <-2147378241>➞[60c] Concept[60c]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[63c] Method (attribute) <-2147314116>➞[62c] Concept[62c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[65c] Procedure site - Direct (attribute) <-2146878287>➞[64c]
 * Concept[64c] Bone structure (body structure) <-2147146938>
 * Some[77c] Role group (SOLOR) <-2147483593>➞[76c] And[76c]➞[69c, 71c, 73c,
 * 75c] Some[69c] Direct morphology (attribute) <-2147378241>➞[68c] Concept[68c]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[71c] Access (attribute) <-2147315914>➞[70c] Concept[70c] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[73c] Method (attribute) <-2147314116>➞[72c] Concept[72c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[75c] Procedure site - Direct (attribute) <-2146878287>➞[74c]
 * Concept[74c] Joint structure (body structure) <-2146932341>
 * Some[87c] Role group (SOLOR) <-2147483593>➞[86c] And[86c]➞[79c, 81c, 83c,
 * 85c] Some[79c] Direct morphology (attribute) <-2147378241>➞[78c] Concept[78c]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[81c] Access (attribute) <-2147315914>➞[80c] Concept[80c] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[83c] Method (attribute) <-2147314116>➞[82c] Concept[82c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[85c] Procedure site - Indirect (attribute) <-2146878264>➞[84c]
 * Concept[84c] Bone structure (body structure) <-2147146938>
 * Some[97c] Role group (SOLOR) <-2147483593>➞[96c] And[96c]➞[89c, 91c, 93c,
 * 95c] Some[89c] Direct morphology (attribute) <-2147378241>➞[88c] Concept[88c]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[91c] Method (attribute) <-2147314116>➞[90c] Concept[90c] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[93c] Procedure site - Direct (attribute) <-2146878287>➞[92c]
 * Concept[92c] Bone structure (body structure) <-2147146938>
 * Some[95c] Using device (attribute) <-2146621201>➞[94c] Concept[94c]
 * Functional brace (physical object) <-2147091701>
 * Some[103c] Role group (SOLOR) <-2147483593>➞[102c] And[102c]➞[99c, 101c]
 * Some[99c] Direct morphology (attribute) <-2147378241>➞[98c] Concept[98c]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[101c] Method (attribute) <-2147314116>➞[100c] Concept[100c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[111c] Role group (SOLOR) <-2147483593>➞[110c] And[110c]➞[105c, 107c,
 * 109c] Some[105c] Direct morphology (attribute) <-2147378241>➞[104c]
 * Concept[104c] Dislocation (morphologic abnormality) <-2147448026>
 * Some[107c] Method (attribute) <-2147314116>➞[106c] Concept[106c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[109c] Procedure site - Direct (attribute) <-2146878287>➞[108c]
 * Concept[108c] Joint structure (body structure) <-2146932341>
 * Some[119c] Role group (SOLOR) <-2147483593>➞[118c] And[118c]➞[113c, 115c,
 * 117c] Some[113c] Direct morphology (attribute) <-2147378241>➞[112c]
 * Concept[112c] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[115c] Method (attribute) <-2147314116>➞[114c] Concept[114c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[117c] Procedure site - Direct (attribute) <-2146878287>➞[116c]
 * Concept[116c] Joint structure (body structure) <-2146932341>
 * Some[129c] Role group (SOLOR) <-2147483593>➞[128c] And[128c]➞[121c, 123c,
 * 125c, 127c] Some[121c] Direct morphology (attribute) <-2147378241>➞[120c]
 * Concept[120c] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[123c] Method (attribute) <-2147314116>➞[122c] Concept[122c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[125c] Procedure site - Direct (attribute) <-2146878287>➞[124c]
 * Concept[124c] Bone structure (body structure) <-2147146938>
 * Some[127c] Revision status (attribute) <-2146315099>➞[126c] Concept[126c]
 * Primary operation (qualifier value) <-2147302589>
 * Some[141c] Role group (SOLOR) <-2147483593>➞[140c] And[140c]➞[131c, 133c,
 * 135c, 137c, 139c] Some[131c] Direct morphology (attribute)
 * <-2147378241>➞[130c] Concept[130c] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[133c] Access (attribute) <-2147315914>➞[132c] Concept[132c] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[135c] Method (attribute) <-2147314116>➞[134c] Concept[134c] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[137c] Procedure site - Indirect (attribute) <-2146878264>➞[136c]
 * Concept[136c] Bone structure (body structure) <-2147146938>
 * Some[139c] Revision status (attribute) <-2146315099>➞[138c] Concept[138c]
 * Primary operation (qualifier value) <-2147302589>
 * Some[153c] Role group (SOLOR) <-2147483593>➞[152c] And[152c]➞[143c, 145c,
 * 147c, 149c, 151c] Some[143c] Direct morphology (attribute)
 * <-2147378241>➞[142c] Concept[142c] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[145c] Method (attribute) <-2147314116>➞[144c] Concept[144c] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[147c] Procedure site - Direct (attribute) <-2146878287>➞[146c]
 * Concept[146c] Bone structure (body structure) <-2147146938>
 * Some[149c] Using device (attribute) <-2146621201>➞[148c] Concept[148c]
 * Functional brace (physical object) <-2147091701>
 * Some[151c] Revision status (attribute) <-2146315099>➞[150c] Concept[150c]
 * Primary operation (qualifier value) <-2147302589>
 * Some[161c] Role group (SOLOR) <-2147483593>➞[160c] And[160c]➞[155c, 157c,
 * 159c] Some[155c] Direct morphology (attribute) <-2147378241>➞[154c]
 * Concept[154c] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[157c] Method (attribute) <-2147314116>➞[156c] Concept[156c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[159c] Procedure site - Direct (attribute) <-2146878287>➞[158c]
 * Concept[158c] Joint structure (body structure) <-2146932341>
 * Some[171c] Role group (SOLOR) <-2147483593>➞[170c] And[170c]➞[163c, 165c,
 * 167c, 169c] Some[163c] Direct morphology (attribute) <-2147378241>➞[162c]
 * Concept[162c] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[165c] Method (attribute) <-2147314116>➞[164c] Concept[164c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[167c] Procedure site - Direct (attribute) <-2146878287>➞[166c]
 * Concept[166c] Bone structure (body structure) <-2147146938>
 * Some[169c] Revision status (attribute) <-2146315099>➞[168c] Concept[168c]
 * Primary operation (qualifier value) <-2147302589>
 * Some[190c] Role group (SOLOR) <-2147483593>➞[189c] And[189c]➞[184c, 186c,
 * 188c] Some[184c] Method (attribute) <-2147314116>➞[183c] Concept[183c]
 * Fixation - action (qualifier value) <-2146939443>
 * Some[186c] Procedure site - Direct (attribute) <-2146878287>➞[185c]
 * Concept[185c] Bone and/or joint structure (body structure) <-2146558726>
 * Some[188c] Using device (attribute) <-2146621201>➞[187c] Concept[187c]
 * Functional brace (physical object) <-2147091701>
 * Some[200c] Role group (SOLOR) <-2147483593>➞[199c] And[199c]➞[192c, 194c,
 * 196c, 198c] Some[192c] Direct morphology (attribute) <-2147378241>➞[191c]
 * Concept[191c] Dislocation (morphologic abnormality) <-2147448026>
 * Some[194c] Method (attribute) <-2147314116>➞[193c] Concept[193c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[196c] Procedure site - Direct (attribute) <-2146878287>➞[195c]
 * Concept[195c] Joint structure (body structure) <-2146932341>
 * Some[198c] Revision status (attribute) <-2146315099>➞[197c] Concept[197c]
 * Primary operation (qualifier value) <-2147302589>
 * Some[210c] Role group (SOLOR) <-2147483593>➞[209c] And[209c]➞[202c, 204c,
 * 206c, 208c] Some[202c] Direct morphology (attribute) <-2147378241>➞[201c]
 * Concept[201c] Fracture (morphologic abnormality) <-2146461022>
 * Some[204c] Method (attribute) <-2147314116>➞[203c] Concept[203c] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[206c] Procedure site - Direct (attribute) <-2146878287>➞[205c]
 * Concept[205c] Bone structure (body structure) <-2147146938>
 * Some[208c] Revision status (attribute) <-2146315099>➞[207c] Concept[207c]
 * Primary operation (qualifier value) <-2147302589>
 * Concept[172c] Procedure categorized by device involved (procedure)
 * <-2147378462>
 * Concept[173c] Open reduction of fracture with fixation (procedure)
 * <-2147062793>
 * Concept[174c] Skeletal fixation procedure (procedure) <-2147013539>
 * Concept[175c] Reduction of fracture (procedure) <-2147008610>
 * Concept[176c] Primary open reduction of fracture dislocation (procedure)
 * <-2146647907>
 * Concept[177c] Procedure involving splint (procedure) <-2146632066>
 * Concept[178c] Operation on fracture (procedure) <-2146429781>
 * Concept[179c] Fixation of fracture (procedure) <-2146429760>
 *
Isomorphic expression:
 *
 * Root[210i]➞[209i] Necessary[209i]➞[208i] And[208i]➞[3i, 7i, 11i, 15i, 19i,
 * 23i, 27i, 31i, 37i, 41i, 49i, 59i, 67i, 77i, 87i, 97i, 103i, 111i, 119i,
 * 129i, 141i, 153i, 161i, 171i, 179i, 189i, 199i, 200i, 201i, 202i, 203i, 204i,
 * 205i, 206i, 207i] Some[3i] Role group (SOLOR) <-2147483593>➞[2i] And[2i]➞[1i]
 * Some[1i] Access (attribute) <-2147315914>➞[0i] Concept[0i] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[7i] Role group (SOLOR) <-2147483593>➞[6i] And[6i]➞[5i] Some[5i] Direct
 * morphology (attribute) <-2147378241>➞[4i] Concept[4i] Dislocation
 * (morphologic abnormality) <-2147448026>
 * Some[11i] Role group (SOLOR) <-2147483593>➞[10i] And[10i]➞[9i] Some[9i]
 * Revision status (attribute) <-2146315099>➞[8i] Concept[8i] Principal
 * (qualifier value) <-2146603744>
 * Some[15i] Role group (SOLOR) <-2147483593>➞[14i] And[14i]➞[13i] Some[13i]
 * Method (attribute) <-2147314116>➞[12i] Concept[12i] Surgical repair - action
 * (qualifier value) <-2146939778>
 * Some[19i] Role group (SOLOR) <-2147483593>➞[18i] And[18i]➞[17i] Some[17i]
 * Direct morphology (attribute) <-2147378241>➞[16i] Concept[16i] Dislocation of
 * joint (disorder) <-2147196846>
 * Some[23i] Role group (SOLOR) <-2147483593>➞[22i] And[22i]➞[21i] Some[21i]
 * Revision status (attribute) <-2146315099>➞[20i] Concept[20i] Primary
 * operation (qualifier value) <-2147302589>
 * Some[27i] Role group (SOLOR) <-2147483593>➞[26i] And[26i]➞[25i] Some[25i]
 * Direct morphology (attribute) <-2147378241>➞[24i] Concept[24i] Traumatic
 * dislocation (morphologic abnormality) <-2146977295>
 * Some[31i] Role group (SOLOR) <-2147483593>➞[30i] And[30i]➞[29i] Some[29i]
 * Method (attribute) <-2147314116>➞[28i] Concept[28i] Surgical action
 * (qualifier value) <-2146940928>
 * Some[37i] Role group (SOLOR) <-2147483593>➞[36i] And[36i]➞[33i, 35i]
 * Some[33i] Procedure site (attribute) <-2147378082>➞[32i] Concept[32i] Joint
 * structure (body structure) <-2146932341>
 * Some[35i] Method (attribute) <-2147314116>➞[34i] Concept[34i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[41i] Role group (SOLOR) <-2147483593>➞[40i] And[40i]➞[39i] Some[39i]
 * Direct morphology (attribute) <-2147378241>➞[38i] Concept[38i]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[49i] Role group (SOLOR) <-2147483593>➞[48i] And[48i]➞[43i, 45i, 47i]
 * Some[43i] Direct morphology (attribute) <-2147378241>➞[42i] Concept[42i]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[45i] Method (attribute) <-2147314116>➞[44i] Concept[44i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[47i] Procedure site - Indirect (attribute) <-2146878264>➞[46i]
 * Concept[46i] Joint structure (body structure) <-2146932341>
 * Some[59i] Role group (SOLOR) <-2147483593>➞[58i] And[58i]➞[51i, 53i, 55i,
 * 57i] Some[51i] Direct morphology (attribute) <-2147378241>➞[50i] Concept[50i]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[53i] Access (attribute) <-2147315914>➞[52i] Concept[52i] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[55i] Method (attribute) <-2147314116>➞[54i] Concept[54i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[57i] Procedure site - Indirect (attribute) <-2146878264>➞[56i]
 * Concept[56i] Bone structure (body structure) <-2147146938>
 * Some[67i] Role group (SOLOR) <-2147483593>➞[66i] And[66i]➞[61i, 63i, 65i]
 * Some[61i] Direct morphology (attribute) <-2147378241>➞[60i] Concept[60i]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[63i] Method (attribute) <-2147314116>➞[62i] Concept[62i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[65i] Procedure site - Direct (attribute) <-2146878287>➞[64i]
 * Concept[64i] Bone structure (body structure) <-2147146938>
 * Some[77i] Role group (SOLOR) <-2147483593>➞[76i] And[76i]➞[69i, 71i, 73i,
 * 75i] Some[69i] Direct morphology (attribute) <-2147378241>➞[68i] Concept[68i]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[71i] Access (attribute) <-2147315914>➞[70i] Concept[70i] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[73i] Method (attribute) <-2147314116>➞[72i] Concept[72i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[75i] Procedure site - Direct (attribute) <-2146878287>➞[74i]
 * Concept[74i] Joint structure (body structure) <-2146932341>
 * Some[87i] Role group (SOLOR) <-2147483593>➞[86i] And[86i]➞[79i, 81i, 83i,
 * 85i] Some[79i] Direct morphology (attribute) <-2147378241>➞[78i] Concept[78i]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[81i] Access (attribute) <-2147315914>➞[80i] Concept[80i] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[83i] Method (attribute) <-2147314116>➞[82i] Concept[82i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[85i] Procedure site - Indirect (attribute) <-2146878264>➞[84i]
 * Concept[84i] Bone structure (body structure) <-2147146938>
 * Some[97i] Role group (SOLOR) <-2147483593>➞[96i] And[96i]➞[89i, 91i, 93i,
 * 95i] Some[89i] Direct morphology (attribute) <-2147378241>➞[88i] Concept[88i]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[91i] Method (attribute) <-2147314116>➞[90i] Concept[90i] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[93i] Procedure site - Direct (attribute) <-2146878287>➞[92i]
 * Concept[92i] Bone structure (body structure) <-2147146938>
 * Some[95i] Using device (attribute) <-2146621201>➞[94i] Concept[94i]
 * Functional brace (physical object) <-2147091701>
 * Some[103i] Role group (SOLOR) <-2147483593>➞[102i] And[102i]➞[99i, 101i]
 * Some[99i] Direct morphology (attribute) <-2147378241>➞[98i] Concept[98i]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[101i] Method (attribute) <-2147314116>➞[100i] Concept[100i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[111i] Role group (SOLOR) <-2147483593>➞[110i] And[110i]➞[105i, 107i,
 * 109i] Some[105i] Direct morphology (attribute) <-2147378241>➞[104i]
 * Concept[104i] Dislocation (morphologic abnormality) <-2147448026>
 * Some[107i] Method (attribute) <-2147314116>➞[106i] Concept[106i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[109i] Procedure site - Direct (attribute) <-2146878287>➞[108i]
 * Concept[108i] Joint structure (body structure) <-2146932341>
 * Some[119i] Role group (SOLOR) <-2147483593>➞[118i] And[118i]➞[113i, 115i,
 * 117i] Some[113i] Direct morphology (attribute) <-2147378241>➞[112i]
 * Concept[112i] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[115i] Method (attribute) <-2147314116>➞[114i] Concept[114i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[117i] Procedure site - Direct (attribute) <-2146878287>➞[116i]
 * Concept[116i] Joint structure (body structure) <-2146932341>
 * Some[129i] Role group (SOLOR) <-2147483593>➞[128i] And[128i]➞[121i, 123i,
 * 125i, 127i] Some[121i] Direct morphology (attribute) <-2147378241>➞[120i]
 * Concept[120i] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[123i] Method (attribute) <-2147314116>➞[122i] Concept[122i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[125i] Procedure site - Direct (attribute) <-2146878287>➞[124i]
 * Concept[124i] Bone structure (body structure) <-2147146938>
 * Some[127i] Revision status (attribute) <-2146315099>➞[126i] Concept[126i]
 * Primary operation (qualifier value) <-2147302589>
 * Some[141i] Role group (SOLOR) <-2147483593>➞[140i] And[140i]➞[131i, 133i,
 * 135i, 137i, 139i] Some[131i] Direct morphology (attribute)
 * <-2147378241>➞[130i] Concept[130i] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[133i] Access (attribute) <-2147315914>➞[132i] Concept[132i] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[135i] Method (attribute) <-2147314116>➞[134i] Concept[134i] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[137i] Procedure site - Indirect (attribute) <-2146878264>➞[136i]
 * Concept[136i] Bone structure (body structure) <-2147146938>
 * Some[139i] Revision status (attribute) <-2146315099>➞[138i] Concept[138i]
 * Primary operation (qualifier value) <-2147302589>
 * Some[153i] Role group (SOLOR) <-2147483593>➞[152i] And[152i]➞[143i, 145i,
 * 147i, 149i, 151i] Some[143i] Direct morphology (attribute)
 * <-2147378241>➞[142i] Concept[142i] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[145i] Method (attribute) <-2147314116>➞[144i] Concept[144i] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[147i] Procedure site - Direct (attribute) <-2146878287>➞[146i]
 * Concept[146i] Bone structure (body structure) <-2147146938>
 * Some[149i] Using device (attribute) <-2146621201>➞[148i] Concept[148i]
 * Functional brace (physical object) <-2147091701>
 * Some[151i] Revision status (attribute) <-2146315099>➞[150i] Concept[150i]
 * Primary operation (qualifier value) <-2147302589>
 * Some[161i] Role group (SOLOR) <-2147483593>➞[160i] And[160i]➞[155i, 157i,
 * 159i] Some[155i] Direct morphology (attribute) <-2147378241>➞[154i]
 * Concept[154i] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[157i] Method (attribute) <-2147314116>➞[156i] Concept[156i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[159i] Procedure site - Direct (attribute) <-2146878287>➞[158i]
 * Concept[158i] Joint structure (body structure) <-2146932341>
 * Some[171i] Role group (SOLOR) <-2147483593>➞[170i] And[170i]➞[163i, 165i,
 * 167i, 169i] Some[163i] Direct morphology (attribute) <-2147378241>➞[162i]
 * Concept[162i] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[165i] Method (attribute) <-2147314116>➞[164i] Concept[164i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[167i] Procedure site - Direct (attribute) <-2146878287>➞[166i]
 * Concept[166i] Bone structure (body structure) <-2147146938>
 * Some[169i] Revision status (attribute) <-2146315099>➞[168i] Concept[168i]
 * Primary operation (qualifier value) <-2147302589>
 * Some[179i] Role group (SOLOR) <-2147483593>➞[178i] And[178i]➞[173i, 175i,
 * 177i] Some[173i] Method (attribute) <-2147314116>➞[172i] Concept[172i]
 * Fixation - action (qualifier value) <-2146939443>
 * Some[175i] Procedure site - Direct (attribute) <-2146878287>➞[174i]
 * Concept[174i] Bone and/or joint structure (body structure) <-2146558726>
 * Some[177i] Using device (attribute) <-2146621201>➞[176i] Concept[176i]
 * Functional brace (physical object) <-2147091701>
 * Some[189i] Role group (SOLOR) <-2147483593>➞[188i] And[188i]➞[181i, 183i,
 * 185i, 187i] Some[181i] Direct morphology (attribute) <-2147378241>➞[180i]
 * Concept[180i] Dislocation (morphologic abnormality) <-2147448026>
 * Some[183i] Method (attribute) <-2147314116>➞[182i] Concept[182i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[185i] Procedure site - Direct (attribute) <-2146878287>➞[184i]
 * Concept[184i] Joint structure (body structure) <-2146932341>
 * Some[187i] Revision status (attribute) <-2146315099>➞[186i] Concept[186i]
 * Primary operation (qualifier value) <-2147302589>
 * Some[199i] Role group (SOLOR) <-2147483593>➞[198i] And[198i]➞[191i, 193i,
 * 195i, 197i] Some[191i] Direct morphology (attribute) <-2147378241>➞[190i]
 * Concept[190i] Fracture (morphologic abnormality) <-2146461022>
 * Some[193i] Method (attribute) <-2147314116>➞[192i] Concept[192i] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[195i] Procedure site - Direct (attribute) <-2146878287>➞[194i]
 * Concept[194i] Bone structure (body structure) <-2147146938>
 * Some[197i] Revision status (attribute) <-2146315099>➞[196i] Concept[196i]
 * Primary operation (qualifier value) <-2147302589>
 * Concept[200i] Procedure categorized by device involved (procedure)
 * <-2147378462>
 * Concept[201i] Open reduction of fracture with fixation (procedure)
 * <-2147062793>
 * Concept[202i] Skeletal fixation procedure (procedure) <-2147013539>
 * Concept[203i] Reduction of fracture (procedure) <-2147008610>
 * Concept[204i] Primary open reduction of fracture dislocation (procedure)
 * <-2146647907>
 * Concept[205i] Procedure involving splint (procedure) <-2146632066>
 * Concept[206i] Operation on fracture (procedure) <-2146429781>
 * Concept[207i] Fixation of fracture (procedure) <-2146429760>
 *
Reference Expression To MergedNodeId Map:
 *
 * [0r:0m, 1r:1m, 2r:2m, 3r:3m, 4r:4m, 5r:5m, 6r:6m, 7r:7m, 8r:8m, 9r:9m,
 * 10r:10m, 11r:11m, 12r:12m, 13r:13m, 14r:14m, 15r:15m, 16r:16m, 17r:17m,
 * 18r:18m, 19r:19m, 20r:20m, 21r:21m, 22r:22m, 23r:23m, 24r:24m, 25r:25m,
 * 26r:26m, 27r:27m, 28r:28m, 29r:29m, 30r:30m, 31r:31m, 32r:32m, 33r:33m,
 * 34r:34m, 35r:35m, 36r:36m, 37r:37m, 38r:38m, 39r:39m, 40r:40m, 41r:41m,
 * 42r:42m, 43r:43m, 44r:44m, 45r:45m, 46r:46m, 47r:47m, 48r:48m, 49r:49m,
 * 50r:50m, 51r:51m, 52r:52m, 53r:53m, 54r:54m, 55r:55m, 56r:56m, 57r:57m,
 * 58r:58m, 59r:59m, 60r:60m, 61r:61m, 62r:62m, 63r:63m, 64r:64m, 65r:65m,
 * 66r:66m, 67r:67m, 68r:68m, 69r:69m, 70r:70m, 71r:71m, 72r:72m, 73r:73m,
 * 74r:74m, 75r:75m, 76r:76m, 77r:77m, 78r:78m, 79r:79m, 80r:80m, 81r:81m,
 * 82r:82m, 83r:83m, 84r:84m, 85r:85m, 86r:86m, 87r:87m, 88r:88m, 89r:89m,
 * 90r:90m, 91r:91m, 92r:92m, 93r:93m, 94r:94m, 95r:95m, 96r:96m, 97r:97m,
 * 98r:98m, 99r:99m, 100r:100m, 101r:101m, 102r:102m, 103r:103m, 104r:104m,
 * 105r:105m, 106r:106m, 107r:107m, 108r:108m, 109r:109m, 110r:110m, 111r:111m,
 * 112r:112m, 113r:113m, 114r:114m, 115r:115m, 116r:116m, 117r:117m, 118r:118m,
 * 119r:119m, 120r:120m, 121r:121m, 122r:122m, 123r:123m, 124r:124m, 125r:125m,
 * 126r:126m, 127r:127m, 128r:128m, 129r:129m, 130r:130m, 131r:131m, 132r:132m,
 * 133r:133m, 134r:134m, 135r:135m, 136r:136m, 137r:137m, 138r:138m, 139r:139m,
 * 140r:140m, 141r:141m, 142r:142m, 143r:143m, 144r:144m, 145r:145m, 146r:146m,
 * 147r:147m, 148r:148m, 149r:149m, 150r:150m, 151r:151m, 152r:152m, 153r:153m,
 * 154r:154m, 155r:155m, 156r:156m, 157r:157m, 158r:158m, 159r:159m, 160r:160m,
 * 161r:161m, 162r:162m, 163r:163m, 164r:164m, 165r:165m, 166r:166m, 167r:167m,
 * 168r:168m, 169r:169m, 170r:170m, 171r:171m, 172r:172m, 173r:173m, 174r:174m,
 * 175r:175m, 176r:176m, 177r:177m, 178r:178m, 179r:179m, 180r:180m, 181r:181m,
 * 182r:182m, 183r:183m, 184r:184m, 185r:185m, 186r:186m, 187r:187m, 188r:188m,
 * 189r:189m, 190r:190m, 191r:191m, 192r:192m, 193r:193m, 194r:194m, 195r:195m,
 * 196r:196m, 197r:197m, 198r:198m, 199r:199m, 200r:201m, 201r:202m, 202r:203m,
 * 203r:204m, 204r:205m, 205r:206m, 206r:207m, 207r:208m, 208r:209m, 209r:210m,
 * 210r:211m, 211r:200m]
 *
 * Reference Expression To ComparisonNodeId Map:
 *
 * [0r:0c, 1r:1c, 2r:2c, 3r:3c, 4r:4c, 5r:5c, 6r:6c, 7r:7c, 8r:8c, 9r:9c,
 * 10r:10c, 11r:11c, 12r:12c, 13r:13c, 14r:14c, 15r:15c, 16r:16c, 17r:17c,
 * 18r:18c, 19r:19c, 20r:20c, 21r:21c, 22r:22c, 23r:23c, 24r:24c, 25r:25c,
 * 26r:26c, 27r:27c, 28r:28c, 29r:29c, 30r:30c, 31r:31c, 32r:32c, 33r:33c,
 * 34r:34c, 35r:35c, 36r:36c, 37r:37c, 38r:38c, 39r:39c, 40r:40c, 41r:41c,
 * 42r:42c, 43r:43c, 44r:44c, 45r:45c, 46r:46c, 47r:47c, 48r:48c, 49r:49c,
 * 50r:50c, 51r:51c, 52r:52c, 53r:53c, 54r:54c, 55r:55c, 56r:56c, 57r:57c,
 * 58r:58c, 59r:59c, 60r:60c, 61r:61c, 62r:62c, 63r:63c, 64r:64c, 65r:65c,
 * 66r:66c, 67r:67c, 68r:68c, 69r:69c, 70r:70c, 71r:71c, 72r:72c, 73r:73c,
 * 74r:74c, 75r:75c, 76r:76c, 77r:77c, 78r:78c, 79r:79c, 80r:80c, 81r:81c,
 * 82r:82c, 83r:83c, 84r:84c, 85r:85c, 86r:86c, 87r:87c, 88r:88c, 89r:89c,
 * 90r:90c, 91r:91c, 92r:92c, 93r:93c, 94r:94c, 95r:95c, 96r:96c, 97r:97c,
 * 98r:98c, 99r:99c, 100r:100c, 101r:101c, 102r:102c, 103r:103c, 104r:104c,
 * 105r:105c, 106r:106c, 107r:107c, 108r:108c, 109r:109c, 110r:110c, 111r:111c,
 * 112r:112c, 113r:113c, 114r:114c, 115r:115c, 116r:116c, 117r:117c, 118r:118c,
 * 119r:119c, 120r:120c, 121r:121c, 122r:122c, 123r:123c, 124r:124c, 125r:125c,
 * 126r:126c, 127r:127c, 128r:128c, 129r:129c, 130r:130c, 131r:131c, 132r:132c,
 * 133r:133c, 134r:134c, 135r:135c, 136r:136c, 137r:137c, 138r:138c, 139r:139c,
 * 140r:140c, 141r:141c, 142r:142c, 143r:143c, 144r:144c, 145r:145c, 146r:146c,
 * 147r:147c, 148r:148c, 149r:149c, 150r:150c, 151r:151c, 152r:152c, 153r:153c,
 * 154r:154c, 155r:155c, 156r:156c, 157r:157c, 158r:158c, 159r:159c, 160r:160c,
 * 161r:161c, 162r:162c, 163r:163c, 164r:164c, 165r:165c, 166r:166c, 167r:167c,
 * 168r:168c, 169r:169c, 170r:170c, 171r:171c, 172r:183c, 173r:184c, 174r:185c,
 * 175r:186c, 176r:187c, 177r:188c, 178r:189c, 179r:190c, 180r:191c, 181r:192c,
 * 182r:193c, 183r:194c, 184r:195c, 185r:196c, 186r:197c, 187r:198c, 188r:199c,
 * 189r:200c, 190r:201c, 191r:202c, 192r:203c, 193r:204c, 194r:205c, 195r:206c,
 * 196r:207c, 197r:208c, 198r:209c, 199r:210c, 200r:172c, 201r:173c, 202r:174c,
 * 203r:175c, 204r:176c, 205r:177c, 206r:178c, 207r:179c, 208r:180c, 209r:181c,
 * 210r:182c, 211r:-1c]
 *
 * Comparison Expression To ReferenceNodeId Map:
 *
 * [0c:0r, 1c:1r, 2c:2r, 3c:3r, 4c:4r, 5c:5r, 6c:6r, 7c:7r, 8c:8r, 9c:9r,
 * 10c:10r, 11c:11r, 12c:12r, 13c:13r, 14c:14r, 15c:15r, 16c:16r, 17c:17r,
 * 18c:18r, 19c:19r, 20c:20r, 21c:21r, 22c:22r, 23c:23r, 24c:24r, 25c:25r,
 * 26c:26r, 27c:27r, 28c:28r, 29c:29r, 30c:30r, 31c:31r, 32c:32r, 33c:33r,
 * 34c:34r, 35c:35r, 36c:36r, 37c:37r, 38c:38r, 39c:39r, 40c:40r, 41c:41r,
 * 42c:42r, 43c:43r, 44c:44r, 45c:45r, 46c:46r, 47c:47r, 48c:48r, 49c:49r,
 * 50c:50r, 51c:51r, 52c:52r, 53c:53r, 54c:54r, 55c:55r, 56c:56r, 57c:57r,
 * 58c:58r, 59c:59r, 60c:60r, 61c:61r, 62c:62r, 63c:63r, 64c:64r, 65c:65r,
 * 66c:66r, 67c:67r, 68c:68r, 69c:69r, 70c:70r, 71c:71r, 72c:72r, 73c:73r,
 * 74c:74r, 75c:75r, 76c:76r, 77c:77r, 78c:78r, 79c:79r, 80c:80r, 81c:81r,
 * 82c:82r, 83c:83r, 84c:84r, 85c:85r, 86c:86r, 87c:87r, 88c:88r, 89c:89r,
 * 90c:90r, 91c:91r, 92c:92r, 93c:93r, 94c:94r, 95c:95r, 96c:96r, 97c:97r,
 * 98c:98r, 99c:99r, 100c:100r, 101c:101r, 102c:102r, 103c:103r, 104c:104r,
 * 105c:105r, 106c:106r, 107c:107r, 108c:108r, 109c:109r, 110c:110r, 111c:111r,
 * 112c:112r, 113c:113r, 114c:114r, 115c:115r, 116c:116r, 117c:117r, 118c:118r,
 * 119c:119r, 120c:120r, 121c:121r, 122c:122r, 123c:123r, 124c:124r, 125c:125r,
 * 126c:126r, 127c:127r, 128c:128r, 129c:129r, 130c:130r, 131c:131r, 132c:132r,
 * 133c:133r, 134c:134r, 135c:135r, 136c:136r, 137c:137r, 138c:138r, 139c:139r,
 * 140c:140r, 141c:141r, 142c:142r, 143c:143r, 144c:144r, 145c:145r, 146c:146r,
 * 147c:147r, 148c:148r, 149c:149r, 150c:150r, 151c:151r, 152c:152r, 153c:153r,
 * 154c:154r, 155c:155r, 156c:156r, 157c:157r, 158c:158r, 159c:159r, 160c:160r,
 * 161c:161r, 162c:162r, 163c:163r, 164c:164r, 165c:165r, 166c:166r, 167c:167r,
 * 168c:168r, 169c:169r, 170c:170r, 171c:171r, 172c:200r, 173c:201r, 174c:202r,
 * 175c:203r, 176c:204r, 177c:205r, 178c:206r, 179c:207r, 180c:208r, 181c:209r,
 * 182c:210r, 183c:172r, 184c:173r, 185c:174r, 186c:175r, 187c:176r, 188c:177r,
 * 189c:178r, 190c:179r, 191c:180r, 192c:181r, 193c:182r, 194c:183r, 195c:184r,
 * 196c:185r, 197c:186r, 198c:187r, 199c:188r, 200c:189r, 201c:190r, 202c:191r,
 * 203c:192r, 204c:193r, 205c:194r, 206c:195r, 207c:196r, 208c:197r, 209c:198r,
 * 210c:199r]
 *
 * Isomorphic solution: [ 0r] ➞ [ 0c] Concept[0r] Open approach - access
 * (qualifier value) <-2146941428>
 * [ 1r] ➞ [ 1c] Some[1r] Access (attribute) <-2147315914>➞[0r] [ 2r] ➞ [ 2c]
 * And[2r]➞[1r] [ 3r] ➞ [ 3c] Some[3r] Role group (SOLOR) <-2147483593>➞[2r] [
 * 4r] ➞ [ 4c] Concept[4r] Dislocation (morphologic abnormality) <-2147448026>
 * [ 5r] ➞ [ 5c] Some[5r] Direct morphology (attribute) <-2147378241>➞[4r] [ 6r]
 * ➞ [ 6c] And[6r]➞[5r] [ 7r] ➞ [ 7c] Some[7r] Role group (SOLOR)
 * <-2147483593>➞[6r] [ 8r] ➞ [ 8c] Concept[8r] Principal (qualifier value)
 * <-2146603744>
 * [ 9r] ➞ [ 9c] Some[9r] Revision status (attribute) <-2146315099>➞[8r] [ 10r]
 * ➞ [ 10c] And[10r]➞[9r] [ 11r] ➞ [ 11c] Some[11r] Role group (SOLOR)
 * <-2147483593>➞[10r] [ 12r] ➞ [ 12c] Concept[12r] Surgical repair - action
 * (qualifier value) <-2146939778>
 * [ 13r] ➞ [ 13c] Some[13r] Method (attribute) <-2147314116>➞[12r] [ 14r] ➞ [
 * 14c] And[14r]➞[13r] [ 15r] ➞ [ 15c] Some[15r] Role group (SOLOR)
 * <-2147483593>➞[14r] [ 16r] ➞ [ 16c] Concept[16r] Dislocation of joint
 * (disorder) <-2147196846>
 * [ 17r] ➞ [ 17c] Some[17r] Direct morphology (attribute) <-2147378241>➞[16r] [
 * 18r] ➞ [ 18c] And[18r]➞[17r] [ 19r] ➞ [ 19c] Some[19r] Role group (SOLOR)
 * <-2147483593>➞[18r] [ 20r] ➞ [ 20c] Concept[20r] Primary operation (qualifier
 * value) <-2147302589>
 * [ 21r] ➞ [ 21c] Some[21r] Revision status (attribute) <-2146315099>➞[20r] [
 * 22r] ➞ [ 22c] And[22r]➞[21r] [ 23r] ➞ [ 23c] Some[23r] Role group (SOLOR)
 * <-2147483593>➞[22r] [ 24r] ➞ [ 24c] Concept[24r] Traumatic dislocation
 * (morphologic abnormality) <-2146977295>
 * [ 25r] ➞ [ 25c] Some[25r] Direct morphology (attribute) <-2147378241>➞[24r] [
 * 26r] ➞ [ 26c] And[26r]➞[25r] [ 27r] ➞ [ 27c] Some[27r] Role group (SOLOR)
 * <-2147483593>➞[26r] [ 28r] ➞ [ 28c] Concept[28r] Surgical action (qualifier
 * value) <-2146940928>
 * [ 29r] ➞ [ 29c] Some[29r] Method (attribute) <-2147314116>➞[28r] [ 30r] ➞ [
 * 30c] And[30r]➞[29r] [ 31r] ➞ [ 31c] Some[31r] Role group (SOLOR)
 * <-2147483593>➞[30r] [ 32r] ➞ [ 32c] Concept[32r] Joint structure (body
 * structure) <-2146932341>
 * [ 33r] ➞ [ 33c] Some[33r] Procedure site (attribute) <-2147378082>➞[32r] [
 * 34r] ➞ [ 34c] Concept[34r] Reduction - action (qualifier value) <-2146938668>
 * [ 35r] ➞ [ 35c] Some[35r] Method (attribute) <-2147314116>➞[34r] [ 36r] ➞ [
 * 36c] And[36r]➞[33r, 35r] [ 37r] ➞ [ 37c] Some[37r] Role group (SOLOR)
 * <-2147483593>➞[36r] [ 38r] ➞ [ 38c] Concept[38r] Fracture-dislocation
 * (morphologic abnormality) <-2147403668>
 * [ 39r] ➞ [ 39c] Some[39r] Direct morphology (attribute) <-2147378241>➞[38r] [
 * 40r] ➞ [ 40c] And[40r]➞[39r] [ 41r] ➞ [ 41c] Some[41r] Role group (SOLOR)
 * <-2147483593>➞[40r] [ 42r] ➞ [ 42c] Concept[42r] Dislocation (morphologic
 * abnormality) <-2147448026>
 * [ 43r] ➞ [ 43c] Some[43r] Direct morphology (attribute) <-2147378241>➞[42r] [
 * 44r] ➞ [ 44c] Concept[44r] Reduction - action (qualifier value) <-2146938668>
 * [ 45r] ➞ [ 45c] Some[45r] Method (attribute) <-2147314116>➞[44r] [ 46r] ➞ [
 * 46c] Concept[46r] Joint structure (body structure) <-2146932341>
 * [ 47r] ➞ [ 47c] Some[47r] Procedure site - Indirect (attribute)
 * <-2146878264>➞[46r] [ 48r] ➞ [ 48c] And[48r]➞[43r, 45r, 47r] [ 49r] ➞ [ 49c]
 * Some[49r] Role group (SOLOR) <-2147483593>➞[48r] [ 50r] ➞ [ 50c] Concept[50r]
 * Fracture (morphologic abnormality) <-2146461022>
 * [ 51r] ➞ [ 51c] Some[51r] Direct morphology (attribute) <-2147378241>➞[50r] [
 * 52r] ➞ [ 52c] Concept[52r] Open approach - access (qualifier value)
 * <-2146941428>
 * [ 53r] ➞ [ 53c] Some[53r] Access (attribute) <-2147315914>➞[52r] [ 54r] ➞ [
 * 54c] Concept[54r] Reduction - action (qualifier value) <-2146938668>
 * [ 55r] ➞ [ 55c] Some[55r] Method (attribute) <-2147314116>➞[54r] [ 56r] ➞ [
 * 56c] Concept[56r] Bone structure (body structure) <-2147146938>
 * [ 57r] ➞ [ 57c] Some[57r] Procedure site - Indirect (attribute)
 * <-2146878264>➞[56r] [ 58r] ➞ [ 58c] And[58r]➞[51r, 53r, 55r, 57r] [ 59r] ➞ [
 * 59c] Some[59r] Role group (SOLOR) <-2147483593>➞[58r] [ 60r] ➞ [ 60c]
 * Concept[60r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [ 61r] ➞ [ 61c] Some[61r] Direct morphology (attribute) <-2147378241>➞[60r] [
 * 62r] ➞ [ 62c] Concept[62r] Reduction - action (qualifier value) <-2146938668>
 * [ 63r] ➞ [ 63c] Some[63r] Method (attribute) <-2147314116>➞[62r] [ 64r] ➞ [
 * 64c] Concept[64r] Bone structure (body structure) <-2147146938>
 * [ 65r] ➞ [ 65c] Some[65r] Procedure site - Direct (attribute)
 * <-2146878287>➞[64r] [ 66r] ➞ [ 66c] And[66r]➞[61r, 63r, 65r] [ 67r] ➞ [ 67c]
 * Some[67r] Role group (SOLOR) <-2147483593>➞[66r] [ 68r] ➞ [ 68c] Concept[68r]
 * Dislocation (morphologic abnormality) <-2147448026>
 * [ 69r] ➞ [ 69c] Some[69r] Direct morphology (attribute) <-2147378241>➞[68r] [
 * 70r] ➞ [ 70c] Concept[70r] Open approach - access (qualifier value)
 * <-2146941428>
 * [ 71r] ➞ [ 71c] Some[71r] Access (attribute) <-2147315914>➞[70r] [ 72r] ➞ [
 * 72c] Concept[72r] Reduction - action (qualifier value) <-2146938668>
 * [ 73r] ➞ [ 73c] Some[73r] Method (attribute) <-2147314116>➞[72r] [ 74r] ➞ [
 * 74c] Concept[74r] Joint structure (body structure) <-2146932341>
 * [ 75r] ➞ [ 75c] Some[75r] Procedure site - Direct (attribute)
 * <-2146878287>➞[74r] [ 76r] ➞ [ 76c] And[76r]➞[69r, 71r, 73r, 75r] [ 77r] ➞ [
 * 77c] Some[77r] Role group (SOLOR) <-2147483593>➞[76r] [ 78r] ➞ [ 78c]
 * Concept[78r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [ 79r] ➞ [ 79c] Some[79r] Direct morphology (attribute) <-2147378241>➞[78r] [
 * 80r] ➞ [ 80c] Concept[80r] Open approach - access (qualifier value)
 * <-2146941428>
 * [ 81r] ➞ [ 81c] Some[81r] Access (attribute) <-2147315914>➞[80r] [ 82r] ➞ [
 * 82c] Concept[82r] Reduction - action (qualifier value) <-2146938668>
 * [ 83r] ➞ [ 83c] Some[83r] Method (attribute) <-2147314116>➞[82r] [ 84r] ➞ [
 * 84c] Concept[84r] Bone structure (body structure) <-2147146938>
 * [ 85r] ➞ [ 85c] Some[85r] Procedure site - Indirect (attribute)
 * <-2146878264>➞[84r] [ 86r] ➞ [ 86c] And[86r]➞[79r, 81r, 83r, 85r] [ 87r] ➞ [
 * 87c] Some[87r] Role group (SOLOR) <-2147483593>➞[86r] [ 88r] ➞ [ 88c]
 * Concept[88r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [ 89r] ➞ [ 89c] Some[89r] Direct morphology (attribute) <-2147378241>➞[88r] [
 * 90r] ➞ [ 90c] Concept[90r] Fixation - action (qualifier value) <-2146939443>
 * [ 91r] ➞ [ 91c] Some[91r] Method (attribute) <-2147314116>➞[90r] [ 92r] ➞ [
 * 92c] Concept[92r] Bone structure (body structure) <-2147146938>
 * [ 93r] ➞ [ 93c] Some[93r] Procedure site - Direct (attribute)
 * <-2146878287>➞[92r] [ 94r] ➞ [ 94c] Concept[94r] Functional brace (physical
 * object) <-2147091701>
 * [ 95r] ➞ [ 95c] Some[95r] Using device (attribute) <-2146621201>➞[94r] [ 96r]
 * ➞ [ 96c] And[96r]➞[89r, 91r, 93r, 95r] [ 97r] ➞ [ 97c] Some[97r] Role group
 * (SOLOR) <-2147483593>➞[96r] [ 98r] ➞ [ 98c] Concept[98r] Fracture
 * (morphologic abnormality) <-2146461022>
 * [ 99r] ➞ [ 99c] Some[99r] Direct morphology (attribute) <-2147378241>➞[98r]
 * [100r] ➞ [100c] Concept[100r] Open reduction - action (qualifier value)
 * <-2146585859>
 * [101r] ➞ [101c] Some[101r] Method (attribute) <-2147314116>➞[100r] [102r] ➞
 * [102c] And[102r]➞[99r, 101r] [103r] ➞ [103c] Some[103r] Role group (SOLOR)
 * <-2147483593>➞[102r] [104r] ➞ [104c] Concept[104r] Dislocation (morphologic
 * abnormality) <-2147448026>
 * [105r] ➞ [105c] Some[105r] Direct morphology (attribute) <-2147378241>➞[104r]
 * [106r] ➞ [106c] Concept[106r] Open reduction - action (qualifier value)
 * <-2146585859>
 * [107r] ➞ [107c] Some[107r] Method (attribute) <-2147314116>➞[106r] [108r] ➞
 * [108c] Concept[108r] Joint structure (body structure) <-2146932341>
 * [109r] ➞ [109c] Some[109r] Procedure site - Direct (attribute)
 * <-2146878287>➞[108r] [110r] ➞ [110c] And[110r]➞[105r, 107r, 109r] [111r] ➞
 * [111c] Some[111r] Role group (SOLOR) <-2147483593>➞[110r] [112r] ➞ [112c]
 * Concept[112r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [113r] ➞ [113c] Some[113r] Direct morphology (attribute) <-2147378241>➞[112r]
 * [114r] ➞ [114c] Concept[114r] Reduction - action (qualifier value)
 * <-2146938668>
 * [115r] ➞ [115c] Some[115r] Method (attribute) <-2147314116>➞[114r] [116r] ➞
 * [116c] Concept[116r] Joint structure (body structure) <-2146932341>
 * [117r] ➞ [117c] Some[117r] Procedure site - Direct (attribute)
 * <-2146878287>➞[116r] [118r] ➞ [118c] And[118r]➞[113r, 115r, 117r] [119r] ➞
 * [119c] Some[119r] Role group (SOLOR) <-2147483593>➞[118r] [120r] ➞ [120c]
 * Concept[120r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [121r] ➞ [121c] Some[121r] Direct morphology (attribute) <-2147378241>➞[120r]
 * [122r] ➞ [122c] Concept[122r] Reduction - action (qualifier value)
 * <-2146938668>
 * [123r] ➞ [123c] Some[123r] Method (attribute) <-2147314116>➞[122r] [124r] ➞
 * [124c] Concept[124r] Bone structure (body structure) <-2147146938>
 * [125r] ➞ [125c] Some[125r] Procedure site - Direct (attribute)
 * <-2146878287>➞[124r] [126r] ➞ [126c] Concept[126r] Primary operation
 * (qualifier value) <-2147302589>
 * [127r] ➞ [127c] Some[127r] Revision status (attribute) <-2146315099>➞[126r]
 * [128r] ➞ [128c] And[128r]➞[121r, 123r, 125r, 127r] [129r] ➞ [129c] Some[129r]
 * Role group (SOLOR) <-2147483593>➞[128r] [130r] ➞ [130c] Concept[130r]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [131r] ➞ [131c] Some[131r] Direct morphology (attribute) <-2147378241>➞[130r]
 * [132r] ➞ [132c] Concept[132r] Open approach - access (qualifier value)
 * <-2146941428>
 * [133r] ➞ [133c] Some[133r] Access (attribute) <-2147315914>➞[132r] [134r] ➞
 * [134c] Concept[134r] Reduction - action (qualifier value) <-2146938668>
 * [135r] ➞ [135c] Some[135r] Method (attribute) <-2147314116>➞[134r] [136r] ➞
 * [136c] Concept[136r] Bone structure (body structure) <-2147146938>
 * [137r] ➞ [137c] Some[137r] Procedure site - Indirect (attribute)
 * <-2146878264>➞[136r] [138r] ➞ [138c] Concept[138r] Primary operation
 * (qualifier value) <-2147302589>
 * [139r] ➞ [139c] Some[139r] Revision status (attribute) <-2146315099>➞[138r]
 * [140r] ➞ [140c] And[140r]➞[131r, 133r, 135r, 137r, 139r] [141r] ➞ [141c]
 * Some[141r] Role group (SOLOR) <-2147483593>➞[140r] [142r] ➞ [142c]
 * Concept[142r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [143r] ➞ [143c] Some[143r] Direct morphology (attribute) <-2147378241>➞[142r]
 * [144r] ➞ [144c] Concept[144r] Fixation - action (qualifier value)
 * <-2146939443>
 * [145r] ➞ [145c] Some[145r] Method (attribute) <-2147314116>➞[144r] [146r] ➞
 * [146c] Concept[146r] Bone structure (body structure) <-2147146938>
 * [147r] ➞ [147c] Some[147r] Procedure site - Direct (attribute)
 * <-2146878287>➞[146r] [148r] ➞ [148c] Concept[148r] Functional brace (physical
 * object) <-2147091701>
 * [149r] ➞ [149c] Some[149r] Using device (attribute) <-2146621201>➞[148r]
 * [150r] ➞ [150c] Concept[150r] Primary operation (qualifier value)
 * <-2147302589>
 * [151r] ➞ [151c] Some[151r] Revision status (attribute) <-2146315099>➞[150r]
 * [152r] ➞ [152c] And[152r]➞[143r, 145r, 147r, 149r, 151r] [153r] ➞ [153c]
 * Some[153r] Role group (SOLOR) <-2147483593>➞[152r] [154r] ➞ [154c]
 * Concept[154r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [155r] ➞ [155c] Some[155r] Direct morphology (attribute) <-2147378241>➞[154r]
 * [156r] ➞ [156c] Concept[156r] Open reduction - action (qualifier value)
 * <-2146585859>
 * [157r] ➞ [157c] Some[157r] Method (attribute) <-2147314116>➞[156r] [158r] ➞
 * [158c] Concept[158r] Joint structure (body structure) <-2146932341>
 * [159r] ➞ [159c] Some[159r] Procedure site - Direct (attribute)
 * <-2146878287>➞[158r] [160r] ➞ [160c] And[160r]➞[155r, 157r, 159r] [161r] ➞
 * [161c] Some[161r] Role group (SOLOR) <-2147483593>➞[160r] [162r] ➞ [162c]
 * Concept[162r] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * [163r] ➞ [163c] Some[163r] Direct morphology (attribute) <-2147378241>➞[162r]
 * [164r] ➞ [164c] Concept[164r] Open reduction - action (qualifier value)
 * <-2146585859>
 * [165r] ➞ [165c] Some[165r] Method (attribute) <-2147314116>➞[164r] [166r] ➞
 * [166c] Concept[166r] Bone structure (body structure) <-2147146938>
 * [167r] ➞ [167c] Some[167r] Procedure site - Direct (attribute)
 * <-2146878287>➞[166r] [168r] ➞ [168c] Concept[168r] Primary operation
 * (qualifier value) <-2147302589>
 * [169r] ➞ [169c] Some[169r] Revision status (attribute) <-2146315099>➞[168r]
 * [170r] ➞ [170c] And[170r]➞[163r, 165r, 167r, 169r] [171r] ➞ [171c] Some[171r]
 * Role group (SOLOR) <-2147483593>➞[170r] [172r] ➞ [183c]* Concept[172r]
 * Fixation - action (qualifier value) <-2146939443>
 * [173r] ➞ [184c]* Some[173r] Method (attribute) <-2147314116>➞[172r] [174r] ➞
 * [185c]* Concept[174r] Bone and/or joint structure (body structure)
 * <-2146558726>
 * [175r] ➞ [186c]* Some[175r] Procedure site - Direct (attribute)
 * <-2146878287>➞[174r] [176r] ➞ [187c]* Concept[176r] Functional brace
 * (physical object) <-2147091701>
 * [177r] ➞ [188c]* Some[177r] Using device (attribute) <-2146621201>➞[176r]
 * [178r] ➞ [189c]* And[178r]➞[173r, 175r, 177r] [179r] ➞ [190c]* Some[179r]
 * Role group (SOLOR) <-2147483593>➞[178r] [180r] ➞ [191c]* Concept[180r]
 * Dislocation (morphologic abnormality) <-2147448026>
 * [181r] ➞ [192c]* Some[181r] Direct morphology (attribute)
 * <-2147378241>➞[180r] [182r] ➞ [193c]* Concept[182r] Open reduction - action
 * (qualifier value) <-2146585859>
 * [183r] ➞ [194c]* Some[183r] Method (attribute) <-2147314116>➞[182r] [184r] ➞
 * [195c]* Concept[184r] Joint structure (body structure) <-2146932341>
 * [185r] ➞ [196c]* Some[185r] Procedure site - Direct (attribute)
 * <-2146878287>➞[184r] [186r] ➞ [197c]* Concept[186r] Primary operation
 * (qualifier value) <-2147302589>
 * [187r] ➞ [198c]* Some[187r] Revision status (attribute) <-2146315099>➞[186r]
 * [188r] ➞ [199c]* And[188r]➞[181r, 183r, 185r, 187r] [189r] ➞ [200c]*
 * Some[189r] Role group (SOLOR) <-2147483593>➞[188r] [190r] ➞ [201c]*
 * Concept[190r] Fracture (morphologic abnormality) <-2146461022>
 * [191r] ➞ [202c]* Some[191r] Direct morphology (attribute)
 * <-2147378241>➞[190r] [192r] ➞ [203c]* Concept[192r] Open reduction - action
 * (qualifier value) <-2146585859>
 * [193r] ➞ [204c]* Some[193r] Method (attribute) <-2147314116>➞[192r] [194r] ➞
 * [205c]* Concept[194r] Bone structure (body structure) <-2147146938>
 * [195r] ➞ [206c]* Some[195r] Procedure site - Direct (attribute)
 * <-2146878287>➞[194r] [196r] ➞ [207c]* Concept[196r] Primary operation
 * (qualifier value) <-2147302589>
 * [197r] ➞ [208c]* Some[197r] Revision status (attribute) <-2146315099>➞[196r]
 * [198r] ➞ [209c]* And[198r]➞[191r, 193r, 195r, 197r] [199r] ➞ [210c]*
 * Some[199r] Role group (SOLOR) <-2147483593>➞[198r] [200r] ➞ [172c]*
 * Concept[200r] Procedure categorized by device involved (procedure)
 * <-2147378462>
 * [201r] ➞ [173c]* Concept[201r] Open reduction of fracture with fixation
 * (procedure) <-2147062793>
 * [202r] ➞ [174c]* Concept[202r] Skeletal fixation procedure (procedure)
 * <-2147013539>
 * [203r] ➞ [175c]* Concept[203r] Reduction of fracture (procedure)
 * <-2147008610>
 * [204r] ➞ [176c]* Concept[204r] Primary open reduction of fracture dislocation
 * (procedure) <-2146647907>
 * [205r] ➞ [177c]* Concept[205r] Procedure involving splint (procedure)
 * <-2146632066>
 * [206r] ➞ [178c]* Concept[206r] Operation on fracture (procedure)
 * <-2146429781>
 * [207r] ➞ [179c]* Concept[207r] Fixation of fracture (procedure) <-2146429760>
 * [208r] ➞ [180c]* And[208r]➞[3r, 7r, 11r, 15r, 19r, 23r, 27r, 31r, 37r, 41r,
 * 49r, 59r, 67r, 77r, 87r, 97r, 103r, 111r, 119r, 129r, 141r, 153r, 161r, 171r,
 * 179r, 189r, 199r, 211r, 200r, 201r, 202r, 203r, 204r, 205r, 206r, 207r]
 * [209r] ➞ [181c]* Necessary[209r]➞[208r] [210r] ➞ [182c]* Root[210r]➞[209r]
 * [211r] ➞ ∅  *
 * Additions:  *
 * Concept[211r] Fixation (procedure) <-2147467999>
 *
 *
Deletions:  *
 *
 * Shared relationship roots:  *
 * Concept[172] Procedure categorized by device involved (procedure)
 * <-2147378462>
 *
  Concept[173] Open reduction of fracture with fixation (procedure)
 * <-2147062793>
 *
  Concept[174] Skeletal fixation procedure (procedure) <-2147013539>
 *
  Concept[175] Reduction of fracture (procedure) <-2147008610>
 *
  Concept[176] Primary open reduction of fracture dislocation (procedure)
 * <-2146647907>
 *
  Concept[177] Procedure involving splint (procedure) <-2146632066>
 *
  Concept[178] Operation on fracture (procedure) <-2146429781>
 *
  Concept[179] Fixation of fracture (procedure) <-2146429760>
 *
  Some[7] Role group (SOLOR) <-2147483593>➞[6] And[6]➞[5] Some[5] Direct
 * morphology (attribute) <-2147378241>➞[4] Concept[4] Dislocation (morphologic
 * abnormality) <-2147448026>
 *
  Some[41] Role group (SOLOR) <-2147483593>➞[40] And[40]➞[39] Some[39] Direct
 * morphology (attribute) <-2147378241>➞[38] Concept[38] Fracture-dislocation
 * (morphologic abnormality) <-2147403668>
 *
  Some[19] Role group (SOLOR) <-2147483593>➞[18] And[18]➞[17] Some[17] Direct
 * morphology (attribute) <-2147378241>➞[16] Concept[16] Dislocation of joint
 * (disorder) <-2147196846>
 *
  Some[3] Role group (SOLOR) <-2147483593>➞[2] And[2]➞[1] Some[1] Access
 * (attribute) <-2147315914>➞[0] Concept[0] Open approach - access (qualifier
 * value) <-2146941428>
 *
  Some[23] Role group (SOLOR) <-2147483593>➞[22] And[22]➞[21] Some[21] Revision
 * status (attribute) <-2146315099>➞[20] Concept[20] Primary operation
 * (qualifier value) <-2147302589>
 *
  Some[27] Role group (SOLOR) <-2147483593>➞[26] And[26]➞[25] Some[25] Direct
 * morphology (attribute) <-2147378241>➞[24] Concept[24] Traumatic dislocation
 * (morphologic abnormality) <-2146977295>
 *
  Some[31] Role group (SOLOR) <-2147483593>➞[30] And[30]➞[29] Some[29] Method
 * (attribute) <-2147314116>➞[28] Concept[28] Surgical action (qualifier value)
 * <-2146940928>
 *
  Some[15] Role group (SOLOR) <-2147483593>➞[14] And[14]➞[13] Some[13] Method
 * (attribute) <-2147314116>➞[12] Concept[12] Surgical repair - action
 * (qualifier value) <-2146939778>
 *
  Some[11] Role group (SOLOR) <-2147483593>➞[10] And[10]➞[9] Some[9] Revision
 * status (attribute) <-2146315099>➞[8] Concept[8] Principal (qualifier value)
 * <-2146603744>
 *
  Some[37] Role group (SOLOR) <-2147483593>➞[36] And[36]➞[33, 35] Some[33]
 * Procedure site (attribute) <-2147378082>➞[32] Concept[32] Joint structure
 * (body structure) <-2146932341>
 * Some[35] Method (attribute) <-2147314116>➞[34] Concept[34] Reduction - action
 * (qualifier value) <-2146938668>
 *
  Some[103] Role group (SOLOR) <-2147483593>➞[102] And[102]➞[99, 101] Some[99]
 * Direct morphology (attribute) <-2147378241>➞[98] Concept[98] Fracture
 * (morphologic abnormality) <-2146461022>
 * Some[101] Method (attribute) <-2147314116>➞[100] Concept[100] Open reduction
 * - action (qualifier value) <-2146585859>
 *
  Some[111] Role group (SOLOR) <-2147483593>➞[110] And[110]➞[105, 107, 109]
 * Some[105] Direct morphology (attribute) <-2147378241>➞[104] Concept[104]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[107] Method (attribute) <-2147314116>➞[106] Concept[106] Open reduction
 * - action (qualifier value) <-2146585859>
 * Some[109] Procedure site - Direct (attribute) <-2146878287>➞[108]
 * Concept[108] Joint structure (body structure) <-2146932341>
 *
  Some[49] Role group (SOLOR) <-2147483593>➞[48] And[48]➞[43, 45, 47] Some[43]
 * Direct morphology (attribute) <-2147378241>➞[42] Concept[42] Dislocation
 * (morphologic abnormality) <-2147448026>
 * Some[45] Method (attribute) <-2147314116>➞[44] Concept[44] Reduction - action
 * (qualifier value) <-2146938668>
 * Some[47] Procedure site - Indirect (attribute) <-2146878264>➞[46] Concept[46]
 * Joint structure (body structure) <-2146932341>
 *
  Some[119] Role group (SOLOR) <-2147483593>➞[118] And[118]➞[113, 115, 117]
 * Some[113] Direct morphology (attribute) <-2147378241>➞[112] Concept[112]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[115] Method (attribute) <-2147314116>➞[114] Concept[114] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[117] Procedure site - Direct (attribute) <-2146878287>➞[116]
 * Concept[116] Joint structure (body structure) <-2146932341>
 *
  Some[161] Role group (SOLOR) <-2147483593>➞[160] And[160]➞[155, 157, 159]
 * Some[155] Direct morphology (attribute) <-2147378241>➞[154] Concept[154]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[157] Method (attribute) <-2147314116>➞[156] Concept[156] Open reduction
 * - action (qualifier value) <-2146585859>
 * Some[159] Procedure site - Direct (attribute) <-2146878287>➞[158]
 * Concept[158] Joint structure (body structure) <-2146932341>
 *
  Some[67] Role group (SOLOR) <-2147483593>➞[66] And[66]➞[61, 63, 65] Some[61]
 * Direct morphology (attribute) <-2147378241>➞[60] Concept[60]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[63] Method (attribute) <-2147314116>➞[62] Concept[62] Reduction - action
 * (qualifier value) <-2146938668>
 * Some[65] Procedure site - Direct (attribute) <-2146878287>➞[64] Concept[64]
 * Bone structure (body structure) <-2147146938>
 *
  Some[190] Role group (SOLOR) <-2147483593>➞[189] And[189]➞[184, 186, 188]
 * Some[184] Method (attribute) <-2147314116>➞[183] Concept[183] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[186] Procedure site - Direct (attribute) <-2146878287>➞[185]
 * Concept[185] Bone and/or joint structure (body structure) <-2146558726>
 * Some[188] Using device (attribute) <-2146621201>➞[187] Concept[187]
 * Functional brace (physical object) <-2147091701>
 *
  Some[77] Role group (SOLOR) <-2147483593>➞[76] And[76]➞[69, 71, 73, 75]
 * Some[69] Direct morphology (attribute) <-2147378241>➞[68] Concept[68]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[71] Access (attribute) <-2147315914>➞[70] Concept[70] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[73] Method (attribute) <-2147314116>➞[72] Concept[72] Reduction - action
 * (qualifier value) <-2146938668>
 * Some[75] Procedure site - Direct (attribute) <-2146878287>➞[74] Concept[74]
 * Joint structure (body structure) <-2146932341>
 *
  Some[129] Role group (SOLOR) <-2147483593>➞[128] And[128]➞[121, 123, 125,
 * 127] Some[121] Direct morphology (attribute) <-2147378241>➞[120] Concept[120]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[123] Method (attribute) <-2147314116>➞[122] Concept[122] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[125] Procedure site - Direct (attribute) <-2146878287>➞[124]
 * Concept[124] Bone structure (body structure) <-2147146938>
 * Some[127] Revision status (attribute) <-2146315099>➞[126] Concept[126]
 * Primary operation (qualifier value) <-2147302589>
 *
  Some[171] Role group (SOLOR) <-2147483593>➞[170] And[170]➞[163, 165, 167,
 * 169] Some[163] Direct morphology (attribute) <-2147378241>➞[162] Concept[162]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[165] Method (attribute) <-2147314116>➞[164] Concept[164] Open reduction
 * - action (qualifier value) <-2146585859>
 * Some[167] Procedure site - Direct (attribute) <-2146878287>➞[166]
 * Concept[166] Bone structure (body structure) <-2147146938>
 * Some[169] Revision status (attribute) <-2146315099>➞[168] Concept[168]
 * Primary operation (qualifier value) <-2147302589>
 *
  Some[87] Role group (SOLOR) <-2147483593>➞[86] And[86]➞[79, 81, 83, 85]
 * Some[79] Direct morphology (attribute) <-2147378241>➞[78] Concept[78]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[81] Access (attribute) <-2147315914>➞[80] Concept[80] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[83] Method (attribute) <-2147314116>➞[82] Concept[82] Reduction - action
 * (qualifier value) <-2146938668>
 * Some[85] Procedure site - Indirect (attribute) <-2146878264>➞[84] Concept[84]
 * Bone structure (body structure) <-2147146938>
 *
  Some[200] Role group (SOLOR) <-2147483593>➞[199] And[199]➞[192, 194, 196,
 * 198] Some[192] Direct morphology (attribute) <-2147378241>➞[191] Concept[191]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[194] Method (attribute) <-2147314116>➞[193] Concept[193] Open reduction
 * - action (qualifier value) <-2146585859>
 * Some[196] Procedure site - Direct (attribute) <-2146878287>➞[195]
 * Concept[195] Joint structure (body structure) <-2146932341>
 * Some[198] Revision status (attribute) <-2146315099>➞[197] Concept[197]
 * Primary operation (qualifier value) <-2147302589>
 *
  Some[97] Role group (SOLOR) <-2147483593>➞[96] And[96]➞[89, 91, 93, 95]
 * Some[89] Direct morphology (attribute) <-2147378241>➞[88] Concept[88]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[91] Method (attribute) <-2147314116>➞[90] Concept[90] Fixation - action
 * (qualifier value) <-2146939443>
 * Some[93] Procedure site - Direct (attribute) <-2146878287>➞[92] Concept[92]
 * Bone structure (body structure) <-2147146938>
 * Some[95] Using device (attribute) <-2146621201>➞[94] Concept[94] Functional
 * brace (physical object) <-2147091701>
 *
  Some[210] Role group (SOLOR) <-2147483593>➞[209] And[209]➞[202, 204, 206,
 * 208] Some[202] Direct morphology (attribute) <-2147378241>➞[201] Concept[201]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[204] Method (attribute) <-2147314116>➞[203] Concept[203] Open reduction
 * - action (qualifier value) <-2146585859>
 * Some[206] Procedure site - Direct (attribute) <-2146878287>➞[205]
 * Concept[205] Bone structure (body structure) <-2147146938>
 * Some[208] Revision status (attribute) <-2146315099>➞[207] Concept[207]
 * Primary operation (qualifier value) <-2147302589>
 *
  Some[59] Role group (SOLOR) <-2147483593>➞[58] And[58]➞[51, 53, 55, 57]
 * Some[51] Direct morphology (attribute) <-2147378241>➞[50] Concept[50]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[53] Access (attribute) <-2147315914>➞[52] Concept[52] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[55] Method (attribute) <-2147314116>➞[54] Concept[54] Reduction - action
 * (qualifier value) <-2146938668>
 * Some[57] Procedure site - Indirect (attribute) <-2146878264>➞[56] Concept[56]
 * Bone structure (body structure) <-2147146938>
 *
  Some[141] Role group (SOLOR) <-2147483593>➞[140] And[140]➞[131, 133, 135,
 * 137, 139] Some[131] Direct morphology (attribute) <-2147378241>➞[130]
 * Concept[130] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[133] Access (attribute) <-2147315914>➞[132] Concept[132] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[135] Method (attribute) <-2147314116>➞[134] Concept[134] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[137] Procedure site - Indirect (attribute) <-2146878264>➞[136]
 * Concept[136] Bone structure (body structure) <-2147146938>
 * Some[139] Revision status (attribute) <-2146315099>➞[138] Concept[138]
 * Primary operation (qualifier value) <-2147302589>
 *
  Some[153] Role group (SOLOR) <-2147483593>➞[152] And[152]➞[143, 145, 147,
 * 149, 151] Some[143] Direct morphology (attribute) <-2147378241>➞[142]
 * Concept[142] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[145] Method (attribute) <-2147314116>➞[144] Concept[144] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[147] Procedure site - Direct (attribute) <-2146878287>➞[146]
 * Concept[146] Bone structure (body structure) <-2147146938>
 * Some[149] Using device (attribute) <-2146621201>➞[148] Concept[148]
 * Functional brace (physical object) <-2147091701>
 * Some[151] Revision status (attribute) <-2146315099>➞[150] Concept[150]
 * Primary operation (qualifier value) <-2147302589>
 *
 *
New relationship roots:  *
 * Concept[211] Fixation (procedure) <-2147467999>
 *
 *
Deleted relationship roots:  *
 *
 * Merged expression:  *
 * Root[211m]➞[210m] Necessary[210m]➞[209m] And[209m]➞[3m, 7m, 11m, 15m, 19m,
 * 23m, 27m, 31m, 37m, 41m, 49m, 59m, 67m, 77m, 87m, 97m, 103m, 111m, 119m,
 * 129m, 141m, 153m, 161m, 171m, 179m, 189m, 199m, 200m, 201m, 202m, 203m, 204m,
 * 205m, 206m, 207m, 208m] Some[3m] Role group (SOLOR) <-2147483593>➞[2m]
 * And[2m]➞[1m] Some[1m] Access (attribute) <-2147315914>➞[0m] Concept[0m] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[7m] Role group (SOLOR) <-2147483593>➞[6m] And[6m]➞[5m] Some[5m] Direct
 * morphology (attribute) <-2147378241>➞[4m] Concept[4m] Dislocation
 * (morphologic abnormality) <-2147448026>
 * Some[11m] Role group (SOLOR) <-2147483593>➞[10m] And[10m]➞[9m] Some[9m]
 * Revision status (attribute) <-2146315099>➞[8m] Concept[8m] Principal
 * (qualifier value) <-2146603744>
 * Some[15m] Role group (SOLOR) <-2147483593>➞[14m] And[14m]➞[13m] Some[13m]
 * Method (attribute) <-2147314116>➞[12m] Concept[12m] Surgical repair - action
 * (qualifier value) <-2146939778>
 * Some[19m] Role group (SOLOR) <-2147483593>➞[18m] And[18m]➞[17m] Some[17m]
 * Direct morphology (attribute) <-2147378241>➞[16m] Concept[16m] Dislocation of
 * joint (disorder) <-2147196846>
 * Some[23m] Role group (SOLOR) <-2147483593>➞[22m] And[22m]➞[21m] Some[21m]
 * Revision status (attribute) <-2146315099>➞[20m] Concept[20m] Primary
 * operation (qualifier value) <-2147302589>
 * Some[27m] Role group (SOLOR) <-2147483593>➞[26m] And[26m]➞[25m] Some[25m]
 * Direct morphology (attribute) <-2147378241>➞[24m] Concept[24m] Traumatic
 * dislocation (morphologic abnormality) <-2146977295>
 * Some[31m] Role group (SOLOR) <-2147483593>➞[30m] And[30m]➞[29m] Some[29m]
 * Method (attribute) <-2147314116>➞[28m] Concept[28m] Surgical action
 * (qualifier value) <-2146940928>
 * Some[37m] Role group (SOLOR) <-2147483593>➞[36m] And[36m]➞[33m, 35m]
 * Some[33m] Procedure site (attribute) <-2147378082>➞[32m] Concept[32m] Joint
 * structure (body structure) <-2146932341>
 * Some[35m] Method (attribute) <-2147314116>➞[34m] Concept[34m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[41m] Role group (SOLOR) <-2147483593>➞[40m] And[40m]➞[39m] Some[39m]
 * Direct morphology (attribute) <-2147378241>➞[38m] Concept[38m]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[49m] Role group (SOLOR) <-2147483593>➞[48m] And[48m]➞[43m, 45m, 47m]
 * Some[43m] Direct morphology (attribute) <-2147378241>➞[42m] Concept[42m]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[45m] Method (attribute) <-2147314116>➞[44m] Concept[44m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[47m] Procedure site - Indirect (attribute) <-2146878264>➞[46m]
 * Concept[46m] Joint structure (body structure) <-2146932341>
 * Some[59m] Role group (SOLOR) <-2147483593>➞[58m] And[58m]➞[51m, 53m, 55m,
 * 57m] Some[51m] Direct morphology (attribute) <-2147378241>➞[50m] Concept[50m]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[53m] Access (attribute) <-2147315914>➞[52m] Concept[52m] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[55m] Method (attribute) <-2147314116>➞[54m] Concept[54m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[57m] Procedure site - Indirect (attribute) <-2146878264>➞[56m]
 * Concept[56m] Bone structure (body structure) <-2147146938>
 * Some[67m] Role group (SOLOR) <-2147483593>➞[66m] And[66m]➞[61m, 63m, 65m]
 * Some[61m] Direct morphology (attribute) <-2147378241>➞[60m] Concept[60m]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[63m] Method (attribute) <-2147314116>➞[62m] Concept[62m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[65m] Procedure site - Direct (attribute) <-2146878287>➞[64m]
 * Concept[64m] Bone structure (body structure) <-2147146938>
 * Some[77m] Role group (SOLOR) <-2147483593>➞[76m] And[76m]➞[69m, 71m, 73m,
 * 75m] Some[69m] Direct morphology (attribute) <-2147378241>➞[68m] Concept[68m]
 * Dislocation (morphologic abnormality) <-2147448026>
 * Some[71m] Access (attribute) <-2147315914>➞[70m] Concept[70m] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[73m] Method (attribute) <-2147314116>➞[72m] Concept[72m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[75m] Procedure site - Direct (attribute) <-2146878287>➞[74m]
 * Concept[74m] Joint structure (body structure) <-2146932341>
 * Some[87m] Role group (SOLOR) <-2147483593>➞[86m] And[86m]➞[79m, 81m, 83m,
 * 85m] Some[79m] Direct morphology (attribute) <-2147378241>➞[78m] Concept[78m]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[81m] Access (attribute) <-2147315914>➞[80m] Concept[80m] Open approach -
 * access (qualifier value) <-2146941428>
 * Some[83m] Method (attribute) <-2147314116>➞[82m] Concept[82m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[85m] Procedure site - Indirect (attribute) <-2146878264>➞[84m]
 * Concept[84m] Bone structure (body structure) <-2147146938>
 * Some[97m] Role group (SOLOR) <-2147483593>➞[96m] And[96m]➞[89m, 91m, 93m,
 * 95m] Some[89m] Direct morphology (attribute) <-2147378241>➞[88m] Concept[88m]
 * Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[91m] Method (attribute) <-2147314116>➞[90m] Concept[90m] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[93m] Procedure site - Direct (attribute) <-2146878287>➞[92m]
 * Concept[92m] Bone structure (body structure) <-2147146938>
 * Some[95m] Using device (attribute) <-2146621201>➞[94m] Concept[94m]
 * Functional brace (physical object) <-2147091701>
 * Some[103m] Role group (SOLOR) <-2147483593>➞[102m] And[102m]➞[99m, 101m]
 * Some[99m] Direct morphology (attribute) <-2147378241>➞[98m] Concept[98m]
 * Fracture (morphologic abnormality) <-2146461022>
 * Some[101m] Method (attribute) <-2147314116>➞[100m] Concept[100m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[111m] Role group (SOLOR) <-2147483593>➞[110m] And[110m]➞[105m, 107m,
 * 109m] Some[105m] Direct morphology (attribute) <-2147378241>➞[104m]
 * Concept[104m] Dislocation (morphologic abnormality) <-2147448026>
 * Some[107m] Method (attribute) <-2147314116>➞[106m] Concept[106m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[109m] Procedure site - Direct (attribute) <-2146878287>➞[108m]
 * Concept[108m] Joint structure (body structure) <-2146932341>
 * Some[119m] Role group (SOLOR) <-2147483593>➞[118m] And[118m]➞[113m, 115m,
 * 117m] Some[113m] Direct morphology (attribute) <-2147378241>➞[112m]
 * Concept[112m] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[115m] Method (attribute) <-2147314116>➞[114m] Concept[114m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[117m] Procedure site - Direct (attribute) <-2146878287>➞[116m]
 * Concept[116m] Joint structure (body structure) <-2146932341>
 * Some[129m] Role group (SOLOR) <-2147483593>➞[128m] And[128m]➞[121m, 123m,
 * 125m, 127m] Some[121m] Direct morphology (attribute) <-2147378241>➞[120m]
 * Concept[120m] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[123m] Method (attribute) <-2147314116>➞[122m] Concept[122m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[125m] Procedure site - Direct (attribute) <-2146878287>➞[124m]
 * Concept[124m] Bone structure (body structure) <-2147146938>
 * Some[127m] Revision status (attribute) <-2146315099>➞[126m] Concept[126m]
 * Primary operation (qualifier value) <-2147302589>
 * Some[141m] Role group (SOLOR) <-2147483593>➞[140m] And[140m]➞[131m, 133m,
 * 135m, 137m, 139m] Some[131m] Direct morphology (attribute)
 * <-2147378241>➞[130m] Concept[130m] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[133m] Access (attribute) <-2147315914>➞[132m] Concept[132m] Open
 * approach - access (qualifier value) <-2146941428>
 * Some[135m] Method (attribute) <-2147314116>➞[134m] Concept[134m] Reduction -
 * action (qualifier value) <-2146938668>
 * Some[137m] Procedure site - Indirect (attribute) <-2146878264>➞[136m]
 * Concept[136m] Bone structure (body structure) <-2147146938>
 * Some[139m] Revision status (attribute) <-2146315099>➞[138m] Concept[138m]
 * Primary operation (qualifier value) <-2147302589>
 * Some[153m] Role group (SOLOR) <-2147483593>➞[152m] And[152m]➞[143m, 145m,
 * 147m, 149m, 151m] Some[143m] Direct morphology (attribute)
 * <-2147378241>➞[142m] Concept[142m] Fracture-dislocation (morphologic
 * abnormality) <-2147403668>
 * Some[145m] Method (attribute) <-2147314116>➞[144m] Concept[144m] Fixation -
 * action (qualifier value) <-2146939443>
 * Some[147m] Procedure site - Direct (attribute) <-2146878287>➞[146m]
 * Concept[146m] Bone structure (body structure) <-2147146938>
 * Some[149m] Using device (attribute) <-2146621201>➞[148m] Concept[148m]
 * Functional brace (physical object) <-2147091701>
 * Some[151m] Revision status (attribute) <-2146315099>➞[150m] Concept[150m]
 * Primary operation (qualifier value) <-2147302589>
 * Some[161m] Role group (SOLOR) <-2147483593>➞[160m] And[160m]➞[155m, 157m,
 * 159m] Some[155m] Direct morphology (attribute) <-2147378241>➞[154m]
 * Concept[154m] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[157m] Method (attribute) <-2147314116>➞[156m] Concept[156m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[159m] Procedure site - Direct (attribute) <-2146878287>➞[158m]
 * Concept[158m] Joint structure (body structure) <-2146932341>
 * Some[171m] Role group (SOLOR) <-2147483593>➞[170m] And[170m]➞[163m, 165m,
 * 167m, 169m] Some[163m] Direct morphology (attribute) <-2147378241>➞[162m]
 * Concept[162m] Fracture-dislocation (morphologic abnormality) <-2147403668>
 * Some[165m] Method (attribute) <-2147314116>➞[164m] Concept[164m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[167m] Procedure site - Direct (attribute) <-2146878287>➞[166m]
 * Concept[166m] Bone structure (body structure) <-2147146938>
 * Some[169m] Revision status (attribute) <-2146315099>➞[168m] Concept[168m]
 * Primary operation (qualifier value) <-2147302589>
 * Some[179m] Role group (SOLOR) <-2147483593>➞[178m] And[178m]➞[173m, 175m,
 * 177m] Some[173m] Method (attribute) <-2147314116>➞[172m] Concept[172m]
 * Fixation - action (qualifier value) <-2146939443>
 * Some[175m] Procedure site - Direct (attribute) <-2146878287>➞[174m]
 * Concept[174m] Bone and/or joint structure (body structure) <-2146558726>
 * Some[177m] Using device (attribute) <-2146621201>➞[176m] Concept[176m]
 * Functional brace (physical object) <-2147091701>
 * Some[189m] Role group (SOLOR) <-2147483593>➞[188m] And[188m]➞[181m, 183m,
 * 185m, 187m] Some[181m] Direct morphology (attribute) <-2147378241>➞[180m]
 * Concept[180m] Dislocation (morphologic abnormality) <-2147448026>
 * Some[183m] Method (attribute) <-2147314116>➞[182m] Concept[182m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[185m] Procedure site - Direct (attribute) <-2146878287>➞[184m]
 * Concept[184m] Joint structure (body structure) <-2146932341>
 * Some[187m] Revision status (attribute) <-2146315099>➞[186m] Concept[186m]
 * Primary operation (qualifier value) <-2147302589>
 * Some[199m] Role group (SOLOR) <-2147483593>➞[198m] And[198m]➞[191m, 193m,
 * 195m, 197m] Some[191m] Direct morphology (attribute) <-2147378241>➞[190m]
 * Concept[190m] Fracture (morphologic abnormality) <-2146461022>
 * Some[193m] Method (attribute) <-2147314116>➞[192m] Concept[192m] Open
 * reduction - action (qualifier value) <-2146585859>
 * Some[195m] Procedure site - Direct (attribute) <-2146878287>➞[194m]
 * Concept[194m] Bone structure (body structure) <-2147146938>
 * Some[197m] Revision status (attribute) <-2146315099>➞[196m] Concept[196m]
 * Primary operation (qualifier value) <-2147302589>
 * Concept[200m] Fixation (procedure) <-2147467999>
 * Concept[201m] Procedure categorized by device involved (procedure)
 * <-2147378462>
 * Concept[202m] Open reduction of fracture with fixation (procedure)
 * <-2147062793>
 * Concept[203m] Skeletal fixation procedure (procedure) <-2147013539>
 * Concept[204m] Reduction of fracture (procedure) <-2147008610>
 * Concept[205m] Primary open reduction of fracture dislocation (procedure)
 * <-2146647907>
 * Concept[206m] Procedure involving splint (procedure) <-2146632066>
 * Concept[207m] Operation on fracture (procedure) <-2146429781>
 * Concept[208m] Fixation of fracture (procedure) <-2146429760>
 *
 *
 *
 *
2018-06-30 11:43:40,002 INFO [ISAAC-Q-work-thread 40]
 * (ChronologyUpdate.java:412) - Comparison expression for: 21792
 *
 * public class ComparisonExpression1 {
 *
 */
public class CorrelationProblem4 {

    static LogicalExpression getReferenceExpression() {
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
                                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)
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
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("62035942-762f-314e-a1b7-2ccf3bc9fe15"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                                        ),
                                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
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
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                         ConceptAssertion(Get.concept("3b28612a-e676-3a89-9507-b4719f1fed21"), leb),
                         ConceptAssertion(Get.concept("b60d1462-0b85-30b8-83e6-961de60ed57c"), leb),
                         ConceptAssertion(Get.concept("4ce0576b-b60e-3936-8b6b-1ed5edc60c88"), leb),
                         ConceptAssertion(Get.concept("11bfbd9a-b79a-36b9-9144-d03ddef630f0"), leb),
                         ConceptAssertion(Get.concept("6757bbb3-b2b1-327f-bc09-96ae3578b2cc"), leb),
                         ConceptAssertion(Get.concept("8be65040-3e28-397a-a31e-bbb2ba27fce2"), leb),
                         ConceptAssertion(Get.concept("af8f7177-3976-3835-b03e-b71e3a18835e"), leb),
                         ConceptAssertion(Get.concept("21094df0-549a-3614-8da2-dba1892d1af6"), leb),
                         ConceptAssertion(Get.concept("1e19f6e6-e7a4-3a8f-b3d8-3095096e79bc"), leb)
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
                                                ConceptAssertion(Get.concept("4aef9662-40ba-3bc6-a0c4-59c879e94812"), leb)
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
                                                ConceptAssertion(Get.concept("e9e3a969-dd62-3a90-ab4d-c61d6c080df9"), leb)
                                        ),
                                         SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                         SomeRole(Get.concept("a63f4bf2-a040-11e5-8994-feff819cdc9f"),
                                And(
                                        SomeRole(Get.concept("d0f9e3b1-29e4-399f-b129-36693ba4acbc"),
                                                ConceptAssertion(Get.concept("fb8443dc-1f63-3d1a-90eb-3aa359e2ee83"), leb)
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("62035942-762f-314e-a1b7-2ccf3bc9fe15"), leb)
                                        ),
                                         SomeRole(Get.concept("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"),
                                                ConceptAssertion(Get.concept("f4ec130e-c284-36a1-810d-d028a41ee1c2"), leb)
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
                                        ),
                                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
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
                                        ),
                                         SomeRole(Get.concept("472df387-0193-300f-9184-85b59aa85416"),
                                                ConceptAssertion(Get.concept("eab0c78c-e870-3c17-88e8-ef74b170a735"), leb)
                                        ),
                                         SomeRole(Get.concept("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"),
                                                ConceptAssertion(Get.concept("1824b976-9839-32fd-9a67-1c226a855f62"), leb)
                                        )
                                )
                        ),
                         ConceptAssertion(Get.concept("b60d1462-0b85-30b8-83e6-961de60ed57c"), leb),
                         ConceptAssertion(Get.concept("4ce0576b-b60e-3936-8b6b-1ed5edc60c88"), leb),
                         ConceptAssertion(Get.concept("11bfbd9a-b79a-36b9-9144-d03ddef630f0"), leb),
                         ConceptAssertion(Get.concept("6757bbb3-b2b1-327f-bc09-96ae3578b2cc"), leb),
                         ConceptAssertion(Get.concept("8be65040-3e28-397a-a31e-bbb2ba27fce2"), leb),
                         ConceptAssertion(Get.concept("af8f7177-3976-3835-b03e-b71e3a18835e"), leb),
                         ConceptAssertion(Get.concept("21094df0-549a-3614-8da2-dba1892d1af6"), leb),
                         ConceptAssertion(Get.concept("1e19f6e6-e7a4-3a8f-b3d8-3095096e79bc"), leb)
                )
        );
        return leb.build();
    }

}
