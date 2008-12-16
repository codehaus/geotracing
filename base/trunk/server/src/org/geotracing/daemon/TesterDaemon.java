// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.daemon;

import org.keyworx.amuse.core.Daemon;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Rand;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.IO;
import org.keyworx.server.ServerConfig;

import java.io.*;
import java.util.Properties;

/**
 * Continuous tester.
 * <p/>
 * <h3>Purpose</h3>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class TesterDaemon extends Daemon {
	protected Log log;
	private Worker[] workers;
	protected String webInfDir;
	protected String cfgDir;

	public TesterDaemon() {
	}

	public Log getLog() {
		return log;
	}

	public void start() {
		log = Logging.getLog("TesterDaemon");

		// Dir for stuff to send (e.g. /var/keyworx/webapps/gt/WEB-INF/data)
		webInfDir = ServerConfig.getProperty("keyworx.cfg.dir") + "/..";
		cfgDir = webInfDir + "/data/testerdaemon/cfg";

		// Get the property files
		File[] propFiles = new File(cfgDir).listFiles(new FilenameFilter() {
			public boolean accept(File aFile, String aName) {
				return aName.endsWith(".properties");
			}
		}
		);

		// Create the workers
		workers = new Worker[propFiles.length];
		File propFile = null;
		for (int i = 0; i < propFiles.length; i++) {
			propFile = propFiles[i];
			if (!propFile.exists()) {
				throw new IllegalArgumentException("propFile=" + propFile.getAbsolutePath() + " does not exist");
			}
			workers[i] = new Worker(propFile.getAbsolutePath());
		}

		// Start the workers
		for (int i = 0; i < workers.length; i++) {
			log.info("starting tester worker #" + i);

			workers[i].start();
		}
	}

	public void stop() {
		log.info("stopped");
		for (int i = 0; i < workers.length; i++) {
			log.info("stopping tester #" + i);
			workers[i].stop();
		}
	}

	private class Worker implements Runnable {
		protected String propFilePath;
		protected String mediaDir;
		protected Properties properties;
		protected Thread thread;
		protected Process proc;

		public Worker(String thePropFilePath) {
			propFilePath = thePropFilePath;
		}

		public void start() {
			try {
				properties = Sys.loadProperties(propFilePath);
				mediaDir = cfgDir + "/" + properties.getProperty("media.dir");
				log.info("mediaDir=" + mediaDir);
				// Save media
				IO.mkdir(mediaDir + "/saved");
				copyFiles(mediaDir, mediaDir + "/saved");
			} catch (Throwable t) {
				log.error("Error in start:" + t);
				return;
			}

			thread = new Thread(this);
			thread.start();
		}

		public void stop() {
			if (proc != null) {
				proc.destroy();
			}
			if (thread == null) {
				return;
			}
			thread.interrupt();
			thread = null;
		}

		/**
		 * Do the trace.
		 */
		public void run() {
			try {
				while (thread != null && thread.isAlive()) {
					// Copy media files
					copyFiles(mediaDir + "/saved", mediaDir);

					// Let all initialize
					Thread.sleep(Rand.randomLong(3000L, 6000L));
					String[] command = new String[5];
					command[0] = System.getProperty("java.home") + "/bin/java";
					command[1] = "-cp";
					command[2] = webInfDir + "/lib/keyworx.jar";
					command[3] = "org.keyworx.amuse.test.protocol.Main";
					command[4] = propFilePath;
					Process process = null;

					try {
						process = (Runtime.getRuntime()).exec(command, null);
						process.waitFor();
					} catch (Throwable t) {
						log.error("error starting tester worker", t);
						return;
					}
					//BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
					BufferedReader errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String nextLine = "";
					try {
						/*		while ((nextLine = output.readLine()) != null) {
																	log.info(nextLine + "\n");
																}   */

						while ((nextLine = errorOutput.readLine()) != null) {
							log.warn(nextLine + "\n");
						}
					} catch (Exception e) {
						return;
					}
					log.info("Started run Tester worker for " + propFilePath);

				}
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

		}

	}

	/**
	 * Recursively copies directory.
	 */
	public static void copyFiles(String fromDirPath, String toDirPath) throws IOException {
		//p("enter cpdir "+from+" to "+to);

		File fromDir = new File(fromDirPath);
		String[] fileList = fromDir.list();

		for (int i = 0; i < fileList.length; i++) {
			String fromPath = fromDirPath + File.separator + fileList[i];

			String toPath = toDirPath + File.separator + fileList[i];
			if (!new File(fromPath).isFile()) {
				continue;
			}
			IO.cp(fromPath, toPath);
		}
	}
}
