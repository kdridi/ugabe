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

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import com.google.gwt.user.client.rpc.AsyncCallback;

public final class FHandler {

	private static FHandler fhinstance = new FHandler();

	private static final String RootJGBEDir() throws IOException {
		String path = "";

		File file;
		path = System.getenv("JGBEDIR");
		if (path != null && !path.equals("")) {
			if (!path.endsWith(File.separator))
				path += File.separator;
			file = new File(path);

			if (file.exists() || file.mkdir())
				return path;

			throw new IOException("Can't find or create '" + path + "'($JGBEDIR)");
		}

		path = System.getProperty("user.home") + File.separator + ".jgbe" + File.separator;
		file = new File(path);

		if (file.exists() || file.mkdir())
			return path;

		throw new IOException("Can't find or create '" + path + "'(user.home)");
	}

	public static String JGBEDir(String relpath) throws IOException {
		String path = "";

		if (relpath.length() > 0 && relpath.startsWith(File.separator))
			relpath = relpath.substring(1);
		if (!relpath.endsWith(File.separator))
			relpath += File.separator;

		String rootpath = RootJGBEDir();

		path = rootpath + relpath;
		File dir = new File(path);
		if (!dir.exists() && !dir.mkdirs())
			throw new IOException("Can't or create '" + relpath + "' in '" + rootpath + "'");

		return path;
	}

	public static DataInputStream getDataInputStream(String fname) throws IOException {
		File file = new File(fname);
		InputStream bistream = new FileInputStream(file);
		return new DataInputStream(new BufferedInputStream(fname.endsWith(".zip") ? new ZipInputStream(bistream) : bistream));

	}

	public static void getDataInputStreasm(String fileName, AsyncCallback<List<Integer>> callback) {
		try {
			InputStream inputStream = new FileInputStream(fileName);
			if (fileName.endsWith(".zip")) {
				inputStream = new ZipInputStream(inputStream);
			}
			DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));

			List<Integer> integers = new ArrayList<Integer>();
			boolean finished = false;
			do {
				try {
					integers.add(dataInputStream.readUnsignedByte());
				} catch (EOFException e) {
					finished = true;
				}
			} while (!finished);
			callback.onSuccess(integers);
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}

	public static DataOutputStream getDataOutputStream(String fname) throws IOException {

		int dotPos = 0;
		int dp = fname.indexOf(".");
		while (dp > 0) {
			dotPos = dp;
			dp = fname.indexOf(".", dp + 1);
		}
		String fext = fname.substring(dotPos);
		if (!fext.equals(".zip")) {

			FileOutputStream fostream = new FileOutputStream(fname);
			BufferedOutputStream bostream = new BufferedOutputStream(fostream);
			DataOutputStream dostream = new DataOutputStream(bostream);

			return dostream;
		} else {
			System.out.println("FHandler opening zipfile not supported!");
			return null;
		}

	}

	public static final BufferedInputStream getResourceStream(String filename) throws IOException {
		Class<? extends Object> clazz = fhinstance.getClass();
		InputStream fistream = clazz.getClassLoader().getResourceAsStream(filename);
		BufferedInputStream bistream = new BufferedInputStream(fistream);
		return bistream;
	}

	public static final Font getVeraMonoFont() {
		try {
			InputStream bistream = getResourceStream("VeraMono.ttf");
			Font base = Font.createFont(Font.TRUETYPE_FONT, bistream);
			bistream.close();
			return base.deriveFont(0, 12);
		} catch (Exception e) {
			System.out.println("Error while loading font, using fallback font");
			return new Font("Bitstream Vera Sans Mono", 0, 12);
		}
	}

}
