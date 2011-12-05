package flashsystem.io;

import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;
import win32lib.JKernel32;

public class USBFlashWin32 {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void open() throws IOException {
		try {
			JKernel32.openDevice();
			readReply();
			JKernel32.closeDevice();
		}catch (Exception e) {
			if (lastreply == null) throw new IOException("Unable to read from device");
		}
	}
	
	public static boolean write(S1Packet p) throws IOException,X10FlashException {
		JKernel32.openDevice();
		JKernel32.writeBytes(p.getByteArray());
		try {
			Thread.sleep(500);
		}
		catch (Exception e) {}
		readReply();
		JKernel32.closeDevice();
		return true;
	}

    public static  void readReply() throws X10FlashException, IOException
    {
    	S1Packet p=null;
		boolean finished = false;
		while (!finished) {
			byte[] read = JKernel32.readBytes(0x10000);
			if (p==null) {
				p = new S1Packet(read);
			}
			else {
				p.addData(read);
			}
			finished=!p.hasMoreToRead();
		}
		lastreply = p.getDataArray();
		lastflags = p.getFlags();
    }

    public static int getLastFlags() {
    	return lastflags;
    }
    
    public static byte[] getLastReply() {
    	return lastreply;
    }

}