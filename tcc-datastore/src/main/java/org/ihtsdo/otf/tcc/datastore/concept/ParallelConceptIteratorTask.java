/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.datastore.concept;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class ParallelConceptIteratorTask extends Task<Boolean> {

   /** Field description */
   ParallelConceptIterator iterator;

   /**
    * Constructs ...
    *
    *
    * @param iterator
    */
   public ParallelConceptIteratorTask(ParallelConceptIterator iterator) {
      this.iterator = iterator;
      iterator.setTask(this);
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws Exception
    */
   @Override
   protected Boolean call() throws Exception {
      return iterator.call();
   }

   /**
    * Method description
    *
    *
    * @param string
    */
   @Override
   public void updateMessage(String string) {
      super.updateMessage(string);   
   }

   /**
    * Method description
    *
    *
    * @param d
    * @param d1
    */
   @Override
   public void updateProgress(double d, double d1) {
      super.updateProgress(d, d1);   
   }

   /**
    * Method description
    *
    *
    * @param l
    * @param l1
    */
   @Override
   public void updateProgress(long l, long l1) {
      super.updateProgress(l, l1);  
   }

   /**
    * Method description
    *
    *
    * @param string
    */
   @Override
   public void updateTitle(String string) {
      super.updateTitle(string);    
   }

    @Override
    protected void scheduled() {
        System.out.println(this.getTitle() + " scheduled. ");
        super.scheduled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void running() {
        System.out.println(this.getTitle() + " running. ");
        super.running(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void succeeded() {
        System.out.println(this.getTitle() + " succeeding. ");
        super.succeeded(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void cancelled() {
         System.out.println(this.getTitle() + " canceled. ");
       super.cancelled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void failed() {
        System.out.println(this.getTitle() + " failed. ");
        super.failed(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean cancel(boolean bln) {
        System.out.println(this.getTitle() + " cancel. ");
        return super.cancel(bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void done() {
        System.out.println(this.getTitle() + " done. ");
        super.done(); //To change body of generated methods, choose Tools | Templates.
    }
}
