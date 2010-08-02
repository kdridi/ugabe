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

import java.io.ByteArrayOutputStream;

public class DirectByteArrayOutputStream extends ByteArrayOutputStream {

	public DirectByteArrayOutputStream() {
		super();
	}

	public DirectByteArrayOutputStream(int size) {
		super(size);
	}

	public synchronized byte[] getByteArray() {
		return buf;
	}

	public synchronized byte[] swap(byte[] newBuf) {
		byte[] oldBuf = buf;
		buf = newBuf;
		reset();
		return oldBuf;
	}
}
