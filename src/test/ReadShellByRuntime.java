package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ReadShellByRuntime {
	public static void main(String[] args) {
//		Process p = null;
		BufferedReader in = null;
		try {
//			String cmds = args[0];
//			String[] cmd = new String[]{"/bin/sh", "-c", cmds};
//			p = Runtime.getRuntime().exec(cmd);
//			p.waitFor();
//			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
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
//				p.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
