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

	/**
	 * LCD Control Register
	 * Bits 0000 0000 ABCD EFGH
	 * 
	 * 7 A : LCD Display						=> Enabled / Disabled
	 * 6 B : Window Tile Map Address			=> 9C00-9FFF / 9800-9BFF
	 * 5 C : Display Window					=> Yes / No
	 * 4 D : BG & Window Tile Data Address	=> 8000-8FFF / 8800-97FF
	 * 3 E : BG Tile Map Display Address		=> 9800-9BFF / 9C00-9FFF
	 * 2 F : Sprite Size						=> 8x16 / 8x8
	 * 1 G : Display Sprites					=> Yes / No
	 * 0 H : Display Background				=> Yes / No
	 */
	
	public static class LCDC {
		public int value;
		public int windowTileMapAddress;
		public boolean windowDisplayEnabled;
		public boolean operationEnabled;
		public int backgroundTileMapAddress;
		public boolean spriteDisplayEnabled;
		public boolean backgroundDisplayEnabled;
		public boolean tileMapAddressLow;
		public int spriteWidth = 8;
		public int spriteHeight = 8;

		public LCDC() {
			setValue(0);
		}

		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
			this.operationEnabled			= (value & (1 << 7)) != 0;
			this.windowTileMapAddress		= (value & (1 << 6)) != 0 ? 0x1C00 : 0x1800;
			this.windowDisplayEnabled		= (value & (1 << 5)) != 0;
			this.tileMapAddressLow			= (value & (1 << 4)) != 0;
			this.backgroundTileMapAddress	= (value & (1 << 3)) != 0 ? 0x1C00 : 0x1800;
			this.spriteHeight				= (value & (1 << 2)) != 0 ? 16 : 8;
			this.spriteDisplayEnabled		= (value & (1 << 1)) != 0;
			this.backgroundDisplayEnabled	= (value & (1 << 0)) != 0;
		}
	}
	public LCDC lcdController = new LCDC();
	
	
	public int currentVRAMBank = 0;
	public int VRAM[] = new int[0x4000];
	public int OAM[] = new int[40 * 4];
	public int LY = 0;
	public int LYC = 0;
	public int SCX = 0;
	public int SCY = 0;
	public int WX = 0;
	public int WY = 0;
	public int LCDCcntdwn = 0;
	public int mode3duration = 0;
	public int STAT_statemachine_state = 0;
	public int STAT = 0;
	public int BGPI = 0;
	public int BGPD[] = new int[8 * 4 * 2];
	public int OBPI = 0;
	public int OBPD[] = new int[8 * 4 * 2];
	public int curWNDY;
	public PixelPatterns patterns = new PixelPatterns();

	public static enum RGB {
		RED, GREEN, BLUE;
	}

	public static enum ColorIndex {
		FIRST, SECOND, THIRD, FOURTH
	}

	public static enum ColorType {
		BACKGROUND, SPRITE1, SPRITE2
	}

	public boolean allow_writes_in_mode_2_3 = true;
	protected boolean isCGB;

	private final static boolean useSubscanlineRendering = false;
	private final static int GRAYSHADES[][] = { { 0xa0, 0xe0, 0x20 }, { 0x70, 0xb0, 0x40 }, { 0x40, 0x70, 0x32 }, { 0x10, 0x50, 0x26 } };

	private CPU cpu;
	private ImageRenderer imageRenderer;

	private int grayColors[][][] = { GRAYSHADES, GRAYSHADES, GRAYSHADES };
	public int fskip = 1;
	int cfskip = 0;

	
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
		currentVRAMBank = 0;
		LY = 0;
		LYC = 0;
		SCX = 0;
		SCY = 0;
		WX = 0;
		WY = 0;
		lcdController.setValue(cpu.BIOS_enabled ? 0x00 : 0x91);
		STAT = 0x85;
		STAT_statemachine_state = 0;
		LCDCcntdwn = 80;

		BGPI = 0;
		OBPI = 0;

		patterns.setDirtyPatternEnabled(true, true);
		patterns.updatePatternPixels(VRAM);

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

	public VideoController(CPU cpu, ImageRenderer imageController) {
		this.cpu = cpu;
		this.imageRenderer = imageController;
		reset();
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
		imageRenderer.updatePalette((index << 2) | 0, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 2) & 3];
		imageRenderer.updatePalette((index << 2) | 1, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 4) & 3];
		imageRenderer.updatePalette((index << 2) | 2, temp[0], temp[1], temp[2]);
		temp = curColors[(value >> 6) & 3];
		imageRenderer.updatePalette((index << 2) | 3, temp[0], temp[1], temp[2]);
	}

	public void setBGColData(int value) {
		BGPD[BGPI & 0x3f] = value;
		int bnum = (BGPI & 0x3e) >> 1;
		updateBGColData(bnum);
		if ((BGPI & (1 << 7)) != 0)
			++BGPI;
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

	public void updateBGColData(int bnum) {
		updateColorsData(bnum, 0x20, BGPD);
	}

	public void updateOBColData(int bnum) {
		updateColorsData(bnum, null, OBPD);
	}

	private void updateColorsData(int bnum, Integer value, int[] dataTable) {
		int base = bnum << 1;

		int data = dataTable[base] | (dataTable[base + 1] << 8);
		int paletteNumber = (base >> 3);
		int colorNumber = (base >> 1) & 3;
		int r = (data >> 0) & 0x1F;
		int g = (data >> 5) & 0x1F;
		int b = (data >> 10) & 0x1F;

		r <<= 3;
		r |= (r >> 5);
		g <<= 3;
		g |= (g >> 5);
		b <<= 3;
		b |= (b >> 5);

		int paletteColorIndex = (paletteNumber << 2) | colorNumber;
		if (value != null) {
			paletteColorIndex |= value.intValue();
		}
		imageRenderer.updatePalette(paletteColorIndex, r, g, b);
	}

	public int getOBColData() {
		return OBPD[OBPI & 0x3f];
	}

	public int render(int cycles) {
		LCDCcntdwn -= cycles;
		while (LCDCcntdwn <= 0) {
			switch (STAT_statemachine_state) {
			case 0:

				mode3duration = 172 + 10 * setSpritesOnScanline();
				LCDCcntdwn += mode3duration;
				STAT = (STAT & 0xFC) | 3;
				++STAT_statemachine_state;

				patterns.updatePatternPixels(VRAM);

				pixpos = -(SCX & 7);
				cyclepos = 0;
				curSprite = 0;
				break;
			case 1:
				if (useSubscanlineRendering) {
					renderScanLinePart();
				} else {
					renderScanLine();
				}

				LCDCcntdwn += (isCGB ? 376 : 372) - mode3duration;
				STAT &= 0xFC;
				if ((STAT & (1 << 3)) != 0)
					cpu.triggerInterrupt(1);
				if (LY < ImageRenderer.SCREEN_HEIGHT)
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
				if (LY < ImageRenderer.SCREEN_HEIGHT) {
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
					if (lcdController.operationEnabled) {
						cpu.triggerInterrupt(0);
					}
					if ((STAT & (1 << 4)) != 0) {
						cpu.triggerInterrupt(1);
					}

					cfskip--;
					if (cfskip < 0) {
						cfskip += fskip;
						imageRenderer.render();
					}
					curWNDY = 0;
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
	int[] zbuffer = new int[ImageRenderer.SCREEN_WIDTH];
	int curSprite = 0;

	private void renderScanLinePart() {
		if ((STAT_statemachine_state != 1))
			return;
		if (!lcdController.operationEnabled) {
			for (int i = pixpos; i < ImageRenderer.SCREEN_WIDTH; ++i) {
				int x = pixpos + i;
				if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
					imageRenderer.updateBLIT(LY, 32 | 0, x);
				}
			}
			pixpos = ImageRenderer.SCREEN_WIDTH;
			return;
		}

		int newLCDCcntdwn = (int) (LCDCcntdwn - (int) (cpu.totalCycleCount - cpu.lastVCRenderCycleCount));
		int cyclesToRender = (mode3duration - newLCDCcntdwn - cyclepos - 4);
		cyclepos += cyclesToRender;

		while (cyclesToRender > 0) {
			int sprXPos = OAM[spritesOnScanline[curSprite] | 1] - 8 - pixpos;
			if ((sprXPos >= 0) && (sprXPos < 8) && (curSprite < spriteCountOnScanline - 1)) {
				cyclesToRender -= 2;
				++curSprite;
				pixpos -= 8;
			} else if ((!isCGB) && !lcdController.spriteDisplayEnabled) {
				for (int i = 0; i < 8; ++i) {
					int x = pixpos + i;
					if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
						imageRenderer.updateBLIT(LY, 0 | 0, x);
						zbuffer[x] = 0;
					}
				}
			} else {
				int bgline = SCY + LY;
				int bgtilemapindex = (((SCX + pixpos) & 0xff) >> 3) + ((bgline & 0xf8) << 2);
				int bgtile = VRAM[lcdController.backgroundTileMapAddress + bgtilemapindex];
				int bgpal = 0x08 << 2;
				if (!lcdController.tileMapAddressLow) {
					bgtile ^= 0x80;
					bgtile += 0x80;
				}
				for (int i = 0; i < 8; ++i) {
					int x = pixpos + i;
					if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
						int color = patterns.get(bgtile, bgline & 7, i);
						imageRenderer.updateBLIT(LY, bgpal | color, x);
						zbuffer[x] = color;
					}
				}
			}
			cyclesToRender -= 8;
			pixpos += 8;
		}
		cyclepos -= cyclesToRender;

		if (pixpos >= 159) {
			int pricol = 0xff0000;
			for (int curSprite = spriteCountOnScanline - 1; curSprite >= 0; --curSprite) {

				if (!isCGB) {

					int line = LY - (OAM[spritesOnScanline[curSprite]] - 16);
					int xpos = OAM[spritesOnScanline[curSprite] | 1] - 8;
					int tile = OAM[spritesOnScanline[curSprite] | 2];
					int attr = OAM[spritesOnScanline[curSprite] | 3];
					if (lcdController.spriteHeight == 16) {
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
					for (int i = 0; i < 8; ++i) {
						int color = patterns.get(tile, line, i);

						if ((xpos >= 0) && (xpos < ImageRenderer.SCREEN_WIDTH) && (color != 0) && ((zbuffer[xpos] == 0) || priority)) {
							imageRenderer.updateBLIT(LY, pallette | color, xpos);
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
		int count = 0;
		for (int spr = 0; (spr < 40 * 4); spr += 4) {
			int sprPos = LY - (OAM[spr] - 16);

			if ((sprPos >= 0) && (sprPos < lcdController.spriteHeight)) {
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
			if (allow_writes_in_mode_2_3 || !lcdController.operationEnabled || ((STAT & 3) != 3)) {
				return VRAM[index - 0x8000 + currentVRAMBank];
			}
			CPULogger.printf("WARNING: Read from VRAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return 0xff;
		}
		if ((index > 0xfdff) && (index < 0xfea0)) {
			if (allow_writes_in_mode_2_3 || !lcdController.operationEnabled || ((STAT & 2) == 0)) {
				return OAM[index - 0xfe00];
			}
			CPULogger.printf("WARNING: Read from OAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return 0xff;
		}
		int b = 0xff;
		switch (index & 0x3f) {
		case 0x00:
			b = lcdController.getValue();
			break;
		case 0x01:
			b = lcdController.operationEnabled ? STAT : (STAT & 0x7c) | 0x00;
			b = STAT;
			break;
		case 0x02:
			b = SCY;
			break;
		case 0x03:
			b = SCX;
			break;
		case 0x04:
			b = lcdController.operationEnabled ? LY : 0;
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
			if (allow_writes_in_mode_2_3 || !lcdController.operationEnabled || ((STAT & 3) != 3)) {
				VRAM[index - 0x8000 + currentVRAMBank] = value;
				patterns.setDirtyPatternEnabled((currentVRAMBank >> 4) + ((index - 0x8000) >> 4), true);
				return;
			}
			CPULogger.printf("WARNING: Write to VRAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x\n", index, cpu.getPC());
			return;
		}
		if ((index > 0xfdff) && (index < 0xfea0)) {
			if (allow_writes_in_mode_2_3 || !lcdController.operationEnabled || ((STAT & 2) == 0)) {
				OAM[index - 0xfe00] = value;
				return;
			}
			CPULogger.printf("WARNING: Write to OAM[0x%04x] denied during mode " + (STAT & 3) + ", PC=0x%04x", index, cpu.getPC());
			return;
		}
		switch (index & 0x3f) {
		case 0x00:
			if (((value & 0x80) != 0) && !lcdController.operationEnabled) {
				restart();
			}
			lcdController.setValue(value);
			break;
		case 0x01:
			STAT = (STAT & 0x87) | (value & 0x78);
			if (!isCGB && ((STAT & 2) == 0) && lcdController.operationEnabled) {
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
		currentVRAMBank = i * 0x2000;
		if ((i < 0) || (i > 1))
			CPULogger.printf("current offset=%x\n", currentVRAMBank);
	}

	public int getcurVRAMBank() {
		return currentVRAMBank / 0x2000;
	}

	public void renderScanLine() {
		int tilebufBG[] = new int[0x200];
		if (cfskip == 0 && lcdController.operationEnabled) {
			patterns.updatePatternPixels(VRAM);
			int windX = ImageRenderer.SCREEN_WIDTH;
			if (lcdController.windowDisplayEnabled && (WX >= 0) && (WX < 167) && (WY >= 0) && (WY < ImageRenderer.SCREEN_HEIGHT) && (LY >= WY)) {
				windX = (WX - 7);
			}
			renderScanlineBG(windX, tilebufBG);
			if (windX < ImageRenderer.SCREEN_WIDTH) {
				renderScanlineWindow(windX, tilebufBG);
			}
			if (lcdController.spriteDisplayEnabled) {
				renderScanlineSprites();
			}
		}
	}

	private void calcBGTileBuf(int bgTileX, int bgTileY, int windX, int tilebufBG[]) {

		int tileMap = lcdController.backgroundTileMapAddress + bgTileX + (bgTileY * 32);
		int attrMap = tileMap + 0x2000;
		int bufMap = 0;
		int cnt = ((windX + 7) >> 3) + 1;

		for (int i = 0; i < cnt; ++i) {
			int tile = VRAM[tileMap++];
			int attr = VRAM[attrMap++];
			if (!lcdController.tileMapAddressLow) {
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

	private void renderScanlineBG(int windX, int tilebufBG[]) {
		int bufMap = 0;
		int cnt = windX;
		if (cnt == 0) {
			return;
		}

		int bgY = (SCY + LY) & 0xFF;
		int bgTileY = bgY >> 3;
		int bgOffsY = bgY & 7;
		int bgX = SCX;
		int bgTileX = bgX >> 3;
		int bgOffsX = bgX & 7;

		calcBGTileBuf(bgTileX, bgTileY, windX, tilebufBG);

		int curX = 0;

		{
			int ii = tilebufBG[bufMap++];
			int tilePal = tilebufBG[bufMap++];
			for (int t = bgOffsX; t < 8; ++t, --cnt) {
				imageRenderer.updateBLIT(LY, tilePal | patterns.get(ii, bgOffsY, t), curX++);
			}
		}

		if (cnt == 0)
			return;

		while (cnt >= 8) {
			{
				int ii = tilebufBG[bufMap++];
				int tilePal = tilebufBG[bufMap++];
				for (int t = 0; t < 8; ++t) {
					imageRenderer.updateBLIT(LY, tilePal | patterns.get(ii, bgOffsY, t), curX++);
				}
			}
			cnt -= 8;
		}
		{
			int ii = tilebufBG[bufMap++];
			int tilePal = tilebufBG[bufMap++];
			for (int t = 0; cnt > 0; --cnt, ++t) {
				imageRenderer.updateBLIT(LY, tilePal | patterns.get(ii, bgOffsY, t), curX++);
			}
		}
	}

	private void calcWindTileBuf(int bgTileY, int windX, int tilebufBG[]) {
		int tileMap = lcdController.windowTileMapAddress + (bgTileY * 32);
		int attrMap = tileMap + 0x2000;
		int bufMap = 0;
		int cnt = ((ImageRenderer.SCREEN_WIDTH - (windX + 7)) >> 3) + 2;

		for (int i = 0; i < cnt; ++i) {
			int tile = VRAM[tileMap++];

			int attr = VRAM[attrMap++];
			if (!lcdController.tileMapAddressLow) {
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

	private void renderScanlineWindow(int windX, int tilebufBG[]) {
		int bufMap = 0;
		int curX = ((windX) < (0) ? (0) : (windX));
		int cnt = ImageRenderer.SCREEN_WIDTH - curX;
		if (cnt == 0)
			return;
		int bgY = curWNDY++;
		int bgTileY = bgY >> 3;
		int bgOffsY = bgY & 7;

		int bgOffsX = curX - windX;

		calcWindTileBuf(bgTileY, windX, tilebufBG);

		{
			int ii = tilebufBG[bufMap++];
			int TilePal = tilebufBG[bufMap++];
			for (int t = bgOffsX; (t < 8) && (cnt > 0); ++t, --cnt) {
				imageRenderer.updateBLIT(LY, TilePal | patterns.get(ii, bgOffsY, t), curX++);
			}
		}
		while (cnt >= 8) {
			{
				int ii = tilebufBG[bufMap++];
				int TilePal = tilebufBG[bufMap++];
				for (int t = 0; t < 8; ++t) {
					imageRenderer.updateBLIT(LY, TilePal | patterns.get(ii, bgOffsY, t), curX++);
				}
			}
			cnt -= 8;
		}
		{
			int ii = tilebufBG[bufMap++];
			int TilePal = tilebufBG[bufMap++];
			for (int t = 0; cnt > 0; --cnt, ++t) {
				imageRenderer.updateBLIT(LY, TilePal | patterns.get(ii, bgOffsY, t), curX++);
			}
		}
	}

	/**
	 * http://fms.komkon.org/GameBoy/Tech/Software.html
	 * 
	 * Sprites GameBoy video controller can display up to 40 sprites either in
	 * 8x8 or in 8x16 mode. Sprite patterns have the same format as tiles, but
	 * they are taken from the Sprite Pattern Table located at 8000-8FFF and
	 * therefore have unsigned numbers. Sprite attributes reside in the Sprite
	 * Attribute Table (aka OAM) at FE00-FE9F. OAM (Object Attribute Memory) is
	 * divided into 40 4-byte blocks each of which corresponds to a sprite.
	 * 
	 * Blocks have the following format: Byte0 Y position on the screen Byte1 X
	 * position on the screen Byte2 Pattern number 0-255 [notice that unlike
	 * tile numbers, sprite pattern numbers are unsigned] Byte3 Flags: Bit7
	 * Priority Sprite is displayed in front of the window if this bit is set to
	 * 1. Otherwise, sprite is shown behind the window but in front of the
	 * background. Bit6 Y flip Sprite pattern is flipped vertically if this bit
	 * is set to 1. Bit5 X flip Sprite pattern is flipped horizontally if this
	 * bit is set to 1. Bit4 Palette number Sprite colors are taken from OBJ1PAL
	 * if this bit is set to 1 and from OBJ0PAL otherwise.
	 */
	private void renderScanlineSprites() {
		for (int spriteIndex = 0; spriteIndex < 40; ++spriteIndex) {
			int spritePositionY = OAM[(spriteIndex * 4) + 0];
			int spritePositionX = OAM[(spriteIndex * 4) + 1];
			int spriteNumber = OAM[(spriteIndex * 4) + 2];
			int spriteAttribute = OAM[(spriteIndex * 4) + 3];

			int offsetY = LY - spritePositionY + 16;

			if ((offsetY >= 0) && (offsetY < lcdController.spriteHeight) && (spritePositionX > 0) && (spritePositionX < (ImageRenderer.SCREEN_WIDTH + lcdController.spriteWidth))) {
				if ((spriteAttribute & (1 << 6)) != 0) {
					offsetY = lcdController.spriteHeight - offsetY - 1;
				}
				if (lcdController.spriteHeight == 16) {
					spriteNumber &= ~1;
					spriteNumber |= (offsetY >= 8) ? 1 : 0;
					offsetY &= 7;
				}

				if ((spriteAttribute & (1 << 5)) != 0) {
					spriteNumber |= (1 << 10);
				}

				int paletteNumber = (spriteAttribute >> 4) & 1;
				if (isCGB) {
					if ((spriteAttribute & (1 << 3)) != 0) {
						spriteNumber |= (1 << 9);
					}
					paletteNumber = spriteAttribute & 7;
				}

				for (int offsetX = 0; offsetX < 8; ++offsetX) {
					int columnIndex = spritePositionX - 8 + offsetX;
					int color = patterns.get(spriteNumber, offsetY, offsetX);
					if (color != 0 && columnIndex >= 0 && columnIndex < ImageRenderer.SCREEN_WIDTH) {
						imageRenderer.updateBLIT(LY, (paletteNumber << 2) | color, columnIndex);
					}
				}
			}
		}
	}
}
