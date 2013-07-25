/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.datastore;

import java.beans.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;

/**
 *
 * @author AKF
 */
public class GlobalPropertyChange {
    
    private static class WeakRefListener implements PropertyChangeListener {
        WeakReference<PropertyChangeListener> wr;
        int hash; 
        String objStr;

        public WeakRefListener(PropertyChangeListener l) {
            this.wr = new WeakReference<>(l);
            objStr = l.toString();
            hash = 7;
            hash = 13 * hash + (this.wr != null ? this.wr.hashCode() : 0);
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            PropertyChangeListener pcl = wr.get();
            try {
                if (pcl != null) {
                    pcl.propertyChange(pce);
                } else {
                    listenerToRemove.add(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listenerToRemove.add(this);
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return this.objStr.equals(obj.toString());
        }
        
        
    }
    
    private static class WeakRefVetoListener implements VetoableChangeListener {
        WeakReference<VetoableChangeListener> wr;
        int hash; 
        String objStr;

        public WeakRefVetoListener(VetoableChangeListener l) {
            this.wr = new WeakReference<>(l);
            this.objStr = l.toString();
            hash = 7;
            hash = 13 * hash + (this.wr != null ? this.wr.hashCode() : 0);
        }


        @Override
        public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
            VetoableChangeListener pcl = wr.get();
            try {
                if (pcl != null) {
                    pcl.vetoableChange(pce);
                } else {
                    vetoListenerToRemove.add(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                vetoListenerToRemove.add(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return this.objStr.equals(obj.toString());
        }

        @Override
        public int hashCode() {
            return hash;
        }
        
        
    }
    
    private static GlobalPropertyChange s = new GlobalPropertyChange();

    private GlobalPropertyChange() {
        gPcs = new PropertyChangeSupport(this);
        gVcs = new VetoableChangeSupport(this);
    }
    private static PropertyChangeSupport gPcs;
    private static VetoableChangeSupport gVcs;
    private static List<PropertyChangeListener> listenerToRemove = new ArrayList<>();
    private static List<VetoableChangeListener> vetoListenerToRemove = new ArrayList<>();
    
    public static void addPropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT eventType, PropertyChangeListener listener) {
        gPcs.addPropertyChangeListener(eventType.toString(), new WeakRefListener(listener));
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        gPcs.removePropertyChangeListener(listener);
    }

    public static void addVetoableChangeListener(TerminologyStoreDI.CONCEPT_EVENT eventType, VetoableChangeListener listener) {
        gVcs.addVetoableChangeListener(eventType.toString(), new WeakRefVetoListener(listener));
    }
    
    public static void removeVetoableChangeListener(VetoableChangeListener listener) {
        gVcs.removeVetoableChangeListener(listener);
    }
    
    public static void firePropertyChange(CONCEPT_EVENT pce, Object oldValue, Object newValue){
        gPcs.firePropertyChange(pce.toString(), oldValue, newValue);
        for (PropertyChangeListener l: listenerToRemove) {
            gPcs.removePropertyChangeListener(l);
        }
    }
    
    public static void fireVetoableChange(CONCEPT_EVENT pce, Object oldValue, Object newValue) throws PropertyVetoException{
        gVcs.fireVetoableChange(pce.toString(), oldValue, newValue);
        for (VetoableChangeListener l: vetoListenerToRemove) {
            gVcs.removeVetoableChangeListener(l);
        }
        gPcs.firePropertyChange(pce.toString(), oldValue, newValue);
        for (PropertyChangeListener l: listenerToRemove) {
            gPcs.removePropertyChangeListener(l);
        }
       
   }

}
