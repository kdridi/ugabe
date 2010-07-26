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
import java.io.DataOutputStream;
import java.io.IOException;


import com.arykow.applications.ugabe.client.AudioController;
import com.arykow.applications.ugabe.server.StateSaveLoad.Impl;

class AudioControllerStateSaveLoad implements StateSaveLoad<AudioController> {
	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, AudioController audioController) throws IOException {
		if (version <= 5) {
			for (int sl_i = 0; sl_i < (0x20); ++sl_i) {
				if ((save))
					dostream.writeByte((audioController.IO[sl_i]) & 0xff);
				else
					audioController.IO[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		if (6 <= version) {
			for (int sl_i = 0; sl_i < (0x30); ++sl_i) {
				if ((save))
					dostream.writeByte((audioController.IO[sl_i]) & 0xff);
				else
					audioController.IO[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x10); ++sl_i) {
				if ((save))
					dostream.writeByte((audioController.WAVE[sl_i]) & 0xff);
				else
					audioController.WAVE[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioController.cyclesLeftToRender);
			else
				audioController.cyclesLeftToRender = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) audioController.TimerCountDown);
			else
				audioController.TimerCountDown = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(audioController.SweepTimerTick);
			else
				audioController.SweepTimerTick = distream.readBoolean();
		}
		;

		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, audioController.S1);
		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, audioController.S2);
		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, audioController.S3);
		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, audioController.S4);
	}
}