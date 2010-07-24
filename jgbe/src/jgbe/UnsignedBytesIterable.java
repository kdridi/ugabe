package jgbe;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class UnsignedBytesIterable implements Iterable<Integer>, Serializable {
	private static final long serialVersionUID = -2842160926898881505L;

	private List<Integer> integers;
	public List<Integer> getIntegers() { return integers; }
	public void setIntegers(List<Integer> integers) { this.integers = integers; }

	public UnsignedBytesIterable(List<Integer> integers) {
		super();
		setIntegers(integers);
	}

	public Iterator<Integer> iterator() {
		return integers.iterator();
	}

}
