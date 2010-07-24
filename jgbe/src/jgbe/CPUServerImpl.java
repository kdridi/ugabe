package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class CPUServerImpl implements CPUServer {

	protected ServerSocket LinkCablesrvr = null;
	protected Socket LinkCablesktOut = null;
	protected Socket LinkCablesktIn = null;
	protected DataInputStream LinkCableIn = null;
	protected DataOutputStream LinkCableOut = null;

	void setDelay(CPU cpu, int ndelay) throws IOException {
		for (int i = 0; i < cpu.LINKdelay; ++i)
			LinkCableIn.readInt();
		cpu.LINKdelay = ndelay;
		for (int i = 0; i < cpu.LINKdelay; ++i)
			LinkCableOut.writeInt(0);
		cpu.LINKmulti = cpu.LINKdelay + 1;
	}

	/* (non-Javadoc)
	 * @see jgbe.CPUServer#severLink(jgbe.CPU)
	 */
	public final void severLink(CPU cpu) {
		try {
			if (LinkCablesrvr != null) {
				LinkCablesrvr.close();
				LinkCablesrvr = null;
			}
			if (LinkCablesktOut != null) {
				LinkCablesktOut.close();
				LinkCablesktOut = null;
			}
			if (LinkCablesktIn != null) {
				LinkCablesktIn.close();
				LinkCablesktIn = null;
			}
			if (LinkCableIn != null) {
				LinkCableIn.close();
				LinkCableIn = null;
			}
			if (LinkCableOut != null) {
				LinkCableOut.close();
				LinkCableOut = null;
			}
		} catch (IOException e) {
			System.out.println("Error while closing socket(s)");
			e.printStackTrace();
		} finally {
			cpu.LinkCableStatus = 0;
		}
	}

	/* (non-Javadoc)
	 * @see jgbe.CPUServer#serveLink(jgbe.CPU)
	 */
	public final void serveLink(CPU cpu) throws IOException {
		if (cpu.LinkCableStatus == 0) {
			LinkCablesrvr = new ServerSocket(0x4321);
			LinkCablesktOut = LinkCablesrvr.accept();
			System.out.println("Connection established");
			LinkCablesktOut.setTcpNoDelay(true);
			LinkCableIn = new DataInputStream(LinkCablesktOut.getInputStream());
			LinkCableOut = new DataOutputStream(LinkCablesktOut.getOutputStream());
			cpu.LinkCableStatus = 1;
			setDelay(cpu, 0);
		} else
			throw new IOException("WARNING: Can't serve while not offline");
	}

	/* (non-Javadoc)
	 * @see jgbe.CPUServer#clientLink(jgbe.CPU, java.lang.String)
	 */
	public final void clientLink(CPU cpu, String target) throws IOException {
		if (cpu.LinkCableStatus == 0) {
			LinkCablesktIn = new Socket(target, 0x4321);
			LinkCablesktIn.setTcpNoDelay(true);
			LinkCableIn = new DataInputStream(LinkCablesktIn.getInputStream());
			LinkCableOut = new DataOutputStream(LinkCablesktIn.getOutputStream());
			cpu.LinkCableStatus = 2;
			setDelay(cpu, 0);
		} else
			throw new IOException("WARNING: Can't client while not offline");
	}

	/* (non-Javadoc)
	 * @see jgbe.CPUServer#updateServer(int)
	 */
	public int updateServer(int lstatus) throws IOException {
		LinkCableOut.writeInt(lstatus);
		LinkCableOut.flush();
		return LinkCableIn.readInt();
	};
}