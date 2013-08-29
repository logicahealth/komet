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
package org.ihtsdo.otf.tcc.model.cc;

import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.coordinate.LanguageSort;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "language-sorting-preferences")
public class LanguageSortPrefs {

    public enum LANGUAGE_SORT_PREF {
        TYPE_B4_LANG("type before language"),
        LANG_REFEX("use language refex"), 
        RF2_LANG_REFEX("use RF2 language refex");

        private String desc;

        private LANGUAGE_SORT_PREF(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public LanguageSort getLangSort() {
            switch (this) {
                case LANG_REFEX:
                    return LanguageSort.LANG_REFEX;
                case TYPE_B4_LANG:
                    return LanguageSort.TYPE_BEFORE_LANG;
                case RF2_LANG_REFEX:
                    return LanguageSort.RF2_LANG_REFEX;
            }
            throw new UnsupportedOperationException("Can't handle: " + this);
        }
        public static LANGUAGE_SORT_PREF  getPref(LanguageSort sort) {
            switch (sort) {
                 case LANG_REFEX:
                    return LANG_REFEX;
                case TYPE_BEFORE_LANG:
                    return TYPE_B4_LANG;
                case RF2_LANG_REFEX:
                    return RF2_LANG_REFEX;
            }
            throw new UnsupportedOperationException("Can't handle: " + sort);
        }
    }
    
}
