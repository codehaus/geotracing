package nl.diwi.util;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.net.NetUtil;
import org.keyworx.utopia.core.data.UtopiaException;

import java.util.Enumeration;
import java.util.Properties;

public class NetConnection {
    static Log log = Logging.getLog("NetConnection");

    public static String postData(String aPostUrl, String thePostBlurb) throws UtopiaException {
        HttpClient client = new HttpClient();

        PostMethod method = new PostMethod(aPostUrl);
        method.setRequestHeader("User-Agent", "XMLHTTP/1.0");
        method.setRequestHeader("ontent-type", "application/x-www-form-urlencoded");
        method.setRequestBody(thePostBlurb);
        
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw new UtopiaException("Posting to " + aPostUrl + " was unsuccesfull! - http status:" + returnCode);
            }

            String result = method.getResponseBodyAsString();
            log.info(result);
            return result;
        } catch (Throwable t) {
            log.error(t.getMessage());
            throw new UtopiaException("Exception in posting data to " + aPostUrl + ": " + t.toString());
        } finally {
            method.releaseConnection();
        }
    }

    public static String postData(String aPostUrl, Properties thePostParams) throws UtopiaException {
        HttpClient client = new HttpClient();
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

        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw new UtopiaException("Posting to " + aPostUrl + " was unsuccesfull! - http status:" + returnCode);
            }
            return method.getResponseBodyAsString();
        } catch (Throwable t) {
            log.error(t.getMessage());
            throw new UtopiaException("Exception in posting data to " + aPostUrl + ": " + t.toString());
        } finally {
            method.releaseConnection();
        }
    }

    public static JXElement getXMLFromREST(String aRESTUrl) {
        try {
            log.info("GET " + aRESTUrl);
            return new JXBuilder().build(NetUtil.fetchURL(aRESTUrl));
        } catch (Throwable t) {
            log.error("Exception retrieving xml data from " + aRESTUrl, t);
            return null;
        }
    }
}
