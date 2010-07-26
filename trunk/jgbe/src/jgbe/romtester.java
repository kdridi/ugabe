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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

public class romtester {
	private static String romfile = "", logfile = "";

	public static void main(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) != '-')
				romfile = args[i];
			if (args[i].equals("-log"))
				logfile = args[++i];
		}
		if (romfile.equals("")) {
			System.out.println();
			System.out.println("ERROR: missing argument");
			System.out.println();
			System.out.println("USAGE: java swinggui ROMFILE [-log LOGFILE]");
			System.out.println();
			return;
		}

		CartridgeController cartridgeController = new CartridgeController();
		cartridgeController.createCartridge(romfile, new CartridgeCreateHandler() {
			public void onCreateCartridge(Cartridge cartridge) {
				CPU cpu = new CPU(new CPUServerImpl());
				Writer logwriter = null;
				try {
					if (!logfile.equals(""))
						logwriter = new BufferedWriter(new FileWriter(logfile));
				} catch (java.io.IOException e) {
					System.out.println("Error opening logfile:" + e.getMessage());
					logwriter = null;
				}

				String[] messages = { "[empty]" };
				if (cartridge.getStatus(messages) == Cartridge.STATUS_FATAL_ERROR) {
					System.out.println("ERROR: " + messages[0]);
					return;
				}

				System.out.println("Succesfully loaded ROM :)");
				cpu.loadCartridge(cartridge);

				cpu.reset();
				cpu.audioController.isMuted = true;

				int instrlimit = 100;

				while (true) {
					if (logwriter != null) {
						String out = String.format("PC=$%04x AF=$%02x%02x BC=$%02x%02x DE=$%02x%02x HL=$%02x%02x SP=$%04x\n", cpu.getPC(), cpu.A, cpu.F, cpu.B, cpu.C, cpu.D, cpu.E, cpu.H, cpu.L, cpu.SP);

						try {
							logwriter.write(out);
						} catch (java.io.IOException e) {
							System.out.println("Error writing logfile:" + e.getMessage());
							logwriter = null;
						}
					}

					boolean failed = false;
					try {
						cpu.runlooponce();
					} catch (Throwable e) {
						failed = true;
					}

					if ((--instrlimit == 0) || failed) {
						String s = String.format("%02x", cpu.read(cpu.getPC()));
						String ss = String.format("%04x", cpu.getPC());
						s = s.toUpperCase();
						ss = ss.toUpperCase();
						if (logwriter != null) {
							String out = String.format("invalid opcode 0x" + s + " at address 0x" + ss + ", rombank = " + cartridge.CurrentROMBank + "\n");
							try {
								logwriter.write(out);
							} catch (java.io.IOException e) {
								System.out.println("Error writing logfile:" + e.getMessage());
								logwriter = null;
							}
						}
						try {
							logwriter.flush();
						} catch (java.io.IOException e2) {
							System.out.println("Error flushing logfile:" + e2.getMessage());
							logwriter = null;
						}
						return;
					}
				}
			}
		});

	}
}
