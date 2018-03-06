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
package sh.komet.gui.provider.concept.detail.logic.panels;

import java.util.Comparator;

/**
 *
 * @author kec
 */
public class LogicDetailPanelComparator implements Comparator<LogicDetailPanel> {

    @Override
    public int compare(LogicDetailPanel o1, LogicDetailPanel o2) {
        if (!o1.getClass().equals(o2.getClass())) {
            if (o1 instanceof LogicDetailConceptPanel) {
                return -1;
            }
            if (o2 instanceof LogicDetailConceptPanel) {
                return 1;
            }
            if (o1 instanceof LogicDetailFeaturePanel) {
                return -1;
            }
            if (o2 instanceof LogicDetailFeaturePanel) {
                return 1;
            }
            if (o1 instanceof LogicDetailRolePanel) {
                return -1;
            }
            if (o2 instanceof LogicDetailRolePanel) {
                return 1;
            }
            throw new UnsupportedOperationException("Can't sort: " + o1.getClass().getName());
        }
        return o1.getLabelText().compareTo(o2.getLabelText());
    }
    
}
