package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class CartridgeStateSaveLoad implements StateSaveLoad<Cartridge> {
	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, Cartridge cartridge) throws IOException {
		boolean isnull = false;
		for (int t = 0; t < Cartridge.MAX_RAM_MM; ++t) {
			if ((save))
				isnull = (cartridge.MM_RAM[t] == null);
			{
				if ((save))
					dostream.writeBoolean(isnull);
				else
					isnull = distream.readBoolean();
			}
			;
			if (!isnull) {
				for (int index = 0; index < (Cartridge.MEMMAP_SIZE); ++index) {
					if ((save))
						dostream.writeByte(((cartridge.MM_RAM[t][index]) & 0xff));
					else
						cartridge.MM_RAM[t][index] = (distream.readUnsignedByte());
				}
				;
			} else if ((!save))
				cartridge.MM_RAM[t] = null;
		}

		{
			for (int sl_i = 0; sl_i < (0x100); ++sl_i) {
				if ((save))
					dostream.writeByte(((cartridge.BIOS_ROM[sl_i]) & 0xff));
				else
					cartridge.BIOS_ROM[sl_i] = (distream.readUnsignedByte());
			}
			;
		}
		;

		{
			if ((save))
				dostream.writeBoolean(cartridge.ram_enabled);
			else
				cartridge.ram_enabled = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(cartridge.RTCRegisterEnabled);
			else
				cartridge.RTCRegisterEnabled = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) cartridge.RomRamModeSelect);
			else
				cartridge.RomRamModeSelect = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) cartridge.CurrentROMBank);
			else
				cartridge.CurrentROMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) cartridge.CurrentRAMBank);
			else
				cartridge.CurrentRAMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) cartridge.CurrentRTCRegister);
			else
				cartridge.CurrentRTCRegister = distream.readInt();
		}
		;
	}
}