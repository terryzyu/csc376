
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



public class ChatClient {

	public static void main(String[] CLA) {
		List<String> args = Arrays.asList(CLA); // Converts CLA to list

		boolean hasL = args.contains("-l");
		boolean hasP = args.contains("-p");
		int lPort = 0, sPort = 0; // Listening Port Number, Server Port Number
		String address = "localhost";

		if (!hasL || !hasP) { // Must contain -l
			System.out.println("Incorrect Parameters");
			System.exit(0);
		}

		lPort = Integer.valueOf(args.get(args.indexOf("-l") + 1)); // Listening port number should be right after -l
		sPort = Integer.valueOf(args.get(args.indexOf("-p") + 1));
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
				String choice = "";
				boolean exit = false;
				while (choice != null && !exit) {
					System.out.println("Enter an option ('m', 'f', 'x'):\n" + "  (M)essage (send)\n"
							+ "  (F)ile (request)\n" + " e(X)it"); // Prompts user for choice
					choice = in.nextLine();
					
					switch (choice) {
					case "m": //Send message
						System.out.println("Enter your message:");
						String message = in.nextLine();
						output.writeUTF(message);
						break;
					case "f":
						System.out.println("Who owns the file?");
						String owner = in.nextLine();
						System.out.println("Which file do you want?");
						String fName = in.nextLine();
						ChatClient.fName = fName;
						output.writeUTF("$" + owner);
						break;
					case "x": //Exits program/loop
						exit = true;
						break;
					default:
						break;
					}
				}
				System.exit(0);
			} catch (Exception e) {
				System.exit(1);
			}
		}

	}
	
	public static class ReceiveClient implements Runnable {
		int lPort;
		
		public ReceiveClient(int lPort) {
			this.lPort = lPort;
		}
		
		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(lPort);
				while(true) {
					Socket clientSocket = serverSocket.accept();
					DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream input = new DataInputStream(clientSocket.getInputStream());
					String fName = input.readUTF();
					File file = new File(fName);
					FileInputStream fInput = new FileInputStream(file);
					byte[] buffer = new byte[1500];
					int number_read;
					while ((number_read = fInput.read(buffer)) != -1) {
						output.write(buffer, 0, number_read);
					}
					fInput.close();
					clientSocket.close();
				}
			} catch (Exception e) {
				System.exit(3);
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

	private static String fName;
	private static void startClient(int lPort, int sPort) {
		try {
			Socket clientSocket = new Socket("localhost", sPort);
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());

			//First message is always the name
			Scanner in = new Scanner(System.in);
			System.out.println("What is your name?");
			String name = in.nextLine();
			
			System.out.println("Sending name to server...");
			output.writeUTF(name);
			
			output.writeUTF(Integer.toString(lPort));
			//Adding above line fixes all the port numbers but messes up the names
			
			ReceiveClient receive = new ReceiveClient(lPort);
			Options options = new Options(output, sPort);

			Thread receiveThread = new Thread(receive);
			Thread optionsThread = new Thread(options);
			
			optionsThread.start();
			receiveThread.start();
			
			String msg = "";
			while((msg = input.readUTF()) != null) {
				if(msg.length() == 0)
					break;
				if(msg.startsWith("$")) {
					int port = Integer.valueOf(msg.substring(1));
					FileTransfer transfer = new FileTransfer(fName, port);
					Thread transferThread = new Thread(transfer);
					transferThread.start();
				}
				else
					System.out.println(msg);
			}
			clientSocket.shutdownOutput();
			clientSocket.close();
			System.exit(0);
		} catch(Exception e) {
			System.exit(1);
		}
	}

}
