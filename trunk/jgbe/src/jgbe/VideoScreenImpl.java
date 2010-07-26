package jgbe;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class VideoScreenImpl implements VideoScreen {
	private final static int MIN_WIDTH = 160;
	private final static int MIN_HEIGHT = 144;

	private int curDrawImg = 0;
	private BufferedImage drawImg[] = new BufferedImage[2];

	public int scaleImage(int scale) {
		int width = scale * MIN_WIDTH;
		int height = scale * MIN_HEIGHT;
		drawImg[0] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		drawImg[1] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		return scale;
	}

	public Image getImage() {
		return drawImg[curDrawImg];
	}

	public int[] getPixels() {
		return DataBufferInt.class.cast(drawImg[curDrawImg ^ 1].getRaster().getDataBuffer()).getData();
	}

	public void swapImages() {
		curDrawImg ^= 1;
	}

	public void swapImage() {
	}
}