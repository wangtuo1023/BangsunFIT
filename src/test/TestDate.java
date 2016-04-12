package test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss.SSS");
		System.out.println("[" + yyyyMMddHHmmssSSS.format(new Date()) + "]");
	}

}
