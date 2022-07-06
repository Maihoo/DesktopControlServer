package TCP;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;
import java.io.*;

public class ConnectionManager {
	public boolean running ;
	public boolean connected;
	public int PORT = 0;
	public int amount = 0;
	public Socket socket;
	public TCPServerThread tcp;
	public ServerSocket serverSocket;
	
	public ConnectionManager(int PORT) {
		this.PORT = PORT;
	}
	
	public void run() {
		
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine(); //you get the IP as a String
			System.out.println("started at: " + ip + ":" + PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		running = true;
		tcp = null;
		
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		while (running) {
			if(!connected) {
				try {
					this.socket = serverSocket.accept();
					System.out.println("Socket accepted");
				} catch (IOException e) {
					System.out.println("I/O error: " + e);
				}
				// new thread for a client	
				executor.execute(new TCPServerThread(this, socket));
				System.out.println("TCP thread created");
			}
		}
	}
	
	public void init() {
		
		try {
			if(serverSocket != null) serverSocket.close();
		} catch (IOException e1) { }
		serverSocket 	= null;
		
		//create Server Socket
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("SOCKET CREATED");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		run();
	}
	
	public void terminate() throws IOException {
		
		System.out.println("Termination");
		connected = false;
		try {
			if(tcp != null) {
				tcp.is.close();
				tcp.os.close();
				tcp.br.close();
				tcp.running = false;
				tcp.socket.close();
				socket.close();
				tcp = null;
			}
			
		}catch (Exception e) {
			System.out.println("tcp probably didn't exist yet. ");
		}
	}
	
}
