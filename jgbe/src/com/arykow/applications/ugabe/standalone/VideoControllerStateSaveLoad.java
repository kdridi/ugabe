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

import com.arykow.applications.ugabe.client.VideoController;

class VideoControllerStateSaveLoad implements StateSaveLoad<VideoController> {
	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, VideoController videoController) throws IOException {
		{
			if ((save))
				dostream.writeInt((int) videoController.currentVRAMBank);
			else
				videoController.currentVRAMBank = distream.readInt();
		}
		;
		{
			for (int index = 0; index < (0x4000); ++index) {
				if ((save))
					dostream.writeByte((videoController.VRAM[index]) & 0xff);
				else
					videoController.VRAM[index] = distream.readUnsignedByte();
			}
			;
		}
		{
			for (int index = 0; index < (0xa0); ++index) {
				if ((save))
					dostream.writeByte((videoController.OAM[index]) & 0xff);
				else
					videoController.OAM[index] = distream.readUnsignedByte();
			}
		}
		{
			if ((save))
				dostream.writeByte((videoController.LY) & 0xff);
			else
				videoController.LY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.LYC) & 0xff);
			else
				videoController.LYC = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.SCX) & 0xff);
			else
				videoController.SCX = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.SCY) & 0xff);
			else
				videoController.SCY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.WX) & 0xff);
			else
				videoController.WX = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.WY) & 0xff);
			else
				videoController.WY = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((videoController.lcdController.getValue()) & 0xff);
			else
				videoController.lcdController.setValue(distream.readUnsignedByte());
		}
		;
		if (15 <= version) {
			{
				if ((save))
					dostream.writeInt((int) videoController.LCDCcntdwn);
				else
					videoController.LCDCcntdwn = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) videoController.mode3duration);
				else
					videoController.mode3duration = distream.readInt();
			}
		}
		if (18 <= version) {
			{
				if ((save))
					dostream.writeInt((int) videoController.STAT_statemachine_state);
				else
					videoController.STAT_statemachine_state = distream.readInt();
			}
		}
		{
			if ((save))
				dostream.writeByte((videoController.STAT) & 0xff);
			else
				videoController.STAT = distream.readUnsignedByte();
		}
		if (version <= 17) {

			switch (videoController.STAT & 3) {
			case 0:
				videoController.LCDCcntdwn = 204;
				videoController.STAT_statemachine_state = 2;
				break;
			case 1:
				videoController.LCDCcntdwn = 0;
				videoController.STAT_statemachine_state = 3;
				break;
			case 2:
				videoController.LCDCcntdwn = 80;
				videoController.STAT_statemachine_state = 0;
				break;
			case 3:
				videoController.LCDCcntdwn = 172;
				videoController.STAT_statemachine_state = 1;
				break;
			}
		}

		{
			if ((save))
				dostream.writeByte((videoController.bgpTable.getIndex()) & 0xff);
			else
				videoController.bgpTable.setIndex(distream.readUnsignedByte());
		}
		{
			for (int sl_i = 0; sl_i < (8 * 4 * 2); ++sl_i) {
				if ((save))
					dostream.writeByte((videoController.bgpTable.getValue(sl_i)) & 0xff);
				else
					videoController.bgpTable.setValue(sl_i, distream.readUnsignedByte());
			}
		}
		{
			if ((save))
				dostream.writeByte((videoController.obpTable.getIndex()) & 0xff);
			else
				videoController.obpTable.setIndex(distream.readUnsignedByte());
		}
		{
			for (int sl_i = 0; sl_i < (8 * 4 * 2); ++sl_i) {
				if ((save))
					dostream.writeByte((videoController.obpTable.getValue(sl_i)) & 0xff);
				else
					videoController.obpTable.setValue(sl_i, distream.readUnsignedByte());
			}
		}
		if (8 <= version) {
			if ((save))
				dostream.writeByte((videoController.curWNDY) & 0xff);
			else
				videoController.curWNDY = distream.readUnsignedByte();
		}
		if ((!save)) {
			videoController.patterns.setDirtyPatternEnabled(true, true);
			for (int i = 0; i < 0x20; ++i) {
				videoController.updateBGColData(i);
				videoController.updateOBColData(i);
			}
			videoController.updateMonoColData(0);
			videoController.updateMonoColData(1);
			videoController.updateMonoColData(2);
		}
	}

}