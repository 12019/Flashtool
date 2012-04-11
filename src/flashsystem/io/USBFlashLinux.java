package flashsystem.io;

import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;
import linuxlib.JUsb;

public class USBFlashLinux {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void open(String pid) throws IOException {
		try {
			JUsb.openDevice();
			if (pid.equals("ADDE")) {
				readS1Reply();
				if (lastreply == null) throw new IOException("Unable to read from device");
			}
		}catch (Exception e) {
			if (lastreply == null) throw new IOException("Unable to read from device");
		}
	}

	public static void writeS1(S1Packet p) throws IOException,X10FlashException {
		JUsb.writeDevice(p.getByteArray());
		int count = 0;
		while (true) {
			try {
				readS1Reply();
				break;
			}
			catch (IOException e) {
				try {
					Thread.sleep(500);
				}
				catch (Exception s) {}
				count++;
				if (count==3) {
					throw e;
				}
			}
			catch (X10FlashException e) {
				throw e;
			}
		}
	}

	public static void write(byte[] array) throws IOException,X10FlashException {
		JUsb.writeDevice(array);
	}

    public static  void readS1Reply() throws X10FlashException, IOException
    {
    	S1Packet p = JUsb.readS1Device();
    	if (p!=null) {
    		lastreply = p.getDataArray();
    		lastflags = p.getFlags();
    	}
    	else {
    		lastreply = null;
    	}
    	p.release();
    }

    public static void readReply()  throws X10FlashException, IOException {
    	lastreply = JUsb.readDevice();
    }
    
    public static int getLastFlags() {
    	return lastflags;
    }
    
    public static byte[] getLastReply() {
    	return lastreply;
    }

    public static void close() {
		JUsb.closeDevice();
	}

}