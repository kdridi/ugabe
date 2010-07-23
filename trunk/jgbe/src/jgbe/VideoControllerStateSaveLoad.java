/**
 * 
 */
package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class VideoControllerStateSaveLoad {
	/**
	 * 
	 */
	private final VideoController videoController;

	/**
	 * @param videoController
	 */
	VideoControllerStateSaveLoad(VideoController videoController) {
		this.videoController = videoController;
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		{
			if ((save))
				dostream.writeInt((int) this.videoController.CurrentVRAMBank);
			else
				this.videoController.CurrentVRAMBank = distream.readInt();
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x4000); ++sl_i) {
				if ((save))
					dostream.writeByte((this.videoController.VRAM[sl_i]) & 0xff);
				else
					this.videoController.VRAM[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			for (int sl_i = 0; sl_i < (0xa0); ++sl_i) {
				if ((save))
					dostream.writeByte((this.videoController.OAM[sl_i]) & 0xff);
				else
					this.videoController.OAM[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		
		{
			if ((save))
				dostream.writeByte((this.videoController.LY) & 0xff);
			else
				this.videoController.LY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.LYC) & 0xff);
			else
				this.videoController.LYC = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.SCX) & 0xff);
			else
				this.videoController.SCX = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.SCY) & 0xff);
			else
				this.videoController.SCY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.WX) & 0xff);
			else
				this.videoController.WX = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.WY) & 0xff);
			else
				this.videoController.WY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.videoController.LCDC) & 0xff);
			else
				this.videoController.LCDC = distream.readUnsignedByte();
		}
		;
		if (15 <= version) {
			{
				if ((save))
					dostream.writeInt((int) this.videoController.LCDCcntdwn);
				else
					this.videoController.LCDCcntdwn = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) this.videoController.mode3duration);
				else
					this.videoController.mode3duration = distream.readInt();
			}
			;
		}
		if (18 <= version) {
			{
				if ((save))
					dostream.writeInt((int) this.videoController.STAT_statemachine_state);
				else
					this.videoController.STAT_statemachine_state = distream.readInt();
			}
			;
		}
		{
			if ((save))
				dostream.writeByte((this.videoController.STAT) & 0xff);
			else
				this.videoController.STAT = distream.readUnsignedByte();
		}
		;
		if (version <= 17) {
			
			switch (this.videoController.STAT & 3) {
			case 0:
				this.videoController.LCDCcntdwn = 204;
				this.videoController.STAT_statemachine_state = 2;
				break;
			case 1:
				this.videoController.LCDCcntdwn = 0;
				this.videoController.STAT_statemachine_state = 3;
				break;
			case 2:
				this.videoController.LCDCcntdwn = 80;
				this.videoController.STAT_statemachine_state = 0;
				break;
			case 3:
				this.videoController.LCDCcntdwn = 172;
				this.videoController.STAT_statemachine_state = 1;
				break;
			}
		}
		
		{
			if ((save))
				dostream.writeByte((this.videoController.BGPI) & 0xff);
			else
				this.videoController.BGPI = distream.readUnsignedByte();
		}
		;
		{
			for (int sl_i = 0; sl_i < (8 * 4 * 2); ++sl_i) {
				if ((save))
					dostream.writeByte((this.videoController.BGPD[sl_i]) & 0xff);
				else
					this.videoController.BGPD[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		
		{
			if ((save))
				dostream.writeByte((this.videoController.OBPI) & 0xff);
			else
				this.videoController.OBPI = distream.readUnsignedByte();
		}
		;
		{
			for (int sl_i = 0; sl_i < (8 * 4 * 2); ++sl_i) {
				if ((save))
					dostream.writeByte((this.videoController.OBPD[sl_i]) & 0xff);
				else
					this.videoController.OBPD[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		
		if (8 <= version) {
			if ((save))
				dostream.writeByte((this.videoController.curWNDY) & 0xff);
			else
				this.videoController.curWNDY = distream.readUnsignedByte();
		}
		;
		
		if ((!save)) {
			for (int i = 0; i < 1024; ++i) {
				this.videoController.patdirty[i] = true;
			}
			this.videoController.anydirty = true;
			for (int i = 0; i < 0x20; ++i) {
				this.videoController.updateBGColData(i);
				this.videoController.updateOBColData(i);
			}
			;
			this.videoController.updateMonoColData(0);
			this.videoController.updateMonoColData(1);
			this.videoController.updateMonoColData(2);
		}
	}
}