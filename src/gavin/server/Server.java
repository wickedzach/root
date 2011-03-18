package gavin.server;

import gavin.Callback;
import gavin.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Server implements Runnable {
	private Thread main;
	private ServerSocket server;
	private boolean running;
	private Collection<Callback> startup;
	private Collection<Callback> initialize;
	private Collection<Callback> destory;
	private Collection<Callback> shutdown;

	public Server(int port) {
		super();
		startup = Collections.synchronizedCollection(new ArrayList<Callback>());
		initialize = Collections.synchronizedCollection(new ArrayList<Callback>());
		destory = Collections.synchronizedCollection(new ArrayList<Callback>());
		shutdown = Collections.synchronizedCollection(new ArrayList<Callback>());
	}

	// call before startup
	public void addStartup(Callback callback) {
		startup.add(callback);
	}

	// call before initialize
	public void addInitialize(Callback callback) {
		initialize.add(callback);
	}

	// call after destory
	public void addDestory(Callback callback) {
		destory.add(callback);
	}

	// call after shutdown
	public void addShutdown(Callback callback) {
		shutdown.add(callback);
	}

	// initialize -> startup -> destory -> shutdown

	public void initialize() {
		Util.call(initialize);
		// do initialize
	}

	public void startup() throws IOException {
		Util.call(startup);

		running = true;
		server = new ServerSocket();
		main = new Thread(this);
		main.start();
	}

	@Override
	public void run() {
		while (running) {
			try {
				Socket client = server.accept();
				// first bit is session flag if 0 then no session id else following 16 bytes is session id
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void destory() {
		// do destory
		Util.call(destory);
	}

	public void shutdown() throws IOException {
		running = false;
		server.close();
		Util.call(shutdown);
	}
}