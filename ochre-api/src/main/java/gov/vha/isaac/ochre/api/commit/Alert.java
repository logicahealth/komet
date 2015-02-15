/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

/**
 *
 * @author kec
 */
public interface Alert {
    
    AlertType getAlertType();
    
    String getAlertText();
    
    int getComponentNidForAlert();
    
    Object[] getFixups();
    
}
