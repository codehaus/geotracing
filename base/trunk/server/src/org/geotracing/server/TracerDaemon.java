// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.server;

import com.messners.mail.*;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.amuse.core.AmuseException;
import org.keyworx.amuse.core.Daemon;
import org.keyworx.amuse.core.DaemonContext;
import org.keyworx.amuse.core.Protocol;
import org.keyworx.client.KWClient;
import org.keyworx.client.KWClientException;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.IO;
import org.keyworx.common.util.Rand;
import org.keyworx.server.ServerConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Implements the Tracer bot.
 *
 * <h3>Purpose</h3>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class TracerDaemon extends Daemon {
	private String dataDir;
	protected Log log;
	private Worker[] workers;
	private long intervalMillis;

	public TracerDaemon() {
	}

	public Log getLog() {
		return log;
	}

	public void start() {
		log = Logging.getLog("TracerDaemon");
		intervalMillis = getContext().getLongProperty("intervalMillis");

		// Dir for stuff to send (e.g. /var/keyworx/webapps/gt/WEB-INF/data)
		dataDir = ServerConfig.getProperty("keyworx.cfg.dir") + "/../data/tracerdaemon";

		// Get the specs
		File[] traceFiles = new File(dataDir).listFiles(new FilenameFilter() {
			public boolean accept(File aFile, String aName) {
				return aName.endsWith(".xml");
			}
		}
		);

		// Create the workers
		workers = new Worker[traceFiles.length];
		for (int i = 0; i < traceFiles.length; i++) {
			File traceFile = traceFiles[i];
			if (!traceFile.exists()) {
				throw new IllegalArgumentException("traceFile=" + traceFile.getAbsolutePath() + " does not exist");
			}

			try {
				JXElement traceSpec = new JXBuilder().build(traceFile);
				workers[i] = new Worker(traceSpec);
			} catch (Throwable t) {
				log.error("cannot parse " + traceFile.getAbsolutePath());

			}
		}

		// Start the workers
		for (int i = 0; i < workers.length; i++) {
			log.info("starting worker #" + i);
			workers[i].start();
		}
	}

	public void stop() {
		log.info("stopped");
		for (int i = 0; i < workers.length; i++) {
			log.info("stopping worker #" + i);
			workers[i].stop();
		}
	}

	private class Worker implements Runnable {
		protected KWClient kwClient;
		protected Thread thread;
		protected JXElement traceSpec;

		public Worker(JXElement aSpec) {
			traceSpec = aSpec;
		}

		public void start() {
			thread = new Thread(this);
			thread.start();
		}

		public void stop() {
			if (thread == null) {
				return;
			}
			thread.interrupt();
			thread = null;
		}

		/** Do the trace. */
		public void run() {

			try {
				// Let KW initialize
				Thread.sleep(Rand.randomLong(10000L, 20000L));

				log.info("Start Tracer loop for user=" + traceSpec.getAttr("user"));

				kwClient = getContext().createUserSession(traceSpec.getAttr("user"));

				JXElement[] traceElements = (JXElement[]) traceSpec.getChildren().toArray(new JXElement[0]);
				while (thread != null && thread.isAlive()) {
					delTrack();
					newTrack(traceSpec.getAttr("trackName"));
					JXElement nextElm = null;
					String nextTag = null;
					for (int i = 0; i < traceElements.length; i++) {
						nextElm = traceElements[i];
						nextTag = nextElm.getTag();

						// Do action based on next element
						if (nextTag.equals(Track.TAG_PT)) {
							writeTrack(nextElm);

							// Only sleep after writng points
							Thread.sleep(Rand.randomLong(intervalMillis / 2, intervalMillis));
						} else if (nextTag.equals("medium")) {
							uploadMedium(nextElm);
						} else if (nextTag.equals("resume")) {
							resumeTrack();
						} else if (nextTag.equals("suspend")) {
							suspendTrack();
						}
					}
				}
			} catch (KWClientException t) {
				log.warn("KWClientException in worker()", t);
				stop();
			} catch (InterruptedException ie) {
				log.info("InterruptedException in worker() thread=" + thread);
			} catch (Throwable t) {
				log.warn("Error in trace()", t);
				stop();
			} finally {
				bailout();
			}

		}

		protected void bailout() {
			if (kwClient == null) {
				return;
			}
			try {
				kwClient.logout();

				log.info("logout OK");
			} catch (Throwable t) {
				// ignore
				log.warn("error in kwClient.logout");
			} finally {
				try {
					kwClient.disconnect();
					log.info("disconnect OK");
				} catch (Throwable t) {
					log.warn("error in kwClient.disconnect()");
				}

				kwClient = null;
			}
		}

		protected void delTrack() throws KWClientException {
			JXElement req = new JXElement("t-trk-delete-req");
			req.setAttr("t", System.currentTimeMillis());
			JXElement rsp = kwClient.performUtopiaRequest(req);
			log.trace("Track deleted");
		}

		protected void newTrack(String aName) throws KWClientException {
			JXElement req = new JXElement("t-trk-create-req");
			req.setAttr("name", aName);
			req.setAttr("t", System.currentTimeMillis());
			JXElement rsp = kwClient.performUtopiaRequest(req);
			log.trace("Track created");
		}

		protected void resumeTrack() throws KWClientException {
			JXElement req = new JXElement("t-trk-resume-req");
			JXElement rsp = kwClient.performUtopiaRequest(req);
			log.trace("Track resumed");
		}

		protected void suspendTrack() throws KWClientException {
			JXElement req = new JXElement("t-trk-suspend-req");
			JXElement rsp = kwClient.performUtopiaRequest(req);
			log.trace("Track suspended");
		}

		protected void uploadMedium(JXElement aMediumSpec) throws IOException, KWClientException {
			JXElement req = new JXElement("t-trk-upload-medium-req");
			String filePath = dataDir + "/" + aMediumSpec.getAttr("file");
			log.trace("adding medium " + filePath);

			String uploadFilePath = filePath;
			if (uploadFilePath.endsWith("jpg")) {
				// Disable finding time from exif date on server...
				uploadFilePath += "u";
			} else {
				// Prepend "u" (Oase will move file...)
				File file = new File(filePath);
				uploadFilePath = file.getParent() + "/" + "u" + file.getName();
			}

			if (uploadFilePath.endsWith("3gp")) {
				req.setAttr("type", "video");
				req.setAttr("mime", "video/3gpp");
			}

			// To save the file (Oase willmove the file...).
			IO.cp(filePath, uploadFilePath);

			// Set request data
			req.setAttr("file", uploadFilePath);
			if (aMediumSpec.hasAttr("name")) {
				req.setAttr("name", aMediumSpec.getAttr("name"));
			}
			if (aMediumSpec.hasText()) {
				req.setAttr("description", aMediumSpec.getText());
			}
			JXElement rsp = kwClient.performUtopiaRequest(req);
			log.trace("File " + uploadFilePath + " uploaded");
		}


		protected void writeTrack(JXElement aPoint) throws KWClientException {
			JXElement req = new JXElement("t-trk-write-req");
			req.addChild(aPoint);

			JXElement rsp = kwClient.performUtopiaRequest(req);
//		log.info("Sample written rsp=" + rsp);
		}
	}
}


/*
 * $Log: TracerDaemon.java,v $
 * Revision 1.11  2006-08-10 23:40:01  just
 * no message
 *
 * Revision 1.10  2006-08-09 16:38:11  just
 * ok
 *
 * Revision 1.9  2005/10/30 19:21:36  just
 * *** empty log message ***
 *
 * Revision 1.8  2005/10/24 22:09:13  just
 * *** empty log message ***
 *
 * Revision 1.7  2005/10/24 10:33:06  just
 * *** empty log message ***
 *
 * Revision 1.6  2005/10/21 23:52:49  just
 * *** empty log message ***
 *
 * Revision 1.5  2005/10/21 20:49:20  just
 * *** empty log message ***
 *
 * Revision 1.4  2005/10/20 15:37:20  just
 * *** empty log message ***
 *
 * Revision 1.3  2005/10/18 12:54:44  just
 * *** empty log message ***
 *
 * Revision 1.2  2005/10/18 07:38:00  just
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/14 23:21:18  just
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/14 18:25:01  just
 * refactored EmailUploadDaemon
 *
 *
 */