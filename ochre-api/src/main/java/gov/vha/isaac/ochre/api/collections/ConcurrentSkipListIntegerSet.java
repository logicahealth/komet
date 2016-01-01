/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.collections;

import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 */
public class ConcurrentSkipListIntegerSet implements IntSet {
	ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
	
	protected ConcurrentSkipListIntegerSet() {
		
	}

	protected ConcurrentSkipListIntegerSet(int... members) {
		for (int member: members) {
			set.add(member);
		}
	}

	protected ConcurrentSkipListIntegerSet(IntStream memberStream) {
		memberStream.forEach((member) -> set.add(member));
	}
	
	@Override
	public void add(int item) {
		set.add(item);
	}

	@Override
	public void addAll(IntStream intStream) {
		intStream.forEach((item) -> add(item));
	}

	@Override
	public int[] asArray() {
		return stream().toArray();
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean contains(int item) {
		return set.contains(item);
	}

	@Override
	public OptionalInt findFirst() {
		return stream().findFirst();
	}

	@Override
	public PrimitiveIterator.OfInt getIntIterator() {
		return stream().iterator();
	}

	@Override
	public PrimitiveIterator.OfInt getReverseIntIterator() {
		return set.descendingSet().stream().mapToInt(item -> (int) item).iterator();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public IntStream parallelStream() {
		return stream().parallel();
	}

	@Override
	public void remove(int item) {
		set.remove(item);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public IntStream stream() {
		return set.stream().mapToInt(item -> (int) item);
	}

	//
	@Override
	public IntSet and(IntSet otherSet) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public IntSet andNot(IntSet otherSet) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public IntSet or(IntSet otherSet) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public IntSet xor(IntSet otherSet) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String toString() {
		return  set.toString();
	}
	
}
