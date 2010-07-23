package jgbe;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

class CPULoadState {
	/**
	 * 
	 */
	private final CPU cpu;

	/**
	 * @param cpu
	 */
	CPULoadState(CPU cpu) {
		this.cpu = cpu;
	}

	public void loadState(DataInputStream distream) throws IOException {
		int loadversion;
		int magix = distream.readInt();
		if (magix != (0x4a374a53)) {
			
			loadversion = 0;
			this.cpu.B = (magix >> 24) & 0xff;
			this.cpu.C = (magix >> 16) & 0xff;
			this.cpu.D = (magix >> 8) & 0xff;
			this.cpu.E = (magix >> 0) & 0xff;
		} else
			loadversion = distream.readInt();
		if (loadversion < (0))
			throw new IOException("save state too old");
		if (loadversion > ((23)))
			throw new IOException("save state too new");
		if (loadversion != (23))
			System.out.println("loading state with old version:" + loadversion);
		
		int compressionmethod = 0;
		if (loadversion >= 5)
			compressionmethod = distream.readInt();
		switch (compressionmethod) {
		
		case 0:
			break;
			
		case 1:
			distream = new DataInputStream(new GZIPInputStream(distream));
			break;
			
		default:
			throw new IOException("unknown compression method:" + compressionmethod);
		}
		new CPUStateSaveLoad(cpu).stateSaveLoad(false, loadversion, null, distream);
	}
}