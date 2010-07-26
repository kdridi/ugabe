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
package com.arykow.applications.ugabe.client;

import java.io.Serializable;


public final class Cartridge implements Serializable {
	private static final long serialVersionUID = -1333527778670544370L;

	public static final int MEMMAP_SIZE = 0x1000;
	public static final int MAX_ROM_MM = 512 << 2;
	public static final int MAX_RAM_MM = 32 << 1;

	public int[][] MM_ROM = new int[MAX_ROM_MM][];
	public int[][] MM_RAM = new int[MAX_RAM_MM][];;
	public int[] BIOS_ROM = new int[0x100];

	public void loadBios(String filename, BiosLoadingFunction function) {
		function.loadBios(filename, new BiosLoadingHandler() {
			public void onLoad(Bios value) {
				value.update(BIOS_ROM);
			}
		});

	}

	public int rom_mm_size;
	public int ram_mm_size;

	public String err_msg;
	public String title;

	public int MBC;

	public boolean ram_enabled = false;
	public boolean RTCRegisterEnabled = false;
	public int RomRamModeSelect = 0;
	public int CurrentROMBank = 1;
	public int CurrentRAMBank = 0;
	public int CurrentRTCRegister = 0;

	public static final int STATUS_OK = 0;
	public static final int STATUS_NONFATAL_ERROR = STATUS_OK + 1;
	public static final int STATUS_FATAL_ERROR = STATUS_NONFATAL_ERROR + 1;
	public int status = STATUS_OK;

	public int getStatus(String[] s) {

		if (s.length > 0) {
			s[0] = err_msg;
		}
		return status;
	}

	public String getTitle() {
		return title;
	}

	public int read(int index) {
		switch (MBC) {
		case 1:
		case 2:
			if (ram_enabled) {
				if ((index >= 0xa000) && (index < 0xa200)) {
					return (MM_RAM[0][index & 0xfff]) & 0xf;
				}
			} else {
				CPULogger.log("Warning: Reading from disabled RAM!");
				return 0;
			}
			CPULogger.printf("Warning: Reading from bogus address: $%04x\n", index);
			return 0xff;
		case 3:

			CPULogger.printf("Error: not using memmap, or reading from cartridge with a noncartridge address $%04x\n", index);
			CPULogger.printf("CurRombank: %d CurrentRAMBank: %d\n", CurrentROMBank, CurrentRAMBank);

			int x[] = new int[] {};
			x[0] = 0;
			return 0xff;
		case 5:

			CPULogger.log("Error: not using memmap, or reading from cartridge with a non cartridge address!");
			return 0xff;
		default:
			CPULogger.log("Error: Cartridge memory bank controller type #" + MBC + " is not implemented!");
			return 0xff;
		}
	}

	public void write(int index, int value) {
		switch (MBC) {
		case 1:

			if ((index >= 0x0000) && (index < 0x2000)) {
				ram_enabled = false;
				if ((value & 0x0f) == 0x0A)
					ram_enabled = true;

			} else if (index < 0x4000) {
				int i = Math.max(1, value & 0x1f);

				CurrentROMBank &= ~0x1f;
				CurrentROMBank |= i;
				CurrentROMBank %= (rom_mm_size >> 2);

			} else if (index < 0x6000) {
				if (RomRamModeSelect == 0) {
					int i = (CurrentROMBank & 0x1f) | ((value & 0x03) << 5);
					CurrentROMBank = i;
					CurrentROMBank %= (rom_mm_size >> 2);
				} else {

					CurrentRAMBank = value & 0x03;
					if (ram_mm_size == 0) {
						CPULogger.log("WARNING! 'Bomberman Collection (J) [S]' hack'" + value);
						CPULogger.printf("setting rom banks 0-15 to banks %d-%d\n", value * 16, (value * 16) + 15);
						for (int i = 0; i < 64; ++i)
							MM_ROM[i] = MM_ROM[(value * 64) | i];
					}
				}
			} else if (index < 0x8000) {
				RomRamModeSelect = value & 1;
			} else if ((index >= 0xA000) && (index <= 0xBFFF)) {

			} else
				CPULogger.printf("TODO: Cartridge writing to $%04x\n", index);
			break;
		case 2:

			if ((0xA0000 <= index) && (index <= 0xA1FF)) {
				CPULogger.log("TODO: write to internal cartridge RAM.");
			} else if ((0x0000 <= index) && (index <= 0x1FFF)) {
				if ((index & (1 << 8)) == 0) {
					ram_enabled = !ram_enabled;

				}
			} else if ((0x2000 <= index) && (index <= 0x3FFFF)) {
				if ((index & (1 << 8)) != 0) {
					value &= 0xf;
					CurrentROMBank = ((value) < (1) ? (1) : (value));
				}
			}
			break;
		case 3:

			if ((index >= 0) && (index < 0x2000)) {
				if ((value & 0x0f) == 0x0A)
					ram_enabled = true;
				else
					ram_enabled = false;
			}
			if ((index >= 0x2000) && (index < 0x4000)) {
				CurrentROMBank = Math.max(value & 0x7f, 1);
			}
			if ((index >= 0x4000) && (index < 0x6000)) {
				if ((value >= 0) && (value < 0x4)) {
					RTCRegisterEnabled = false;
					CurrentRAMBank = value;
				}
				if ((value >= 0x08) && (value < 0x0c)) {
					RTCRegisterEnabled = true;
					CurrentRTCRegister = value - 0x08;
				}
			}
			if ((index >= 0x6000) && (index < 0x8000)) {
				CPULogger.log("TODO: Cartridge.write(): Latch Clock Data!");
			}
			if ((index >= 0xa000) && (index < 0xc000)) {
				if (RTCRegisterEnabled) {
					CPULogger.log("TODO: Cartridge.write(): writing to RAM in RTC mode");
				} else {
					CPULogger.log("Error: not using memmap!");
				}
			}
			if (((index >= 0x8000) && (index < 0xa000)) || ((index > 0xc000)))
				CPULogger.printf("WARNING: Cartridge.write(): Unsupported address for write ($%04x)\n", index);
			break;

		case 5:

			if ((index >= 0) && (index < 0x2000)) {
				if ((value & 0x0f) == 0x0A)
					ram_enabled = true;
				else
					ram_enabled = false;
			}
			if ((index >= 0x2000) && (index < 0x3000)) {
				CurrentROMBank &= 0x100;
				CurrentROMBank |= value;
			}
			if ((index >= 0x3000) && (index < 0x4000)) {
				CurrentROMBank &= 0xff;
				CurrentROMBank |= (value & 1) << 8;
			}
			if ((index >= 0x4000) && (index < 0x6000)) {
				if (value < 0x10)
					CurrentRAMBank = value;
			}
			if ((index >= 0xa000) && (index < 0xc000)) {
				CPULogger.log("Error: not using memmap!");
			}
			if (((index >= 0x6000) && (index < 0xa000)) || ((index > 0xc000)))
				CPULogger.printf("WARNING: Cartridge.write(): Unsupported address for write ($%04x)\n", index);
			break;
		default:
			CPULogger.log("ERROR: Cartridge.write(): Cartridge memory bank controller type #" + MBC + " is not implemented");
		}
	}
}
