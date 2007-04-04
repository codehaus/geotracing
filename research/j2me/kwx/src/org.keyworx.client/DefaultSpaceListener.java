/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.geotracing.client.kwx;

import nl.justobjects.mjox.JXElement;

public class DefaultSpaceListener implements SpaceListener {
		public void onEnterIndication(JXElement agentFrom) {
			p(agentFrom, "onEnterIndication", null);
		}

		public void onExitIndication(JXElement agentFrom, String reason) {
			p(agentFrom, "onExitIndication reason=" + reason, null);
		}

		public void onJoinIndication(JXElement agentFrom, JXElement joinCommand) {
			p(agentFrom, "onJoinIndication", joinCommand);
		}

		public void onLeaveIndication(JXElement agentFrom, String anAmuletId) {
			p(agentFrom, "onLeaveIndication amulet=" + anAmuletId, null);
		}

		public void onAmuletIndication(JXElement agentFrom, String anAmuletId, JXElement anIndication) {
			p(agentFrom, "onAmuletIndication amulet=" + anAmuletId, anIndication);
		}

		public void onAmuletRequest(JXElement agentFrom, String anAmuletId, JXElement aRequest) {
			p(agentFrom, "onAmuletRequest amulet=" + anAmuletId, aRequest);
		}

		public void onError(String errorMessage) {
			//System.out.println("DefaultSpaceListener: ERROR" + errorMessage);
		}

		protected void p(JXElement agentFrom, String someDetails, JXElement aCommand) {
			//String agentId = agentFrom == null ? "unknown" : agentFrom.getId();
			//System.out.println("DefaultSpaceListener: from [" + agentId + "] details=" + someDetails + " cmd=" + aCommand);
		}
	}