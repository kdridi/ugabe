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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class LZInputStream extends InputStream {
	private InputStream instr;

	private int wind_bits;
	private int wind_size;
	private int wind_mask;

	private byte[] inbuf;
	private int inpos;
	private int inlen;

	private void write_byte(int b) throws IOException {
		inbuf[(inpos + inlen) & wind_mask] = (byte) b;
		++inlen;
	}

	private void read_input() throws IOException {
		int icode = instr.read();

		if (icode == -1)
			return;

		if ((icode & (1 << 7)) != 0) {

			int nmlen = icode & ~(1 << 7);
			for (; nmlen > 0; --nmlen) {
				icode = instr.read();
				if (icode == -1)
					throw new IOException("Unexpected end of file: literal bytes");
				write_byte(icode);
			}
		} else {

			int rb1 = instr.read();
			int rb2 = instr.read();
			if (rb1 == -1 || rb2 == -1)
				throw new IOException("Unexpected end of file: copy info");
			int offset = (icode << 8) | rb1;
			int mlen = rb2 + LZOutputStream.min_copy_len;
			for (int i = 0; i < mlen; ++i)
				write_byte(inbuf[(inpos + inlen - offset) & wind_mask] & 0xff);
		}
	}

	public LZInputStream(InputStream istr) throws IOException {
		instr = istr;

		int read = instr.read();
		if (read != LZOutputStream.FILE_MAGIX_UNKNOWN)
			throw new IOException("Invalid LZ stream");
		wind_bits = instr.read();
		if (wind_bits == -1 || wind_bits <= 0 || wind_bits > 15)
			throw new IOException("Invalid LZ stream");

		wind_size = 1 << wind_bits;
		wind_mask = wind_size - 1;

		inbuf = new byte[wind_size];

		inpos = 0;
		inlen = 0;
	}

	public int read() throws IOException {
		if (inlen == 0)
			read_input();
		if (inlen == 0)
			return -1;
		int ret = inbuf[inpos] & 0xff;
		++inpos;
		--inlen;
		inpos &= wind_mask;
		return ret;
	}

	public void close() throws IOException {
		if (inbuf != null) {
			inbuf = null;
		}
		instr.close();
	}

	protected void finalize() throws Throwable {
		close();
	}

	public static void main(String[] args) {
		try {
			String ifname = args[0];
			String ofname = args[1];

			System.out.println("Input file : " + ifname);
			System.out.println("Output file: " + ofname);

			InputStream istr = new LZInputStream(new FileInputStream(ifname));
			OutputStream ostr = new FileOutputStream(ofname);

			int x = 0;
			int bread;
			while (-1 != (bread = istr.read())) {
				ostr.write(bread);
				if (++x == 1024 * 1024) {
					System.out.print(".");
					x = 0;
				}
			}

			System.out.println("Done");
			istr.close();
			ostr.close();
		} catch (Throwable e) {
			System.out.println("Something went wrong!");
			e.printStackTrace();
		}
	}

};
