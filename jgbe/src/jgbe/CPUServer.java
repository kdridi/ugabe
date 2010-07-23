package jgbe;

import java.io.IOException;

interface CPUServer {

	public abstract void severLink(CPU cpu);

	public abstract void serveLink(CPU cpu) throws IOException;

	public abstract void clientLink(CPU cpu, String target) throws IOException;

	public abstract int updateServer(int lstatus) throws IOException;

}