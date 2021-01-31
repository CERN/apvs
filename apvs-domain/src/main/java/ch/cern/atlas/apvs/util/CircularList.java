package ch.cern.atlas.apvs.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class CircularList<T> extends AbstractList<T> implements RandomAccess {

	private final int n;
	private final List<T> list;
	private int head;
	private int tail;

	public CircularList(int capacity) {
		n = capacity + 1;
		list = new ArrayList<T>(Collections.nCopies(n, (T) null));
	}

	public int capacity() {
		return n - 1;
	}

	private int index(int i) {
		int m = i % n;
		if (m < 0) {
			m += n;
		}
		return m;
	}

	private void shift(int startIndex, int endIndex) {
		assert (endIndex > startIndex);
		for (int i = endIndex - 1; i >= startIndex; i--) {
			set(i + 1, get(i));
		}
	}

	@Override
	public int size() {
		return tail - head + (tail < head ? n : 0);
	}

	@Override
	public T get(int i) {
		if (i < 0 || i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		return list.get(index(head + i));
	}

	@Override
	public T set(int i, T e) {
		if (i < 0 || i >= size()) {
			throw new IndexOutOfBoundsException();
		}
		return list.set(index(head + i), e);
	}

	@Override
	public void add(int i, T e) {
		int s = size();
		if (s == n - 1) {
			throw new IllegalStateException("CircularList is full, cannot add element");
		}
		if (i < 0 || i > s) {
			throw new IndexOutOfBoundsException();
		}
		tail = index(tail + 1);
		if (i < s) {
			shift(i, s);
		}
		set(i, e);
	}

	@Override
	public T remove(int i) {
		int s = size();
		if (i < 0 || i >= s) {
			throw new IndexOutOfBoundsException();
		}
		T e = get(i);
		if (i > 0) {
			shift(0, i);
		}
		head = index(head + 1);
		return e;
	}
}
