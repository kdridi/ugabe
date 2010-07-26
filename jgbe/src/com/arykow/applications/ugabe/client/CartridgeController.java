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

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CartridgeController {
	private final UGABEServiceAsync service;

	public CartridgeController(UGABEServiceAsync service) {
		super();
		this.service = service;
	}

	public void createCartridge(String filename, final CartridgeCreateHandler handler) {
		service.loadCartridge(filename, new AsyncCallback<List<Integer>>() {
			private Cartridge cartridge = new Cartridge();

			public void onFailure(Throwable caught) {
				CPULogger.log("error loading cartridge from file!: " + caught.toString());
				cartridge.status = Cartridge.STATUS_FATAL_ERROR;
				cartridge.err_msg = caught.getMessage();
//				if (caught instanceof EOFException) {
//					cartridge.err_msg = "This ROM image should have " + (cartridge.rom_mm_size >> 2) + " banks of data,\nbut not all banks appear to be present in the ROM image.\nJGBE will try to emulate the ROM regardless, but beware\nthat this may cause the ROM to lockup or crash.";
//					cartridge.status = Cartridge.STATUS_NONFATAL_ERROR;
//				}
//				if (cartridge.err_msg == null) {
//					cartridge.err_msg = "Java Error messages are useless! (UNKNOWN_ERROR)";
//				}
			}

			public void onSuccess(List<Integer> result) {
				try {
					loadFromStream(cartridge, result.iterator());
				} catch (IOException caught) {
					onFailure(caught);
				}
				handler.onCreateCartridge(cartridge);
			}
		});
	}

	public void loadFromStream(Cartridge cartridge, Iterator<Integer> iterator) throws IOException {
		cartridge.MM_ROM[0] = new int[Cartridge.MEMMAP_SIZE];
		for (int i = 0; i < Cartridge.MEMMAP_SIZE; ++i) {
			cartridge.MM_ROM[0][i] = iterator.next();
		}
		CPULogger.printf("Cartridge MBC type: 0x%02x - ", (cartridge.MM_ROM[0][0x0147]));
		switch ((cartridge.MM_ROM[0][0x0147])) {
		case 0x00:
			cartridge.MBC = 0;
			CPULogger.printf("ROM ONLY");
			break;
		case 0x01:
			cartridge.MBC = 1;
			CPULogger.printf("MBC1");
			break;
		case 0x02:
			cartridge.MBC = 1;
			CPULogger.printf("MBC1+RAM");
			break;
		case 0x03:
			cartridge.MBC = 1;
			CPULogger.printf("MBC1+RAM+BATTERY");
			break;
		case 0x05:
			cartridge.MBC = 2;
			CPULogger.printf("MBC2");
			break;
		case 0x06:
			cartridge.MBC = 2;
			CPULogger.printf("MBC2+BATTERY");
			break;
		case 0x08:
			cartridge.MBC = 0;
			CPULogger.printf("ROM+RAM");
			break;
		case 0x09:
			cartridge.MBC = 0;
			CPULogger.printf("ROM+RAM+BATTERY");
			break;
		case 0x0b:
			cartridge.MBC = -1;
			CPULogger.printf("MMM01");
			break;
		case 0x0c:
			cartridge.MBC = -1;
			CPULogger.printf("MMM01+RAM");
			break;
		case 0x0d:
			cartridge.MBC = -1;
			CPULogger.printf("MMM01+RAM+BATTERY");
			break;
		case 0x0f:
			cartridge.MBC = 3;
			CPULogger.printf("MBC3+TIMER+BATTERY");
			break;
		case 0x10:
			cartridge.MBC = 3;
			CPULogger.printf("MBC3+TIMER+RAM+BATTERY");
			break;
		case 0x11:
			cartridge.MBC = 3;
			CPULogger.printf("MBC3");
			break;
		case 0x12:
			cartridge.MBC = 3;
			CPULogger.printf("MBC3+RAM");
			break;
		case 0x13:
			cartridge.MBC = 3;
			CPULogger.printf("MBC3+RAM+BATTERY");
			break;
		case 0x15:
			cartridge.MBC = 4;
			CPULogger.printf("MBC4");
			break;
		case 0x16:
			cartridge.MBC = 4;
			CPULogger.printf("MBC4+RAM");
			break;
		case 0x17:
			cartridge.MBC = 4;
			CPULogger.printf("MBC4+RAM+BATTERY");
			break;
		case 0x19:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5");
			break;
		case 0x1a:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5+RAM");
			break;
		case 0x1b:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5+RAM+BATTERY");
			break;
		case 0x1c:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5+RUMBLE");
			break;
		case 0x1d:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5+RUMBLE+RAM");
			break;
		case 0x1e:
			cartridge.MBC = 5;
			CPULogger.printf("MBC5+RUMBLE+RAM+BATTERY");
			break;
		case 0xfc:
			cartridge.MBC = -2;
			CPULogger.printf("POCKET CAMERA");
			break;
		case 0xfd:
			cartridge.MBC = -5;
			CPULogger.printf("BANDAI TAMA5");
			break;
		case 0xfe:
			cartridge.MBC = -42;
			CPULogger.printf("HuC3");
			break;
		case 0xff:
			cartridge.MBC = -99;
			CPULogger.printf("HuC1+RAM+BATTERY");
			break;
		default:
			cartridge.MBC = -666;
			CPULogger.log("*UNKNOWN*");
			throw new java.io.IOException("unknown MBC type");
		}
		CPULogger.log(" (MBC" + cartridge.MBC + ")");
		if ((cartridge.MM_ROM[0][0x0143]) == 0) {
			CPULogger.log("Cartridge appears to be a GameBoy game");
		} else {
			CPULogger.log("Cartridge could be a ColorGameBoy game");
		}

		cartridge.rom_mm_size = 0;
		switch ((cartridge.MM_ROM[0][0x0148])) {
		case 0x00:
			cartridge.rom_mm_size = 2 << 2;
			CPULogger.log("ROM size = 32KByte (no ROM banking)");
			break;
		case 0x01:
			cartridge.rom_mm_size = 4 << 2;
			CPULogger.log("ROM size = 64KByte (4 banks)");
			break;
		case 0x02:
			cartridge.rom_mm_size = 8 << 2;
			CPULogger.log("ROM size = 128KByte (8 banks)");
			break;
		case 0x03:
			cartridge.rom_mm_size = 16 << 2;
			CPULogger.log("ROM size = 256KByte (16 banks)");
			break;
		case 0x04:
			cartridge.rom_mm_size = 32 << 2;
			CPULogger.log("ROM size = 512KByte (32 banks)");
			break;
		case 0x05:
			cartridge.rom_mm_size = 64 << 2;
			CPULogger.log("ROM size = 1MByte (64 banks) - only 63 banks used by MBC1");
			break;
		case 0x06:
			cartridge.rom_mm_size = 128 << 2;
			CPULogger.log("ROM size = 2MByte (128 banks) - only 125 banks used by MBC1");
			break;
		case 0x07:
			cartridge.rom_mm_size = 256 << 2;
			CPULogger.log("ROM size = 4MByte (256 banks)");
			break;
		case 0x08:
			cartridge.rom_mm_size = 512 << 2;
			CPULogger.log("ROM size = 8MByte (512 banks)");
			break;
		case 0x52:
			cartridge.rom_mm_size = 72 << 2;
			CPULogger.log("ROM size = 1.1MByte (72 banks)");
			break;
		case 0x53:
			cartridge.rom_mm_size = 80 << 2;
			CPULogger.log("ROM size = 1.2MByte (80 banks)");
			break;
		case 0x54:
			cartridge.rom_mm_size = 96 << 2;
			CPULogger.log("ROM size = 1.5MByte (96 banks)");
			break;
		default:
			CPULogger.printf("WARNING: Non-standard ROM size! (MM_ROM[0][0x0148]=0x%02x=%d)\n", (cartridge.MM_ROM[0][0x0148]), (cartridge.MM_ROM[0][0x0148]));
			cartridge.rom_mm_size = 1;
		}

		cartridge.ram_mm_size = 0;
		switch ((cartridge.MM_ROM[0][0x0149])) {
		case 0x00:
			cartridge.ram_mm_size = 0;
			CPULogger.log(cartridge.MBC == 2 ? "Cartridge has 512x4 bits of RAM" : "Cartridge has no RAM");
			break;
		case 0x01:
			cartridge.ram_mm_size = 1;
			CPULogger.log("Cartridge has 2KBytes of RAM");
			break;
		case 0x02:
			cartridge.ram_mm_size = 2;
			CPULogger.log("Cartridge has 8Kbytes of RAM");
			break;
		case 0x03:
			cartridge.ram_mm_size = 8;
			CPULogger.log("Cartridge has 32 KBytes of RAM (4 banks of 8KBytes each)");
			break;
		case 0x04:
			cartridge.ram_mm_size = 32;
			CPULogger.log("Cartridge has 128 KBytes of RAM (16 banks of 8KBytes each)");
			break;
		default:
			CPULogger.printf("WARNING: Non-standard RAM size! (MM_ROM[0][0x0149]=0x%02x=%d)\n", (cartridge.MM_ROM[0][0x0149]), (cartridge.MM_ROM[0][0x0149]));
			cartridge.ram_mm_size = 32;
		}

		if ((cartridge.MBC == 2) && (cartridge.ram_mm_size == 0))
			cartridge.ram_mm_size = 1;

		cartridge.title = "";
		for (int i = 0; i < 16; ++i) {
			if ((cartridge.MM_ROM[0][0x0134 + i]) == 0)
				break;
			cartridge.title += (char) (cartridge.MM_ROM[0][0x0134 + i]);
		}
		CPULogger.log("ROM Name appears to be `" + cartridge.title + "'");

		CPULogger.printf("Trying to load " + (cartridge.rom_mm_size >> 2) + " banks of ROM ");

		for (int i = 1; i < cartridge.rom_mm_size; ++i) {
			cartridge.MM_ROM[i] = new int[Cartridge.MEMMAP_SIZE];
		}
		for (int i = 1; i < cartridge.rom_mm_size; ++i) {
			CPULogger.printf(".");
			for (int j = 0; j < Cartridge.MEMMAP_SIZE; ++j) {
				cartridge.MM_ROM[i][j] = iterator.next();
			}
		}
		CPULogger.printf("\n");

		for (int i = 0; i < cartridge.ram_mm_size; ++i)
			cartridge.MM_RAM[i] = new int[Cartridge.MEMMAP_SIZE];

	}
}