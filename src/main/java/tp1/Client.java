package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import tp1.exceptions.SocketUnavailableException;

public class Client {

	private final String CLIENTSRC = "./src/main/java/tp1/FichiersClient/";

	private int port;
	private String host;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataInputStream input;
	private DataOutputStream output;

	public Client(String h, int p) {
		host = h;
		port = p;
	}

	public void initierConnexion() {
		try {
			Scanner in = new Scanner(System.in);
			String command, response, fileName = "";
			StringTokenizer responseToken;
			File f;
			byte[] buffer = new byte[4*1024];
			long fileSize;
			int bytes = 0;
			clientSocket = new Socket(host, port);
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			System.out.println(input.readUTF());
			command = in.nextLine();
			output.writeUTF(command);
			response = input.readUTF();
			responseToken = new StringTokenizer(response, ":");
			System.out.println(response);
			if (responseToken.countTokens() != 2) {
				if (response.contains("sendoff")) {
					fileSize = input.readLong();
					StringTokenizer commandToken = new StringTokenizer(command, ":");
					while (commandToken.hasMoreTokens()) {
						fileName = commandToken.nextToken();
					}
					FileOutputStream outputStream = new FileOutputStream(CLIENTSRC + fileName, false);
					while (fileSize>0 && (bytes = input.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
						outputStream.write(buffer, 0, bytes);
						fileSize -= bytes;
					}
					outputStream.close();
					System.out.println("fichier " + fileName + " bien reçu ");
				} else if (response.contains("reception")) {
					StringTokenizer commandToken = new StringTokenizer(command, ":");
					while (commandToken.hasMoreTokens()) {
						fileName = commandToken.nextToken();
					}
					f = new File(CLIENTSRC + fileName);
					FileInputStream fileInputStream = new FileInputStream(f);
					output.writeLong(f.length());
					while ((bytes = fileInputStream.read(buffer)) != -1) {
						output.write(buffer, 0, bytes);
						output.flush();
					}
					fileInputStream.close();
				}

			} else {
				while (responseToken.hasMoreTokens()) {
					System.err.println(responseToken.nextToken());
				}
			}

		} catch(IOException e) {
			throw new SocketUnavailableException("Socket not found or unavailable");
		}
	}

	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 2121);
		client.initierConnexion();
	}
}
