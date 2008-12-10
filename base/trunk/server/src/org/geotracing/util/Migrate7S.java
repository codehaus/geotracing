package org.geotracing.util;

import org.keyworx.oase.api.*;
import org.keyworx.oase.config.StoreContextConfig;
import org.keyworx.oase.main.Main;
import org.keyworx.oase.main.OaseContextManager;
import org.keyworx.common.util.Sys;
import org.keyworx.common.util.IO;
import org.postgis.PGgeometryLW;
import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.util.Util;

import java.io.File;

/**
 * Test class <code>Finder</code>.
 *
 * @author Just van den Broecke
 */
public class Migrate7S {
	private String oaseContextName;
	private String outputDirPath;
	private String outputFilePath;
	private String oaseFileDirPath;

	private OaseSession session;
	private Finder finder;
	private Relater relater;
	private JXElement iniDataDoc = new JXElement("initialdata");
	private JXElement iniDataRecs = new JXElement("records");
	private JXElement iniMedia = new JXElement("media");
	private JXElement iniRelations = new JXElement("relations");
	private String[] ROLES7 = {"god", "admin", "user", "guest"};
	private Record[] persons;

	public Migrate7S(String anOaseContextName, String anOutputDir) {
		oaseContextName = anOaseContextName;
	}

	public void migrate() throws Exception {
		migrateUsers();
		migrateMedia();
	}

	public void migrateUsers() throws Exception {
		persons = finder.readAll("utopia_person");

		info("migrateUsers: read " + persons.length + " persons");

		for (Record person : persons) {
			Record account = relater.getRelated(person, "utopia_account", null)[0];
			info("adding " + account.getStringField("loginname"));

			JXElement userRec = new JXElement("record");
			userRec.setAttr("id", "user" + person.getId());
			userRec.setAttr("table", "gw_user");

			userRec.addTextChild("name", account.getStringField("loginname"));
			userRec.addTextChild("password", account.getStringField("password"));
			userRec.addTextChild("creationdate", account.getLongField("creationdate") + "");

			Record role = relater.getRelated(account, "utopia_role", null)[0];

			// Role: 0=god, 1=admin, 2=user 3=guest
			String roleName = role.getStringField("name");
			for (int roleVal = 0; roleVal < ROLES7.length; roleVal++) {
				if (ROLES7[roleVal].equals(roleName)) {
					userRec.addTextChild("role", roleVal + "");
				}
			}


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

			String email = person.getStringField("email");
			if (!person.isNull("email") && email.length() > 0) {
				userRec.addTextChild("email", email);
			} else {
				// We must have unique email (non null constraint)
				userRec.addTextChild("email", "user" + person.getId() + "@unknown.com");
			}

			JXElement profileElm = null;

			JXElement extraElm = person.getXMLField("extra");
			if (extraElm != null) {
				profileElm = extraElm;
				profileElm.setTag("profile");
			}

			String mobileNr = person.getStringField("mobilenr");
			if (mobileNr != null && mobileNr.length() > 0) {
				if (profileElm == null) {
					profileElm = new JXElement("profile");
				}
				profileElm.addTextChild("mobilenr", mobileNr);
			}

			if (profileElm != null) {
				userRec.addChild(profileElm);
			}
			// Icon id
			Record[] iconMedium = relater.getRelated(person, "base_medium", "thumb");
			if (iconMedium.length > 0) {
				userRec.addTextChild("iconid", "medium" + iconMedium[0].getId());
			}

			iniDataRecs.addChild(userRec);
			info(userRec.toFormattedString());
		}
	}

	public void migrateMedia() throws Exception {
		Record[] media = finder.readAll("base_medium");
		info("migrateMedia: read " + media.length + " media");

		int cnt = 0;
		for (Record medium : media) {
			migrateMedium(medium);
			if (cnt++ > 20) {
				migrateMedium(finder.read(4632, "base_medium"));
				migrateMedium(finder.read(5715, "base_medium"));
				break;
			}
		}
	}

	public void migrateMedium(Record medium) throws Exception {
		JXElement mediumElm = new JXElement("medium");

		mediumElm.setAttr("id", "medium" + medium.getId());

		Record owners[] = relater.getRelated(medium, "utopia_person", null);
		if (owners.length > 0) {
			mediumElm.addTextChild("owner", "user" + owners[0].getId());
			addRelation("medium" + medium.getId(), "user" + owners[0].getId(), "medium");
		}

		String filePath = oaseFileDirPath + "/" + medium.getId() + ".file";
		if (!new File(filePath).exists()) {
			error(filePath + " does not exist!");
			return;
		}

		mediumElm.setAttr("filename", filePath);

		String name = medium.getStringField("name");
		if (name != null && name.length() > 0) {
			mediumElm.addTextChild("name", name);
		} else {
			mediumElm.addTextChild("name", "unnamed");
		}

		String desc = medium.getStringField("description");
		if (desc != null && desc.length() > 0) {
			mediumElm.addTextChild("description", desc);
		}

		String kind = medium.getStringField("kind");
		if (kind != null && kind.length() > 0) {
			mediumElm.addTextChild("kind", kind);
		}

		String mime = medium.getStringField("mime");
		if (mime != null && mime.length() > 0) {
			mediumElm.addTextChild("mime", mime);
		}

		mediumElm.addTextChild("creationdate", medium.getLongField("creationdate") + "");

		Record location = null;
		Record locations[] = relater.getRelated(medium, "g_location", null);
		if (locations.length > 0) {
			location = locations[0];
			PGgeometryLW geom = (PGgeometryLW) location.getObjectField("point");

			mediumElm.addTextChild("point", geom.toString());
		}
		iniMedia.addChild(mediumElm);

	}

	private void addRelation(String rec1, String rec2) {
		addRelation(rec1, rec2, null);
	}

	private void addRelation(String rec1, String rec2, String aTag) {
		JXElement relElm = new JXElement("relation");
		relElm.setAttr("rec1", rec1);
		relElm.setAttr("rec2", rec2);
		if (aTag != null) {
			relElm.setAttr("tag", aTag);
		}

		iniRelations.addChild(relElm);
	}

	public void init() {
		try {
			outputDirPath = "output/" + oaseContextName;
			IO.mkdir(outputDirPath);

			outputFilePath = outputDirPath + "/initial.xml";
			File outputFile = new File(outputFilePath);
			if (outputFile.exists()) {
				outputFile.delete();
			}

			oaseFileDirPath = "/var/keyworx/data/oase/" + oaseContextName + "/files";
			if (!new File(oaseFileDirPath).exists()) {
				throw new IllegalArgumentException(oaseFileDirPath + " does not exist!");
			}

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
		iniDataDoc.addChild(iniMedia);
		iniDataDoc.addChild(iniRelations);
	}

	public void exit() throws OaseException {
		Main.exit();
		Util.saveElement(iniDataDoc, outputFilePath);
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
