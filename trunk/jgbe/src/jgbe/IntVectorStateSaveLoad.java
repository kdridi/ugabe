package jgbe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class IntVectorStateSaveLoad implements StateSaveLoad<IntVector> {
	public void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, IntVector intVector) throws IOException {
		{
			if ((save))
				dostream.writeInt((int) intVector.length);
			else
				intVector.length = distream.readInt();
		}
		;
		if ((!save))
			intVector.data = new int[Math.max(1, intVector.length * 2)];
		{
			for (int sl_i = 0; sl_i < (intVector.length); ++sl_i) {
				if ((save))
					dostream.writeInt((int) intVector.data[sl_i]);
				else
					intVector.data[sl_i] = distream.readInt();
			}
			;
		}
		;
	}
}