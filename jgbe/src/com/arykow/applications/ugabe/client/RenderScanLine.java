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

public class RenderScanLine {
	public final void execute(VideoController videoController) {
		int tilebufBG[] = new int[0x200];
		videoController.patterns.updatePatternPixels(videoController.VRAM);
		int windX = ImageRenderer.SCREEN_WIDTH;
		if (
				videoController.lcdController.windowDisplayEnabled &&
				videoController.WX >= 0 &&
				videoController.WX < 167 &&
				videoController.WY >= 0 &&
				videoController.WY < ImageRenderer.SCREEN_HEIGHT &&
				videoController.LY >= videoController.WY
		) {
			windX = (videoController.WX - 7);
		}
		// renderScanlineBG
		{
			int bufMap = 0;
			int cnt = windX;
			if (cnt == 0) {
				return;
			}

			int bgY = (videoController.SCY + videoController.LY) & 0xFF;
			int bgTileY = bgY >> 3;
			int bgOffsY = bgY & 7;
			int bgX = videoController.SCX;
			int bgTileX = bgX >> 3;
			int bgOffsX = bgX & 7;

			// calcBGTileBuf
			{
				{

					int tileMap = videoController.lcdController.backgroundTileMapAddress + bgTileX + (bgTileY << 5);
					int attrMap = tileMap + 0x2000;
					int bufferIndex = 0;

					for (int i = 0; i < ((windX + 7) >> 3) + 1; ++i) {
						int tile = videoController.VRAM[tileMap++];
						int attr = videoController.VRAM[attrMap++];
						if (!videoController.lcdController.tileMapAddressLow) {
							tile ^= 0x80;
							tile += 0x80;
						}
						tilebufBG[bufferIndex++] = tile | ((attr & 0x08) << 6) | ((attr & 0x60) << 5);
						tilebufBG[bufferIndex++] = ((attr & 7) | 0x08) << 2;
						if ((tileMap & 31) == 0) {
							tileMap -= 32;
							attrMap -= 32;
						}
					}
				}
			}
			int curX = 0;

			{
				int ii = tilebufBG[bufMap++];
				int tilePal = tilebufBG[bufMap++];
				for (int t = bgOffsX; t < 8; ++t, --cnt) {
					videoController.imageRenderer.updateBLIT(videoController.LY, tilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
				}
			}

			if (cnt == 0)
				return;

			while (cnt >= 8) {
				{
					int ii = tilebufBG[bufMap++];
					int tilePal = tilebufBG[bufMap++];
					for (int t = 0; t < 8; ++t) {
						videoController.imageRenderer.updateBLIT(videoController.LY, tilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
					}
				}
				cnt -= 8;
			}
			{
				int ii = tilebufBG[bufMap++];
				int tilePal = tilebufBG[bufMap++];
				for (int t = 0; cnt > 0; --cnt, ++t) {
					videoController.imageRenderer.updateBLIT(videoController.LY, tilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
				}
			}

		}
		if (windX < ImageRenderer.SCREEN_WIDTH) {
			// renderScanlineWindow
			{
				int bufMap = 0;
				int curX = ((windX) < (0) ? (0) : (windX));
				int cnt = ImageRenderer.SCREEN_WIDTH - curX;
				if (cnt == 0)
					return;
				int bgY = videoController.curWNDY++;
				int bgTileY = bgY >> 3;
				int bgOffsY = bgY & 7;

				int bgOffsX = curX - windX;

				// calcWindTileBuf
				{
					int tileMap = videoController.lcdController.windowTileMapAddress + (bgTileY << 5);
					int attrMap = tileMap + 0x2000;
					int bufferIndex = 0;

					for (int i = 0; i < ((ImageRenderer.SCREEN_WIDTH - (windX + 7)) >> 3) + 2; ++i) {
						int tile = videoController.VRAM[tileMap++];

						int attr = videoController.VRAM[attrMap++];
						if (!videoController.lcdController.tileMapAddressLow) {
							tile ^= 0x80;
							tile += 0x80;
						}
						tilebufBG[bufferIndex++] = tile | ((attr & 0x08) << 6) | ((attr & 0x60) << 5);
						tilebufBG[bufferIndex++] = ((attr & 7) | 0x8) << 2;
						if ((tileMap & 31) == 0) {
							tileMap -= 32;
							attrMap -= 32;
						}
					}
				}

				{
					int ii = tilebufBG[bufMap++];
					int TilePal = tilebufBG[bufMap++];
					for (int t = bgOffsX; (t < 8) && (cnt > 0); ++t, --cnt) {
						videoController.imageRenderer.updateBLIT(videoController.LY, TilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
					}
				}
				while (cnt >= 8) {
					{
						int ii = tilebufBG[bufMap++];
						int TilePal = tilebufBG[bufMap++];
						for (int t = 0; t < 8; ++t) {
							videoController.imageRenderer.updateBLIT(videoController.LY, TilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
						}
					}
					cnt -= 8;
				}
				{
					int ii = tilebufBG[bufMap++];
					int TilePal = tilebufBG[bufMap++];
					for (int t = 0; cnt > 0; --cnt, ++t) {
						videoController.imageRenderer.updateBLIT(videoController.LY, TilePal | videoController.patterns.get(ii, bgOffsY, t), curX++);
					}
				}

			}
		}
		if (videoController.lcdController.spriteDisplayEnabled) {
			// renderScanlineSprites();
			{
				for (int spriteIndex = 0; spriteIndex < 40; ++spriteIndex) {
					int spritePositionY = videoController.OAM[(spriteIndex * 4) + 0];
					int spritePositionX = videoController.OAM[(spriteIndex * 4) + 1];
					int spriteNumber = videoController.OAM[(spriteIndex * 4) + 2];
					int spriteAttribute = videoController.OAM[(spriteIndex * 4) + 3];

					int offsetY = videoController.LY - spritePositionY + 16;

					if ((offsetY >= 0) && (offsetY < videoController.lcdController.spriteHeight) && (spritePositionX > 0) && (spritePositionX < (ImageRenderer.SCREEN_WIDTH + videoController.lcdController.spriteWidth))) {
						if ((spriteAttribute & (1 << 6)) != 0) {
							offsetY = videoController.lcdController.spriteHeight - offsetY - 1;
						}
						if (videoController.lcdController.spriteHeight == 16) {
							spriteNumber &= ~1;
							spriteNumber |= (offsetY >= 8) ? 1 : 0;
							offsetY &= 7;
						}

						if ((spriteAttribute & (1 << 5)) != 0) {
							spriteNumber |= (1 << 10);
						}

						int paletteNumber = (spriteAttribute >> 4) & 1;
						if (videoController.isCGB) {
							if ((spriteAttribute & (1 << 3)) != 0) {
								spriteNumber |= (1 << 9);
							}
							paletteNumber = spriteAttribute & 7;
						}

						for (int offsetX = 0; offsetX < 8; ++offsetX) {
							int columnIndex = spritePositionX - 8 + offsetX;
							int color = videoController.patterns.get(spriteNumber, offsetY, offsetX);
							if (color != 0 && columnIndex >= 0 && columnIndex < ImageRenderer.SCREEN_WIDTH) {
								videoController.imageRenderer.updateBLIT(videoController.LY, (paletteNumber << 2) | color, columnIndex);
							}
						}
					}
				}
			}
		}
	}
}