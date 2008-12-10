package org.geotracing.util;

import org.keyworx.oase.api.*;
import org.keyworx.oase.config.StoreContextConfig;
import org.keyworx.oase.main.Main;
import org.keyworx.oase.main.OaseContextManager;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.IO;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.util.Util;

/**
 * Test class <code>Finder</code>.
 *
 * @author Just van den Broecke
 */
public class Migrate7S {
	private String oaseContextName;
	private String outputDirPath;

	private OaseSession session;
	private Finder finder;
	private Relater relater;
	private JXElement iniDataDoc = new JXElement("initialdata");
	private JXElement iniDataRecs= new JXElement("records");
	private String[] ROLES7={"god", "admin", "user", "guest"};

	public Migrate7S(String anOaseContextName, String anOutputDir) {
		oaseContextName = anOaseContextName;
	}

	public void migrate() throws Exception {
		migrateUsers();
	}

	public void migrateUsers() throws Exception {
		Record[] accounts = finder.readAll("utopia_account");

		info("read " + accounts.length + " accounts");

		for (Record account: accounts)  {
			info("adding " + account.getStringField("loginname"));

			JXElement userRec = new JXElement("record");
			userRec.setAttr("id", "user" + account.getId());
			userRec.setAttr("table", "gw_user");

			userRec.addTextChild("name", account.getStringField("loginname"));
			userRec.addTextChild("password", account.getStringField("password"));
			userRec.addTextChild("creationdate", account.getLongField("creationdate")+"");

			Record role = relater.getRelated(account, "utopia_role", null)[0];

			// Role: 0=god, 1=admin, 2=user 3=guest
			String roleName = role.getStringField("name");
			for (int roleVal=0; roleVal < ROLES7.length; roleVal++) {
				if (ROLES7[roleVal].equals(roleName)) {
					userRec.addTextChild("role", roleVal+"");
				}
			}


			Record person = relater.getRelated(account, "utopia_person", null)[0];

			String fullName = null;
			if (!person.isNull("firstname")) {
				fullName = person.getStringField("firstname");
			}

			if (!person.isNull("lastname")) {
				String lastName = person.getStringField("lastname");
				fullName = fullName != null ? fullName + " " + lastName : lastName;
			}

			if (fullName != null) {
				userRec.addTextChild("fullname", fullName);
			}

			if (!person.isNull("email")) {
				userRec.addTextChild("email", person.getStringField("email"));
			}
			
			iniDataRecs.addChild(userRec);
		}
	}

	public void init() {
		try {
			outputDirPath = "output/" + oaseContextName;
			IO.mkdir(outputDirPath);
		} catch (Throwable oe) {
			fatal("FATAL: could make dir e=", oe);
		}

		try {
			// Only init if not already running
			Main.init("cfg");
		} catch (OaseException oe) {
			System.err.println("FATAL: could not init Oase e=" + oe);
		}

		try {
			// Only init if not already running
			OaseContextManager.addOaseContext("oase-context.xml", oaseContextName);
		} catch (OaseException oe) {
			System.err.println("FATAL: could not add oase context e=" + oe);
		}


		try {
			// Create session per test.
			session = Oase.createSession(oaseContextName);

			finder = session.getFinder();
			relater = session.getRelater();
		} catch (OaseException oe) {
			fatal("FATAL: could not create session e=", oe);
		}

		// Init doc
		iniDataDoc.setAttr("xmlns", "http://www.keyworx.org/initialdata/1.1");
		iniDataDoc.setAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		iniDataDoc.setAttr("xsi:schemaLocation", "http://www.keyworx.org/initialdata/1.1 ../../cfg/initialdata-1.1.xsd");
		iniDataDoc.addChild(iniDataRecs);
	}

	public void exit() throws OaseException {
		Main.exit();
		Util.saveElement(iniDataDoc, outputDirPath + "/initial.xml");
	}


	static protected void error(String aMessage) {
		System.err.println("ERROR: " + aMessage);
	}

	static protected void info(String aMessage) {
		System.out.println("INFO: " + aMessage);
	}

	static protected void fatal(String aMessage) {
		System.err.println("FATAL: " + aMessage);
		System.exit(-1);
	}

	static protected void fatal(String aMessage, Throwable t) {
		System.err.println("FATAL: " + aMessage + " e=" + t);
		t.printStackTrace();
		System.exit(-1);
	}

	static protected void dbg(String aMessage) {
		System.err.println("DEBUG: " + aMessage);
	}

	public static void main(String[] args) {
		try {
			Migrate7S migrator = new Migrate7S(args[0], args[1]);
			migrator.init();
			migrator.migrate();
			migrator.exit();
		} catch (Throwable t) {
			fatal("exception: ", t);
		}
	}

}
