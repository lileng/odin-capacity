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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Similar to HTTPResponse:
 * <ul>
 * <li>0 - the program successfully returned
 * <li>4xx - Request not satisfied
 * <li>5xx - Program-side error
 * </ul>
 * 
 * @author mlileng
 *
 */
public class OdinResponse {

	public OdinResponse() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public String getMessageBody() {
		return messageBody.toString();
	}

	public void setMessageBody(String messageBody) {
		this.messageBody.append(messageBody);
	}

	@Override
	public String toString() {
		String returnValue = "<p>Running on: " + hostName + ". <p>Exit Code: " + this.getStatusCode()
				+ "<p>Job Output: <p>"
				+ this.getMessageBody();
			return returnValue;

	}

	private String hostName = "-";
	private int statusCode = 0;
	private String reasonPhrase = "";
	private StringBuffer messageBody = new StringBuffer();

}
