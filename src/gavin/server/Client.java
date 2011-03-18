package gavin.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Client implements Runnable {
	private Socket socket;

	public Client(Socket socket) {
		super();
		this.socket = socket;
	}

	@Override
	public void run() {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = socket.getInputStream();
			output = socket.getOutputStream();
			execute(input, output);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null)
					input.close();
				if (output != null)
					output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract void execute(InputStream input, OutputStream output) throws IOException;
}