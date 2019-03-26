import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MessengerWithFiles {
	
	public static void main(String[] CLA) {
		List<String> args = Arrays.asList(CLA); // Converts CLA to list

		boolean hasL = args.contains("-l");
		boolean hasS = args.contains("-s");
		boolean hasP = args.contains("-p");
		boolean asServer = false;
		int lPort = 0, sPort = 0; // Listening Port Number, Server Port Number
		String address = "localhost";

		if (!hasL) { // Must contain -l
			System.out.println("Incorrect Parameters");
			System.exit(0);
		}

		lPort = Integer.valueOf(args.get(args.indexOf("-l") + 1)); // Listening port number should be right after -l

		if (hasS || hasP) { // Run as Client
			if (hasP)
				sPort = Integer.valueOf(args.get(args.indexOf("-p") + 1));
			if (hasS)
				address = args.get(args.indexOf("-s") + 1);
		} else
			asServer = true;

		if (asServer)
			startServer(lPort);
		else
			startClient(lPort, sPort);

	}

	public static class Options implements Runnable {
		private DataOutputStream output;
		private int lPort;

		public Options(DataOutputStream output, int lPort) {
			this.output = output;
			this.lPort = lPort;
		}

		@Override
		public void run() {
			try {
				Scanner in = new Scanner(System.in);
				String choice = ""; // Placeholder for the user's choice
				boolean exit = false; //Exits loop
				
				while (choice != null && !exit) {
					System.out.println("Enter an option ('m', 'f', 'x'):\n" + "  (M)essage (send)\n"
							+ "  (F)ile (request)\n" + " e(X)it"); //Prompts user for choice
					choice = in.nextLine();
					switch (choice) {
					case "m": //Send message
						System.out.println("Enter your message:");
						String message = in.nextLine();
						output.writeUTF(message);
						break;
					case "f":
						System.out.println("Which file do you want?");
						String fName = in.nextLine();
						FileTransfer fileTransfer = new FileTransfer(fName, lPort);
						Thread fileTransferThread = new Thread(fileTransfer);
						fileTransferThread.start();
						break;
					case "x": //Exits program/loop
						exit = true;
						break;
					default:
						System.out.println("Invalid");
					}
				}
				
				in.close();
				output.writeUTF(""); //Disconnect from server with EOF
			} catch (Exception e) {
				System.exit(1);
			}
		}
	}
	
	public static class FileTransfer implements Runnable {
		private String fName;
		private int lPort;

		public FileTransfer(String fName, int lPort) {
			this.fName = fName;
			this.lPort = lPort;
		}

		@Override
		public void run() {
			try {
				Socket clientSocket = new Socket("localhost", lPort);
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());

				output.writeUTF(fName);
				FileOutputStream fOut = new FileOutputStream(fName);
				int number_read;
				byte[] buffer = new byte[1500];
				while ((number_read = input.read(buffer)) != -1)
					fOut.write(buffer, 0, number_read);
				fOut.close();
				clientSocket.close();
			} catch (Exception e) {
				System.exit(2);
			}

		}
	}
	
	public static class ReceiveClient implements Runnable { //Client receives from the server i.e. server sends to client
		int lPort;

		public ReceiveClient(int lPort) {
			this.lPort = lPort;
		}

		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(lPort);
				while (true) {
					Socket clientSocket = serverSocket.accept();
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					String fName = input.readUTF();
					File file = new File(fName);
					FileInputStream fInput = new FileInputStream(file);
					byte[] buffer = new byte[1500];
					int number_read;
					while ((number_read = fInput.read(buffer)) != -1)
						output.write(buffer, 0, number_read);
					fInput.close();
					clientSocket.close();
				}
			} catch (Exception e) {
				System.exit(3);
			}

		}
	}
	
	public static class ReceiveServer implements Runnable{
		ServerSocket serverSocket;
		
		public ReceiveServer(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Socket clientSocket = serverSocket.accept();
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					String fName = input.readUTF();
					File file = new File(fName);
					FileInputStream fInput = new FileInputStream(file);
					byte[] buffer = new byte[1500];
					int number_read;
					while ((number_read = fInput.read(buffer)) != -1)
						output.write(buffer, 0, number_read);
					fInput.close();
					clientSocket.close();
				}
			} catch (Exception e) {
				System.exit(4);
			}
		}
	}


	
	private static void startServer(int port) {
		try {
			//Creates new sockets to handle client/server
			ServerSocket serverSocket= new ServerSocket(port);
			Socket clientSocket= serverSocket.accept();
			
			//Handles output and input
			DataOutputStream output= new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input= new DataInputStream(clientSocket.getInputStream());
			
			String lPortString = input.readUTF();
			int lPort = Integer.valueOf(lPortString);
			
			ReceiveServer receive = new ReceiveServer(serverSocket);
			Options options = new Options(output, lPort);

			Thread receiveThread = new Thread(receive);
			Thread optionsThread = new Thread(options);
			
			receiveThread.start();
			optionsThread.start();
			
			String message = "";

			while ((message = input.readUTF()) != null) {
				if (message.length() == 0) {
					break;
				} else {
					System.out.println(message);
				}

			}
			output.writeUTF("");
			serverSocket.close();
			clientSocket.shutdownOutput();
			clientSocket.close();
			System.exit(0);
		} catch (Exception e) {
			System.exit(5);
		}
	}

	private static void startClient(int lPort, int sPort) {
		try {
			Socket clientSocket = new Socket("localhost", sPort);
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			output.writeUTF(Integer.toString(lPort));
			
			ReceiveClient receive = new ReceiveClient(lPort);
			Options options = new Options(output, sPort);

			Thread receiveThread = new Thread(receive);
			Thread optionsThread = new Thread(options);
			
			optionsThread.start();
			receiveThread.start();
			

			String message = "";

			while ((message = input.readUTF()) != null) {
				if (message.length() == 0) {
					break;
				} else {
					System.out.println(message);
				}

			}
			output.writeUTF("");
			clientSocket.close();
			System.exit(0);

		} catch (Exception e) {// Exit when error
			System.exit(6);
		}

	}
}
