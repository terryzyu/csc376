import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Messenger {
	public static void main(String[] CLA) {
		List<String> args = Arrays.asList(CLA); //Converts CLA to list
		
		boolean asServer = args.contains("-l");
		int port = 0;
		String address = "localhost";
		
		if (asServer) { //Run as server
			try { // Attempts to get port number
				port = Integer.valueOf(args.get(1));
			} catch (IndexOutOfBoundsException | NumberFormatException e) { // No port number provided
				System.exit(0);
			}
		} else { //Run as client
			try { //Get port number
				port = Integer.valueOf(args.get(0));
			} catch(IndexOutOfBoundsException | NumberFormatException e) {
				System.exit(0);
			}
			
			try { //Get server address
				address = args.get(1);
			} catch(IndexOutOfBoundsException e) {}
		}
		
		//Starts appropriate version
		if(asServer)
			startServer(port);
		else
			startClient(port, address);
	}

	private static void startServer(int port) {
		try {
			//Creates new sockets to handle client/server
			ServerSocket serverSocket= new ServerSocket(port);
			Socket clientSocket= serverSocket.accept();
			
			//Handles output and input
			DataOutputStream output= new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input= new DataInputStream(clientSocket.getInputStream());
			
			//Creates new runnable objects for the thread to use later
			Send send = new Send(output, clientSocket);
			ReceiveServer receive = new ReceiveServer(input, clientSocket, serverSocket);
			
			//Creates new threads
			Thread sendThread = new Thread(send);
			Thread receiveThread = new Thread(receive);
			
			//Starts threads
			sendThread.start();
			receiveThread.start();
		}catch(Exception e) { //Exit when error
			System.exit(0);
		}
		
	}

	private static void startClient(int port, String address) {
		try {
			//Create new client socket
			Socket clientSocket = new Socket(address, port);
			
			//Handles output and input
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			
			//Creates new runnable objects for the thread to use later
			Send send = new Send(output, clientSocket);
			ReceiveClient receive = new ReceiveClient(input, clientSocket);
			
			//Creates new threads
			Thread sendThread = new Thread(send);
			Thread receiveThread = new Thread(receive);
			
			//Starts threads
			sendThread.start();
			receiveThread.start();
			
		}catch(Exception e) {//Exit when error
			System.exit(0);
		}
		
	}

	
	/*
	 * Both client and server use the same Send class to send messages to each other
	 * Each is passed their output and client socket to handle the delivery
	 */
	public static class Send implements Runnable {
		
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
			} catch (IOException e) {System.exit(0);
			}
		}
	}
	
	/*
	 * Handles the receiving end of client i.e. the client receives a message from the server
	 * Passed the input stream and client socket
	 */
	public static class ReceiveClient implements Runnable{
		private DataInputStream input;
		private Socket clientSocket;
		
		public ReceiveClient(DataInputStream input, Socket clientSocket) {
			this.input = input;
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			String message = ""; //Placeholder message
			try {
				message = input.readUTF(); //Attempts to read the incoming message
				while(message != null) { //Prints message and continuously receives new messages
					System.out.println(message);
					message = input.readUTF();
				}
				input.close(); //When no more messages, close input stream and shutdown client
				clientSocket.shutdownOutput();
				clientSocket.close();	
				System.exit(0);
			} catch (Exception e) {
				System.exit(0);
			}
			
		}
		
	}
	
	/*
	 * Handles the receiving end of server i.e. the server receives a message from the client
	 * Passed the input stream, client socket, and server socket
	 */
	public static class ReceiveServer implements Runnable{
		private DataInputStream input;
		private Socket clientSocket;
		private ServerSocket serverSocket;
		
		public ReceiveServer(DataInputStream input, Socket clientSocket, ServerSocket serverSocket) {
			this.input = input;
			this.clientSocket = clientSocket;
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			String message = ""; //Placeholder message
			try { //Attempts to read message
				message = input.readUTF();
				while(message != null) { //Continuously reads message and prints until no more is received
					System.out.println(message);
					message = input.readUTF();
				}
				
				input.close(); //Closes input stream and close client and server. 
				clientSocket.shutdownOutput();
				clientSocket.close();
				serverSocket.close();
				System.exit(0);
			} catch (Exception e) {
				System.exit(0);
			}
			
		}
		
	}
}
