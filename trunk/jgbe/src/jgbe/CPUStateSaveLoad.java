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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class CPUStateSaveLoad implements StateSaveLoad<CPU> {

	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, CPU cpu) throws IOException {
		if (((0 == -1) || (0 <= version)) && ((9 == -1) || (version <= 9)))
			cpu.first_save_string = "unknown";
		if (((0 == -1) || (0 <= version)) && ((9 == -1) || (version <= 9)))
			cpu.last_save_string = "unknown";
		if (((10 == -1) || (10 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeUTF(cpu.first_save_string);
			else
				cpu.first_save_string = distream.readUTF();
		}
		;
		if ((save))
			cpu.last_save_string = Version.str;
		if (((10 == -1) || (10 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeUTF(cpu.last_save_string);
			else
				cpu.last_save_string = distream.readUTF();
		}
		;

		if (((1 == -1) || (1 <= version)) && ((-1 == -1) || (version <= -1))) {
			{
				if ((save))
					dostream.writeByte((cpu.B) & 0xff);
				else
					cpu.B = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((cpu.C) & 0xff);
				else
					cpu.C = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((cpu.D) & 0xff);
				else
					cpu.D = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((cpu.E) & 0xff);
				else
					cpu.E = distream.readUnsignedByte();
			}
			;
		}
		{
			if ((save))
				dostream.writeByte((cpu.H) & 0xff);
			else
				cpu.H = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((cpu.L) & 0xff);
			else
				cpu.L = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((cpu.F) & 0xff);
			else
				cpu.F = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((cpu.A) & 0xff);
			else
				cpu.A = distream.readUnsignedByte();
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x80); ++sl_i) {
				if ((save))
					dostream.writeByte((cpu.IOP[sl_i]) & 0xff);
				else
					cpu.IOP[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x7f); ++sl_i) {
				if ((save))
					dostream.writeByte(((cpu.HRAM[sl_i]) & 0xff));
				else
					cpu.HRAM[sl_i] = (distream.readUnsignedByte());
			}
			;
		}
		;
		if (((0 == -1) || (0 <= version)) && ((0 == -1) || (version <= 0))) {
			for (int i = 0; i < 8; ++i) {
				{
					for (int sl_i = 0; sl_i < (0x1000); ++sl_i) {
						if ((save))
							dostream.writeByte(((cpu.WRAM[i][sl_i]) & 0xff));
						else
							cpu.WRAM[i][sl_i] = (distream.readUnsignedByte());
					}
					;
				}
				;

			}
		}
		if (((1 == -1) || (1 <= version)) && ((-1 == -1) || (version <= -1))) {
			for (int sl_i = 0; sl_i < (0x08); ++sl_i)
				for (int sl_j = 0; sl_j < (0x1000); ++sl_j) {
					if ((save))
						dostream.writeByte(((cpu.WRAM[sl_i][sl_j]) & 0xff));
					else
						cpu.WRAM[sl_i][sl_j] = (distream.readUnsignedByte());
				}
			;
		}
		;
		{
			if ((save))
				dostream.writeInt((int) cpu.CurrentWRAMBank);
			else
				cpu.CurrentWRAMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cpu.doublespeed);
			else
				cpu.doublespeed = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cpu.speedswitch);
			else
				cpu.speedswitch = distream.readBoolean();
		}
		;

		int DIVcntdwnFix = -1;
		if (((-1 == -1) || (-1 <= version)) && ((18 == -1) || (version <= 18))) {
			if ((save))
				dostream.writeInt((int) DIVcntdwnFix);
			else
				DIVcntdwnFix = distream.readInt();
		}
		;
		if (((19 == -1) || (19 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.divReset);
			else
				cpu.divReset = distream.readLong();
		}
		;
		int TIMAcntdwnFix = -1;
		if (((-1 == -1) || (-1 <= version)) && ((19 == -1) || (version <= 19))) {
			if ((save))
				dostream.writeInt((int) TIMAcntdwnFix);
			else
				TIMAcntdwnFix = distream.readInt();
		}
		;
		if (((20 == -1) || (20 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.TIMAEventCycleCount);
			else
				cpu.TIMAEventCycleCount = distream.readLong();
		}
		;

		if (((-1 == -1) || (-1 <= version)) && ((14 == -1) || (version <= 14))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeInt((int) tempskip);
				else
					tempskip = distream.readInt();
			}
			;
		}
		;
		if (((21 == -1) || (21 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.lastVCRenderCycleCount);
			else
				cpu.lastVCRenderCycleCount = distream.readLong();
		}
		;

		int pc = cpu.getPC();
		{
			if ((save))
				dostream.writeShort((pc) & 0xffff);
			else
				pc = distream.readUnsignedShort();
		}
		;
		cpu.setPC(pc);

		{
			if ((save))
				dostream.writeShort((cpu.SP) & 0xffff);
			else
				cpu.SP = distream.readUnsignedShort();
		}
		;
		{
			if ((save))
				dostream.writeByte((cpu.IE) & 0xff);
			else
				cpu.IE = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cpu.IME);
			else
				cpu.IME = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cpu.halted);
			else
				cpu.halted = distream.readBoolean();
		}
		;
		if (((16 == -1) || (16 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeBoolean(cpu.delayed_halt);
			else
				cpu.delayed_halt = distream.readBoolean();
		}
		;
		if (((16 == -1) || (16 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeBoolean(cpu.halt_fail_inc_pc);
			else
				cpu.halt_fail_inc_pc = distream.readBoolean();
		}
		;
		if (((0 == -1) || (0 <= version)) && ((2 == -1) || (version <= 2))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeByte((tempskip) & 0xff);
				else
					tempskip = distream.readUnsignedByte();
			}
			;
		}
		;
		if (((0 == -1) || (0 <= version)) && ((2 == -1) || (version <= 2))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeByte((tempskip) & 0xff);
				else
					tempskip = distream.readUnsignedByte();
			}
			;
		}
		;
		if (((11 == -1) || (11 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeByte((cpu.KeyStatus) & 0xff);
			else
				cpu.KeyStatus = distream.readUnsignedByte();
		}
		;
		if (((12 == -1) || (12 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeInt((int) cpu.keyBounce);
			else
				cpu.keyBounce = distream.readInt();
		}
		;
		if (((22 == -1) || (22 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.KeyBounceEventCycleCount);
			else
				cpu.KeyBounceEventCycleCount = distream.readLong();
		}
		;
		int keyBounceWaitNextFix = 0;
		if (((12 == -1) || (12 <= version)) && ((21 == -1) || (version <= 21))) {
			if ((save))
				dostream.writeInt((int) keyBounceWaitNextFix);
			else
				keyBounceWaitNextFix = distream.readInt();
		}
		;
		if (((-1 == -1) || (-1 <= version)) && ((18 == -1) || (version <= 18))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeInt((int) tempskip);
				else
					tempskip = distream.readInt();
			}
			;
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cpu.BIOS_enabled);
			else
				cpu.BIOS_enabled = distream.readBoolean();
		}
		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, cpu.cartridge);
		StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, cpu.videoController);
		if (((2 == -1) || (2 <= version)) && ((-1 == -1) || (version <= -1)))
			StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, cpu.audioController);
		;

		if (((3 == -1) || (3 <= version)) && ((13 == -1) || (version <= 13))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeByte((tempskip) & 0xff);
				else
					tempskip = distream.readUnsignedByte();
			}
			;
		}
		;

		if (((0 == -1) || (0 <= version)) && ((8 == -1) || (version <= 8))) {
			if ((save))
				dostream.writeInt((int) cpu.TotalInstrCount);
			else
				cpu.TotalInstrCount = distream.readInt();
		}
		;
		if (((0 == -1) || (0 <= version)) && ((8 == -1) || (version <= 8))) {
			if ((save))
				dostream.writeInt((int) cpu.TotalCycleCount);
			else
				cpu.TotalCycleCount = distream.readInt();
		}
		;
		if (((9 == -1) || (9 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.TotalInstrCount);
			else
				cpu.TotalInstrCount = distream.readLong();
		}
		;
		if (((9 == -1) || (9 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(cpu.TotalCycleCount);
			else
				cpu.TotalCycleCount = distream.readLong();
		}
		;

		if (((4 == -1) || (4 <= version)) && ((16 == -1) || (version <= 16))) {
			int tempskip = 0;
			for (int sl_i = 0; sl_i < (1); ++sl_i) {
				if ((save))
					dostream.writeByte((tempskip) & 0xff);
				else
					tempskip = distream.readUnsignedByte();
			}
			;
		}
		;

		if (((7 == -1) || (7 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeByte((cpu.hblank_dma_state) & 0xff);
			else
				cpu.hblank_dma_state = distream.readUnsignedByte();
		}
		;

		if (((13 == -1) || (13 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save) && cpu.playbackHistoryIndex != -1) {
				boolean t = true;
				{
					if ((save))
						dostream.writeBoolean(t);
					else
						t = distream.readBoolean();
				}
				;
				{
					if ((save))
						dostream.writeInt((int) cpu.lastKeyChange);
					else
						cpu.lastKeyChange = distream.readInt();
				}
				;
				int olen = cpu.keyHistory.size();
				cpu.keyHistory.setSize(cpu.playbackHistoryIndex);
				StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, cpu.keyHistory);
				;
				cpu.keyHistory.setSize(olen);
			} else {
				{
					if ((save))
						dostream.writeBoolean(cpu.keyHistoryEnabled);
					else
						cpu.keyHistoryEnabled = distream.readBoolean();
				}
				;
				if (cpu.keyHistoryEnabled) {
					{
						if ((save))
							dostream.writeInt((int) cpu.lastKeyChange);
						else
							cpu.lastKeyChange = distream.readInt();
					}
					StateSaveLoad.Impl.stateSaveLoad(save, version, dostream, distream, cpu.keyHistory);
				}
			}
			if ((!save))
				cpu.playbackHistoryIndex = -1;
		} else
			cpu.keyHistoryEnabled = false;

		if ((!save)) {
			cpu.refreshMemMap();
			cpu.calcCyclesPerTIMA();
			cpu.NextEventCycleCount = 0;
			cpu.VCRenderEventCycleCount = 0;
			if (((-1 == -1) || (-1 <= version)) && ((18 == -1) || (version <= 18))) {

				cpu.divReset = (cpu.TotalCycleCount + CPU.CYCLES_PER_DIV - 1) - (cpu.IOP[0x04] * CPU.CYCLES_PER_DIV);
				cpu.divReset -= CPU.CYCLES_PER_DIV;
				cpu.divReset += DIVcntdwnFix;
			}
			if (((-1 == -1) || (-1 <= version)) && ((19 == -1) || (version <= 19))) {

				cpu.calcTIMAEventCycleCount();
				cpu.TIMAEventCycleCount -= cpu.cyclesPerTIMA;
				cpu.TIMAEventCycleCount += TIMAcntdwnFix;
				cpu.addEventCycleCount(cpu.TIMAEventCycleCount);
			}
			if (((-1 == -1) || (-1 <= version)) && ((20 == -1) || (version <= 20)))
				cpu.lastVCRenderCycleCount = cpu.TotalCycleCount;
			if (((-1 == -1) || (-1 <= version)) && ((21 == -1) || (version <= 21)))
				cpu.KeyBounceEventCycleCount = (cpu.keyBounce > 0) ? cpu.TotalCycleCount + keyBounceWaitNextFix * ((cpu.doublespeed) ? 10 : 20) : CPU.MAX_CYCLE_COUNT;
			if (((22 == -1) || (22 <= version)) && ((22 == -1) || (version <= 22))) {
				cpu.TIMAEventCycleCount = cpu.TotalCycleCount;
				cpu.KeyBounceEventCycleCount = cpu.TotalCycleCount;
			}
			cpu.TIMAEventPending = (cpu.TIMAEventCycleCount != CPU.MAX_CYCLE_COUNT);
			cpu.KeyBounceEventPending = (cpu.KeyBounceEventCycleCount != CPU.MAX_CYCLE_COUNT);
		}
	}
}