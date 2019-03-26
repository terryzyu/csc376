import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ChatClient {
	public static void main(String[] args) {
		int port = 0;

		if (args.length != 1) // Does not contain port number in CLA
			System.exit(0);

		try { // Get port number
			port = Integer.valueOf(args[0]);
		} catch (IndexOutOfBoundsException | NumberFormatException e) { // Invalid port number
			System.exit(0);
		}

		startClient(port);

	}

	private static void startClient(int port) {
		try {
			Socket clientSocket = new Socket("localhost", port); //Socket to localhost
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

			Send send = new Send(output, clientSocket); //Class to handle sending messages to server
			Receive receive = new Receive(input, clientSocket); //Class to handle receiving messages from server
			
			//First message is always the name
			Scanner in = new Scanner(System.in);
			System.out.println("What is your name?");
			String name = in.nextLine();
			
			System.out.println("Sending name to server...");
			output.writeUTF(name);
			
			// Creates new threads
			Thread sendThread = new Thread(send);
			Thread receiveThread = new Thread(receive);
			
			//Start threads
			sendThread.start();
			receiveThread.start();
			
		} catch (Exception e) {
		}

	}
	
	public static class Send implements Runnable{
		private DataOutputStream output;
		private Socket clientSocket;
		
		public Send(DataOutputStream output, Socket clientSocket) {
			this.output = output;
			this.clientSocket = clientSocket;
		}
		@Override
		public void run() {
			Scanner input = new Scanner(System.in); //Handles standard input
			String message = ""; //Placeholder message
			while (input.hasNext()) { //Continuously runs until no input is given
				message = input.nextLine(); //Stores standard input
				try { //Attempts to send message to client/server
					output.writeUTF(message); 
				} catch (IOException e) {}
			}
			input.close(); //Close input when no more messages
			try { //Attempt to close the client socket and shutdown
				clientSocket.shutdownOutput();
				clientSocket.close();
			} catch (Exception e) {
				System.exit(0);
			}
		}
		
	}
	
	public static class Receive implements Runnable{
		DataInputStream input;
		Socket clientSocket;
		
		public Receive(DataInputStream input, Socket clientSocket) {
			this.input = input;
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			String message = ""; //Placeholder message
			try {
				message = input.readUTF(); //Attempts to read the incoming message
				while(true) { //Prints message and continuously receives new messages
					System.out.println(message);
					message = input.readUTF();
				}
			} catch (Exception e) {
			}
			
		}
		
	}
}
