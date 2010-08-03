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
	public ColorsTable bgpTable = new ColorsTable(0x20);
	public ColorsTable obpTable = new ColorsTable(0x00);
	public void updateBGColData(int i) {
		bgpTable.updateColors(imageRenderer, i);
	}

	public void updateOBColData(int i) {
		obpTable.updateColors(imageRenderer, i);
		
	}

	private RenderScanLine renderScanLine = new RenderScanLine();
	private RenderScanLinePart renderScanLinePart = new RenderScanLinePart();
	private final static boolean useSubscanlineRendering = false;
	
	public LCDController lcdController = new LCDController();
	
	
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

	private final static int GRAYSHADES[][] = { { 0xa0, 0xe0, 0x20 }, { 0x70, 0xb0, 0x40 }, { 0x40, 0x70, 0x32 }, { 0x10, 0x50, 0x26 } };

	final CPU cpu;
	final ImageRenderer imageRenderer;

	private int grayColors[][][] = { GRAYSHADES, GRAYSHADES, GRAYSHADES };
	public int fskip = 1;
	int cfskip = 0;

	
	public final void setGrayShade(int i, int j, int[] colors) {
		System.arraycopy(colors, 0, grayColors[i][j], 0, RGB.values().length);
		updateMonoColDatas();
	}

	private final void updateMonoColDatas() {
		for (int index = 0; index < ColorType.values().length; index++) {
			updateMonoColData(index);
		}
	}

	public final void setGrayShades(int[][][] g) {
		for (int i = 0; i < g.length; i++) {
			setGrayShades(i, g[i]);
		}
		updateMonoColDatas();

	}

	public final void setGrayShades(int[][] g) {
		for (int index = 0; index < ColorType.values().length; index++) {
			setGrayShades(index, g);
		}
		updateMonoColDatas();
	}

	private final void setGrayShades(int index, int[][] values) {
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

	public final void restart() {
		LY = 0;
		STAT = STAT & 0xFC;
		STAT_statemachine_state = 0;
		LCDCcntdwn = 80;
	}

	public final void reset() {
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


		patterns.setDirtyPatternEnabled(true, true);
		patterns.updatePatternPixels(VRAM);

		bgpTable.reset(imageRenderer);
		obpTable.reset(imageRenderer);

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

	public final void updateMonoColData(int index) {
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
					renderScanLinePart.execute(this);
				} else if(cfskip == 0 && lcdController.operationEnabled) {
					renderScanLine.execute(this);
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
		if ((index > 0xFDFF) && (index < 0xFEA0)) {
			if (allow_writes_in_mode_2_3 || !lcdController.operationEnabled || ((STAT & 2) == 0)) {
				return OAM[index - 0xFE00];
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
			b = bgpTable.getIndex();
			break;
		case 0x29:
			b = bgpTable.getColor();
			break;
		case 0x2a:
			b = obpTable.getIndex();
			break;
		case 0x2b:
			b = obpTable.getColor();
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

	public final void write(int index, int value) {
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
				renderScanLinePart.execute(this);
			SCY = value;
			break;
		case 0x03:
			if (useSubscanlineRendering)
				renderScanLinePart.execute(this);
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
				renderScanLinePart.execute(this);
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
			bgpTable.setIndex(value);
			break;
		case 0x29:
			bgpTable.setColor(imageRenderer, value);
			break;
		case 0x2a:
			obpTable.setIndex(value);
			break;
		case 0x2b:
			obpTable.setColor(imageRenderer, value);
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

	public final void selectVRAMBank(int i) {
		currentVRAMBank = i * 0x2000;
		if ((i < 0) || (i > 1))
			CPULogger.printf("current offset=%x\n", currentVRAMBank);
	}

	public int getcurVRAMBank() {
		return currentVRAMBank / 0x2000;
	}
}
