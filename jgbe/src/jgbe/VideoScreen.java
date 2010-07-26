package jgbe;

public interface VideoScreen {
	public abstract int scaleImage(int scale);
	public abstract int[] getPixels();
	public void swapImage();
}
