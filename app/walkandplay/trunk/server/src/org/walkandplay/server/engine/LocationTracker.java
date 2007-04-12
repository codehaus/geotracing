/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.util.Sys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Tracks actual location matters (zones, confrontations).
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class LocationTracker {
    private HashMap zoneAreas = new HashMap(6);
    private HashMap teamsInZones = new HashMap(6);
    private Set teamCollisions = new HashSet(6);
    private HashMap teamLocations = new HashMap(6);

    /**
     * Use coordinates on real map.
     */
    private static final String ATTR_X_NAME = "rx";
    private static final String ATTR_Y_NAME = "ry";

    /**
     * Ref coordinates on both maps.
     */
    private static final String ATTR_REFRX_NAME = "refrx";
    private static final String ATTR_REFRY_NAME = "refry";
    private static final String ATTR_REFMX_NAME = "refmx";
    private static final String ATTR_REFMY_NAME = "refmy";

    public LocationTracker(String aConfigFile) throws GameException {
        try {            
            // Init all points
            // TODO: No zone support for now
            /*
            JXElement zonesElement = new JXBuilder().build(new File(aConfigFile));
            Vector zones = zonesElement.getChildren();
			ZoneArea zoneArea;
			for (int i = 0; i < zones.size(); i++) {
				zoneArea = new ZoneArea((JXElement) zones.elementAt(i));
				zoneAreas.put(zoneArea.zoneName, zoneArea);
			}*/
        } catch (Throwable t) {
            throw new GameException("Cannot init LocationTracker file=" + aConfigFile);
        }
    }

    /**
     * Add a collision between two teams.
     */
    public boolean addCollision(String aTeamName, String anotherTeamName) {
        return teamCollisions.add(new TeamCollision(aTeamName, anotherTeamName));
    }

    /**
     * Check if a collision between two teams exists.
     */
    public boolean containsCollision(String aTeamName, String anotherTeamName) {
        return teamCollisions.contains(new TeamCollision(aTeamName, anotherTeamName));
    }

    /**
     * Get time of collision, or -1 if no collision available.
     */
    public long getCollisionTimeMillis(String aTeamName, String anotherTeamName) {
        Iterator iter = teamCollisions.iterator();
        TeamCollision matcher = new TeamCollision(aTeamName, anotherTeamName);
        TeamCollision next = null;
        while (iter.hasNext()) {
            next = (TeamCollision) iter.next();
            if (next.equals(matcher)) {
                return next.startTimeMillis;
            }
        }
        return -1L;
    }

    /**
     * Reset time of collision.
     */
    public void clearCollisionTimeMillis(String aTeamName, String anotherTeamName) {
        Iterator iter = teamCollisions.iterator();
        TeamCollision matcher = new TeamCollision(aTeamName, anotherTeamName);
        TeamCollision next = null;
        while (iter.hasNext()) {
            next = (TeamCollision) iter.next();
            if (next.equals(matcher)) {
                next.startTimeMillis = 0L;
                break;
            }
        }
    }

    /**
     * Remove a collision between two teams.
     */
    public boolean removeCollision(String aTeamName, String anotherTeamName) {
        return teamCollisions.remove(new TeamCollision(aTeamName, anotherTeamName));
    }

    // TODO: No zone support for now
    /** Get reference location in zone. */
    /*public JXElement getReferenceLocation(String aZoneName) {
            ZoneArea zoneArea = (ZoneArea) zoneAreas.get(aZoneName);
            return zoneArea.referenceLocation;
        }
    */
    /** Get location element of team. */
    public JXElement getTeamLocation(String aTeamName) {
        return (JXElement) teamLocations.get(aTeamName);
    }

    /**
     * Get names of teams being tracked (have GPS).
     */
    public String[] getTeams() {
        return (String[]) teamLocations.keySet().toArray(new String[0]);
    }

    /**
     * Reset location status for all teams.
     */
    public void reset() {
        String[] teamNames = getTeams();
        for (int i = 0; i < teamNames.length; i++) {
            resetTeam(teamNames[i]);
        }
    }

    /**
     * Reset team location status.
     */
    public void resetTeam(String aTeamName) {
        teamsInZones.remove(aTeamName);
        teamLocations.remove(aTeamName);

        // Remove all collisions for team
        TeamCollision[] collisions = (TeamCollision[]) teamCollisions.toArray(new TeamCollision[0]);
        for (int i = 0; i < collisions.length; i++) {
            if (collisions[i].teamOne.equals(aTeamName) || collisions[i].teamTwo.equals(aTeamName)) {
                teamCollisions.remove(collisions[i]);
            }
        }
    }

    /**
     * Returns zone name if zone first entered and updates location with zone name.
     */
    public String trackTeam(String aTeamName, JXElement aLocation) {
        // Always remember current location
        teamLocations.put(aTeamName, aLocation);

        TeamInZone teamInZone = (TeamInZone) teamsInZones.get(aTeamName);
        if (teamInZone == null) {
            teamInZone = new TeamInZone();
            teamInZone.teamName = aTeamName;
            teamsInZones.put(aTeamName, teamInZone);
        }

        // ASSERT:  teamInZone != null

        // There is already a valid zone, first quickly check
        // if we are still in that zone
        if (!teamInZone.zoneName.equals("none")) {
            // If team still in same zone return null (no new zone)
            // TODO: commented out zone support
            /*if (isTeamInZone(aTeamName, teamInZone.zoneName, aLocation)) {
                // Always update current zone in location element
                aLocation.setAttr(GameProtocol.ATTR_ZONENAME, teamInZone.zoneName);
                return null;
            }*/
        }

        // ASSERT: No zone yet for team || team has moved to other zone
        // TODO: No zone support for now
        // Find zone team is in
        /*ZoneArea nextZoneArea;
          for (Iterator iter = zoneAreas.values().iterator(); iter.hasNext();) {
              nextZoneArea = (ZoneArea) iter.next();
              if (nextZoneArea.isInZone(aLocation)) {
                  // Always update current zone in location element
                  aLocation.setAttr(GameProtocol.ATTR_ZONENAME, nextZoneArea.zoneName);

                  // If not yet in any zone || a different zone
                  // then a new zone is really entered
                  if (!teamInZone.zoneName.equals(nextZoneArea.zoneName)) {
                      // System.out.println("CHANGE ZONE TO " + nextZoneArea.zoneName);
                      teamInZone.zoneName = nextZoneArea.zoneName;
                      return teamInZone.zoneName;
                  }
              }
          }*/

        // Either no zone change or no zone found at all
        return null;
    }

    /**
     * Get zone name where team is located.
     */
    public String getZoneForTeam(String aTeamName) {
        TeamInZone teamInZone = (TeamInZone) teamsInZones.get(aTeamName);
        if (teamInZone == null) {
            return null;
        }
        return teamInZone.zoneName;
    }

    // TODO: No zone support for now
    /** Tests weather point is contained in GeoArea. */
    /*public boolean isTeamInZone(String aTeamName, String aZoneName, JXElement aLocation) {
         ZoneArea zoneArea = (ZoneArea) zoneAreas.get(aZoneName);
         return (zoneArea.isInZone(aLocation));
     }*/

    private static class TeamCollision {
        private int hashValue;
        public String teamOne;
        public String teamTwo;
        public long startTimeMillis = Sys.now();

        public TeamCollision(String aTeamName, String anotherTeamName) {
            teamOne = aTeamName;
            teamTwo = anotherTeamName;

            // Needed to fool the HashSet algoritm
            // Team names may be reversed but we should still match...
            hashValue = 13;
        }


        public int hashCode() {
            return hashValue;
        }

        public boolean equals(Object anObject) {
            if (!(anObject instanceof TeamCollision)) {
                return false;
            }

            // equals if teamNames matches either team name
            TeamCollision teamCollision = (TeamCollision) anObject;
            return
                    (teamOne.equals(teamCollision.teamOne) && teamTwo.equals(teamCollision.teamTwo) ||
                            teamOne.equals(teamCollision.teamTwo) && teamTwo.equals(teamCollision.teamOne)
                    );

        }
    }

    private static class TeamInZone {
        public String teamName;
        public String zoneName = "none";
    }

    // TODO :  no zone support for now..
    /*private static class ZoneArea {
		public String zoneName;
		public GeoArea mapArea;
		public JXElement referenceLocation = new JXElement("location");

		public ZoneArea(JXElement aZoneElement) {
			// Reference point (for boobytrap resolve point)
			referenceLocation.setAttr("rx", aZoneElement.getIntAttr(ATTR_REFRX_NAME));
			referenceLocation.setAttr("ry", aZoneElement.getIntAttr(ATTR_REFRY_NAME));
			referenceLocation.setAttr("mx", aZoneElement.getIntAttr(ATTR_REFMX_NAME));
			referenceLocation.setAttr("my", aZoneElement.getIntAttr(ATTR_REFMY_NAME));

			Vector pointElements = aZoneElement.getChildren();
			int pointCount = pointElements.size();
			int[] xPoints = new int[pointCount];
			int[] yPoints = new int[pointCount];
			JXElement nextPoint = null;
			for (int i = 0; i < pointCount; i++) {
				nextPoint = (JXElement) pointElements.elementAt(i);
				xPoints[i] = nextPoint.getIntAttr(ATTR_X_NAME);
				yPoints[i] = nextPoint.getIntAttr(ATTR_Y_NAME);
			}

			mapArea = new GeoArea(xPoints, yPoints);
			zoneName = aZoneElement.getAttr("name");
		}

		public boolean isInZone(JXElement aLocation) {
			return isInZone(aLocation.getIntAttr(ATTR_X_NAME),
					aLocation.getIntAttr(ATTR_Y_NAME));
		}

		public boolean isInZone(int x, int y) {
			return mapArea.contains(x, y);
		}
	}*/
}
