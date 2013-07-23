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
package org.ihtsdo.otf.tcc.chronicle.cc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PathBI;

public class PathSetReadOnly implements Set<PathBI> {
	PathBI[] paths = new PathBI[0];
	NidSetBI pathNids = new NidSet();
	
	public NidSetBI getPathNidSet() {
	    return pathNids;
	}
	public PathSetReadOnly(Set<PathBI> paths) {
		super();
		this.paths = paths.toArray(this.paths);
		for (PathBI p: paths) {
		    pathNids.add(p.getConceptNid());
		}
	}

	public PathSetReadOnly(PathBI path) {
		super();
		this.paths = new PathBI[1];
		this.paths[0] = path;
	}

	@Override
	public boolean add(PathBI e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends PathBI> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		for (PathBI p: paths) {
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
		return paths.length == 0;
	}
	
	private class PositionIterator implements Iterator<PathBI> {
		int index = 0;
		@Override
		public boolean hasNext() {
			return index < paths.length;
		}

		@Override
		public PathBI next() {
			return paths[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	@Override
	public Iterator<PathBI> iterator() {
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
		return paths.length;
	}

	@Override
	public Object[] toArray() {
		return paths;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		return (T[]) Arrays.copyOf(paths, paths.length);
	}

	@Override
	public String toString() {
		return Arrays.asList(paths).toString();
	}
	
	
}
