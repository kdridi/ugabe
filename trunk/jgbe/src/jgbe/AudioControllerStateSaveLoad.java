package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class AudioControllerStateSaveLoad {
	/**
	 * 
	 */
	private final AudioController audioController;

	/**
	 * @param audioController
	 */
	AudioControllerStateSaveLoad(AudioController audioController) {
		this.audioController = audioController;
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		if (version <= 5) {
			for (int sl_i = 0; sl_i < (0x20); ++sl_i) {
				if ((save))
					dostream.writeByte((this.audioController.IO[sl_i]) & 0xff);
				else
					this.audioController.IO[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		if (6 <= version) {
			for (int sl_i = 0; sl_i < (0x30); ++sl_i) {
				if ((save))
					dostream.writeByte((this.audioController.IO[sl_i]) & 0xff);
				else
					this.audioController.IO[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x10); ++sl_i) {
				if ((save))
					dostream.writeByte((this.audioController.WAVE[sl_i]) & 0xff);
				else
					this.audioController.WAVE[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioController.cyclesLeftToRender);
			else
				this.audioController.cyclesLeftToRender = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.audioController.TimerCountDown);
			else
				this.audioController.TimerCountDown = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.audioController.SweepTimerTick);
			else
				this.audioController.SweepTimerTick = distream.readBoolean();
		}
		;
		
		(this.audioController.S1).stateSaveLoad(save, version, dostream, distream);
		;
		(this.audioController.S2).stateSaveLoad(save, version, dostream, distream);
		;
		(this.audioController.S3).stateSaveLoad(save, version, dostream, distream);
		;
		(this.audioController.S4).stateSaveLoad(save, version, dostream, distream);
		;
		
	}
}