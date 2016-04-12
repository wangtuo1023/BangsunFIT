package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ReadShell {
	public static void main(String[] args) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(System.in));
			String str;
			System.out.println("InputStream");
			while ((str = in.readLine()) != null) {
				System.out.println(">>>" + str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
