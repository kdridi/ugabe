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
package com.arykow.applications.ugabe.standalone;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.arykow.applications.ugabe.client.ImageRenderer;

public class ImageRendererGUI extends JPanel implements ImageRenderer {
	private static final long serialVersionUID = 2139799954267592566L;

	private int scale = 3;
	public int nscale = 3;
	public boolean mixFrames;
	private int blitImg[][] = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
	private int blitImg_prev[][] = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
	private int paletteColors[] = new int[8 * 4 * 2];

	public void updateBLIT(int index, int srcPos, int dstPos) {
		System.arraycopy(paletteColors, srcPos, blitImg[index], dstPos, 1);
	}

	public void updatePalette(int index, int r, int g, int b) {
		paletteColors[index] = ((r << 16) | (g << 8) | (b << 0));
		// paletteColors[4 * index + 0] = r & 0x0FF;
		// paletteColors[4 * index + 1] = g & 0x0FF;
		// paletteColors[4 * index + 2] = b & 0x0FF;
		// paletteColors[4 * index + 3] = 0x0FF;
	}

	public void render() {
		if (scale != nscale) {
			scaleImage(scale = nscale);
		}

		int pixels[] = getPixels();

		if (mixFrames) {
			for (int y = 0; y < SCREEN_HEIGHT; ++y) {
				for (int x = 0; x < SCREEN_WIDTH; ++x) {
					blitImg[y][x] = (((((blitImg[y][x]) ^ (blitImg_prev[y][x])) & 0xfffefefe) >> 1) + ((blitImg[y][x]) & (blitImg_prev[y][x])));
					blitImg_prev[y][x] = blitImg[y][x];
				}
			}
		}
		if (scale == 1) {
			for (int y = 0; y < SCREEN_HEIGHT; ++y) {
				int[] blitLine = blitImg[y];
				System.arraycopy(blitLine, 0, pixels, y * SCREEN_WIDTH, SCREEN_WIDTH);
			}
		} else if (scale == 2) {
			int ti1 = -1, ti2 = -1;
			ti2 += SCREEN_WIDTH * 2;
			for (int y = 0; y < SCREEN_HEIGHT; ++y) {
				int yn = (y == 0) ? 0 : y - 1;
				int yp = (y == 143) ? 143 : y + 1;
				int[] blitLine2 = blitImg[y];
				int[] blitLine1 = blitImg[yn];
				int[] blitLine3 = blitImg[yp];
				for (int x = 0; x < SCREEN_WIDTH; ++x) {
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
				ti1 += SCREEN_WIDTH * 2;
				ti2 += SCREEN_WIDTH * 2;
			}
		} else if (scale == 3) {

			int ti1 = -1, ti2 = -1, ti3 = -1;
			ti2 += SCREEN_WIDTH * 3;
			ti3 += SCREEN_WIDTH * 3 * 2;
			for (int y = 0; y < SCREEN_HEIGHT; ++y) {
				int yn = (y == 0) ? 0 : y - 1;
				int yp = (y == 143) ? 143 : y + 1;
				int[] blitLine2 = blitImg[y];
				int[] blitLine1 = blitImg[yn];
				int[] blitLine3 = blitImg[yp];
				for (int x = 0; x < SCREEN_WIDTH; ++x) {
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
				ti1 += SCREEN_WIDTH * 3 * 2;
				ti2 += SCREEN_WIDTH * 3 * 2;
				ti3 += SCREEN_WIDTH * 3 * 2;
			}
		} else if (scale == 4) {

			int ti1 = -1, ti2 = -1, ti3 = -1, ti4 = -1;
			ti2 += SCREEN_WIDTH * 4;
			ti3 += SCREEN_WIDTH * 4 * 2;
			ti4 += SCREEN_WIDTH * 4 * 3;
			for (int y = 0; y < SCREEN_HEIGHT; ++y) {
				int yn = (y == 0) ? 0 : y - 1;
				int yp = (y == 143) ? 143 : y + 1;
				int[] blitLine2 = blitImg[y];
				int[] blitLine1 = blitImg[yn];
				int[] blitLine3 = blitImg[yp];
				for (int x = 0; x < SCREEN_WIDTH; ++x) {
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
				ti1 += SCREEN_WIDTH * 4 * 3;
				ti2 += SCREEN_WIDTH * 4 * 3;
				ti3 += SCREEN_WIDTH * 4 * 3;
				ti4 += SCREEN_WIDTH * 4 * 3;
			}
		}
		swapImage();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private final GUI gui;

	public ImageRendererGUI(GUI gui) {
		this.gui = gui;
	}


	private int curDrawImg = 0;
	private BufferedImage drawImg[] = new BufferedImage[2];

	public void scaleImage(int scale) {
		int width = scale * SCREEN_WIDTH;
		int height = scale * SCREEN_HEIGHT;
		drawImg[0] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		drawImg[1] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	public Image getImage() {
		return drawImg[curDrawImg];
	}

	public int[] getPixels() {
		return DataBufferInt.class.cast(drawImg[curDrawImg ^ 1].getRaster().getDataBuffer()).getData();
	}

	public int interpolation;

	Color ColorTextShadow = new Color(128, 128, 128);
	Color ColorTextLight = new Color(255, 255, 255);
	int DrawingAreaFontSize = 10;
	Font DrawingAreaFont = new Font("SansSerif", Font.BOLD, DrawingAreaFontSize);

	public void swapImage() {
		curDrawImg ^= 1;
		updateVideoImage(getImage());
	}

	public void updateVideoImage(Image img) {
		repaint();
		if (this.gui.speedRunPlayWithOutputVideoStream != null) {
			BufferedImage bimg = (BufferedImage) img;
			int[] data = (int[]) bimg.getRaster().getDataElements(0, 0, bimg.getWidth(), bimg.getHeight(), null);

			for (int i = 0; i < data.length; ++i) {
				int j = data[i];
				int ar = (j >> 16) & 0xff;
				int ag = (j >> 8) & 0xff;
				int ab = (j >> 0) & 0xff;
				try {
					this.gui.speedRunPlayWithOutputVideoStream.writeByte(ar);
					this.gui.speedRunPlayWithOutputVideoStream.writeByte(ag);
					this.gui.speedRunPlayWithOutputVideoStream.writeByte(ab);
				} catch (IOException ee) {
					this.gui.speedRunPlayWithOutputVideoStream = null;
					JOptionPane.showMessageDialog(GUI.frame, "Error while writing to file, aborting recording:\n" + ee.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	transient Image menuimage = null;
	Image scaledLogo = null;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if ((this.gui.logo != null)) {
			if ((scaledLogo == null) || ((scaledLogo != null) && !((scaledLogo.getHeight(null) == getHeight()) || (scaledLogo.getWidth(null) == getWidth())))) {
				double imgaspect = (((double) this.gui.logo.getWidth(null)) / ((double) this.gui.logo.getHeight(null)));
				double wndaspect = (((double) getWidth()) / ((double) getHeight()));
				if (imgaspect > wndaspect) {
					scaledLogo = this.gui.logo.getScaledInstance(getWidth(), (int) (getWidth() / imgaspect), Image.SCALE_AREA_AVERAGING);
				} else if (imgaspect < wndaspect) {
					scaledLogo = this.gui.logo.getScaledInstance((int) (getHeight() * imgaspect), getHeight(), Image.SCALE_AREA_AVERAGING);
				} else
					scaledLogo = this.gui.logo.getScaledInstance(getWidth(), getHeight(), Image.SCALE_AREA_AVERAGING);
			}
			if (scaledLogo != null) {
				while ((scaledLogo.getWidth(null) == -1) || (scaledLogo.getHeight(null) == -1)) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
				;
				Dimension size = new Dimension(scaledLogo.getWidth(null), scaledLogo.getHeight(null));
				int x = (getWidth() - size.width) / 2;
				int y = (getHeight() - size.height) / 2;
				g.drawImage(scaledLogo, x, y, this);
				return;
			}
		} else
			scaledLogo = null;

		if (interpolation == 0) {
			g.drawImage(getImage(), 0, 0, this);
		} else {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, (interpolation == 1) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : (interpolation == 2) ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : (interpolation == 3) ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : null);

			g.drawImage(getImage(), 0, 0, getWidth(), getHeight(), this);
		}
		if (GUI.osdLines.size() > 0) {
			while (GUI.osdLines.size() > 3 * this.gui.imageRenderer.nscale)
				GUI.osdLines.remove(0);
			g.setColor(new Color(255, 255, 255));
			for (int i = 0; i < GUI.osdLines.size(); ++i) {

				String s = (String) GUI.osdLines.get(i);

				g.setColor(ColorTextShadow);
				g.drawString(s, 11, (DrawingAreaFontSize + 1) + DrawingAreaFontSize * i);
				g.drawString(s, 11, (DrawingAreaFontSize + 1) + DrawingAreaFontSize * i);
				g.setColor(ColorTextLight);
				g.drawString(s, 10, DrawingAreaFontSize + DrawingAreaFontSize * i);
			}
		}
		++this.gui.fps;
		if (this.gui.fulls) {
			int mheight = this.gui.mainMenuBar.getHeight();
			if (this.gui.lastmousey <= mheight) {
				int mwidth = this.gui.mainMenuBar.getWidth();
				Rectangle bounds = getBounds();
				if (mwidth > bounds.x && mheight > bounds.y) {

					if (menuimage == null)
						menuimage = new BufferedImage(this.gui.mainMenuBar.getWidth(), this.gui.mainMenuBar.getHeight(), BufferedImage.TYPE_INT_RGB);
					this.gui.mainMenuBar.paint(menuimage.getGraphics());
					g.drawImage(menuimage, 0, 0, mwidth - bounds.x, mheight - bounds.y, bounds.x, bounds.y, mwidth, mheight, this);
				}
			} else if (--this.gui.lastmousecnt == 0) {
				try {
					GUI.addOSDLine("Hiding mouse");
					new Robot().mouseMove(GUI.fsframe.getWidth(), GUI.fsframe.getHeight());
					this.gui.mousehidden = 1;
				} catch (AWTException e) {
				}
			}
		}
	}

}