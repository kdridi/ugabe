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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DebugServer {
	DebugServer() {
	}

	void run() {
		String[] historyA = new String[32];
		String[] historyB = new String[32];
		int historyIndex = 0;
		try {
			ServerSocket srvr = new ServerSocket(2016);
			System.out.println("Awaiting 2 connections");
			Socket socketa = srvr.accept();
			BufferedReader readera = new BufferedReader(new InputStreamReader(socketa.getInputStream()));
			System.out.println("Awaiting 1 connection");
			Socket socketb = srvr.accept();
			BufferedReader readerb = new BufferedReader(new InputStreamReader(socketb.getInputStream()));
			System.out.println("All aboard");
			while (true) {
				String a = readera.readLine();
				String b = readerb.readLine();
				historyA[historyIndex] = a;
				historyB[historyIndex] = b;
				historyIndex = (historyIndex + 1) & (32 - 1);
				if (a == null && b == null || !a.equals(b)) {
					System.out.println("A: " + a);
					System.out.println("B: " + b);
					break;
				}
			}
			for (int i = 0; i < 32; ++i) {
				System.out.println("A(" + i + "): " + historyA[(historyIndex + i) & (32 - 1)]);
				System.out.println("B(" + i + "): " + historyB[(historyIndex + i) & (32 - 1)]);
			}
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		DebugServer server = new DebugServer();
		server.run();
	}
}
