import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ChatServer {
	
	static class ClientObject implements Runnable {
		private String name;
		private Socket clientSocket;
		private DataOutputStream output;
		private DataInputStream input;
		private int lPort;
		
		public ClientObject(String name, Socket socket, DataOutputStream output, DataInputStream input, int lPort) {
			this.name = name;
			this.clientSocket = socket;
			this.output = output;
			this.input = input;
			this.lPort = lPort;
		}
		
		@Override
		public void run() {
			while (true) { // Always accepts messages
				try {
					String message = input.readUTF(); // Reads message
					if(message.startsWith("$")) {
						String transfer = message.substring(1);
						for(ClientObject client : ChatServer.clientsAL) {
							if(client.getName().equals(transfer)) {
								output.writeUTF("$" + client.getLPort());
							}
						}
					} else {
						for(ClientObject client : ChatServer.clientsAL) {
							if(!client.getName().equals(name)) {
								client.getOutput().writeUTF(name + ": " + message);
							}
						}
					}
					
				} catch(Exception e) {
					try {
						clientSocket.close();
					} catch (Exception e2) {
					}
					
					ChatServer.clientsAL.remove(this);
				}
			}
		}
		

		public String getName() {
			return name;
		}

		public Socket getSocket() {
			return clientSocket;
		}

		public DataOutputStream getOutput() {
			return output;
		}

		public int getLPort() {
			return lPort;
		}
		
	}
	
	private static ServerSocket serverSocket;
	private static ArrayList<ClientObject> clientsAL;
	
	public static void main(String[] args) {
		int port = 0;

		if (args.length != 1) // Does not contain port number in CLA
			System.exit(0);

		try { // Get port number
			port = Integer.valueOf(args[0]);
		} catch (IndexOutOfBoundsException | NumberFormatException e) { // Invalid port number
			System.exit(0);
		}

		startServer(port);
		
	}

	private static void startServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			clientsAL = new ArrayList<ClientObject>();
			Socket clientSocket;
			while(true) {
				clientSocket = serverSocket.accept();
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

				String name = input.readUTF();
				int lPort = Integer.valueOf(input.readUTF());
				
				ClientObject serverReceive = new ClientObject(name, clientSocket, output, input, lPort);
				Thread clientThread = new Thread(serverReceive);
				clientsAL.add(serverReceive);
				clientThread.start();
			}
		} catch (IOException e) {
		}
	}
	


}
