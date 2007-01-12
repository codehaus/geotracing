// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import org.keyworx.common.util.Sys;

import java.util.Hashtable;

/** Cache based on time expiry.. */
public class TimedCache {
	private boolean locked;
	private Object item;
	private long createTime;
	private long leaseTime;

	private static Hashtable caches = new Hashtable(3);

	public static void createEntry(String aName, long aLeaseTime) {
		TimedCache timedCache = new TimedCache();
		timedCache.leaseTime = aLeaseTime;
		caches.put(aName, timedCache);
	}

	public static void destroyEntry(String aName) {
		if (caches.remove(aName) == null) {
			throw new IllegalArgumentException("No entry found for " + aName + " (do createEntry() first)");
		}
	}

	public static Object getItem(String aName) {
		TimedCache timedCache = (TimedCache) caches.get(aName);
		if (timedCache == null) {
			throw new IllegalArgumentException("No entry found for " + aName + " (do createEntry() first)");
		}

		return timedCache.getCachedItem();
	}

	public static void lockItem(String aName) {
		TimedCache timedCache = (TimedCache) caches.get(aName);
		if (timedCache == null) {
			throw new IllegalArgumentException("No entry found for " + aName + " (do createEntry() first)");
		}

		timedCache.lock();
	}

	public static void putItem(String aName, Object anItem) {
		TimedCache timedCache = (TimedCache) caches.get(aName);
		if (timedCache == null) {
			throw new IllegalArgumentException("No entry found for " + aName + " (do createEntry() first)");
		}

		timedCache.setCachedItem(anItem);
	}

	synchronized private Object getCachedItem() {
		if (item == null) {
			while (locked) {
				try {
					// log.info(": WAITING FOR ENTRY...");
					wait();
				} catch (InterruptedException ie) {
					return null;
				}
			}
		}

		// Not null: may invalidate cache
		if (item != null) {
			if (Sys.now() - createTime > leaseTime) {
				item = null;
				// log.info(": CLEAR CACHE FOR  FOR ENTRY...");
			}
		}

		return item;
	}

	synchronized private void lock() {
		locked = true;
	}

	synchronized private void setCachedItem(Object anItem) {
		item = anItem;
		locked = false;
		createTime = Sys.now();
		notifyAll();
	}

/*	void sendCachedWorldResult(String item, HttpServletResponse response) {
		response.setContentType("text/xml;charset=utf-8");
		try {
			Writer writer = response.getWriter();
			writer.write(item);
			writer.flush();
			writer.close();
		} catch (Throwable th) {
			log.info("error writing cached response");
		}
	}  */
}