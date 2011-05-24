package org.servalproject.receiver;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class WriterThread extends Thread {

	private Socket socket;

	public static final int BUFFER_SIZE = 65536;

	/** Canonical name of the video */
	private String fileNameRoot;

	/** Instance en cours */
	private int instance;

	/**
	 * Initialize a writer.
	 * 
	 * @param socket
	 *            The accepted socket
	 * @param instance
	 *            The instance
	 */
	public WriterThread(Socket socket, int instance) {
		this.socket = socket;
		this.instance = instance;

		System.out.println("[Writer #" + instance + "] Init with " + socket);

		fileNameRoot = "video-" + instance;
	}

	@Override
	public void run() {
		System.out.println("[Writer #" + instance + "] Start.");
		try {
			InputStream inputFromPhone = socket.getInputStream();
			DataOutputStream fichier = new DataOutputStream(
					new FileOutputStream("dumps/" + fileNameRoot + ".dump"));

			// Process ffplay = new ProcessBuilder("lib/ffplay",
			// fileNameRoot+".mp4").start();

			byte[] buffer = new byte[WriterThread.BUFFER_SIZE];
			
			/** The minimal number of packet for starting the demuxer */
			int numberMinPktMux = 100;
			
			/** The minimal number of packet for starting ffplay */
			int numberMinPktPlay = 300;
			
			/** The  number of bytes sent */
			long bytesNumber = 0;

			while (true) {
				int nBytes = inputFromPhone.read(buffer, 0,
						WriterThread.BUFFER_SIZE);

				if (nBytes < 0)
					break;
				
				bytesNumber += nBytes;
				
				System.out.println("[ Writer #" + instance + "] Wrote "
						+ nBytes + " bytes (total: "+bytesNumber+") ");

				fichier.write(buffer, 0, nBytes);
				fichier.flush();

				
				if (numberMinPktMux-- ==  0) {
					System.out.println("[ Muxer #" + instance + "] Ready to go.");
					new DemuxerThread(instance).start();
				}

				if (numberMinPktPlay-- ==  0) {
					System.out.println("[ Player #" + instance + "] Ready to go.");
					new FFplayThread(instance).start();
				}

				
			}

			System.out.println("[ Writer #" + instance
					+ "] End of transmission.");
			fichier.flush();
			fichier.close();
			inputFromPhone.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
