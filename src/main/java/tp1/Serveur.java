package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import tp1.exceptions.ConnectionRefusedException;
import tp1.exceptions.SocketUnavailableException;

public class Serveur {

	private final int PORT = 2121;
	private final String SERVERSRC = "./src/main/java/tp1/FichiersServeur/";

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataInputStream input;
	private DataOutputStream output;

	public Serveur() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch(IOException e) {
			throw new SocketUnavailableException("Socket Not found or unavailable");
		}
	}

	public static void main(String[] args) {
		Serveur server = new Serveur();
		server.ecouter();
	}

	public void deconnecterClient() {
		try {
			output.close();
			input.close();
			clientSocket.close();
		} catch(IOException e) {
			throw new SocketUnavailableException("Socket not found or unavailable or input/output compromised");
		}

	}

	public void deconnecterClient(String s) {
		try {
			output.writeUTF(s);
		} catch(IOException e) {
			throw new ConnectionRefusedException("The connection seems to have been interrupted");
		}
		deconnecterClient();
	}

	public void ecouter() {
		while(true) {
			try {
				String command, tokenCommand, tokenFile;
				StringTokenizer commandToken;
				int bytes = 0;
				File f;
				byte[] buffer = new byte[4*1024];
				long size;
				System.out.println("Initiating Server, awaiting Client");
				clientSocket = serverSocket.accept(); // bloquant jusqu'à connexion client
				input = new DataInputStream(clientSocket.getInputStream());
				output = new DataOutputStream(clientSocket.getOutputStream());
				output.writeUTF(
					"Holà gringo ! Yé mé nomme Manuel et yé vais te ajudar à outiliser cé magnifique serveur !\n Tou as le choix entre deux commandes:\n <PUT:nomFichier> - envoyé nomFichier vers le serveur\n <GET:nomFichier> - récoupérer nomFichier dépouis le serveur, caramba !\n Ahora, c'est à toi de youé gringo, moi y'ai oune frontière à passer:");
				command = input.readUTF();
				commandToken = new StringTokenizer(command, ":");
				if (commandToken.countTokens() != 2) {
					deconnecterClient("ERROR:Commande non reconnue, référez-vous au manuel");
					continue;
				}
				tokenCommand = commandToken.nextToken();
				tokenFile = commandToken.nextToken();
				f = new File(SERVERSRC + tokenFile);
				if (tokenCommand.equalsIgnoreCase("PUT")) {
					if (f.exists() && !f.isDirectory()) {
						deconnecterClient("ERROR:Fichier déjà présent sur le serveur");
						continue;
					}
					output.writeUTF("All is good for file reception");
					size = input.readLong();
					FileOutputStream fileOutputStream = new FileOutputStream(f);
					while (size > 0 && (bytes = input.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1 ) {
						fileOutputStream.write(buffer, 0, bytes);
						size -= bytes;
					}
					fileOutputStream.close();
				} else if (tokenCommand.equalsIgnoreCase("GET")) {
					if (f.exists()) {
						output.writeUTF("All is good for file sendoff");
						FileInputStream fileInputStream = new FileInputStream(f);
						output.writeLong(f.length());
						while ((bytes = fileInputStream.read(buffer)) != -1) {
							output.write(buffer, 0, bytes);
							output.flush();
						}
						fileInputStream.close();
					} else {
						deconnecterClient("ERROR:Fichier non existant");
						continue;
					}

				} else {
					deconnecterClient("ERROR:Token non reconnu");
					continue;
				}
			} catch(IOException e) {
				throw new SocketUnavailableException("Connection denied");
			}

		}
	}
}
