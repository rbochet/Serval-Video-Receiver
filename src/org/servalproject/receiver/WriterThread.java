package org.servalproject.receiver;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class WriterThread extends Thread {

	private Timestamp currentTimestamp;
	private Socket socket;
	public static final int BUFFER_SIZE = 65536;
	private static int NbInst;
	private final int currentNbInst;

	public WriterThread(Socket socket) {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		currentTimestamp = new Timestamp(now.getTime());

		this.socket = socket;

		currentNbInst = WriterThread.NbInst++;

		System.out.println("[" + currentTimestamp + "] Created Writer thread #"
				+ currentNbInst + " : " + socket);
	}

	@Override
	public void run() {
		try {
			InputStream inputFromPhone = socket.getInputStream();
			DataOutputStream fichier = new DataOutputStream(
					new FileOutputStream("video-" + currentTimestamp + ".mp4"));

			byte[] buffer = new byte[WriterThread.BUFFER_SIZE];
			while (true) {
				int nBytes = inputFromPhone.read(buffer, 0,
						WriterThread.BUFFER_SIZE);
				System.out.println("[" + currentTimestamp + "] Wrote " + nBytes
						+ " bytes on the file for thread #" + currentNbInst
						+ ".");

				if (nBytes < 0)
					break;

				fichier.write(buffer, 0, nBytes);

			}

			System.out.println("[" + currentTimestamp
					+ "] End of data from the client #" + currentNbInst + " on "+ --NbInst);
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
