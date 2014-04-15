/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refex4.data;

import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexColumnInfoBI;

/**
 * {@link RefexColumnInfo}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexColumnInfo implements RefexColumnInfoBI {
    private String columnName_;
    private String columnDescription_;
    private int columnOrder_;

    public RefexColumnInfo(String columnName, String columnDescription) {
        columnName_ = columnName;
        columnDescription_ = columnDescription;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexColumnInfoBI#getColumnName()
     */
    @Override
    public String getColumnName() {
        return columnName_;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexColumnInfoBI#getColumnDescription()
     */
    @Override
    public String getColumnDescription() {
        return columnDescription_;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexColumnInfoBI#getColumnOrder()
     */
    @Override
    public int getColumnOrder() {
        return columnOrder_;
    }
}
