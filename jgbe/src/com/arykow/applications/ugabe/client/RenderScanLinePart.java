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

public class RenderScanLinePart {
	public final void execute(VideoController videoController) {
		if ((videoController.STAT_statemachine_state != 1))
			return;
		if (!videoController.lcdController.operationEnabled) {
			for (int i = videoController.pixpos; i < ImageRenderer.SCREEN_WIDTH; ++i) {
				int x = videoController.pixpos + i;
				if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
					videoController.imageRenderer.updateBLIT(videoController.LY, 32 | 0, x);
				}
			}
			videoController.pixpos = ImageRenderer.SCREEN_WIDTH;
			return;
		}
		
		int newLCDCcntdwn = (int) (videoController.LCDCcntdwn - (int) (videoController.cpu.totalCycleCount - videoController.cpu.lastVCRenderCycleCount));
		int cyclesToRender = (videoController.mode3duration - newLCDCcntdwn - videoController.cyclepos - 4);
		videoController.cyclepos += cyclesToRender;
		
		while (cyclesToRender > 0) {
			int sprXPos = videoController.OAM[videoController.spritesOnScanline[videoController.curSprite] | 1] - 8 - videoController.pixpos;
			if ((sprXPos >= 0) && (sprXPos < 8) && (videoController.curSprite < videoController.spriteCountOnScanline - 1)) {
				cyclesToRender -= 2;
				++videoController.curSprite;
				videoController.pixpos -= 8;
			} else if ((!videoController.isCGB) && !videoController.lcdController.spriteDisplayEnabled) {
				for (int i = 0; i < 8; ++i) {
					int x = videoController.pixpos + i;
					if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
						videoController.imageRenderer.updateBLIT(videoController.LY, 0 | 0, x);
						videoController.zbuffer[x] = 0;
					}
				}
			} else {
				int bgline = videoController.SCY + videoController.LY;
				int bgtilemapindex = (((videoController.SCX + videoController.pixpos) & 0xff) >> 3) + ((bgline & 0xf8) << 2);
				int bgtile = videoController.VRAM[videoController.lcdController.backgroundTileMapAddress + bgtilemapindex];
				int bgpal = 0x08 << 2;
				if (!videoController.lcdController.tileMapAddressLow) {
					bgtile ^= 0x80;
					bgtile += 0x80;
				}
				for (int i = 0; i < 8; ++i) {
					int x = videoController.pixpos + i;
					if ((x >= 0) && (x < ImageRenderer.SCREEN_WIDTH)) {
						int color = videoController.patterns[bgtile][bgline & 7][i];
						videoController.imageRenderer.updateBLIT(videoController.LY, bgpal | color, x);
						videoController.zbuffer[x] = color;
					}
				}
			}
			cyclesToRender -= 8;
			videoController.pixpos += 8;
		}
		videoController.cyclepos -= cyclesToRender;
		
		if (videoController.pixpos >= 159) {
			int pricol = 0xff0000;
			for (int curSprite = videoController.spriteCountOnScanline - 1; curSprite >= 0; --curSprite) {
				
				if (!videoController.isCGB) {
					
					int line = videoController.LY - (videoController.OAM[videoController.spritesOnScanline[curSprite]] - 16);
					int xpos = videoController.OAM[videoController.spritesOnScanline[curSprite] | 1] - 8;
					int tile = videoController.OAM[videoController.spritesOnScanline[curSprite] | 2];
					int attr = videoController.OAM[videoController.spritesOnScanline[curSprite] | 3];
					if (videoController.lcdController.spriteHeight == 16) {
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
						int color = videoController.patterns[tile][line][i];
						
						if ((xpos >= 0) && (xpos < ImageRenderer.SCREEN_WIDTH) && (color != 0) && ((videoController.zbuffer[xpos] == 0) || priority)) {
							videoController.imageRenderer.updateBLIT(videoController.LY, pallette | color, xpos);
						}
						++xpos;
					}
					pricol += 0x1600;
				} else {
					
				}
			}
			
		}
	}
}