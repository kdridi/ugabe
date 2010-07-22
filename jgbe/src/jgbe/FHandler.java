package jgbe;

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

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

		int dotPos = 0;
		int dp = fname.indexOf(".");
		while (dp >= 0) {
			dotPos = dp;
			dp = fname.indexOf(".", dp + 1);
		}
		String fext = fname.substring(dotPos);
		if (!fext.equals(".zip")) {

			InputStream bistream = new FileInputStream(fname);

			DataInputStream distream = new DataInputStream(bistream);

			return distream;
		} else {

			FileInputStream fistream = new FileInputStream(fname);
			ZipInputStream zistream = new ZipInputStream(fistream);

			BufferedInputStream bistream = new BufferedInputStream(zistream);
			DataInputStream distream = new DataInputStream(bistream);

			return distream;
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
