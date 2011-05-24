package org.servalproject.receiver;

import java.io.IOException;
import java.io.InputStream;

public class FFplayThread extends Thread {
	/** Path to fps */
	public static final String FFPLAY_BIN = "lib/ffplay";

	/** Canonical name of hte video */
	private String name;

	private int instanceNumber;

	/**
	 * Set up the name of the video for further treatment in dumps/videos
	 * 
	 * @param instanceNumber
	 *            The number of the writer thread
	 */
	public FFplayThread(int instanceNumber) {
		this.instanceNumber = instanceNumber;
		this.name = new String("video-" + instanceNumber);
		System.out.println("[Player #" + instanceNumber + "] Init.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		System.out.println("[Player #" + instanceNumber + "] Start.");
		try {
			ProcessBuilder demux = new ProcessBuilder(FFPLAY_BIN, "videos/"
					+ name + ".mp4");
			InputStream is = demux.start().getErrorStream();
			dumpStream(is);

		} catch (IOException e) {
			System.err.println("Problem with the demuxer.");
			e.printStackTrace();
		}
	}

	/**
	 * Dump the stream on the error output
	 * 
	 * @param in
	 *            The stream
	 * @throws IOException
	 */
	private void dumpStream(InputStream in) throws IOException {
		byte[] arr = new byte[20];
		int n;
		do {
			n = in.read(arr);
			System.err.write(arr, 0, n);
		} while (n != -1);
	}

}
