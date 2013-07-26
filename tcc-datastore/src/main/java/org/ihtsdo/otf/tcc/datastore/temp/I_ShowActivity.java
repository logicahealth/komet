/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.datastore.temp;

import java.awt.event.ActionListener;

import javax.swing.JPanel;


public interface I_ShowActivity extends ActionListener {
    public boolean isStopButtonVisible();

    public void setStopButtonVisible(boolean visible);

    public JPanel getViewPanel(boolean showBorder);

    public void setProgressInfoUpper(String text);

    public void setProgressInfoLower(String text);

    public String getProgressInfoUpper();

    public String getProgressInfoLower();

    public void setIndeterminate(boolean newValue);

    public boolean isIndeterminate();

    public void setMaximum(int n);

    public int getMaximum();

    public void setValue(int n);

    public int getValue();

    public void addRefreshActionListener(ActionListener l);

    public void removeRefreshActionListener(ActionListener l);
    
    public void addStopActionListener(ActionListener l);
    
    public void removeStopActionListener(ActionListener l);

    public void complete() throws ComputationCanceled;
    
    public void removeActivityFromViewer();

    public boolean isComplete() throws ComputationCanceled;

    public boolean isCompleteForComparison();
    
    public long getStartTime();

    public void setStartTime(long time);

    public void addShowActivityListener(I_ShowActivity listener);

    public void removeShowActivityListener(I_ShowActivity listener);

    public boolean isStringPainted();

    public void setStringPainted(boolean stringPainted); // displays a string on

    public void update();
    
    public boolean isCanceled();

	public void cancel();
}
