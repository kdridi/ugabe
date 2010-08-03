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

// =========================================================================
// LCD Control Register
// Bits 0000 0000 ABCD EFGH
// 
// 7 A : LCD Display					=> Enabled / Disabled
// 6 B : Window Tile Map Address		=> 9C00-9FFF / 9800-9BFF
// 5 C : Display Window					=> Yes / No
// 4 D : BG & Window Tile Data Address	=> 8000-8FFF / 8800-97FF
// 3 E : BG Tile Map Display Address	=> 9800-9BFF / 9C00-9FFF
// 2 F : Sprite Size					=> 8x16 / 8x8
// 1 G : Display Sprites				=> Yes / No
// 0 H : Display Background				=> Yes / No
// =========================================================================
public class LCDController {
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

	public LCDController() {
		setValue(0);
	}

	public int getValue() {
		return value;
	}

	public final void setValue(int value) {
		this.value = value;
		this.operationEnabled = (value & (1 << 7)) != 0;
		this.windowTileMapAddress = (value & (1 << 6)) != 0 ? 0x1C00 : 0x1800;
		this.windowDisplayEnabled = (value & (1 << 5)) != 0;
		this.tileMapAddressLow = (value & (1 << 4)) != 0;
		this.backgroundTileMapAddress = (value & (1 << 3)) != 0 ? 0x1C00 : 0x1800;
		this.spriteHeight = (value & (1 << 2)) != 0 ? 16 : 8;
		this.spriteDisplayEnabled = (value & (1 << 1)) != 0;
		this.backgroundDisplayEnabled = (value & (1 << 0)) != 0;
	}
}