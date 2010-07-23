package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class AudioControllerSoundRegisterStateSaveLoad {
	/**
	 * 
	 */
	private final AudioControllerSoundRegister audioControllerSoundRegister;

	/**
	 * @param audioControllerSoundRegister
	 */
	AudioControllerSoundRegisterStateSaveLoad(AudioControllerSoundRegister audioControllerSoundRegister) {
		this.audioControllerSoundRegister = audioControllerSoundRegister;
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.on);
			else
				this.audioControllerSoundRegister.on = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.pos);
			else
				this.audioControllerSoundRegister.pos = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.cnt);
			else
				this.audioControllerSoundRegister.cnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.encnt);
			else
				this.audioControllerSoundRegister.encnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.swcnt);
			else
				this.audioControllerSoundRegister.swcnt = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.len);
			else
				this.audioControllerSoundRegister.len = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.enlen);
			else
				this.audioControllerSoundRegister.enlen = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.swlen);
			else
				this.audioControllerSoundRegister.swlen = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.swfreq);
			else
				this.audioControllerSoundRegister.swfreq = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.freq);
			else
				this.audioControllerSoundRegister.freq = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.envol);
			else
				this.audioControllerSoundRegister.envol = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioControllerSoundRegister.endir);
			else
				this.audioControllerSoundRegister.endir = distream.readInt();
		}
		;
	}
}