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
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.arykow.applications.ugabe.client.CPU;

class CPULoadState {

	public void loadState(DataInputStream distream, CPU cpu) throws IOException {
		int loadversion;
		int magix = distream.readInt();
		if (magix != (0x4a374a53)) {

			loadversion = 0;
			cpu.B = (magix >> 24) & 0xff;
			cpu.C = (magix >> 16) & 0xff;
			cpu.D = (magix >> 8) & 0xff;
			cpu.E = (magix >> 0) & 0xff;
		} else
			loadversion = distream.readInt();
		if (loadversion < (0))
			throw new IOException("save state too old");
		if (loadversion > ((23)))
			throw new IOException("save state too new");
		if (loadversion != (23))
			System.out.println("loading state with old version:" + loadversion);

		int compressionmethod = 0;
		if (loadversion >= 5)
			compressionmethod = distream.readInt();
		switch (compressionmethod) {

		case 0:
			break;

		case 1:
			distream = new DataInputStream(new GZIPInputStream(distream));
			break;

		default:
			throw new IOException("unknown compression method:" + compressionmethod);
		}
		StateSaveLoad.Impl.stateSaveLoad(false, loadversion, null, distream, cpu);
	}
}