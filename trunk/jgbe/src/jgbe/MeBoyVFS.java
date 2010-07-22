package jgbe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

class MeBoyVFS {

	static protected IVFSNode vfsroot = VFSRoot.getRoot();

	static protected void getFlattenedList(Vector<String> ret, IVFSNode node, String path) {
		if (node.isDir()) {
			Enumeration<IVFSNode> e = node.getChildren();
			while (e.hasMoreElements()) {
				IVFSNode cn = e.nextElement();
				getFlattenedList(ret, cn, path + cn.getName());
			}
		} else if (node.isFile()) {
			ret.addElement(node.getName());
			ret.addElement(node.getName());
			ret.addElement(path);

		}
	}

	static public IVFSNode getNodeFromPath(String[] pathelems, int pathofs, IVFSNode node) {
		if (pathofs == pathelems.length)
			return node;

		if (!node.isDir())
			return null;

		String pelem = pathelems[pathofs];
		Enumeration<IVFSNode> e = node.getChildren();
		while (e.hasMoreElements()) {
			IVFSNode cn = e.nextElement();

			if (cn.getName().equals(pelem) || cn.getName().equals(pelem + "/"))
				return getNodeFromPath(pathelems, pathofs + 1, cn);
		}
		return null;
	}

	private static String[] splitPath(String original) {
		Vector<String> nodes = new Vector<String>();
		String separator = "/";

		int index = original.indexOf(separator);
		while (index >= 0) {
			nodes.addElement(original.substring(0, index));
			original = original.substring(index + separator.length());
			index = original.indexOf(separator);
		}

		nodes.addElement(original);

		String[] result = new String[nodes.size()];
		if (nodes.size() > 0) {
			for (int loop = 0; loop < nodes.size(); loop++) {
				result[loop] = (String) nodes.elementAt(loop);

			}

		}

		return result;
	}

	static public IVFSNode getNodeFromPath(String path) {
		String[] pathelems = splitPath(path);
		return getNodeFromPath(pathelems, 1, vfsroot);
	}

	public static InputStream getCartFileStream(String cartName, int bankpostfix) {
		System.out.println("MeBoyVFS.getInputStream(" + cartName + "," + bankpostfix + ")");

		if (bankpostfix != 0)
			return null;
		IVFSNode cartnode = getNodeFromPath(cartName);
		return cartnode.getInputStream();

	}

	public static InputStream getCartListStream() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		Vector<String> cl = new Vector<String>();
		getFlattenedList(cl, vfsroot, "/");
		System.out.println(cl.size());
		dos.writeInt(cl.size() / 3);
		Enumeration<String> e = cl.elements();
		while (e.hasMoreElements()) {
			String str =  e.nextElement();
			System.out.println(str);
			dos.writeUTF(str);
		}

		dos.flush();
		return new ByteArrayInputStream(bos.toByteArray());

	}
};
