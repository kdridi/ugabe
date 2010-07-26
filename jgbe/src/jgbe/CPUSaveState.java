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
package jgbe;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

class CPUSaveState {
	public void saveState(DataOutputStream dostream, CPU cpu) throws IOException {
		int saveversion = (23);
		dostream.writeInt((0x4a374a53));
		dostream.writeInt(saveversion);

		int compressionmethod = 1;
		DeflaterOutputStream zostream = null;

		dostream.writeInt(compressionmethod);
		switch (compressionmethod) {

		case 0:
			break;

		case 1: {
			zostream = new GZIPOutputStream(dostream);
			dostream = new DataOutputStream(zostream);
		}
			;
			break;

		default:
			if (!(false))
				throw new Error("Assertion failed: " + "false");
		}
		StateSaveLoad.Impl.stateSaveLoad(true, saveversion, dostream, null, cpu);

		if (zostream != null) {
			dostream.flush();
			zostream.finish();
		}

	}
}