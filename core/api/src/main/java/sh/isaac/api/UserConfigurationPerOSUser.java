/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * See {@link UserConfigurationInternalImpl}
 * 
 * Implementations of this class should store their data inside an appropriate user profile
 * folder on the underlying operating system - or, store the data on a remote service where
 * it will survive isaac being removed from the system, and then reinstalled later.
 * 
 * Also, as this store is intended to cross databases, ensure that all int types that represent
 * concepts are translated to UUID for storage.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface UserConfigurationPerOSUser extends UserConfigurationInternalImpl
{

}
