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


public class ShTablesSLA {

	public final static short val[][] = new short[2][];
	public final static short flag[][] = new short[2][];

	static {
		val[0] = new short[256];
		flag[0] = new short[256];
		for (short i = 0; i < 256; ++i) {
			val[0][i] = (short) ((i << 1) & 0xff);
			flag[0][i] = (short) ((val[0][i] == 0 ? CPU.ZF_Mask : 0) | ((i & 0x80) > 0 ? CPU.CF_Mask : 0));
		}
		val[1] = val[0];
		flag[1] = flag[0];
	}

};
