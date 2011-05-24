package org.servalproject.receiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author rbochet
 * 
 */
public class Receiver {

	private static final int LOCAL_PORT = 1234;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Start of the receiver program on the port "
				+ Receiver.LOCAL_PORT + " (buffer : "
				+ WriterThread.BUFFER_SIZE + ").");
		ServerSocket serverSocket = new ServerSocket(Receiver.LOCAL_PORT);

		int instanceNumber = 0;
		
		while (true) {
			Socket clientSocket = serverSocket.accept();
			new WriterThread(clientSocket, instanceNumber).start();
			instanceNumber++;
		}
	}
}
