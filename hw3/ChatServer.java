import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
	private static ArrayList<ClientObject> clientsAL = new ArrayList<ClientObject>(); // AL to keep track of all clients

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
			ServerSocket serverSocket = new ServerSocket(port);
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept(); // Connects new clients
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

				String name = input.readUTF(); // First message of client is always the name
				ClientObject serverReceive = new ClientObject(clientSocket, input, output, name); // Class to handle
																									// client
				Thread clientThread = new Thread(serverReceive); // New thread for client messages

				clientsAL.add(serverReceive); // Adds the client to the array list
				clientThread.start(); // Starts thread

			}

		} catch (Exception e) {
		}

	}

	public static class ClientObject implements Runnable {
		private Socket clientSocket;
		private DataInputStream input;
		private DataOutputStream output;
		private String name;

		public ClientObject(Socket clientSocket, DataInputStream input, DataOutputStream output, String name) {
			this.clientSocket = clientSocket;
			this.input = input;
			this.output = output;
			this.name = name;
		}

		@Override
		public void run() {
			while (true) { // Always accepts messages
				try {

					String message = input.readUTF(); // Reads message

					if (message == null || message == "") { // Check if null or EOF. Close client connection and remove
															// from the AL
						clientSocket.close();
						ChatServer.clientsAL.remove(this);
					}

					for (ClientObject client : ChatServer.clientsAL) { // Loop through all clients to relay message
						if (client != this) { // Do not send message to original client
							client.output.writeUTF(name + ": " + message);
						}
					}
				} catch (Exception e) {
					try {
						clientSocket.close();
					} catch (IOException e1) {
					}

					ChatServer.clientsAL.remove(this); // Remove client when complete
				}
			}

		}

	}
}
