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
	private int patterns[][][] = new int[4096][][];
	public boolean dirtyPatterns[] = new boolean[1024];
	public boolean dirtyPattern = true;
	
	public void setDirtyPatternEnabled(boolean enable, boolean propagation) {
		dirtyPattern = enable;
		if (propagation) {
			for (int i = 0; i < 1024; ++i) {
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
			for (int i = 0; i < 1024; ++i) {
				if (i == 384) {
					i = 512;
				}
				if (i == 896) {
					break;
				}
				if (dirtyPatterns[i]) {
					if (patterns[i] == null) {
						patterns[i + 0 * 1024] = new int[8][8];
						patterns[i + 1 * 1024] = new int[8][8];
						patterns[i + 2 * 1024] = new int[8][8];
						patterns[i + 3 * 1024] = new int[8][8];
					}
					for (int y = 0; y < 8; ++y) {
						int offset = (i * 16) + (y * 2);
						for (int x = 0; x < 8; ++x) {
							int color = 0;
							for (int index = 0; index < 2; index++) {
								color |= ((VRAM[offset + index] >> x) & 1) << index;
							}
							patterns[i + 0 * 1024][y - 0][7 - x] = color;
							patterns[i + 1 * 1024][y - 0][x - 0] = color;
							patterns[i + 2 * 1024][7 - y][7 - x] = color;
							patterns[i + 3 * 1024][7 - y][x - 0] = color;
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