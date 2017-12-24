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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sh.isaac.api;

import java.util.Optional;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

/**
 * 
 * {@link UserService}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface UserService {
   
   /**
    * Add a user to the set of known users
    */
   public void addUser(User user);
   

   /**
    * 
    * This method attempts to retrieve the User object corresponding to the passed user id.
    * 
    * @param userId The UUID of the concept that represents the user
    * @return User object
    * @throws Exception
    */
   public Optional<User> get(UUID userId);
   
   /**
    * 
    * This method attempts to retrieve the User object corresponding to the passed user id.
    * 
    * @param userId The nid or sequence of the concept that represents the user
    * @return User object
    * @throws Exception
    */
   public Optional<User> get(int userId);
}
