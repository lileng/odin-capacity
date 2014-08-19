/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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