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

public class ColorsTable {
	private final Integer mask;
	public int index = 0;
	public int values[] = new int[8 * 4 * 2];

	public ColorsTable(Integer mask) {
		super();
		this.mask = mask;
	}

	public final void setColor(ImageRenderer imageRenderer, int value) {
		values[this.index & 0x3f] = value;
		updateColors(imageRenderer, (this.index & 0x3E) >> 1);
		if ((this.index & (1 << 7)) != 0) {
			++this.index;
		}
	}

	public int getColor() {
		return values[index & 0x3f];
	}

	public final void updateColors(ImageRenderer imageRenderer, int index) {
		int base = index << 1;

		int data = values[base] | (values[base + 1] << 8);
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
		if (mask != null) {
			paletteColorIndex |= mask.intValue();
		}
		imageRenderer.updatePalette(paletteColorIndex, r, g, b);
	}

	void reset(ImageRenderer imageRenderer) {
		index = 0;
		for (int index = 0; index < 0x20; ++index) {
			values[index * 2] = values[index * 2 + 1] = 0;
			updateColors(imageRenderer, index);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getValue(int index) {
		return values[index];
	}

	public void setValue(int index, int value) {
		values[index] = value;
	}
}