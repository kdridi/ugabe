package com.arykow.applications.ugabe.client;

public interface VideoScreen {
	public final static int MIN_WIDTH = 160;
	public final static int MIN_HEIGHT = 144;

	public abstract void scaleImage(int scale);
	public abstract int[] getPixels();
	public void swapImage();
}
