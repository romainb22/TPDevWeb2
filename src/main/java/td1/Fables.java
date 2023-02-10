/*
package td1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Fables implements Runnable {

	private String fileName;
	private PrintWriter pw;

	public Fables( String s, PrintWriter pw) {
		fileName = s;
		pw = pw;
	}

	@Override public void run() {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			try {
				while (line = br.readLine() != null) {

				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		} catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}
}
*/
