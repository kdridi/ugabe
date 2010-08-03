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

public class PixelPatterns {
	private static final int SIZE = 1024;
	private static final int INDEX1 = 0 * SIZE;
	private static final int INDEX2 = 1 * SIZE;
	private static final int INDEX3 = 2 * SIZE;
	private static final int INDEX4 = 3 * SIZE;

	private int patterns[][][] = new int[SIZE << 2][][];
	public boolean dirtyPatterns[] = new boolean[SIZE];
	public boolean dirtyPattern = true;
	
	public void setDirtyPatternEnabled(boolean enable, boolean propagation) {
		dirtyPattern = enable;
		if (propagation) {
			for (int i = 0; i < SIZE; ++i) {
				dirtyPatterns[i] = enable;
			}
		}
	}
	
	public void setDirtyPatternEnabled(int index, boolean enable) {
		dirtyPatterns[index] = enable;
		if (enable) {
			setDirtyPatternEnabled(enable, false);
		}
	}

	public void updatePatternPixels(int[] VRAM) {
		if (dirtyPattern) {
			for (int i = 0; i < SIZE; ++i) {
				if (i == 384) {
					i = 512;
				}
				if (i == 896) {
					break;
				}
				if (dirtyPatterns[i]) {
					if (patterns[i] == null) {
						patterns[i + INDEX1] = new int[8][8];
						patterns[i + INDEX2] = new int[8][8];
						patterns[i + INDEX3] = new int[8][8];
						patterns[i + INDEX4] = new int[8][8];
					}
					for (int y = 0; y < 8; ++y) {
						int offset = (i * 16) + (y * 2);
						for (int x = 0; x < 8; ++x) {
							int color = 0;
							for (int index = 0; index < 2; index++) {
								color |= ((VRAM[offset + index] >> x) & 1) << index;
							}
							patterns[i + INDEX1][y - 0][7 - x] = color;
							patterns[i + INDEX2][y - 0][x - 0] = color;
							patterns[i + INDEX3][7 - y][7 - x] = color;
							patterns[i + INDEX4][7 - y][x - 0] = color;
						}
					}
				}
				setDirtyPatternEnabled(i, false);
			}
			dirtyPattern = false;
		}
	}

	public int get(int ii, int bgOffsY, int t) {
		return patterns[ii][bgOffsY][t];
	}
}