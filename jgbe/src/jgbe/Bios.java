package jgbe;

import java.io.Serializable;

public class Bios implements Serializable {
	public static final int SIZE = 0x100;

	private static final long serialVersionUID = -6789752022323288453L;

	private int[] array;
	private int[] getArray() { return array; }
	private void setArray(int[] array) { this.array = array; }

	public Bios() {
		super();
	}

	public Bios(int[] array) {
		this();
		setArray(array);
	}

	public void update(int[] dst) {
		int[] array = getArray();
		System.arraycopy(array, 0, dst, 0, array.length);
	}
}
