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
package com.arykow.applications.ugabe.standalone;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import com.arykow.applications.ugabe.client.AudioControllerSoundRegister;

class AudioControllerSoundRegisterStateSaveLoad implements StateSaveLoad<AudioControllerSoundRegister> {
	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, AudioControllerSoundRegister audioControllerSoundRegister) throws IOException {
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.on);
			else
				audioControllerSoundRegister.on = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.pos);
			else
				audioControllerSoundRegister.pos = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.cnt);
			else
				audioControllerSoundRegister.cnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.encnt);
			else
				audioControllerSoundRegister.encnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.swcnt);
			else
				audioControllerSoundRegister.swcnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.len);
			else
				audioControllerSoundRegister.len = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.enlen);
			else
				audioControllerSoundRegister.enlen = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.swlen);
			else
				audioControllerSoundRegister.swlen = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.swfreq);
			else
				audioControllerSoundRegister.swfreq = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.freq);
			else
				audioControllerSoundRegister.freq = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.envol);
			else
				audioControllerSoundRegister.envol = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioControllerSoundRegister.endir);
			else
				audioControllerSoundRegister.endir = distream.readInt();
		}
		;
	}
}