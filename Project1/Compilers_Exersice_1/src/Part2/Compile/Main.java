import java_cup.runtime.*;
import java.io.*;


public class Main {
	public static void main(String[] argv) throws Exception{
		File file = new File("Out/Main.java");
		PrintStream out = new PrintStream(file);
		System.setOut( out);
		Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
		p.parse();
	}
}