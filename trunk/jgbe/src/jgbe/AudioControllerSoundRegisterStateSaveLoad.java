package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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