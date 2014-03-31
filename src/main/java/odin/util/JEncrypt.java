package odin.util;

import org.apache.commons.codec.binary.Base64;

public class JEncrypt {

	public static void main(String[] argv) {
		System.out.println("Action received: " + argv[0]);
		System.out.println("String received: " + argv[1]);

		if (argv[0].equalsIgnoreCase("decode")) {
			byte[] encodedBytes = argv[1].getBytes();
			String decoded = new String(decode(encodedBytes));
			System.out.println(decoded);
		} else if (argv[0].equalsIgnoreCase("encode")) {
			byte[] encodedBytes2 = encode(argv[0]);
			System.out.println("encodedBytes2 " + new String(encodedBytes2));
		} else {
			System.out.println("Only supported actions are encode or decode");
		}
	}

	public static byte[] decode(byte[] encodedBytes) {
		return Base64.decodeBase64(encodedBytes);
	}

	public static byte[] encode(String text) {
		return Base64.encodeBase64(text.getBytes());
	}
}