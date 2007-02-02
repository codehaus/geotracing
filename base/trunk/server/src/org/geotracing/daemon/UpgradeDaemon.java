/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.geotracing.daemon;

import org.keyworx.amuse.core.Daemon;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.*;
import org.keyworx.plugin.tagging.logic.TagLogic;

import java.util.HashMap;

/**
 * Daemon for various upgrades.
 * <p/>
 * <h3>Purpose</h3>
 * This daemon will do software and database upgrades based on parameters given.
 * <p/>
 * <h3>Examples</h3>
 * <p/>
 * <h3>Implementation</h3>
 * <p/>
 * <h3>Configuration</h3>
 * <p>This Daemon can be configured with the following parameters,
 * those marked with a * are mandatory.</p>
 * <p/>
 * pois2textmedia* - migrate POIs to text media<br>
 * <h3>Concurrency</h3>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class UpgradeDaemon extends Daemon {
	protected Log log;

	public UpgradeDaemon() {
	}

	public Log getLog() {
		return log;
	}

	public void start() {
		// Log init
		log = Logging.getLog("UpgradeDaemon[" + getContext().getApplication().getId() + "]");

		log.info("started");

		// Start the Timer to first wait for all inits
		getContext().startTimer(200000);

	}

	public void stop() {
		log.info("stopped");
	}

	/** Time to check mail. */
	synchronized public void timerFired() {
		getContext().stopTimer();

		// Check which upgrades to do
		if (getContext().getBoolProperty("pois2textmedia")) {
			migratePOIs();
		}

	}

	protected void migratePOIs() {
		try {
			// Start
			log.info("START MIGRATING POIS");
			OaseSession oase = getContext().createOaseSession().getOaseSession();
			Relater relater = oase.getRelater();
			Modifier modifier = oase.getModifier();
			MediaFiler mediaFiler = oase.getMediaFiler();

			// 1. get list of POIs
			Record[] pois = oase.getFinder().readAll("g_poi");
			if (pois.length == 0) {
				log.info("MIGRATING POIS : NOTHING TODO");
				return;
			}

			log.info("MIGRATING POIS : processing " + pois.length + " POIs");

			// 2. each POI
			//  a. get location
			//  b. get track
			//  c. get person
			//  d. insert (upload) medium
			//  e. relate location to medium
			//  f. relate track to medium
			//  e. relate medium to person
			//  f. add tag from POI type
			//  g. delete POI
			Record poi, location, track, person, medium, related[];
			String content, name, type, tags[] = new String[1];
			HashMap fields = new HashMap(3);
			TagLogic tagLogic = new TagLogic(oase);
			int[] mediumIds = new int[1];
			for (int i=0; i < pois.length; i++) {

				poi = pois[i];
				log.info("MIGRATING POIS : processing poi id=" + poi.getId());
				related = relater.getRelated(poi, "g_location", null);
				if (related.length == 0) {
					modifier.delete(poi);
					log.warn("MIGRATING POIS : no related location skipping");
					continue;
				}
				location = related[0];
				related = relater.getRelated(poi, "g_track", null);
				if (related.length == 0) {
					modifier.delete(poi);
					log.warn("MIGRATING POIS : no related track skipping");
					continue;
				}
				track = related[0];
				related = relater.getRelated(poi, "utopia_person", null);
				if (related.length == 0) {
					modifier.delete(poi);
					log.warn("MIGRATING POIS : no related person skipping");
					continue;
				}
				person = related[0];

				// Insert medium
				content = poi.getStringField("description");
				if (content == null || content.length() == 0 || content.equals("null")) {
					/* modifier.delete(poi);
					log.warn("MIGRATING POIS : no content, skipping");
					continue;  */
					content = "no content in text medium";
				}

				fields.clear();
				name = poi.getStringField("name");
				if (name == null) {
					name = "unnamed";
				}
				// Adds medium and relates it to team
				fields.put(MediaFiler.FIELD_NAME, name.trim());
				fields.put(MediaFiler.FIELD_DESCRIPTION, " ");
				fields.put(MediaFiler.FIELD_MIME, "text/plain");
				fields.put(MediaFiler.FIELD_KIND, "text");

				medium = mediaFiler.insert(content.getBytes(), fields);
				medium.setTimestampField(MediaFiler.FIELD_CREATIONDATE, poi.getTimestampField("creationdate"));
				modifier.update(medium);

				relater.relate(location, medium, "medium");
				relater.relate(track, medium, "medium");
				relater.relate(medium, person);
				type = poi.getStringField("type");
				modifier.delete(poi);
				if (type != null) {
					// Add tag
					tags[0] = type.trim().toLowerCase().replace(' ', '-');
					mediumIds[0] = medium.getId();
					tagLogic.tag(person.getId(), mediumIds, tags, TagLogic.MODE_ADD);
					log.info("MIGRATING POIS : added tag=" + tags[0]);
				}
				// if (i==2) break;
				log.info("MIGRATING POIS : done id=" + poi.getId());
			}
			log.info("FINISHED MIGRATING POIS OK");
		} catch (Throwable t) {
			log.error("ERROR MIGRATING POIS", t);
		}
	}

}
