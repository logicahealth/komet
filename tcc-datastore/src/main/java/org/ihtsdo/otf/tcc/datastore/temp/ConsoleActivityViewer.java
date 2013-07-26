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
package org.ihtsdo.otf.tcc.datastore.temp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ConsoleActivityViewer implements I_ShowActivity {

    long startTime;
    int value;
    int max;
    boolean complete = false;
    private boolean indeterminate = true;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void addRefreshActionListener(ActionListener l) {
        // Nothing to do
    }

    public void complete() {
        this.complete = true;
    }

    public JPanel getViewPanel(boolean showBorder) {
        return null;
    }

    public void removeRefreshActionListener(ActionListener l) {
        // Nothing to do
    }

    public void setIndeterminate(boolean newValue) {
        this.indeterminate = newValue;
    }

    public void setMaximum(int n) {
        max = n;
    }

    public void setProgressInfoLower(String text) {
        // Nothing to do
    }

    public void setProgressInfoUpper(String text) {
        AceLog.getAppLog().info(text);

    }

    public void setValue(int n) {
        value = n;
    }

    public void addShowActivityListener(I_ShowActivity listener) {
    }

    public void removeShowActivityListener(I_ShowActivity listener) {
    }

    public int getMaximum() {
        return max;
    }

    public int getValue() {
        return value;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isIndeterminate() {
        return this.indeterminate;
    }

    public I_ShowActivity getSecondaryPanel() {
        return null;
    }

    public void setSecondaryPanel(I_ShowActivity panel) {
        // Nothing to do
    }

    public void setStringPainted(boolean stringPainted) {
        // Nothing to do
    }

    public JButton getStopButton() {
        return null;
    }

    public void setStopButton(JButton stopButton) {
        // Nothing to do
    }

    public String getProgressInfoLower() {
        return null;
    }

    public String getProgressInfoUpper() {
        return null;
    }

    public boolean isStringPainted() {
        return false;
    }

    public void syncWith(I_ShowActivity another) {
        // Nothing to do
    }

    @Override
    public void removeActivityFromViewer() {
        // Nothing to do
    }

    @Override
    public void update() {
        // Nothing to do
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
    public boolean isStopButtonVisible() {
        return false;
    }

    @Override
    public void setStopButtonVisible(boolean visible) {
        // Nothing to do
    }

    @Override
    public void addStopActionListener(ActionListener l) {
        // Nothing to do
    }

    @Override
    public void removeStopActionListener(ActionListener l) {
        // Nothing to do
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public boolean isCompleteForComparison() {
        return false;
    }

    @Override
    public void cancel() {
        // Nothing to do
    }
}
