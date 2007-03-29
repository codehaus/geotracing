package nl.diwi.util;

import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.common.net.NetUtil;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: ronald
 * Date: Mar 28, 2007
 * Time: 8:09:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetConnection {
    static Log log  = Logging.getLog("NetConnection");

    public static String postData(String aPostUrl, Hashtable thePostParams) throws UtopiaException {
		HttpClient client = new HttpClient();
		//client.getParams().setParameter("http.useragent", "Test Client");
		BufferedReader br = null;
		PostMethod method = new PostMethod(aPostUrl);
		method.setParameter("http.useragent", "KICH Client");

		if (thePostParams != null) {
			Enumeration elements = thePostParams.keys();
			while (elements.hasMoreElements()) {
				String name = (String) elements.nextElement();
				String value = (String) thePostParams.get(name);
				method.addParameter(name, value);
			}
		}

		StringBuffer sb = new StringBuffer();
		try {
			int returnCode = client.executeMethod(method);
			if (returnCode != HttpStatus.SC_OK) {
				throw new UtopiaException("Posting to " + aPostUrl + " was unsuccesfull! - http status:" + returnCode);
			}

			br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			String readLine;
			while (((readLine = br.readLine()) != null)) {
				sb.append(readLine);
			}

		} catch (Throwable t) {
			log.error(t.toString());
			throw new UtopiaException("Exception in posting data to " + aPostUrl + ": " + t.toString());
		} finally {
			method.releaseConnection();
			if (br != null) try {
				br.close();
			} catch (Exception fe) {
			}
		}

		return sb.toString();

	}

	public static JXElement getXMLFromREST(String aRESTUrl) {
		try {
			return new JXBuilder().build(NetUtil.fetchURL(aRESTUrl));
		} catch (Throwable t) {
			log.error("Exception retrieving xml data from " + aRESTUrl, t);
			return null;
		}
	}
}
