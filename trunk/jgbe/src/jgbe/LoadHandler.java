package jgbe;

import java.io.Serializable;

public interface LoadHandler<T extends Serializable> {
	public void onLoad(T value);
}
