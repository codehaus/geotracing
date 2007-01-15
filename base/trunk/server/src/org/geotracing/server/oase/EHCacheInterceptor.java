/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.geotracing.server.oase;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.keyworx.oase.api.FieldDef;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.TableDef;
import org.keyworx.oase.config.OaseConfig;
import org.keyworx.oase.config.TypeDef;
import org.keyworx.oase.store.StoreSession;
import org.keyworx.oase.store.source.FileSource;
import org.keyworx.oase.store.interceptor.DefaultInterceptor;
import org.keyworx.oase.store.record.FileFieldImpl;
import org.keyworx.oase.store.record.RecordImpl;
import org.keyworx.oase.util.Log;

import java.io.File;

/**
 * Implementes caching using EHCache.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * General explanation
 * <p/>
 * <h3>Examples</h3>
 * see {@link org.keyworx.oase.api.Record}
 * <p/>
 * <h3>Implementation</h3>
 * see {@link org.keyworx.oase.api.Record}
 * <p/>
 * <h3>Concurrency</h3>
 * not apllicable
 *
 * @author Just van den Broecke
 * @version $Id$
 */

public class EHCacheInterceptor extends DefaultInterceptor {
	private Cache recordCache;
	private CacheManager cacheManager;

	private static final String ignoreTables = "oase_index,oase_lastid,oase_relation";

	public void exit() throws OaseException {
		try {
			cacheManager.shutdown();
			Log.info("caches shutdown");
		} catch (Throwable t) {
			Log.error("Cannot exit caching: " + t);
			throw new OaseException("Cannot exit caching", t);
		}
	}

	public void init() throws OaseException {
		try {
			String configFilePath = OaseConfig.getConfigRootDir() + "/ehcache.xml";
			cacheManager = CacheManager.create(configFilePath);
			recordCache = cacheManager.getCache("oaserecordcache");
			Log.info("caches created");
		}
		catch (Throwable t) {
			Log.error("Cannot initialize caching: " + t);
			throw new OaseException("Cannot initialize caching", t);
		}
	}

	/**
	 * Updates the Record values in .
	 *
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public void insert(StoreSession aStoreSession, Record aRecord) throws OaseException {
		getNext().insert(aStoreSession, aRecord);
		if (isCachable(aRecord)) {
			cache(aRecord);
		}
	}

	/**
	 * Read Record by id.
	 *
	 * @param aRecordId record id.
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public Record read(StoreSession aStoreSession, int aRecordId, String aTableName) throws OaseException {
		if (!isCachable(aTableName)) {
			return getNext().read(aStoreSession, aRecordId, aTableName);
		}

		// Ok we may get from cache
		Record record = getCached(aRecordId, aTableName);
		if (record == null) {
			// Not in cache: read and put in cache
			record = getNext().read(aStoreSession, aRecordId, aTableName);
			cache(record);
		}
		return record;
	}

	/**
	 * Updates the Record values .
	 *
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public void update(StoreSession aStoreSession, Record aRecord) throws OaseException {
		getNext().update(aStoreSession, aRecord);
		if (isCachable(aRecord)) {
			cache(aRecord);
		}
	}


	/**
	 * Deletes a Record object.
	 *
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public void delete(StoreSession aStoreSession, Record aRecord) throws OaseException {
		int id = aRecord.getId();
		String aTableName = aRecord.getTableName();
		getNext().delete(aStoreSession, aRecord);
		if (isCachable(aTableName)) {
			unCache(id, aTableName);
		}
	}

	/*final public void resume() throws OaseException {
		 suspended = false;
	} */

	/**
	 * Perform SQL query on a single table.
	 *
	 * @param aTableName  table to perform query on..
	 * @param constraints The sql constraints, e.g.. a 'WHERE' statement.
	 * @return the Records found by the query.
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public Record[] queryTable(StoreSession aStoreSession, String aTableName, String constraints) throws OaseException {
		return getNext().queryTable(aStoreSession, aTableName, constraints);
	}

	/**
	 * Perform query with join on multiple tables in the Store.
	 *
	 * @param tables	  The tables needed for the query.
	 * @param fields	  The fields needed for the query.
	 * @param constraints The sql constraints, e.g.WHERE, JOIN etc statement.
	 * @return the Record array
	 * @throws org.keyworx.oase.api.OaseException
	 *          Standard exception.
	 */
	public Record[] queryStore(StoreSession aStoreSession, String tables, String fields, String constraints) throws OaseException {
		return getNext().queryStore(aStoreSession, tables, fields, constraints);
	}

	public Record[] queryStore(StoreSession aStoreSession, String queryString, String tableName) throws OaseException {
		return getNext().queryStore(aStoreSession, queryString, tableName);
	}


	protected boolean cache(Record aRecord) {

		try {
			CachedRecord cachedRecord = new CachedRecord();
			TableDef tableDef = aRecord.getTableDef();
			cachedRecord.table = tableDef.getName();
			FieldDef[] fieldDefs = tableDef.getFieldDefs();
			cachedRecord.values = new Object[fieldDefs.length];
			FieldDef fieldDef;
			String fieldName;
			Object fieldValue;
			for (int i = 0; i < fieldDefs.length; i++) {
				fieldDef = fieldDefs[i];
				fieldName = fieldDef.getName();
				fieldValue = aRecord.getField(fieldName);
				if (fieldValue == null) {
					continue;
				}
				switch (fieldDef.getType()) {

					case FieldDef.TYPE_OBJECT:
						fieldValue = fieldValue.toString();
						break;

					case FieldDef.TYPE_BLOB:
						Log.warn("blobs not supported for caching");
						break;

					case FieldDef.TYPE_FILE:
						FileFieldImpl fileField = (FileFieldImpl) fieldValue;
						File file = fileField.getStoredFile();
						if (file != null) {
							fieldValue = file.getAbsolutePath();
						} else {
							// Set empty (is filled in getCache())
							fieldValue = "";
						}
						break;

					default:
						// Default is immutable object like Integer, String etc
						break;
				}
				cachedRecord.values[i] = fieldValue;
			}

			Element element = new Element(aRecord.getIdString(), cachedRecord);
			recordCache.put(element, true);
			Log.trace("CACHE PUT record table=" + aRecord.getTableName() + " id=" + aRecord.getId());
			return true;
		} catch (Throwable t) {
			Log.warn("CACHE PUT FAILED record table=" + aRecord.getTableName() + " id=" + aRecord.getId(), t);
			return false;
		}
	}


	protected Record getCached(int aRecordId, String aTableName) throws OaseException {
		try {
			Element element = recordCache.get(aRecordId + "");
			if (element == null) {
				Log.trace("CACHE MISS record table=" + aTableName + " id=" + aRecordId);
				return null;
			}
			Log.trace("CACHE HIT record table=" + aTableName + " id=" + aRecordId);
			CachedRecord cachedRecord = (CachedRecord) element.getObjectValue();

			TableDef tableDef = getStoreContext().getStoreContextConfig().getTableDef(aTableName);
			Object[] values = cachedRecord.values;
			FieldDef[] fieldDefs = tableDef.getFieldDefs();
			FieldDef fieldDef;
			String fieldName;
			Object fieldValue;
			RecordImpl record = new RecordImpl(tableDef);
			for (int i = 0; i < fieldDefs.length; i++) {
				fieldDef = fieldDefs[i];
				fieldName = fieldDef.getName();
				fieldValue = values[i];
				if (fieldValue != null) {
					switch (fieldDef.getType()) {

						case FieldDef.TYPE_OBJECT:
							fieldValue = TypeDef.string2FieldObject(fieldDef, (String) fieldValue);
							break;

						case FieldDef.TYPE_BLOB:
							Log.warn("blobs not supported for caching");
							break;

						case FieldDef.TYPE_FILE:
							FileFieldImpl fileField = new FileFieldImpl();
							String storedFilePath = (String) fieldValue;
							File file;
							if (storedFilePath.length() > 0) {
								file = new File((String) fieldValue);
							} else {
								// In some cases the file may be empty (is filled in when reading)
								FileSource fileSource = (FileSource) getStoreContext().getSourceById("file");
								file = new File(fileSource.getStoredFilePath(record, fieldDef));
							}
							fileField.setStoredFile(file);
							fieldValue = fileField;
							break;

						default:
							// Default is immutable object like Integer, String etc
							break;
					}
				}
				record.setFieldValue(fieldName, fieldValue);
			}
			record.clearChangedFields();
			record.setState(RecordImpl.STATE_STORED);
			return record;
		} catch (Throwable t) {
			Log.warn("CACHE GET FAILED record table=" + aTableName + " id=" + aRecordId, t);
			return null;
		}
	}

	protected boolean unCache(int anId, String aTableName) {

		if (!recordCache.remove(anId + "", true)) {
			return false;
		}
		Log.trace("CACHE REMOVE record table=" + aTableName + " id=" + anId);
		return true;
	}

	protected boolean isCachable(String aTableName) {
		return ignoreTables.indexOf(aTableName) == -1;
	}

	protected boolean isCachable(Record aRecord) {
		return isCachable(aRecord.getTableName());
	}

	private static class CachedRecord {
		public String table;
		public Object[] values;
	}
}
