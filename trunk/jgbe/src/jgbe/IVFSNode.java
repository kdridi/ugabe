package jgbe;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public interface IVFSNode {
	public boolean exists();

	public boolean isFile();

	public boolean isDir();

	public String getURL();

	public String getName();

	public InputStream getInputStream();

	public OutputStream getOutputStream();

	public Enumeration<IVFSNode> getChildren();

	public IVFSNode createFile(String name);

	public IVFSNode createDir(String name);

	public void delete(String name);
};
