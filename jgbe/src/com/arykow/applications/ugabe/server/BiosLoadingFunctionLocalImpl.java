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
package com.arykow.applications.ugabe.server;

import java.io.DataInputStream;

import com.arykow.applications.ugabe.client.Bios;
import com.arykow.applications.ugabe.client.BiosLoadingFunction;
import com.arykow.applications.ugabe.client.BiosLoadingHandler;

public class BiosLoadingFunctionLocalImpl implements BiosLoadingFunction {
	public void loadBios(String filename, BiosLoadingHandler handler) {
		int[] array = new int[Bios.SIZE];

		for (int i = 0; i < 0x100; ++i) {
			array[i] = (0);
		}
		try {
			DataInputStream distream = FHandler.getDataInputStream(filename);
			for (int i = 0; i < array.length; ++i) {
				array[i] = (distream.readUnsignedByte());
			}
		} catch (Throwable ioe) {
			System.out.println("Using BIOS stub");
			array[0] = (0xc3);
			array[1] = (0x00);
			array[2] = (0x01);
		}

		handler.onLoad(new Bios(array));
	}
}
