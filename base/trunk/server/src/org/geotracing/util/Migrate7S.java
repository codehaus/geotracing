package org.geotracing.util;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.util.Util;
import org.keyworx.common.util.IO;
import org.keyworx.oase.api.*;
import org.keyworx.oase.main.Main;
import org.keyworx.oase.main.OaseContextManager;
import org.postgis.PGgeometryLW;

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
	private JXElement iniQueries = new JXElement("queries");
	private JXElement iniRecs = new JXElement("records");
	private JXElement iniMedia = new JXElement("media");
	private JXElement iniRelations = new JXElement("relations");
	private String[] ROLES7 = {"god", "admin", "user", "guest"};
	private Record[] persons;

	public Migrate7S(String anOaseContextName, String anOutputDir) {
		oaseContextName = anOaseContextName;
	}

	public String createId(Record rec) {
		String table = rec.getTableName();
		String prefix = null;
		if (table.equals("utopia_person")) {
			prefix = "user";
		} else if (table.equals("g_track")) {
			prefix = "trace";
		} else if (table.equals("base_medium")) {
			prefix = "medium";
		} else if (table.equals("kw_comment")) {
			prefix = "comment";
		} else if (table.equals("cc_tag")) {
			prefix = "tag";
		} else if (table.equals("cc_tagrelation")) {
			prefix = "tagrel";
		} else if (table.equals("oase_tabledef")) {
			prefix = "table";
		} else {
			throw new IllegalArgumentException("Cannot generate prefix table=" + table);
		}

		return prefix + rec.getId();
	}

	public String createQueryId(Record rec) {
		return "q-" + createId(rec);
	}

	public String transTableName(String table) {
		if (table.equals("utopia_person")) {
			return "gw_user";
		} else if (table.equals("g_track")) {
			return "gw_trace";
		} else if (table.equals("base_medium")) {
			return "oase_medium";
		} else if (table.equals("kw_comment")) {
			return  "gw_comment";
		} else if (table.equals("cc_tag")) {
			return "gw_tag";
		} else if (table.equals("cc_tagrelation")) {
			return "gw_tagrelation";
		} else if (table.equals("oase_tabledef")) {
		}

		return table;
	}

	public void migrate() throws Exception {
		migrateUsers();
		migrateMedia();
		migrateTracks();
		migrateComments();
		migrateTags();
	}

	public void migrateComments() throws Exception {
		Record[] comments = finder.queryTable("kw_comment", null, "creationdate", null);

		info("migrateComments: read " + comments.length + " comments");

		String commentId;
		for (Record comment : comments) {
			commentId = createId(comment);

			JXElement cmtElm = new JXElement("record");
			cmtElm.setAttr("id", commentId);
			cmtElm.setAttr("table", "gw_comment");

			if (comment.getIntField("owner") != -1) {
				cmtElm.addTextChild("owner", createId(finder.read(comment.getIntField("owner"))));
			}

			if (comment.getIntField("target") != -1) {
				Record target = finder.read(comment.getIntField("target"));
				if (target.getTableName().equals("utopia_portal")) {
					cmtElm.addTextChild("target", -1 + "");
				} else {
					cmtElm.addTextChild("target", createId(finder.read(comment.getIntField("target"))));
				}
			}

			if (comment.getIntField("targettable") != -1) {
				Record targetTable = finder.read(comment.getIntField("targettable"));
				String targetTableName = targetTable.getStringField("name");
				if (targetTableName.equals("utopia_portal")) {
					cmtElm.addTextChild("targettable", -1 + "");
				} else {
					cmtElm.addTextChild("targettable", createQueryId(targetTable));
					addQuery(createQueryId(targetTable), "oase_tabledef", "name", transTableName(targetTableName));
				}
			}

			cmtElm.addTextChild("author", comment.getStringField("author"));

			String email = comment.getStringField("email");
			if (email != null && email.length() > 0) {
				cmtElm.addTextChild("email", email);
			}


			String url = comment.getStringField("url");
			if (url != null && url.length() > 0) {
				cmtElm.addTextChild("url", url);
			}

			String ip = comment.getStringField("ip");
			if (ip != null && ip.length() > 0) {
				cmtElm.addTextChild("ip", ip);
			}

			cmtElm.addTextChild("content", comment.getStringField("content"));
			cmtElm.addTextChild("state", comment.getIntField("state") + "");
			cmtElm.addTextChild("creationdate", comment.getLongField("creationdate") + "");

			iniRecs.addChild(cmtElm);
		}

	}

	public void migrateTags() throws Exception {
		Record[] tags = finder.queryTable("cc_tag", null, "name", null);

		info("migrateTags: read " + tags.length + " tags");

		String tagId;
		for (Record tag : tags) {
			tagId = createId(tag);

			JXElement tagElm = new JXElement("record");
			tagElm.setAttr("id", tagId);
			tagElm.setAttr("table", "gw_tag");

			if (tag.getIntField("owner") != -1) {
				tagElm.addTextChild("owner", createId(finder.read(tag.getIntField("owner"))));
			}

			tagElm.addTextChild("name", tag.getStringField("name"));
			tagElm.addTextChild("creationdate", tag.getLongField("creationdate") + "");

			iniRecs.addChild(tagElm);
		}

		Record[] tagRels = finder.queryTable("cc_tagrelation", null, "creationdate", null);

		info("migrateTags: read " + tagRels.length + " tagRels");

		String tagRelId;
		for (Record tagRel : tagRels) {
			tagRelId = createId(tagRel);

			JXElement tagRelElm = new JXElement("record");
			tagRelElm.setAttr("id", tagRelId);
			tagRelElm.setAttr("table", "gw_tagrelation");

			if (tagRel.getIntField("tagger") != -1) {
				Record tagger = finder.read(tagRel.getIntField("tagger"));
				if (tagger != null) {
					String taggerId = createId(tagger);
					tagRelElm.addTextChild("owner", taggerId);
					tagRelElm.addTextChild("tagger", taggerId);
				}
				
				Record taggerTable = finder.read(tagRel.getIntField("taggertable"));
				String taggerTableName = taggerTable.getStringField("name");
				tagRelElm.addTextChild("taggertable", createId(taggerTable));
				addQuery(createId(taggerTable), "oase_tabledef", "name", transTableName(taggerTableName));
			}

			if (tagRel.getIntField("item") != -1) {
				Record item = finder.read(tagRel.getIntField("item"));
				String itemId = createId(item);
				
				tagRelElm.addTextChild("item", itemId);

				Record itemTable = finder.read(tagRel.getIntField("itemtable"));
				String itemTableName = itemTable.getStringField("name");
				tagRelElm.addTextChild("itemtable", createQueryId(itemTable));
				addQuery(createQueryId(itemTable), "oase_tabledef", "name", transTableName(itemTableName));
			}


			tagRelElm.addTextChild("tag", "tag" + tagRel.getIntField("tag"));
			tagRelElm.addTextChild("creationdate", tagRel.getLongField("creationdate") + "");
			iniRecs.addChild(tagRelElm);
		}
	}

	public void migrateTracks() throws Exception {
		Record[] tracks = finder.queryTable("g_track", null, "startdate", null);

		info("migrateTracks: read " + tracks.length + " tracks");

		String trackName, traceId;
		for (Record track : tracks) {
			trackName = track.getStringField("name");
			traceId = createId(track);

			info("adding " + trackName);
			Record person = relater.getRelated(track, "utopia_person", null)[0];

			JXElement traceElm = new JXElement("record");
			traceElm.setAttr("id", traceId);
			traceElm.setAttr("table", "gw_trace");

			traceElm.addTextChild("owner", createId(person));
			traceElm.addTextChild("name", track.getStringField("name"));
			traceElm.addTextChild("description", track.getStringField("description"));
			traceElm.addTextChild("type", track.getIntField("type") + "");
			traceElm.addTextChild("format", track.getStringField("format"));
			traceElm.addTextChild("state", track.getIntField("state") + "");
			traceElm.addTextChild("startdate", track.getLongField("startdate") + "");
			traceElm.addTextChild("enddate", track.getLongField("enddate") + "");
			traceElm.addTextChild("ptcount", track.getIntField("ptcount") + "");
			traceElm.addTextChild("distance", track.getRealField("distance") + "");

			JXElement lastEvt = track.getXMLField("lastevt");
			if (lastEvt != null) {
				traceElm.addTextChild("lastevt", lastEvt.toFormattedString());
			}

			JXElement extra = track.getXMLField("extra");
			if (extra != null) {
				traceElm.addTextChild("extra", extra.toFormattedString());
			}

			traceElm.addTextChild("creationdate", track.getLongField("creationdate") + "");

			// First and last point
			Record[] points = relater.getRelated(track, "g_location", "firstpt");
			if (points.length > 0) {
				PGgeometryLW point = (PGgeometryLW) points[0].getObjectField("point");
				if (point != null) {
					traceElm.addTextChild("firstpoint", point.toString());
				}
			}

			points = relater.getRelated(track, "g_location", "lastpt");
			if (points.length > 0) {
				PGgeometryLW point = (PGgeometryLW) points[0].getObjectField("point");
				if (point != null) {
					traceElm.addTextChild("lastpoint", point.toString());
				}
			}

			// Add file
			String sourceFilePath = oaseFileDirPath + "/" + track.getId() + ".data";
			if (!new File(sourceFilePath).exists()) {
				error(sourceFilePath + " does not exist!");
			} else {
				String destFilePath = "traces/" + traceId + ".txml";
				IO.cp(sourceFilePath, outputDirPath + "/" + destFilePath);
				traceElm.addTextChild("data", destFilePath);
			}

			// Relate media
			Record[] media = relater.getRelated(track, "base_medium", null);
			for (Record medium : media) {
				String mediumId = "medium" + medium.getId();
				if (iniMedia.getChildById(mediumId) == null) {
					info("skip rel medium/trace for " + mediumId);
					continue;
				}
				// Relate to trace
				addRelation("trace" + track.getId(), mediumId, "medium");
			}

			// Relate to user
			addRelation(traceId, createId(person), "owner");

			// Add to all recs
			iniRecs.addChild(traceElm);

		}
	}

	public void migrateUsers() throws Exception {
		persons = finder.readAll("utopia_person");

		info("migrateUsers: read " + persons.length + " persons");

		String loginName;
		for (Record person : persons) {
			Record account = relater.getRelated(person, "utopia_account", null)[0];

			loginName = account.getStringField("loginname");
			if (loginName.startsWith("geoapp-")) {
				info("skipping " + loginName);
				continue;
			}

			info("adding " + loginName);

			JXElement userRec = new JXElement("record");
			userRec.setAttr("id", createId(person));
			userRec.setAttr("table", "gw_user");

			userRec.addTextChild("name", loginName);
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
				// userRec.addTextChild("iconid", "medium" + iconMedium[0].getId());
			}

			iniRecs.addChild(userRec);
			info(userRec.toFormattedString());
		}
	}

	public void migrateMedia() throws Exception {
		Record[] media = finder.queryTable("base_medium", null, "creationdate", null);
		info("migrateMedia: read " + media.length + " media");

		int cnt = 0;
		for (Record medium : media) {
			migrateMedium(medium);
			if (cnt++ > 30) {
//				migrateMedium(finder.read(4632, "base_medium"));
//				migrateMedium(finder.read(5715, "base_medium"));
//				break;
			}
		}
	}

	public void migrateMedium(Record medium) throws Exception {
		JXElement mediumElm = new JXElement("medium");

		String mediumId = createId(medium);
		mediumElm.setAttr("id", mediumId);
		String filePath = oaseFileDirPath + "/" + medium.getId() + ".file";
		if (!new File(filePath).exists()) {
			error(filePath + " does not exist!");
			return;
		}

		mediumElm.setAttr("filename", filePath);

		Record owners[] = relater.getRelated(medium, "utopia_person", null);
		if (owners.length > 0) {
			mediumElm.addTextChild("owner", createId(owners[0]));
			addRelation(mediumId, "user" + owners[0].getId(), "medium");
		}

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

	private void addQuery(String id, String table, String colName, String colVal) {
		JXElement existingQuery = iniQueries.getChildById(id);
		if (existingQuery != null) {
			return;
		}
		JXElement queryElm = new JXElement("record");
		queryElm.setAttr("id", id);
		queryElm.setAttr("table", table);
		queryElm.addTextChild(colName, colVal);

		iniQueries.addChild(queryElm);
	}

	public void init() {
		try {
			outputDirPath = "output/" + oaseContextName;
			new File(outputDirPath).delete();
			IO.mkdir(outputDirPath);
			IO.mkdir(outputDirPath + "/traces");

			outputFilePath = outputDirPath + "/initial.xml";

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
		iniDataDoc.addChild(iniQueries);
		iniDataDoc.addChild(iniRecs);
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
