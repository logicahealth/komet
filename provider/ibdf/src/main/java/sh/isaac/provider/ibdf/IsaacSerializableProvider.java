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
package sh.isaac.provider.ibdf;

import java.nio.ByteBuffer;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.SerializationService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampUniversal;
import sh.isaac.api.identity.IdentifiedObject;

/**
 *
 * @author kec
 */
@Service(name = "isaac-serializer")
@Singleton
public class IsaacSerializableProvider implements SerializationService {

   @Override
   public ByteBuffer toBytes(IdentifiedObject object) {
      //TODO support versions, by serilizing the chronicle, but returning the correct version of the chronicle...
      IsaacExternalizable externalizable = (IsaacExternalizable) object;
      ByteArrayDataBuffer dataBuffer = new ByteArrayDataBuffer();
      dataBuffer.setExternalData(true);
      externalizable.getExternalizableObjectType().writeToByteArrayDataBuffer(dataBuffer);
      externalizable.putExternal(dataBuffer);
      return ByteBuffer.wrap(dataBuffer.getData());
   }

   @Override
   public <T extends IdentifiedObject> T  toObject(ByteBuffer bytes) {
      ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(bytes.array());
      IsaacExternalizableUnparsed unparsed = new IsaacExternalizableUnparsed(buffer);
      IsaacExternalizable parsed = unparsed.parse();
      if (parsed instanceof StampUniversal) {
         StampUniversal stampUniversal = (StampUniversal) parsed;
         int stampSequence = stampUniversal.getStampSequence();
         IsaacExternalizableUnparsed unparsedTwo = new IsaacExternalizableUnparsed(buffer);
         Chronology chronology = (Chronology) unparsedTwo.parse();
         for (Version v: chronology.getVersionList()) {
            if (v.getStampSequence() == stampSequence) {
               return (T) v;
            }
         }
         throw new IllegalStateException("Can't find specified version: " + 
                 stampUniversal + " \n in: " + chronology);
      }
      
      return (T) parsed;
   }

}
