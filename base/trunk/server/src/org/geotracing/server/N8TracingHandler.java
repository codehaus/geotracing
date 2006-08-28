// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.server;

import nl.justobjects.jox.dom.JXElement;
import org.geotracing.gis.GISCalc;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.data.Account;
import org.keyworx.utopia.core.data.Person;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.util.Oase;

import java.util.Vector;
import java.util.List;
import java.util.Date;


/**
 * Overidden for N8 game.
 *
 * NOTE: not used but kept here for reference (contains LBS for bombs) !
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class N8TracingHandler extends TracingHandler {
	public final static String ATTR_LOGIN_NAME = "loginname";
	public final static String ATTR_OWNER = "owner";
	public final static String ATTR_TRACK_NAME = "trackname";
	public final static String ATTR_LON = "lon";
	public final static String ATTR_LAT = "lat";
	public final static String ATTR_COUNT = "count";
	public final static String TAG_BOMBS = "bombs";
	public final static String TAG_POI = "poi";
	public final static int VAL_BOMB_INACTIVE = 1;
	public final static int BOMB_SUPPLY = 4;
	public final static String T_TRK_GET_BOMBS_SERVICE = "t-trk-get-bombs";
	public final static String TYPE_POI_BOMB = "bomb";
	public final static double BOMB_DETONE_DIST = 0.04D;
	Log log = Logging.getLog("N8TracingHandler");
	private static Bomb[] bombCache = null;
	private static Object semaphore = new Object();

	/**
	 * Overridden implementation for N8-specific requests.
	 *
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A negative UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement unknownReq(UtopiaRequest anUtopiaReq) throws UtopiaException {

		// Get the service name for the request
		String service = anUtopiaReq.getServiceName();
		log.trace("Handling request for service=" + service);

		JXElement response = null;
		try {
			if (service.equals(T_TRK_GET_BOMBS_SERVICE)) {
				response = getBombsReq(anUtopiaReq);
			} else {
				// Let superclass handle really unknown requests...
				response = super.unknownReq(anUtopiaReq);
			}
		} catch (UtopiaException ue) {
			log.warn("Negative response service=" + service, ue);
			response = createNegativeResponse(service, ue.getErrorCode(), ue.getMessage());
		} catch (Throwable t) {
			log.error("Unexpected error service=" + service, t);
			response = createNegativeResponse(service, ErrorCode.__6005_Unexpected_error, "Unexpected error in request " + t);
		}

		// Always return a response
		log.trace("Handled service=" + service + " response=" + response.getTag());
		trackLogic = null;
		return response;

	}

	/**
	 * Add Point of Interest to Track.
	 *
	 *
	 * Example
	 * <code>
	 * &lt;t-trk-add-poi-req name="lake view" type="view" t="1247554522225"  /&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement addPOIReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);
		JXElement reqElm = anUtopiaReq.getRequestCommand();
		String type = reqElm.getAttr(ATTR_TYPE, null);
		throwOnMissingAttr(ATTR_TYPE, type);

		// Other POI (not probable but ok)
		if (!type.equals(TYPE_POI_BOMB)) {
			return super.addPOIReq(anUtopiaReq);
		}

		// Check if enough bombs left
		int bombCount = getBombSupplyCount(anUtopiaReq);
		if (bombCount == 0) {
			throw new UtopiaException("Insufficient bombs", ErrorCode.__6003_Illegal_command_for_state);
		}

		// First add POI
		JXElement rsp = super.addPOIReq(anUtopiaReq);

		// Now decrease and update bomb count
		setBombSupplyCount(--bombCount, anUtopiaReq);

		flushBombCache();
		log.info("N8 bomb dropped by user" + getUserName(anUtopiaReq) + " bombId=" + rsp.getId() + " usrBombSupply is now " + bombCount);

		return rsp;
	}

	/**
	 * Create new Track and add initial bomb supply.
	 *
	 * Examples
	 * <code>
	 * &lt;t-trk-create-req &gt; &lt;
	 *
	 * &lt;t-trk-create-rsp id="t-trk-id" &gt; &lt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement createReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		Log log = Logging.getLog(anUtopiaReq);
		JXElement rsp = super.createReq(anUtopiaReq);

		flushBombCache();

		// Set initial bomb count
		setBombSupplyCount(BOMB_SUPPLY, anUtopiaReq);

		log.info("N8 Track created user=" + getUserId(anUtopiaReq) + " id=" + rsp.getId() + " bombCnt=" + BOMB_SUPPLY);

		return rsp;
	}

	/**
	 * Delete track.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement deleteReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		flushBombCache();
		return super.deleteReq(anUtopiaReq);
	}

	/**
	 * Hanlde get bombs request.
	 *
	 * Example
	 * <code>
	 * &lt;t-trk-get-bombs-req/&gt;
	 * </code>
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement getBombsReq(UtopiaRequest anUtopiaReq) throws UtopiaException {

		// Fill response
		JXElement responseElement = createResponse(T_TRK_GET_BOMBS_SERVICE);
		responseElement.setAttr(ATTR_COUNT, getBombSupplyCount(anUtopiaReq));
		return responseElement;
	}

	/**
	 * Overridden: checks for user walking on bomb.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public JXElement writeReq(UtopiaRequest anUtopiaReq) throws UtopiaException {
		JXElement reqElm = anUtopiaReq.getRequestCommand();

		Vector result = trackLogic.write(reqElm.getChildren(), getUserId(anUtopiaReq));
		Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));
		JXElement rsp = createResponse(T_TRK_WRITE_SERVICE);
		JXElement lastPt = (JXElement) result.get(0);
		if (lastPt == null) {
			return rsp;
		}
		try {
			// Publish any points
			EventPublisher.tracerMove(track, result, anUtopiaReq);
			synchronized (semaphore) {
				// Get the bombs
				Bomb[] bombs = getActiveBombs(anUtopiaReq);
				Bomb bombHit = null;
				double userLon = lastPt.getDoubleAttr(ATTR_LON);
				double userLat = lastPt.getDoubleAttr(ATTR_LAT);
				double bombLon, bombLat, distance = -1;

				for (int i = 0; i < bombs.length; i++) {
					bombLon = bombs[i].lon;
					bombLat = bombs[i].lat;
					distance = GISCalc.distance(userLat, userLon, bombLat, bombLon, 'K');
					if (distance < BOMB_DETONE_DIST) {
						bombHit = bombs[i];
						break;
					}
				}

				if (bombHit != null) {
					Account account = getAccount(anUtopiaReq);
					String bomberName = bombHit.loginName;
					String victimName = account.getLoginName();
					log.trace("N8 BOMB NEAR distance=" + distance + " id=" + bombHit.id + " trackName=" + bombHit.trackName + " victim=" + victimName + " bomber=" + bomberName);

					if (victimName.equals(bomberName)) {
						log.trace("N8 OK bomb NOT detoned (was my own bomb) me=" + victimName);
					} else {
						log.info("N8 BOM HIT: id=" + bombHit.id + " victim=" + victimName + " bomber=" + bomberName + " distance=" + distance);
						Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
						POI bomb = (POI) oase.get(POI.class, bombHit.id + "");
						if (bomb == null) {
							log.warn("Bomb with id=" + bombHit.id + " does not exist");
							return rsp;
						}

						// Remove bomb by changing its state
						bomb.setIntValue(ATTR_STATE, VAL_BOMB_INACTIVE);
						bomb.saveUpdate();

						// Bomb removed
						flushBombCache();

						// Do something nasty
	/*					Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));
						if (track == null) {
							log.warn("No active Track found for bomb victim" + getUserName(anUtopiaReq));
							return rsp;
						}  */


						// Ok track found: remove quarter of content
						Vector content = track.getDataElements();
						if (track.getIntValue(Track.FIELD_PTCOUNT) < 4) {
							log.warn("Not enough points to be replaced for bomb victim=" + getUserName(anUtopiaReq));
							return rsp;
						}

						// Content has at least size 4
						int newSize = content.size() - content.size() / 4;
						Vector newContent = new Vector();
						for (int i = 0; i < newSize; i++) {
							newContent.add(content.get(i));
						}

						track.clearData();
						track.addData(newContent);
						trackLogic.suspend(getUserId(anUtopiaReq), Sys.now());
                        trackLogic.resume(getUserId(anUtopiaReq), Track.VAL_NORMAL_TRACK, Sys.now());

						log.info("N8 Bomb: replaced track data old=" + content.size() + " elms new=" + newContent.size() + " elms, victim=" + getUserName(anUtopiaReq));

						// Add generic poi info to response
						JXElement poiElm = new JXElement(TAG_POI);
						poiElm.setAttr(ATTR_ID, bombHit.id);
						poiElm.setAttr(ATTR_TYPE, TYPE_POI_BOMB);
						poiElm.setAttr(ATTR_OWNER, bombHit.loginName);
						poiElm.setAttr(ATTR_TRACK_NAME, bombHit.trackName);
						rsp.addChild(poiElm);

						// Send Pushlet "poi-hit" event to notify listeners
						EventPublisher.poiHit(bombHit.id, anUtopiaReq);
					}
				}
			}
		} catch (Throwable t) {
			log.warn("N8 Error during bomb check, chugging along...", t);
		}
		return rsp;
	}

	/**
	 * Flush bomb cache
	 *
	 */
	protected void flushBombCache() {
		synchronized (semaphore) {
			if (bombCache != null) {
				log.trace("N8 Flushing bombcache count=" + bombCache.length);
				bombCache = null;
			}
		}
	}

	/**
	 * Get all bombs placed and still active.
	 *
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	protected Bomb[] getActiveBombs(UtopiaRequest anUtopiaReq) throws UtopiaException {
		if (bombCache != null) {
			return bombCache;
		}

		bombCache = new Bomb[0];
		long t1 = Sys.now();
		try {
			Oase oase = anUtopiaReq.getUtopiaSession().getContext().getOase();
			String tables = "g_track,g_poi,g_location";
			String fields = "g_track.name,g_poi.id,g_location.lon,g_location.lat";
			String where = "g_track.state = 1 AND g_poi.state = 0";
			String relations = "g_track,g_poi,bomb;g_poi,g_location";
			Record[] records = QueryHandler.queryStore(oase, tables, fields, where, relations, null);

			bombCache = new Bomb[records.length];
			Bomb nextBomb = null;
			Track nextTrack = null;
			POI nextPOI = null;
			Person nextPerson = null;
			Account nextAccount = null;
			for (int i = 0; i < bombCache.length; i++) {
				nextBomb = new Bomb();
				nextPOI = (POI) oase.get(POI.class, records[i].getId() + "");
				nextTrack = (Track) nextPOI.getRelatedObject(Track.class);
				nextPerson = (Person) nextTrack.getRelatedObject(Person.class);
				nextAccount = nextPerson.getAccount();

				nextBomb.id = nextPOI.getId();
				nextBomb.loginName = nextAccount.getLoginName();
				nextBomb.personId = nextPerson.getId();
				nextBomb.trackId = nextTrack.getId();
				nextBomb.trackName = nextTrack.getName();
				nextBomb.lon = records[i].getRealField(ATTR_LON);
				nextBomb.lat = records[i].getRealField(ATTR_LAT);

				bombCache[i] = nextBomb;
			}
		} catch (Throwable t) {
			log.error("N8 Error in query for active Bombs", t);
		}
		log.info("N8 refreshed bomb cache count=" + bombCache.length + " querytime=" + (Sys.now() - t1) + " ms");
		return bombCache;
	}

	/**
	 * get bomb element for track.
	 *
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	protected int getBombSupplyCount(UtopiaRequest anUtopiaReq) throws UtopiaException {

		Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));
		if (track == null) {
			throw new UtopiaException("No active Track", ErrorCode.__6003_Illegal_command_for_state);
		}

		// Ok active track found get the count
		JXElement bombsElement = track.getExtra();
		if (bombsElement == null) {
			throw new UtopiaException("No bombs in active Track", ErrorCode.__6003_Illegal_command_for_state);
		}

		return bombsElement.getIntAttr(ATTR_COUNT);
	}

	/**
	 * Set bomb element for track.
	 *
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	protected void setBombSupplyCount(int aCount, UtopiaRequest anUtopiaReq) throws UtopiaException {
		Track track = trackLogic.getActiveTrack(getUserId(anUtopiaReq));
		if (track == null) {
			throw new UtopiaException("No active Track", ErrorCode.__6003_Illegal_command_for_state);
		}

		// Add bomb supply in extra element of track
		JXElement bombsElm = new JXElement(TAG_BOMBS);
		bombsElm.setAttr(ATTR_COUNT, aCount);
		track.setExtraElementValue(Track.FIELD_EXTRA, bombsElm);
		track.saveUpdate();
	}

	private class Bomb {
		int id;
		int personId;
		String loginName;
		int trackId;
		String trackName;
		double lon;
		double lat;
	}

}