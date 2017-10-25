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
package sh.isaac.api.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 *
 * @author kec
 */
public class AlertObject {
   final String alertTitle;
   final String alertDescription;
   final AlertType alertType;
   final AlertCategory alertCategory;
   final Callable<Boolean> resolutionTester;
   private final List<Resolver> resolvers = new ArrayList();

   public AlertObject(String alertTitle, String alertDescription, AlertType alertType, AlertCategory alertCategory, Callable<Boolean> resolutionTester) {
      this.alertTitle = alertTitle;
      this.alertDescription = alertDescription;
      this.alertType = alertType;
      this.alertCategory = alertCategory;
      this.resolutionTester = resolutionTester;
   }


   public AlertObject(String alertTitle, String alertDescription, AlertType alertType, AlertCategory alertCategory) {
      this(alertTitle, alertDescription, alertType, alertCategory, null);
   }

   public String getAlertTitle() {
      return alertTitle;
   }

   public String getAlertDescription() {
      return alertDescription;
   }

   public AlertType getAlertType() {
      return alertType;
   }

   public AlertCategory getAlertCategory() {
      return alertCategory;
   }

   public Optional<Callable<Boolean>> getResolutionTester() {
      return Optional.ofNullable(resolutionTester);
   }

   public List<Resolver> getResolvers() {
      return resolvers;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + " alertTitle=" + alertTitle + ", alertType=" + alertType + 
              ", resolvers=" + resolvers + ", resolutionTester=" + resolutionTester;
   }
}
