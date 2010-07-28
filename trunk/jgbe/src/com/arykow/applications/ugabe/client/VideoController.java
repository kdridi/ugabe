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

public final class VideoController {
	public static enum RGB {
		RED, GREEN, BLUE;
	}

	public static enum ColorIndex {
		FIRST, SECOND, THIRD, FOURTH
	}

	public static enum ColorType {
		BACKGROUND, SPRITE1, SPRITE2
	}

	private final VideoScreen screen;
	public int CurrentVRAMBank = 0;
	public int VRAM[] = new int[0x4000];
	public int OAM[] = new int[0xa0];
	protected boolean isCGB;
	public int LY = 0;
	public int LYC = 0;
	public int SCX = 0;
	public int SCY = 0;
	public int WX = 0;
	public int WY = 0;
	public int LCDC = 0;
	public int STAT = 0;
	public int LCDCcntdwn = 0;
	public boolean useSubscanlineRendering = false;
	protected int curBGY;
	public int curWNDY;
	protected int[][][] Scalerx4 = new int[0x100][4][4];
	private final static int GRAYSHADES[][] = { { 0xa0, 0xe0, 0x20 }, { 0x70, 0xb0, 0x40 }, { 0x40, 0x70, 0x32 }, { 0x10, 0x50, 0x26 } };
	private int grayColors[][][] = { GRAYSHADES, GRAYSHADES, GRAYSHADES };
	public int BGPI = 0;
	public int BGPD[] = new int[8 * 4 * 2];
	public int OBPI = 0;
	public int OBPD[] = new int[8 * 4 * 2];
	private int blitImg[][] = new int[VideoScreen.SCREEN_HEIGHT][VideoScreen.SCREEN_WIDTH];
	private int blitImg_prev[][] = new int[VideoScreen.SCREEN_HEIGHT][VideoScreen.SCREEN_WIDTH];
	private int palColors[] = new int[8 * 4 * 2];
	private int patpix[][][] = new int[4096][][];
	public boolean patdirty[] = new boolean[1024];
	public boolean anydirty = true;
	private CPU cpu;
	private int scale = 1;
	public int nscale = 1;
	private int cfskip = 0;
	public int fskip = 1;
	public boolean MixFrames;
	public boolean allow_writes_in_mode_2_3 = true;

	public void setGrayShade(int i, int j, int[] colors) {
		System.arraycopy(colors, 0, grayColors[i][j], 0, RGB.values().length);
		updateMonoColDatas();
	}

	private void updateMonoColDatas() {
		for (int index = 0; index < ColorType.values().length; index++) {
			updateMonoColData(index);
		}
	}

	public void setGrayShades(int[][][] g) {
		for (int i = 0; i < g.length; i++) {
			setGrayShades(i, g[i]);
		}
		updateMonoColDatas();

	}

	public void setGrayShades(int[][] g) {
		for (int index = 0; index < ColorType.values().length; index++) {
			setGrayShades(index, g);
		}
		updateMonoColDatas();
	}

	private void setGrayShades(int index, int[][] values) {
		grayColors[index] = new int[ColorIndex.values().length][RGB.values().length];
		for (int i = 0; i < ColorIndex.values().length; ++i) {
			System.arraycopy(values[i], 0, grayColors[index][i], 0, RGB.values().length);
		}
	}

	public int[][] getGrayShade(int i) {
		return grayColors[i];
	}

	public int[][][] getGrayShades() {
		return grayColors;
	}

	public void restart() {
		LY = 0;
		STAT = STAT & 0xFC;
		STAT_statemachine_state = 0;
		LCDCcntdwn = 80;
	}

	public void reset() {
		CurrentVRAMBank = 0;
		LY = 0;
		LYC = 0;
		SCX = 0;
		SCY = 0;
		WX = 0;
		WY = 0;
		LCDC = cpu.BIOS_enabled ? 0x00 : 0x91;
		STAT = 0x85;
		STAT_statemachine_state = 0;
		LCDCcntdwn = 80;

		BGPI = 0;
		OBPI = 0;
		anydirty = true;
		for (int i = 0; i < 1024; ++i) {
			patdirty[i] = true;
		}
		updatepatpix();

		for (int i = 0; i < 0x20; ++i) {
			OBPD[i * 2] = OBPD[i * 2 + 1] = 0;
			BGPD[i * 2] = BGPD[i * 2 + 1] = 0;
			updateBGColData(i);
			updateOBColData(i);
		}
		;
		updateMonoColData(0);
		updateMonoColData(1);
		updateMonoColData(2);

		for (int i = 0; i < 0xa0; ++i)
			OAM[i] = 0;
		for (int i = 0; i < 0x4000; ++i)
			VRAM[i] = 0;
	}

	public VideoController(CPU cpu, int image_width, int image_height, VideoScreen screen) {
		this.cpu = cpu;
		this.screen = screen;
		screen.scaleImage(scale = nscale);
		reset();
	}

	private void palChange(int palcol, int r, int g, int b) {

		palColors[palcol] = ((r << 16) | (g << 8) | (b << 0));

	}

	long lastms;

	private void blitImage() {
		cfskip--;
		if (cfskip < 0) {
			cfskip += fskip;
			if (scale != nscale) {
				screen.scaleImage(scale = nscale);
			}

			int pixels[] = screen.getPixels();

			if (MixFrames) {
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					for (int x = 0; x < VideoScreen.SCREEN_WIDTH; ++x) {
						blitImg[y][x] = (((((blitImg[y][x]) ^ (blitImg_prev[y][x])) & 0xfffefefe) >> 1) + ((blitImg[y][x]) & (blitImg_prev[y][x])));
						blitImg_prev[y][x] = blitImg[y][x];
					}
				}
			}
			if (scale == 1) {
				for (int y = 0; y < VideoScreen.SCREEN_HEIGHT; ++y) {
					int[] blitLine = blitImg[y];
					System.arraycopy(blitLine, 0, pixels, y * VideoScreen.SCREEN_WIDTH, VideoScreen.SCREEN_WIDTH);
				}
			} else if (scale == 2) {
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
			} else if (scale == 3) {

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
			} else if (scale == 4) {

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
		curBGY = 0;
		curWNDY = 0;

	}

	public void updateMonoColData(int index) {
		if (isCGB)
			return;

		int[][] curColors = grayColors[index];
		int value = cpu.IOP[index + 0x47];
		if (index == 0)
			index = (0x20 >> 2);
		else
			--index;
		int temp[] = null;
		temp = curColors[(value >> 0) & 3];
		palChange((index << 2) | 0, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 2) & 3];
		palChange((index << 2) | 1, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 4) & 3];
		palChange((index << 2) | 2, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 6) & 3];
		palChange((index << 2) | 3, temp[0], temp[1], temp[2]);
	}

	public void setBGColData(int value) {
		BGPD[BGPI & 0x3f] = value;
		int bnum = (BGPI & 0x3e) >> 1;
		updateBGColData(bnum);
		if ((BGPI & (1 << 7)) != 0)
			++BGPI;
	}

	public void updateBGColData(int bnum) {
		int base = bnum << 1;

		int data = BGPD[base] | (BGPD[base + 1] << 8);
		int palnum = base >> 3;
		int colnum = (base >> 1) & 3;
		int r = (data >> 0) & 0x1F;
		int g = (data >> 5) & 0x1F;
		int b = (data >> 10) & 0x1F;

		r <<= 3;
		r |= (r >> 5);
		g <<= 3;
		g |= (g >> 5);
		b <<= 3;
		b |= (b >> 5);

		palChange((palnum << 2) | colnum | 0x20, r, g, b);
	}

	public int getBGColData() {
		return BGPD[BGPI & 0x3f];
	}

	public void setOBColData(int value) {
		OBPD[OBPI & 0x3f] = value;
		int bnum = (OBPI & 0x3e) >> 1;
		updateOBColData(bnum);
		if ((OBPI & (1 << 7)) != 0)
			++OBPI;
	}

	public void updateOBColData(int bnum) {
		int base = bnum << 1;

		int data = OBPD[base] | (OBPD[base + 1] << 8);
		int palnum = base >> 3;
		int colnum = (base >> 1) & 3;
		int r = (data >> 0) & 0x1F;
		int g = (data >> 5) & 0x1F;
		int b = (data >> 10) & 0x1F;

		r <<= 3;
		r |= (r >> 5);
		g <<= 3;
		g |= (g >> 5);
		b <<= 3;
		b |= (b >> 5);

		palChange((palnum << 2) | colnum, r, g, b);
	}

	public int getOBColData() {
		return OBPD[OBPI & 0x3f];
	}

	private void updatepatpix() {
		if (!anydirty)
			return;

		for (int i = 0; i < 1024; ++i) {
			if (i == 384)
				i = 512;
			if (i == 896)
				break;
			if (!patdirty[i])
				continue;
			if (patpix[i] == null) {
				patpix[i] = new int[8][8];
				patpix[i + 1024] = new int[8][8];
				patpix[i + 2048] = new int[8][8];
				patpix[i + 3072] = new int[8][8];
			}
			patdirty[i] = false;

			for (int y = 0; y < 8; ++y) {
				int lineofs = (i * 16) + (y * 2);
				for (int x = 0; x < 8; ++x) {

					int col = (VRAM[lineofs] >> x) & 1;
					col |= ((VRAM[lineofs + 1] >> x) & 1) << 1;
					patpix[i][y][7 - x] = col;
					patpix[i + 1024][y][x] = col;
					patpix[i + 2048][7 - y][7 - x] = col;
					patpix[i + 3072][7 - y][x] = col;
				}
			}
		}
		anydirty = false;

	}

	public int STAT_statemachine_state = 0;
	public int mode3duration = 0;
	int scanlinepos = 0;

	public int render(int cycles) {
		LCDCcntdwn -= cycles;
		while (LCDCcntdwn <= 0) {
			switch (STAT_statemachine_state) {
			case 0:

				mode3duration = 172 + 10 * setSpritesOnScanline();
				LCDCcntdwn += mode3duration;
				STAT = (STAT & 0xFC) | 3;
				++STAT_statemachine_state;

				updatepatpix();

				pixpos = -(SCX & 7);
				cyclepos = 0;
				curSprite = 0;
				break;
			case 1:
				if (useSubscanlineRendering)
					renderScanLinePart();
				else
					renderScanLine();

				LCDCcntdwn += (isCGB ? 376 : 372) - mode3duration;
				STAT &= 0xFC;
				if ((STAT & (1 << 3)) != 0)
					cpu.triggerInterrupt(1);
				if (LY < VideoScreen.SCREEN_HEIGHT)
					cpu.elapseTime(cpu.hblank_dma());
				++STAT_statemachine_state;
				break;
			case 2:
				LY++;
				STAT = (STAT & (~(1 << 2)));
				LCDCcntdwn += (isCGB ? 0 : 4);
				++STAT_statemachine_state;
				break;
			case 3:
				if (LY < VideoScreen.SCREEN_HEIGHT) {
					LCDCcntdwn += 80;
					STAT = (STAT & 0xFC) | 2;
					if (LY == LYC) {
						STAT = STAT | (1 << 2);
						if ((STAT & (1 << 6)) != 0) {
							cpu.triggerInterrupt(1);
						}
					}
					if ((STAT & (1 << 5)) != 0)
						cpu.triggerInterrupt(1);
					STAT_statemachine_state = 0;
				} else {
					STAT = (STAT & 0xFC) | 1;
					++STAT_statemachine_state;
					if ((LCDC & 0x80) != 0)
						cpu.triggerInterrupt(0);
					if ((STAT & (1 << 4)) != 0)
						cpu.triggerInterrupt(1);
					blitImage();
				}
				break;
			case 4:
				if (LY == LYC) {
					STAT = STAT | 4;
					if ((STAT & (1 << 6)) != 0) {
						cpu.triggerInterrupt(1);
					}
				}
				if (LY == 153)
					LY = 0;
				LCDCcntdwn += (isCGB ? 456 : 452);
				++STAT_statemachine_state;
				break;
			case 5:
				LCDCcntdwn += (isCGB ? 0 : 4);
				if (LY == 0) {
					++STAT_statemachine_state;
				} else {
					++LY;
					STAT = (STAT & (~4));
					--STAT_statemachine_state;
				}
				break;
			case 6:
				STAT = (STAT & 0xfc) | 2;
				if ((LY == LYC) && (STAT & (1 << 6)) != 0) {
					cpu.triggerInterrupt(1);
				}
				if ((STAT & (1 << 5)) != 0)
					cpu.triggerInterrupt(1);
				LCDCcntdwn += 80;
				STAT_statemachine_state = 0;
				break;
			default:
				throw new Error("Assertion failed: " + "false");
			}
		}
		if (!(LCDCcntdwn > 0))
			throw new Error("Assertion failed: " + "LCDCcntdwn > 0");
		return LCDCcntdwn;
	}

	int pixpos = 0;
	int cyclepos = 0;
	int[] zbuffer = new int[VideoScreen.SCREEN_WIDTH];
	int curSprite = 0;

	private void renderScanLinePart() {
		if ((STAT_statemachine_state != 1))
			return;
		blitLine = blitImg[LY];
		if ((LCDC & 0x80) == 0) {
			for (int i = pixpos; i < VideoScreen.SCREEN_WIDTH; ++i) {
				int x = pixpos + i;
				if ((x >= 0) && (x < VideoScreen.SCREEN_WIDTH)) {
					blitLine[x] = palColors[32 | 0];
				}
				;
			}
			pixpos = VideoScreen.SCREEN_WIDTH;
			return;
		}

		int newLCDCcntdwn = (int) (LCDCcntdwn - (int) (cpu.TotalCycleCount - cpu.lastVCRenderCycleCount));
		int cyclesToRender = (mode3duration - newLCDCcntdwn - cyclepos - 4);
		cyclepos += cyclesToRender;

		while (cyclesToRender > 0) {
			int sprXPos = OAM[spritesOnScanline[curSprite] | 1] - 8 - pixpos;
			if ((sprXPos >= 0) && (sprXPos < 8) && (curSprite < spriteCountOnScanline - 1)) {
				cyclesToRender -= 2;
				++curSprite;
				pixpos -= 8;
			} else if ((!isCGB) && ((LCDC & (1 << 0)) == 0)) {
				for (int i = 0; i < 8; ++i) {
					int x = pixpos + i;
					if ((x >= 0) && (x < VideoScreen.SCREEN_WIDTH)) {
						{
							blitLine[x] = palColors[0 | 0];
						}
						;
						zbuffer[x] = 0;
					}
				}
			} else {

				int BGTileMap = ((LCDC & (1 << 3)) == 0) ? 0x1800 : 0x1c00;

				int bgline = SCY + LY;

				int bgtilemapindex = (((SCX + pixpos) & 0xff) >> 3) + ((bgline & 0xf8) << 2);
				int bgtile = VRAM[BGTileMap + bgtilemapindex];
				int bgpal = 0x08 << 2;

				if (((LCDC & (1 << 4)) == 0)) {

					bgtile ^= 0x80;
					bgtile += 0x80;
				}
				int[] patLine = patpix[bgtile][bgline & 7];
				for (int i = 0; i < 8; ++i) {
					int x = pixpos + i;
					if ((x >= 0) && (x < VideoScreen.SCREEN_WIDTH)) {
						{
							blitLine[x] = palColors[bgpal | patLine[i]];
						}
						;
						zbuffer[x] = patLine[i];
					}
				}
			}
			cyclesToRender -= 8;
			pixpos += 8;
		}
		cyclepos -= cyclesToRender;

		if (pixpos >= 159) {
			int pricol = 0xff0000;
			boolean spr8x16 = ((LCDC & (1 << 2)) != 0);
			for (int curSprite = spriteCountOnScanline - 1; curSprite >= 0; --curSprite) {

				if (!isCGB) {

					int line = LY - (OAM[spritesOnScanline[curSprite]] - 16);
					int xpos = OAM[spritesOnScanline[curSprite] | 1] - 8;
					int tile = OAM[spritesOnScanline[curSprite] | 2];
					int attr = OAM[spritesOnScanline[curSprite] | 3];
					if (spr8x16) {

						tile &= ~1;
						tile |= (line >= 8) ? 1 : 0;
						line &= 7;
					}
					boolean priority = ((attr & (1 << 7)) == 0);
					if ((attr & (1 << 6)) != 0)
						line = 7 - line;
					if ((attr & (1 << 5)) != 0)
						tile |= (1 << 10);
					int pallette = ((attr >> 4) & 1) << 2;
					int[] patLine = patpix[tile][line];
					for (int i = 0; i < 8; ++i) {
						int color = patLine[i];

						if ((xpos >= 0) && (xpos < VideoScreen.SCREEN_WIDTH) && (color != 0) && ((zbuffer[xpos] == 0) || priority)) {
							{
								blitLine[xpos] = palColors[pallette | color];
							}
							;

						}
						++xpos;
					}
					pricol += 0x1600;
				} else {

				}
			}

		}
	}

	int spriteCountOnScanline;
	int[] spritesOnScanline = new int[40];

	private int setSpritesOnScanline() {
		int sprYSize = ((LCDC & (1 << 2)) != 0) ? 16 : 8;
		int count = 0;
		for (int spr = 0; (spr < 40 * 4); spr += 4) {
			int sprPos = LY - (OAM[spr] - 16);

			if ((sprPos >= 0) && (sprPos < sprYSize)) {
				spritesOnScanline[count] = spr;
				++count;
			}
		}

		for (int i = count - 1; i >= 0; --i) {
			for (int j = i - 1; j >= 0; --j) {
				int k = spritesOnScanline[i];
				if (OAM[spritesOnScanline[j] | 1] > OAM[k | 1]) {
					spritesOnScanline[i] = spritesOnScanline[j];
					spritesOnScanline[j] = k;
				}
			}
		}

		count = count > 10 ? 10 : count;
		spriteCountOnScanline = count;
		return count;
	}

	public int read(int index) {
		if (index < 0xa000) {
			if (allow_writes_in_mode_2_3 || ((LCDC & 0x80) == 0) || ((STAT & 3) != 3))
				return VRAM[index - 0x8000 + CurrentVRAMBank];
			CPULogger.printf("WARNING: Read from VRAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return 0xff;
		}
		if ((index > 0xfdff) && (index < 0xfea0)) {
			if (allow_writes_in_mode_2_3 || ((LCDC & 0x80) == 0) || ((STAT & 2) == 0))
				return OAM[index - 0xfe00];
			CPULogger.printf("WARNING: Read from OAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return 0xff;
		}
		int b = 0xff;
		switch (index & 0x3f) {
		case 0x00:
			b = LCDC;
			break;
		case 0x01:

			b = ((LCDC & 0x80) == 0) ? (STAT & 0x7c) | 0x00 : STAT;
			b = STAT;
			break;
		case 0x02:
			b = SCY;
			break;
		case 0x03:
			b = SCX;
			break;
		case 0x04:

			b = ((LCDC & 0x80) == 0) ? 0 : LY;

			break;
		case 0x05:
			b = LYC;
			break;
		case 0x07:
		case 0x08:
		case 0x09:
			b = cpu.IOP[index - 0xff00];
			break;
		case 0x0a:
			b = WY;
			break;
		case 0x0b:
			b = WX;
			break;
		case 0x0d:
			b = cpu.doublespeed ? (1 << 7) : 0;
			break;
		case 0x0f:
			b = getcurVRAMBank();
			break;
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
		case 0x15:
			b = cpu.IOP[index - 0xff00];
			break;
		case 0x28:
			b = BGPI;
			break;
		case 0x29:
			b = getBGColData();
			break;
		case 0x2a:
			b = OBPI;
			break;
		case 0x2b:
			b = getOBColData();
			break;
		case 0x2c:
			CPULogger.printf("WARNING: VC.read(): Read from *undocumented* IO port $%04x\n", index);
			b = cpu.IOP[index - 0xff00] | 0xfe;
			break;
		default:
			CPULogger.printf("TODO: VC.read(): Read from IO port $%04x\n", index);
		}
		return b;
	}

	public void write(int index, int value) {
		if (index < 0xa000) {
			if (allow_writes_in_mode_2_3 || ((LCDC & 0x80) == 0) || ((STAT & 3) != 3)) {
				VRAM[index - 0x8000 + CurrentVRAMBank] = value;
				patdirty[(CurrentVRAMBank >> 4) + ((index - 0x8000) >> 4)] = true;
				anydirty = true;
				return;
			}
			CPULogger.printf("WARNING: Write to VRAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return;
		}
		if ((index > 0xfdff) && (index < 0xfea0)) {
			if (allow_writes_in_mode_2_3 || ((LCDC & 0x80) == 0) || ((STAT & 2) == 0)) {
				OAM[index - 0xfe00] = value;
				return;
			}
			CPULogger.printf("WARNING: Write to OAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x", index, cpu.getPC());
			return;
		}
		switch (index & 0x3f) {
		case 0x00:
			if (((value & 0x80) != 0) && ((LCDC & 0x80) == 0))
				restart();
			LCDC = value;
			break;
		case 0x01:
			STAT = (STAT & 0x87) | (value & 0x78);
			if (!isCGB && ((STAT & 2) == 0) && ((LCDC & 0x80) != 0)) {

				cpu.triggerInterrupt(1);
			}
			break;
		case 0x02:
			if (useSubscanlineRendering)
				renderScanLinePart();
			SCY = value;
			break;
		case 0x03:
			if (useSubscanlineRendering)
				renderScanLinePart();
			SCX = value;
			break;
		case 0x04:
			LY = 0;
			break;
		case 0x05:

			STAT &= ~(1 << 2);
			if (LYC != value && LY == value && (STAT & (1 << 6)) != 0) {
				STAT |= (1 << 2);
				cpu.triggerInterrupt(1);
			}
			LYC = value;
			break;
		case 0x06: {

			cpu.last_memory_access = cpu.last_memory_access_internal;

			for (int i = 0; i < 0xa0; ++i) {
				cpu.write(0xfe00 | i, cpu.read(i + (value << 8)));
			}
			cpu.last_memory_access_internal = cpu.last_memory_access;
		}
			break;
		case 0x07:
		case 0x08:
		case 0x09:
			if (useSubscanlineRendering)
				renderScanLinePart();
			cpu.IOP[index - 0xff00] = value;
			updateMonoColData(index - 0xff47);
			break;
		case 0x0a:
			WY = value;
			break;
		case 0x0b:
			WX = value;
			break;
		case 0x0d:
			cpu.speedswitch = ((value & 1) != 0);
			break;
		case 0x0f:
			selectVRAMBank(value & 1);
			break;
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
			cpu.IOP[index - 0xff00] = value;
			break;
		case 0x15:
			int mode = ((cpu.hblank_dma_state | value) & 0x80);
			if (mode == 0) {
				int src = ((cpu.IOP[0x51] << 8) | cpu.IOP[0x52]) & 0xfff0;
				int dst = (((cpu.IOP[0x53] << 8) | cpu.IOP[0x54]) & 0x1ff0) | 0x8000;
				int len = ((value & 0x7f) + 1) << 4;
				CPULogger.log("WARNING: cpu.write(): TODO: Untimed H-DMA Transfer");

				for (int i = 0; i < len; ++i)
					write(dst++, cpu.read(src++));
				cpu.IOP[0x51] = src >> 8;
				cpu.IOP[0x52] = src & 0xF0;
				cpu.IOP[0x53] = 0x1F & (dst >> 8);
				cpu.IOP[0x54] = dst & 0xF0;
				cpu.IOP[0x55] = 0xff;

			} else {
				cpu.hblank_dma_state = value;
				cpu.IOP[0x55] = value & 0x7f;
			}
			break;
		case 0x28:
			BGPI = value;
			;
			break;
		case 0x29:
			setBGColData(value);
			break;
		case 0x2a:
			OBPI = value;
			;
			break;
		case 0x2b:
			setOBColData(value);
			break;
		case 0x2c:
			CPULogger.printf("WARNING: VC.write(): Write %02x to *undocumented* IO port $%04x\n", value, index);
			cpu.IOP[index - 0xff00] = value;
			break;
		default:
			CPULogger.printf("TODO: VC.write(): Write %02x to IO port $%04x\n", value, index);
			break;
		}
	}

	public void selectVRAMBank(int i) {
		CurrentVRAMBank = i * 0x2000;
		if ((i < 0) || (i > 1))
			CPULogger.printf("current offset=%x\n", CurrentVRAMBank);
	}

	public int getcurVRAMBank() {
		return CurrentVRAMBank / 0x2000;
	}

	private int TileData;
	private int BGTileMap;
	private int WindowTileMap;
	private int bgY;
	private int bgTileY;
	private int bgOffsY;
	private int bgX;
	private int bgTileX;
	private int bgOffsX;
	private int windX;
	private int tilebufBG[] = new int[0x200];
	private int[] blitLine;

	public void renderScanLine() {

		if (cfskip != 0)
			return;
		if ((LCDC & (1 << 7)) != 0) {

			updatepatpix();

			blitLine = blitImg[LY];

			TileData = ((LCDC & (1 << 4)) == 0) ? 0x0800 : 0x0000;
			BGTileMap = ((LCDC & (1 << 3)) == 0) ? 0x1800 : 0x1c00;
			WindowTileMap = ((LCDC & (1 << 6)) == 0) ? 0x1800 : 0x1c00;

			windX = VideoScreen.SCREEN_WIDTH;
			if (((LCDC & (1 << 5)) != 0) && (WX >= 0) && (WX < 167) && (WY >= 0) && (WY < VideoScreen.SCREEN_HEIGHT) && (LY >= WY))
				windX = (WX - 7);

			renderScanlineBG();

			if (windX < VideoScreen.SCREEN_WIDTH) {
				renderScanlineWindow();
			}

			if ((LCDC & (1 << 1)) != 0) {
				renderScanlineSprites();
			}
		}
	}

	private void calcBGTileBuf() {

		int tileMap = BGTileMap + bgTileX + (bgTileY * 32);
		int attrMap = tileMap + 0x2000;
		int bufMap = 0;
		int cnt = ((windX + 7) >> 3) + 1;

		for (int i = 0; i < cnt; ++i) {
			int tile = VRAM[tileMap++];
			int attr = VRAM[attrMap++];
			if (TileData == 0x0800) {
				tile ^= 0x80;
				tile += 0x80;
			}
			tilebufBG[bufMap++] = tile | ((attr & 0x08) << 6) | ((attr & 0x60) << 5);
			tilebufBG[bufMap++] = ((attr & 7) | 0x08) << 2;
			if ((tileMap & 31) == 0) {
				tileMap -= 32;
				attrMap -= 32;
			}
		}
	}

	private void renderScanlineBG() {
		int bufMap = 0;
		int cnt = windX;
		if (cnt == 0)
			return;

		bgY = (SCY + LY) & 0xFF;
		bgTileY = bgY >> 3;
		bgOffsY = bgY & 7;
		bgX = SCX;
		bgTileX = bgX >> 3;
		bgOffsX = bgX & 7;

		calcBGTileBuf();

		int PatLine[] = patpix[tilebufBG[bufMap++]][bgOffsY];
		int TilePal = tilebufBG[bufMap++];
		int curX = 0;

		for (int t = bgOffsX; t < 8; ++t, --cnt) {
			blitLine[curX++] = palColors[TilePal | PatLine[t]];
		}
		;

		if (cnt == 0)
			return;

		while (cnt >= 8) {
			PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
			TilePal = tilebufBG[bufMap++];
			for (int t = 0; t < 8; ++t) {
				blitLine[curX++] = palColors[TilePal | PatLine[t]];
			}
			;
			cnt -= 8;
		}
		PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
		TilePal = tilebufBG[bufMap++];
		for (int t = 0; cnt > 0; --cnt, ++t) {
			blitLine[curX++] = palColors[TilePal | PatLine[t]];
		}
		;
	}

	private void calcWindTileBuf() {
		int tileMap = WindowTileMap + (bgTileY * 32);
		int attrMap = tileMap + 0x2000;
		int bufMap = 0;
		int cnt = ((VideoScreen.SCREEN_WIDTH - (windX + 7)) >> 3) + 2;

		for (int i = 0; i < cnt; ++i) {
			int tile = VRAM[tileMap++];

			int attr = VRAM[attrMap++];
			if (TileData == 0x0800) {
				tile ^= 0x80;
				tile += 0x80;
			}
			tilebufBG[bufMap++] = tile | ((attr & 0x08) << 6) | ((attr & 0x60) << 5);
			tilebufBG[bufMap++] = ((attr & 7) | 0x8) << 2;
			if ((tileMap & 31) == 0) {
				tileMap -= 32;
				attrMap -= 32;
			}
		}
	}

	private void renderScanlineWindow() {
		int bufMap = 0;
		int curX = ((windX) < (0) ? (0) : (windX));
		int cnt = VideoScreen.SCREEN_WIDTH - curX;
		if (cnt == 0)
			return;
		bgY = curWNDY++;
		bgTileY = bgY >> 3;
		bgOffsY = bgY & 7;

		bgOffsX = curX - windX;

		calcWindTileBuf();

		int PatLine[] = patpix[tilebufBG[bufMap++]][bgOffsY];
		int TilePal = tilebufBG[bufMap++];

		for (int t = bgOffsX; (t < 8) && (cnt > 0); ++t, --cnt) {
			blitLine[curX++] = palColors[TilePal | PatLine[t]];
		}
		;

		while (cnt >= 8) {
			PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
			TilePal = tilebufBG[bufMap++];
			for (int t = 0; t < 8; ++t) {
				blitLine[curX++] = palColors[TilePal | PatLine[t]];
			}
			;
			cnt -= 8;
		}
		PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
		TilePal = tilebufBG[bufMap++];
		for (int t = 0; cnt > 0; --cnt, ++t) {
			blitLine[curX++] = palColors[TilePal | PatLine[t]];
		}
		;
	}

	private void renderScanlineSprites() {
		boolean spr8x16 = ((LCDC & (1 << 2)) != 0);

		for (int spr = 0; spr < 40; ++spr) {
			int sprY = OAM[(spr * 4) + 0];
			int sprX = OAM[(spr * 4) + 1];
			int sprNum = OAM[(spr * 4) + 2];
			int sprAttr = OAM[(spr * 4) + 3];

			int ofsY = LY - sprY + 16;

			if ((ofsY >= 0) && (ofsY < (spr8x16 ? 16 : 8)) && (sprX > 0) && (sprX < 168)) {
				if ((sprAttr & (1 << 6)) != 0)
					ofsY = (spr8x16 ? 15 : 7) - ofsY;
				if (spr8x16) {
					sprNum &= ~1;
					sprNum |= (ofsY >= 8) ? 1 : 0;
					ofsY &= 7;
				}

				if ((sprAttr & (1 << 5)) != 0)
					sprNum |= (1 << 10);

				int palnr;
				if (isCGB) {
					if ((sprAttr & (1 << 3)) != 0)
						sprNum |= (1 << 9);
					palnr = sprAttr & 7;
				} else
					palnr = (sprAttr >> 4) & 1;

				int[] PatLine = patpix[sprNum][ofsY];

				for (int ofsX = 0; ofsX < 8; ++ofsX) {
					int rx = sprX - 8 + ofsX;

					int col = PatLine[ofsX];
					if ((col != 0) && (rx >= 0) && (rx < VideoScreen.SCREEN_WIDTH)) {
						{
							blitLine[rx] = palColors[(palnr << 2) | col];
						}
						;
					}
				}
			}
		}
	}
}
