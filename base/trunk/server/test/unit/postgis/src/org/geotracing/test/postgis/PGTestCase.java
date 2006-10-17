package org.geotracing.test.postgis;

import junit.framework.TestCase;
import junit.framework.ComparisonFailure;
import org.keyworx.oase.config.StoreContextConfig;
import org.keyworx.oase.main.Main;
import org.keyworx.oase.main.OaseContextManager;
import org.keyworx.oase.util.Util;
import org.keyworx.oase.api.*;
import org.keyworx.common.util.Rand;

/**
 * Test class <code>Finder</code>.
 *
 * @author Just van den Broecke
 */
public class PGTestCase extends TestCase {
	static final public String OASE_CONTEXT_NAME = "postgistest";
	static final public String SPATIAL_ONE_TABLE_NAME = "spatialone";


	private OaseSession session;
	private String name;

	public PGTestCase(String aName) {
		super(aName);

		name = aName;
	}

	public void setUp() {
		try {
			// Only init if not already running
			if (!OaseContextManager.isReady()) {
				Main.init();
			}
		} catch (OaseException oe) {
			System.err.println("FATAL: could not init Oase e=" + oe);
		}

		try {
			// Create session per test.
			session = Oase.createSession(OASE_CONTEXT_NAME);
		} catch (OaseException oe) {
			error("FATAL: could not create session e=" + oe);
			oe.printStackTrace();
		}
	}

	public void tearDown() throws OaseException {
		deleteAll();
		Main.exit();
	}

	public Record insertRandomSpatialOneRecord() throws OaseException {
		Record record = getModifier().create(SPATIAL_ONE_TABLE_NAME);
		getModifier().insert(record);
		return record;
	}

	protected void deleteAll(String aTableName) throws OaseException {
		Record[] record = getSession().getFinder().queryTable(aTableName, null, null, null);
		for (int i = 0; i < record.length; i++) {
			getSession().getModifier().delete(record[i].getId(), aTableName);
		}
	}

	protected void deleteAll() throws OaseException {
		// deleteAll(SPATIAL_ONE_TABLE_NAME);
		deleteAll(StoreContextConfig.TABLE_NAME_RELATION);
		deleteAll(StoreContextConfig.TABLE_NAME_RELATIONDEF);
	}

	/** Get Admin. */
	protected Admin getAdmin() throws OaseException {
		return session.getAdmin();
	}

	/** Get OaseSession.. */
	protected OaseSession getSession() {
		return session;
	}

	/** Get Finder.. */
	protected Finder getFinder() throws OaseException {
		return session.getFinder();
	}

	/** Get MediaFiler. */
	protected MediaFiler getMediaFiler() throws OaseException {
		return session.getMediaFiler();
	}

	/** Get Modifier. */
	protected Modifier getModifier() throws OaseException {
		return session.getModifier();
	}

	protected void error(String aMessage) {
		System.err.println("ERROR[" + name + "]: " + aMessage);
	}

	protected void info(String aMessage) {
		System.out.println("INFO[" + name + "]: " + aMessage);
	}

	protected void fatal(String aMessage) {
		System.err.println("FATAL[" + name + "]: " + aMessage);
		System.exit(-1);
	}

	protected void failTest(String aMessage, Throwable anException) {
/*		if (anException instanceof ComparisonFailure) {
			info(((ComparisonFailure) anException).getLocalizedMessage());
		}   */
		error(aMessage);
		anException.printStackTrace();
		fail(aMessage);
	}

	protected void dbg(String aMessage) {
		System.err.println("DEBUG[" + name + "]: " + aMessage);
	}
}
