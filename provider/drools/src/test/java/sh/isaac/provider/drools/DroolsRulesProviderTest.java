/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.drools;

import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.PropertySheet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class DroolsRulesProviderTest {
   DroolsRulesProvider instance;      
   public DroolsRulesProviderTest() {
      instance = new DroolsRulesProvider();  
   }
   
   @BeforeClass
   public static void setUpClass() {
   }
   
   @AfterClass
   public static void tearDownClass() {
   }
   
   @Before
   public void setUp() {
      System.out.println("startMe");
      instance.startMe();
   }
   
   @After
   public void tearDown() {
      System.out.println("stopMe");
      instance.stopMe();
   }

   /**
    * Test of getEditMenuItems method, of class DroolsRulesProvider.
    */
   @Ignore
   @Test
   public void testGetEditMenuItems() {
      Manifold manifold = null;
      ObservableCategorizedVersion categorizedVersion = null;
      Consumer<PropertySheetMenuItem> propertySheetConsumer = null;
      List<MenuItem> result = instance.getEditMenuItems(manifold, categorizedVersion, propertySheetConsumer);
      assertEquals(1, result.size());
   }

   /**
    * Test of getAttachmentMenuItems method, of class DroolsRulesProvider.
    */
   @Ignore
   @Test
   public void testGetAttachmentMenuItems() {
      Manifold manifold = null;
      ObservableCategorizedVersion categorizedVersion = null;
      Consumer<PropertySheetMenuItem> propertySheetConsumer = null;
      List<MenuItem> result = instance.getAttachmentMenuItems(manifold, categorizedVersion, propertySheetConsumer);
      assertEquals(1, result.size());
   }
   
}
