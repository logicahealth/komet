/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.memory;

import gov.vha.isaac.ochre.api.memory.MemoryConfigurations;
import gov.vha.isaac.ochre.api.memory.MemoryManagementService;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
public class MemoryManagementProvider implements MemoryManagementService {

    @Override
    public void setMemoryConfiguration(MemoryConfigurations memoryConfiguration) {
        System.out.println("Setting memory configuration to: " + memoryConfiguration);
        switch (memoryConfiguration) {
            case CLASSIFY:
                // reclaim as much memory as possible...
                HoldInMemoryCache.clearCache();
                WriteToDiskCache.flushAndClearCache();
                break;
            case IMPORT:
                default:
        }
    }
    
}
