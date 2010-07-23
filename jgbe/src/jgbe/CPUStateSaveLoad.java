package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class CPUStateSaveLoad {
	/**
	 * 
	 */
	private final CPU cpu;

	/**
	 * @param cpu
	 */
	CPUStateSaveLoad(CPU cpu) {
		this.cpu = cpu;
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		if (((0 == -1) || (0 <= version)) && ((9 == -1) || (version <= 9)))
			this.cpu.first_save_string = "unknown";
		if (((0 == -1) || (0 <= version)) && ((9 == -1) || (version <= 9)))
			this.cpu.last_save_string = "unknown";
		if (((10 == -1) || (10 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeUTF(this.cpu.first_save_string);
			else
				this.cpu.first_save_string = distream.readUTF();
		}
		;
		if ((save))
			this.cpu.last_save_string = Version.str;
		if (((10 == -1) || (10 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeUTF(this.cpu.last_save_string);
			else
				this.cpu.last_save_string = distream.readUTF();
		}
		;
		
		if (((1 == -1) || (1 <= version)) && ((-1 == -1) || (version <= -1))) {
			{
				if ((save))
					dostream.writeByte((this.cpu.B) & 0xff);
				else
					this.cpu.B = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((this.cpu.C) & 0xff);
				else
					this.cpu.C = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((this.cpu.D) & 0xff);
				else
					this.cpu.D = distream.readUnsignedByte();
			}
			;
			{
				if ((save))
					dostream.writeByte((this.cpu.E) & 0xff);
				else
					this.cpu.E = distream.readUnsignedByte();
			}
			;
		}
		{
			if ((save))
				dostream.writeByte((this.cpu.H) & 0xff);
			else
				this.cpu.H = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.cpu.L) & 0xff);
			else
				this.cpu.L = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.cpu.F) & 0xff);
			else
				this.cpu.F = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.cpu.A) & 0xff);
			else
				this.cpu.A = distream.readUnsignedByte();
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x80); ++sl_i) {
				if ((save))
					dostream.writeByte((this.cpu.IOP[sl_i]) & 0xff);
				else
					this.cpu.IOP[sl_i] = distream.readUnsignedByte();
			}
			;
		}
		;
		{
			for (int sl_i = 0; sl_i < (0x7f); ++sl_i) {
				if ((save))
					dostream.writeByte(((this.cpu.HRAM[sl_i]) & 0xff));
				else
					this.cpu.HRAM[sl_i] = (distream.readUnsignedByte());
			}
			;
		}
		;
		if (((0 == -1) || (0 <= version)) && ((0 == -1) || (version <= 0))) {
			for (int i = 0; i < 8; ++i) {
				{
					for (int sl_i = 0; sl_i < (0x1000); ++sl_i) {
						if ((save))
							dostream.writeByte(((this.cpu.WRAM[i][sl_i]) & 0xff));
						else
							this.cpu.WRAM[i][sl_i] = (distream.readUnsignedByte());
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
						dostream.writeByte(((this.cpu.WRAM[sl_i][sl_j]) & 0xff));
					else
						this.cpu.WRAM[sl_i][sl_j] = (distream.readUnsignedByte());
				}
			;
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.cpu.CurrentWRAMBank);
			else
				this.cpu.CurrentWRAMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.cpu.doublespeed);
			else
				this.cpu.doublespeed = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.cpu.speedswitch);
			else
				this.cpu.speedswitch = distream.readBoolean();
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
				dostream.writeLong(this.cpu.divReset);
			else
				this.cpu.divReset = distream.readLong();
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
				dostream.writeLong(this.cpu.TIMAEventCycleCount);
			else
				this.cpu.TIMAEventCycleCount = distream.readLong();
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
				dostream.writeLong(this.cpu.lastVCRenderCycleCount);
			else
				this.cpu.lastVCRenderCycleCount = distream.readLong();
		}
		;
		
		int pc = this.cpu.getPC();
		{
			if ((save))
				dostream.writeShort((pc) & 0xffff);
			else
				pc = distream.readUnsignedShort();
		}
		;
		this.cpu.setPC(pc);
		
		{
			if ((save))
				dostream.writeShort((this.cpu.SP) & 0xffff);
			else
				this.cpu.SP = distream.readUnsignedShort();
		}
		;
		{
			if ((save))
				dostream.writeByte((this.cpu.IE) & 0xff);
			else
				this.cpu.IE = distream.readUnsignedByte();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.cpu.IME);
			else
				this.cpu.IME = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.cpu.halted);
			else
				this.cpu.halted = distream.readBoolean();
		}
		;
		if (((16 == -1) || (16 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeBoolean(this.cpu.delayed_halt);
			else
				this.cpu.delayed_halt = distream.readBoolean();
		}
		;
		if (((16 == -1) || (16 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeBoolean(this.cpu.halt_fail_inc_pc);
			else
				this.cpu.halt_fail_inc_pc = distream.readBoolean();
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
				dostream.writeByte((this.cpu.KeyStatus) & 0xff);
			else
				this.cpu.KeyStatus = distream.readUnsignedByte();
		}
		;
		if (((12 == -1) || (12 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeInt((int) this.cpu.keyBounce);
			else
				this.cpu.keyBounce = distream.readInt();
		}
		;
		if (((22 == -1) || (22 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(this.cpu.KeyBounceEventCycleCount);
			else
				this.cpu.KeyBounceEventCycleCount = distream.readLong();
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
				dostream.writeBoolean(this.cpu.BIOS_enabled);
			else
				this.cpu.BIOS_enabled = distream.readBoolean();
		}
		;
		
		(this.cpu.cartridge).stateSaveLoad(save, version, dostream, distream);
		;
		(this.cpu.videoController).stateSaveLoad(save, version, dostream, distream);
		;
		if (((2 == -1) || (2 <= version)) && ((-1 == -1) || (version <= -1)))
			new AudioControllerStateSaveLoad(this.cpu.audioController).stateSaveLoad(save, version, dostream, distream);
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
				dostream.writeInt((int) this.cpu.TotalInstrCount);
			else
				this.cpu.TotalInstrCount = distream.readInt();
		}
		;
		if (((0 == -1) || (0 <= version)) && ((8 == -1) || (version <= 8))) {
			if ((save))
				dostream.writeInt((int) this.cpu.TotalCycleCount);
			else
				this.cpu.TotalCycleCount = distream.readInt();
		}
		;
		if (((9 == -1) || (9 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(this.cpu.TotalInstrCount);
			else
				this.cpu.TotalInstrCount = distream.readLong();
		}
		;
		if (((9 == -1) || (9 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save))
				dostream.writeLong(this.cpu.TotalCycleCount);
			else
				this.cpu.TotalCycleCount = distream.readLong();
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
				dostream.writeByte((this.cpu.hblank_dma_state) & 0xff);
			else
				this.cpu.hblank_dma_state = distream.readUnsignedByte();
		}
		;
		
		if (((13 == -1) || (13 <= version)) && ((-1 == -1) || (version <= -1))) {
			if ((save) && this.cpu.playbackHistoryIndex != -1) {
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
						dostream.writeInt((int) this.cpu.lastKeyChange);
					else
						this.cpu.lastKeyChange = distream.readInt();
				}
				;
				int olen = this.cpu.keyHistory.size();
				this.cpu.keyHistory.setSize(this.cpu.playbackHistoryIndex);
				(this.cpu.keyHistory).stateSaveLoad(save, version, dostream, distream);
				;
				this.cpu.keyHistory.setSize(olen);
			} else {
				{
					if ((save))
						dostream.writeBoolean(this.cpu.keyHistoryEnabled);
					else
						this.cpu.keyHistoryEnabled = distream.readBoolean();
				}
				;
				if (this.cpu.keyHistoryEnabled) {
					{
						if ((save))
							dostream.writeInt((int) this.cpu.lastKeyChange);
						else
							this.cpu.lastKeyChange = distream.readInt();
					}
					;
					(this.cpu.keyHistory).stateSaveLoad(save, version, dostream, distream);
					;
				}
			}
			if ((!save))
				this.cpu.playbackHistoryIndex = -1;
		} else
			this.cpu.keyHistoryEnabled = false;
		
		if ((!save)) {
			this.cpu.refreshMemMap();
			this.cpu.calcCyclesPerTIMA();
			this.cpu.NextEventCycleCount = 0;
			this.cpu.VCRenderEventCycleCount = 0;
			if (((-1 == -1) || (-1 <= version)) && ((18 == -1) || (version <= 18))) {
				
				this.cpu.divReset = (this.cpu.TotalCycleCount + CPU.CYCLES_PER_DIV - 1) - (this.cpu.IOP[0x04] * CPU.CYCLES_PER_DIV);
				this.cpu.divReset -= CPU.CYCLES_PER_DIV;
				this.cpu.divReset += DIVcntdwnFix;
			}
			if (((-1 == -1) || (-1 <= version)) && ((19 == -1) || (version <= 19))) {
				
				this.cpu.calcTIMAEventCycleCount();
				this.cpu.TIMAEventCycleCount -= this.cpu.cyclesPerTIMA;
				this.cpu.TIMAEventCycleCount += TIMAcntdwnFix;
				this.cpu.addEventCycleCount(this.cpu.TIMAEventCycleCount);
			}
			if (((-1 == -1) || (-1 <= version)) && ((20 == -1) || (version <= 20)))
				this.cpu.lastVCRenderCycleCount = this.cpu.TotalCycleCount;
			if (((-1 == -1) || (-1 <= version)) && ((21 == -1) || (version <= 21)))
				this.cpu.KeyBounceEventCycleCount = (this.cpu.keyBounce > 0) ? this.cpu.TotalCycleCount + keyBounceWaitNextFix * ((this.cpu.doublespeed) ? 10 : 20) : CPU.MAX_CYCLE_COUNT;
				if (((22 == -1) || (22 <= version)) && ((22 == -1) || (version <= 22))) {
					this.cpu.TIMAEventCycleCount = this.cpu.TotalCycleCount;
					this.cpu.KeyBounceEventCycleCount = this.cpu.TotalCycleCount;
				}
				this.cpu.TIMAEventPending = (this.cpu.TIMAEventCycleCount != CPU.MAX_CYCLE_COUNT);
				this.cpu.KeyBounceEventPending = (this.cpu.KeyBounceEventCycleCount != CPU.MAX_CYCLE_COUNT);
		}
	}
}