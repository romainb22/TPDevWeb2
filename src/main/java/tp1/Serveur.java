package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;
import org.mindrot.jbcrypt.BCrypt; /* Utilisation de la librairie jbcrypt pour m'initier sur le blowfish, dépendence */
import tp1.exceptions.ConnectionRefusedException;
import tp1.exceptions.SocketUnavailableException;

public class Serveur {

	private final int PORT = 2121;
	private final int GENSALT = 20;
	private final String SERVERSRC = "./src/main/java/tp1/FichiersServeur/";

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataInputStream input;
	private DataOutputStream output;

	private String salt = "";

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
				String command, tokenCommand, tokenFile, response, password, pwdHash;
				StringTokenizer commandToken;
				boolean loggedIn = false, register = false;
				int bytes = 0, i;
				Properties userProps = new Properties();
				File f;
				byte[] buffer = new byte[4*1024];
				long size;
				InputStream in = new FileInputStream("./src/main/java/tp1/users.properties");
				userProps.load(in);
				System.out.println("Initiating Server, awaiting Client");
				clientSocket = serverSocket.accept(); // bloquant jusqu'à connexion client
				input = new DataInputStream(clientSocket.getInputStream());
				output = new DataOutputStream(clientSocket.getOutputStream());
				output.writeUTF(
					"Holà gringo ! Yé mé nomme Manuel et yé bais te ajudar à outiliser cé magnifique serveur !\n"
						+ "Tou as le choix entre deux commandes:\n		<PUT:nomFichier> - envoyé nomFichier vers le serveur\n"
						+ "		<GET:nomFichier> - récoupérer nomFichier dépouis le serveur, caramba !\n"
						+ "Mais antes de poder faire ça, tou bas deboir te connecter bia oune système de pseudo / mot de passe\n"
						+ "Si yamais tou n'est pas enrégistré dans le système tou auras le choix de t'enregistrer, gringo\n"
						+ "Ahora, c'est à toi de youé gringo, moi y'ai oune frontière à passer:");
				while (!loggedIn) {
					output.writeUTF("Veuillez renseigner votre identifiant:");
					command = input.readUTF();
					String login = command;
					String loginProperty;
					for (i=1; !loggedIn ; i++) {
						if (!userProps.containsKey("user."+ i + ".login")) {
							output.writeUTF("Login inconnu, voulez-vous vous enregistrer ? (y/n)");
							response = input.readUTF();
							if (response.equals("y")) {
								register = true;
							}
							break;
						}
						loginProperty = userProps.getProperty("user."+ i + ".login");
						if (loginProperty.equals(login)) {
							output.writeUTF("Bonjour " + login + ", veuillez rentrer votre mot de passe");
							for (int j=1; j<=3 && !loggedIn ; j++) {
								password = input.readUTF();
								long startTime = System.nanoTime();
								if (BCrypt.checkpw(password, userProps.getProperty("user."+ i + ".password"))) {
									output.writeUTF(login +", vous êtes autorisés à utiliser le serveur");
									loggedIn = true;
									long stopTime = System.nanoTime();
									System.out.println(GENSALT + " rounds salt password check: " + (stopTime-startTime)/1000000 + "ms");
									break;
								}
								output.writeUTF("Mot de passe erroné, il vous reste " + (3 - j) + " essai(s)");
							}
						}
					}
					if (register) {
						output.writeUTF("Veuillez rentrer votre mot de passe:");
						response = input.readUTF();
						userProps.setProperty("user."+ i +".login", login);
						long startTime = System.nanoTime();
						userProps.setProperty("user."+ i + ".password", BCrypt.hashpw(response, BCrypt.gensalt(GENSALT)));
						long stopTime = System.nanoTime();
						System.out.println(GENSALT + " rounds salt user register: " + (stopTime-startTime)/1000000 + "ms");
						FileOutputStream fo = new FileOutputStream("./src/main/java/tp1/users.properties");
						userProps.store(fo, "Properties");
						fo.close();
						break;
					} else if(!loggedIn) {
						in.close();
						deconnecterClient("ERROR: Connexion echouée");
						return;
					}
				}
				in.close();

				output.writeUTF("Veuillez rentrer votre commande:");
				// output.writeBoolean(true);

				command = input.readUTF();
				commandToken = new StringTokenizer(command, ":");
				if (commandToken.countTokens() != 2) {
					deconnecterClient("ERROR:Commande non reconnue, référez-vous à Manuel");
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
