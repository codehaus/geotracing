package org.geotracing.client;

import nl.justobjects.mjox.JXBuilder;
import nl.justobjects.mjox.JXElement;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>HTTPUploader</code> is used to write
 * "multipart/form-data" to a <code>java.net.URLConnection</code> for
 * POSTing.  This is primarily for file uploading to HTTP servers.
 * See original version at:
 * http://forum.java.sun.com/thread.jspa?forumID=256&threadID=451245
 * other:
 * http://www.thisismobility.com/blog/?p=15&mobilenow
 * <p/>
 * Example
 * HTTPUploader uploader = new HTTPUploader();
 * uploader.connect("http://www.bla.com/upload.jsp");
 * <p/>
 * // write a text field element
 * uploader.writeField("myText", "text field text");
 * // upload filebytes (e.g. image byte array)
 * uploader.writeFile("myFile", "text/plain", myBytes);
 * JXElement rsp uploader.getResponse();
 * <p/>
 */

public class HTTPUploader {
	/**
	 * The line end characters.
	 */
	private static final String NEWLINE = "\r\n";

	/**
	 * The boundary prefix.
	 */
	private static final String PREFIX = "--";

	/**
	 * HTTP connection to remote host.
	 */
	private HttpConnection httpConn;

	/**
	 * The output stream to write to.
	 */
	private DataOutputStream out = null;

	/**
	 * The multipart boundary string.
	 */
	private static final String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);

	/**
	 * Creates a new <code>HTTPUploader</code> object using
	 * the specified output stream and boundary.  The boundary is required
	 * to be created before using this method, as described in the
	 * description for the <code>getContentType(String)</code> method.
	 * The boundary is only checked for <code>null</code> or empty string,
	 * but it is recommended to be at least 6 characters.  (Or use the
	 * static createBoundary() method to create one.)
	 */
	public HTTPUploader() {
	}

	/**
	 * Creates a new <code>java.net.URLConnection</code> object from the
	 * specified <code>java.net.URL</code>.  This is a convenience method
	 * which will set the <code>doInput</code>, <code>doOutput</code>,
	 * <code>useCaches</code> and <code>defaultUseCaches</code> fields to
	 * the appropriate settings in the correct order.
	 *
	 * @throws java.io.IOException on input/output errors
	 */
	public void connect(String url) throws java.io.IOException {
		httpConn = (HttpConnection) Connector.open(url);
		httpConn.setRequestMethod(HttpConnection.POST);
		// httpConn.setRequestProperty("Accept", "*/*");
		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		// httpConn.setRequestProperty("Connection", "Keep-Alive");
		// httpConn.setRequestProperty("Cache-Control", "no-cache");

		out = httpConn.openDataOutputStream();

		// Keyworx media servlet: force XML response
		writeField("xmlrsp", "true");
	}

	/**
	 * Gets response and closes connections/streams.
	 * <br />
	 * <b>NOTE:</b> This method <b>MUST</b> be called to finalize the
	 * upload.
	 *
	 * @throws java.io.IOException on input/output errors
	 */
	public JXElement getResponse() throws Exception {
		InputStream is = null;
		JXElement rsp = null;
		try {
			// write final boundary
			write(PREFIX);
			write(boundary);
			write(PREFIX);
			write(NEWLINE);
			// out.flush();
			out.close();

			// Getting the response code will open the connection,
			// send the request, and read the HTTP response headers.
			// The headers are stored until requested.
			int rc = httpConn.getResponseCode();
			if (rc != HttpConnection.HTTP_OK) {
				throw new IOException("HTTP response code: " + rc);
			}

			// Get response XML
			is = httpConn.openInputStream();
			rsp = new JXBuilder().build(is);

		} finally {
			// Always cleanup connections
			if (is != null) {
				is.close();
			}

			if (httpConn != null) {
				httpConn.close();
				httpConn = null;
			}

		}
		return rsp;
	}


	/**
	 * Writes an string field value.  If the value is null, an empty string
	 * is sent ("").
	 *
	 * @param name  the field name (required)
	 * @param value the field value
	 * @throws java.io.IOException on input/output errors
	 */
	public void writeField(String name, String value) throws java.io.IOException {
		if (value == null) {
			value = "";
		}
		/*
		--boundary\r\n
		Content-Disposition: form-data; name="<fieldName>"\r\n
		\r\n
		<value>\r\n
		*/
		// write boundary
		write(PREFIX);
		write(boundary);
		write(NEWLINE);
		// write content header
		write("Content-Disposition: form-data; name=\"" + name + "\"");
		write(NEWLINE);
		write(NEWLINE);
		// write content
		write(value);
		write(NEWLINE);
		// out.flush();
	}

	/**
	 * Writes the given bytes.  The bytes are assumed to be the contents
	 * of a file, and will be sent as such.  If the data is null, a
	 * <code>java.lang.IllegalArgumentException</code> will be thrown.
	 *
	 * @param name	 the field name
	 * @param mimeType the file content type (optional, recommended)
	 * @param fileName the file name (required)
	 * @param data	 the file data
	 * @throws java.io.IOException on input/output errors
	 */
	public void writeFile(String name, String mimeType, String fileName, byte[] data) throws java.io.IOException {
		/*
		--boundary\r\n
		Content-Disposition: form-data; name="<fieldName>"; filename="<filename>"\r\n
		Content-Type: <mime-type>\r\n
		\r\n
		<file-data>\r\n
		*/
		// write boundary
		write(PREFIX);
		write(boundary);
		write(NEWLINE);
		// write content header
		write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"");
		write(NEWLINE);
		if (mimeType != null) {
			write("Content-Type: " + mimeType);
			write(NEWLINE);
		}
		write(NEWLINE);

		// write content
		out.write(data);
		write(NEWLINE);
		// out.flush();
	}


	/**
	 * Writes out the string to the underlying output stream as a
	 * sequence of bytes.
	 *
	 * @param s a string of bytes to be written.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void write(String s) throws IOException {
		out.write(s.getBytes());
	}
}
