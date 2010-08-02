/* ==========================================================================
 * GNU GENERAL PUBLIC LICENSE
 * Version 2, June 1991
 * 
 * Copyright (C) 1989, 1991 Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * 
 * $LastChangeDate$
 * $Rev$
 * $LastChangedBy$
 * $URL$
 * $Id$
 * ========================================================================== */ 
package com.arykow.applications.ugabe.client;


public class ImageRendererGUI implements ImageRenderer {
	public VideoScreen screen;
	private static final int ARRAY_SIZE = 1;
	private int blitImg[][] = new int[VideoScreen.SCREEN_HEIGHT][VideoScreen.SCREEN_WIDTH];
	private int blitImg_prev[][] = new int[VideoScreen.SCREEN_HEIGHT][VideoScreen.SCREEN_WIDTH];
	private int paletteColors[] = new int[8 * 4 * 2 * ARRAY_SIZE];

	public ImageRendererGUI(VideoScreen screen) {
		this.screen = screen;
	}

	public void updateBLITFromPaletteColors(VideoController videoController, int srcPos, int dstPos) {
		System.arraycopy(paletteColors, srcPos, blitImg[videoController.LY], dstPos, ARRAY_SIZE);
	}

	public void updatePaletteColors(int index, int r, int g, int b) {
		paletteColors[index] = ((r << 16) | (g << 8) | (b << 0));
		// paletteColors[4 * index + 0] = r & 0x0FF;
		// paletteColors[4 * index + 1] = g & 0x0FF;
		// paletteColors[4 * index + 2] = b & 0x0FF;
		// paletteColors[4 * index + 3] = 0x0FF;
	}

	public void blitImage(VideoController videoController) {
		videoController.cfskip--;
		if (videoController.cfskip < 0) {
			videoController.cfskip += videoController.fskip;
			if (videoController.scale != videoController.nscale) {
				screen.scaleImage(videoController.scale = videoController.nscale);
			}

			int pixels[] = screen.getPixels();

			if (videoController.mixFrames) {
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					for (int x = 0; x < VideoScreen.SCREEN_WIDTH; ++x) {
						blitImg[y][x] = (((((blitImg[y][x]) ^ (blitImg_prev[y][x])) & 0xfffefefe) >> 1) + ((blitImg[y][x]) & (blitImg_prev[y][x])));
						blitImg_prev[y][x] = blitImg[y][x];
					}
				}
			}
			if (videoController.scale == 1) {
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					int[] blitLine = blitImg[y];
					System.arraycopy(blitLine, 0, pixels, y * VideoScreen.SCREEN_WIDTH, VideoScreen.SCREEN_WIDTH);
				}
			} else if (videoController.scale == 2) {
				int ti1 = -1, ti2 = -1;
				ti2 += VideoScreen.SCREEN_WIDTH * 2;
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					int yn = (y == 0) ? 0 : y - 1;
					int yp = (y == 143) ? 143 : y + 1;
					int[] blitLine2 = blitImg[y];
					int[] blitLine1 = blitImg[yn];
					int[] blitLine3 = blitImg[yp];
					for (int x = 0; x < VideoScreen.SCREEN_WIDTH; ++x) {
						int xn = (x == 0) ? 0 : x - 1;
						int xp = (x == 159) ? 159 : x + 1;
						if (!((blitLine2[xn]) == (blitLine2[xp])) && !((blitLine1[x]) == (blitLine3[x]))) {
							pixels[++ti1] = ((blitLine1[x]) == (blitLine2[xn])) ? blitLine2[xn] : blitLine2[x];
							pixels[++ti1] = ((blitLine1[x]) == (blitLine2[xp])) ? blitLine2[xp] : blitLine2[x];
							pixels[++ti2] = ((blitLine3[x]) == (blitLine2[xn])) ? blitLine2[xn] : blitLine2[x];
							pixels[++ti2] = ((blitLine3[x]) == (blitLine2[xp])) ? blitLine2[xp] : blitLine2[x];
						} else {
							int col = blitLine2[x];
							pixels[++ti1] = col;
							pixels[++ti1] = col;
							pixels[++ti2] = col;
							pixels[++ti2] = col;
						}
					}
					ti1 += VideoScreen.SCREEN_WIDTH * 2;
					ti2 += VideoScreen.SCREEN_WIDTH * 2;
				}
			} else if (videoController.scale == 3) {

				int ti1 = -1, ti2 = -1, ti3 = -1;
				ti2 += VideoScreen.SCREEN_WIDTH * 3;
				ti3 += VideoScreen.SCREEN_WIDTH * 3 * 2;
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					int yn = (y == 0) ? 0 : y - 1;
					int yp = (y == 143) ? 143 : y + 1;
					int[] blitLine2 = blitImg[y];
					int[] blitLine1 = blitImg[yn];
					int[] blitLine3 = blitImg[yp];
					for (int x = 0; x < VideoScreen.SCREEN_WIDTH; ++x) {
						int xn = (x == 0) ? 0 : x - 1;
						int xp = (x == 159) ? 159 : x + 1;
						if (!((blitLine1[x]) == (blitLine3[x])) && !((blitLine2[xn]) == (blitLine2[xp]))) {
							pixels[++ti1] = ((blitLine2[xn]) == (blitLine1[x])) ? blitLine2[xn] : blitLine2[x];
							pixels[++ti1] = (((blitLine2[xn]) == (blitLine1[x])) && !((blitLine2[x]) == (blitLine1[xp]))) || (((blitLine1[x]) == (blitLine2[xp])) && !((blitLine2[x]) == (blitLine1[xn]))) ? blitLine1[x] : blitLine2[x];
							pixels[++ti1] = ((blitLine1[x]) == (blitLine2[xp])) ? blitLine2[xp] : blitLine2[x];
							pixels[++ti2] = (((blitLine2[xn]) == (blitLine1[x])) && !((blitLine2[x]) == (blitLine3[xn]))) || (((blitLine2[xn]) == (blitLine3[x])) && !((blitLine2[x]) == (blitLine1[xn]))) ? blitLine2[xn] : blitLine2[x];
							pixels[++ti2] = blitLine2[x];
							pixels[++ti2] = (((blitLine1[x]) == (blitLine2[xp])) && !((blitLine2[x]) == (blitLine3[xp]))) || (((blitLine3[x]) == (blitLine2[xp])) && !((blitLine2[x]) == (blitLine1[xp]))) ? blitLine2[xp] : blitLine2[x];
							pixels[++ti3] = ((blitLine2[xn]) == (blitLine3[x])) ? blitLine2[xn] : blitLine2[x];
							pixels[++ti3] = (((blitLine2[xn]) == (blitLine3[x])) && !((blitLine2[x]) == (blitLine3[xp]))) || (((blitLine3[x]) == (blitLine2[xp])) && !((blitLine2[x]) == (blitLine3[xn]))) ? blitLine3[x] : blitLine2[x];
							pixels[++ti3] = ((blitLine3[x]) == (blitLine2[xp])) ? blitLine2[xp] : blitLine2[x];
						} else {
							int col = blitLine2[x];
							pixels[++ti1] = col;
							pixels[++ti1] = col;
							pixels[++ti1] = col;
							pixels[++ti2] = col;
							pixels[++ti2] = col;
							pixels[++ti2] = col;
							pixels[++ti3] = col;
							pixels[++ti3] = col;
							pixels[++ti3] = col;
						}
					}
					ti1 += VideoScreen.SCREEN_WIDTH * 3 * 2;
					ti2 += VideoScreen.SCREEN_WIDTH * 3 * 2;
					ti3 += VideoScreen.SCREEN_WIDTH * 3 * 2;
				}
			} else if (videoController.scale == 4) {

				int ti1 = -1, ti2 = -1, ti3 = -1, ti4 = -1;
				ti2 += VideoScreen.SCREEN_WIDTH * 4;
				ti3 += VideoScreen.SCREEN_WIDTH * 4 * 2;
				ti4 += VideoScreen.SCREEN_WIDTH * 4 * 3;
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					int yn = (y == 0) ? 0 : y - 1;
					int yp = (y == 143) ? 143 : y + 1;
					int[] blitLine2 = blitImg[y];
					int[] blitLine1 = blitImg[yn];
					int[] blitLine3 = blitImg[yp];
					for (int x = 0; x < VideoScreen.SCREEN_WIDTH; ++x) {
						int xn = (x == 0) ? 0 : x - 1;
						int xp = (x == 159) ? 159 : x + 1;

						if (((blitLine1[x]) == (blitLine2[xn]))) {
							pixels[++ti1] = blitLine1[x];
							pixels[++ti1] = blitLine1[x];
							pixels[++ti2] = blitLine1[x];
						} else {
							pixels[++ti1] = blitLine2[x];
							pixels[++ti1] = blitLine2[x];
							pixels[++ti2] = blitLine2[x];
						}
						pixels[++ti2] = blitLine2[x];
						pixels[++ti2] = blitLine2[x];
						if (((blitLine1[x]) == (blitLine2[xp]))) {
							pixels[++ti1] = blitLine1[x];
							pixels[++ti1] = blitLine1[x];
							pixels[++ti2] = blitLine1[x];
						} else {
							pixels[++ti1] = blitLine2[x];
							pixels[++ti1] = blitLine2[x];
							pixels[++ti2] = blitLine2[x];
						}
						if (((blitLine3[x]) == (blitLine2[xn]))) {
							pixels[++ti3] = blitLine3[x];
							pixels[++ti4] = blitLine3[x];
							pixels[++ti4] = blitLine3[x];
						} else {
							pixels[++ti3] = blitLine2[x];
							pixels[++ti4] = blitLine2[x];
							pixels[++ti4] = blitLine2[x];
						}
						pixels[++ti3] = blitLine2[x];
						pixels[++ti3] = blitLine2[x];
						if (((blitLine3[x]) == (blitLine2[xp]))) {
							pixels[++ti3] = blitLine3[x];
							pixels[++ti4] = blitLine3[x];
							pixels[++ti4] = blitLine3[x];
						} else {
							pixels[++ti3] = blitLine2[x];
							pixels[++ti4] = blitLine2[x];
							pixels[++ti4] = blitLine2[x];
						}
					}
					ti1 += VideoScreen.SCREEN_WIDTH * 4 * 3;
					ti2 += VideoScreen.SCREEN_WIDTH * 4 * 3;
					ti3 += VideoScreen.SCREEN_WIDTH * 4 * 3;
					ti4 += VideoScreen.SCREEN_WIDTH * 4 * 3;
				}
			}
			if (screen != null) {
				screen.swapImage();
			}
		}
		videoController.curWNDY = 0;

	}
}