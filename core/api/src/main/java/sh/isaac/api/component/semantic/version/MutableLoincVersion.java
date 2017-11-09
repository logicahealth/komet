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
package sh.isaac.api.component.semantic.version;

/**
 *
 * @author kec
 */
public interface MutableLoincVersion extends LoincVersion {
   void setLoincNum(String value); 
   void setComponent(String value);  
   void setProperty(String value);  
   void setTimeAspect(String value);  
   void setSystem(String value);  
   void setScaleType(String value);  
   void setMethodType(String value);  
   void setStatus(String value);   
   void setShortName(String value);    
   void setLongCommonName(String value); 
   
}
