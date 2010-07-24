/**
 * 
 */
package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class CartridgeStateSaveLoad {
	/**
	 * 
	 */
	private final Cartridge cartridge;

	/**
	 * @param cartridge
	 */
	CartridgeStateSaveLoad(Cartridge cartridge) {
		this.cartridge = cartridge;
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		boolean isnull = false;
		for (int t = 0; t < Cartridge.MAX_RAM_MM; ++t) {
			if ((save))
				isnull = (this.cartridge.MM_RAM[t] == null);
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
						dostream.writeByte(((this.cartridge.MM_RAM[t][index]) & 0xff));
					else
						this.cartridge.MM_RAM[t][index] = (distream.readUnsignedByte());
				}
				;
			} else if ((!save))
				this.cartridge.MM_RAM[t] = null;
		}

		{
			for (int sl_i = 0; sl_i < (0x100); ++sl_i) {
				if ((save))
					dostream.writeByte(((this.cartridge.BIOS_ROM[sl_i]) & 0xff));
				else
					this.cartridge.BIOS_ROM[sl_i] = (distream.readUnsignedByte());
			}
			;
		}
		;

		{
			if ((save))
				dostream.writeBoolean(this.cartridge.ram_enabled);
			else
				this.cartridge.ram_enabled = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeBoolean(this.cartridge.RTCRegisterEnabled);
			else
				this.cartridge.RTCRegisterEnabled = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.cartridge.RomRamModeSelect);
			else
				this.cartridge.RomRamModeSelect = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.cartridge.CurrentROMBank);
			else
				this.cartridge.CurrentROMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.cartridge.CurrentRAMBank);
			else
				this.cartridge.CurrentRAMBank = distream.readInt();
		}
		;
		{
			if ((save))
				dostream.writeInt((int) this.cartridge.CurrentRTCRegister);
			else
				this.cartridge.CurrentRTCRegister = distream.readInt();
		}
		;
	}
}