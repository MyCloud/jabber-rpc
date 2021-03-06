/**
 * Copyright 2011  Pierre-Luc Bacon <pierrelucbacon@aqra.ca>
 * Based off the work of Ulrich Staudinger on XmppXmlRpcApi
 * svn://activequant.org/opt/repositories/xmppxmlrpcapi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activequant.xmpprpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;

import org.activequant.xmpprpc.examplehandler.XmlRpcExampleHandler;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcStreamServer;

public class XmlRpcByteArrayProcessor extends XmlRpcStreamServer {

	protected class ByteArrayStreamConnection implements ServerStreamConnection {
		private ByteArrayInputStream theInByteStream;
		private ByteArrayOutputStream theOutByteStream;
		private byte[] theResponseBytes;

		public byte[] getTheResponseBytes() {
			return theResponseBytes;
		}

		public ByteArrayStreamConnection(byte[] inputArray) {
			theInByteStream = new ByteArrayInputStream(inputArray);
			theOutByteStream = new ByteArrayOutputStream();
		}

		@Override
		public void close() throws IOException {
			theResponseBytes = theOutByteStream.toByteArray();
		}

		@Override
		public InputStream newInputStream() throws IOException {
			return theInByteStream;
		}

		@Override
		public OutputStream newOutputStream() throws IOException {
			return theOutByteStream;
		}
	}

	protected class ByteArrayConfig implements XmlRpcStreamRequestConfig {

		@Override
		public boolean isEnabledForExceptions() {
			return true;
		}

		@Override
		public boolean isGzipCompressing() {
			return false;
		}

		@Override
		public boolean isGzipRequesting() {
			return false;
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public TimeZone getTimeZone() {
			return null;
		}

		@Override
		public boolean isEnabledForExtensions() {
			return false;
		}

	}

	/**
	 * Processes the xmpp request.
	 * 
	 * @param pRequest
	 *            The servlet request being read.
	 * @param pResponse
	 *            The servlet response being created.
	 * @throws IOException
	 *             Reading the request or writing the response failed.
	 * 
	 */
	public String execute(String aRequest) throws XmlRpcException, IOException {
		ByteArrayStreamConnection myConnection = new ByteArrayStreamConnection(
				aRequest.getBytes());
		ByteArrayConfig myConfig = new ByteArrayConfig();
		super.execute(myConfig, myConnection);
		return new String(myConnection.getTheResponseBytes());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		XmlRpcByteArrayProcessor myServer = new XmlRpcByteArrayProcessor();

		// set some handlers ...
		PropertyHandlerMapping myPhm = new PropertyHandlerMapping();
		myPhm.addHandler("examples", XmlRpcExampleHandler.class);

		myServer.setHandlerMapping(myPhm);

		System.out.println(myServer.execute("<methodCall>"
				+ "<methodName>examples.getStateName</methodName>" + "<params>"
				+ "<param>" + "<value><i4>6</i4></value>" + "</param>"
				+ "</params>" + "</methodCall>"));
	}

}
