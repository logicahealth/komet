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
package sh.isaac.komet.statement;

import java.util.ArrayList;
import java.util.List;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.model.statement.ClinicalStatementImpl;
import sh.komet.gui.control.IsaacPropertyEditorFactory;
import sh.komet.gui.control.concept.PropertySheetItemConceptNidWrapper;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.circumstance.PropertySheetCircumstanceWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class StatementPropertySheet {
    
    private final Manifold manifold;
    
    
    private final PropertySheet propertySheet = new PropertySheet();
    {
        this.propertySheet.setMode(PropertySheet.Mode.NAME);
        this.propertySheet.setSearchBoxVisible(true);
    }

    public StatementPropertySheet(Manifold manifold) {
        this.manifold = manifold;
        this.propertySheet.setPropertyEditorFactory(new IsaacPropertyEditorFactory(this.manifold));
    }
    
    public void setClinicalStatement(ClinicalStatementImpl clinicalStatement) {
        
        propertySheet.getItems().clear();
        if (clinicalStatement != null) {
            propertySheet.getItems().addAll(getProperties(clinicalStatement));
        }
    }
    
    public List<PropertySheet.Item> getProperties(ClinicalStatementImpl clinicalStatement) {
        ArrayList<PropertySheet.Item> items = new ArrayList<>();
        
        items.add(new PropertySheetItemConceptNidWrapper(manifold, 
                clinicalStatement.modeProperty(),
        TermAux.TEMPLATE.getNid(), TermAux.INSTANCE.getNid()));
        
        
        
        items.add(new PropertySheetTextWrapper(manifold, clinicalStatement.narrativeProperty()));
        items.add(new PropertySheetItemConceptNidWrapper(manifold, 
                clinicalStatement.statementTypeProperty(),
            TermAux.REQUEST_STATEMENT.getNid(), TermAux.PERFORMANCE_STATEMENT.getNid()));
        items.add(new PropertySheetItemConceptNidWrapper(manifold, 
                clinicalStatement.subjectOfInformationProperty(), 
                TermAux.SUBJECT_OF_RECORD.getNid(),
                TermAux.MOTHER_OF_SUBJECT_OF_RECORD.getNid(), 
                TermAux.FATHER_OF_SUBJECT_OF_RECORD.getNid(),
                TermAux.MATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD.getNid(),
                TermAux.PATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD.getNid()
                
        ));
        items.add(new PropertySheetItemConceptNidWrapper(manifold, 
                clinicalStatement.topicProperty()));
        items.add(new PropertySheetMeasureWrapper(manifold, clinicalStatement.statementTimeProperty()));
        items.add(new PropertySheetCircumstanceWrapper(manifold, clinicalStatement.circumstanceProperty()));
        
        
        
        return items;
    }

    public PropertySheet getPropertySheet() {
        return propertySheet;
    }
}
