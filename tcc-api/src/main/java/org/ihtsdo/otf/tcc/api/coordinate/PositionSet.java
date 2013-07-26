/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package org.ihtsdo.otf.tcc.api.coordinate;

import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


public class PositionSet implements PositionSetBI, Serializable {
  private static final int dataVersion = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {
          positions = (PositionBI[]) in.readObject();
          pathNids = (NidSetBI) in.readObject();
      }
      else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(positions);
      out.writeObject(pathNids);
   }


    PositionBI[] positions = new PositionBI[0];
    NidSetBI pathNids = new NidSet();

    public PositionSet(Set<? extends PositionBI> positionSet) {
        super();
        if (positionSet != null) {
            this.positions = positionSet.toArray(this.positions);
            for (PositionBI p : positionSet) {
                pathNids.add(p.getPath().getConceptNid());
            }
        }
    }

    public PositionSet(PositionBI viewPosition) {
        if (viewPosition != null) {
            positions = new PositionBI[]{viewPosition};
            pathNids.add(viewPosition.getPath().getConceptNid());
        }
    }

    
    @Override
    public NidSetBI getViewPathNidSet() {
        return pathNids;
    }

    @Override
    public boolean add(PositionBI e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends PositionBI> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        for (PositionBI p : positions) {
            if (p.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return positions.length == 0;
    }

    private class PositionIterator implements Iterator<PositionBI> {

        int index = 0;

        @Override
        public boolean hasNext() {
            return index < positions.length;
        }

        @Override
        public PositionBI next() {
            return positions[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<PositionBI> iterator() {
        return new PositionIterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return positions.length;
    }

    @Override
    public Object[] toArray() {
        return positions.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) positions.clone();
    }

    @Override
    public String toString() {
        return Arrays.asList(positions).toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public PositionBI[] getPositionArray() {
        return positions;
    }

}
