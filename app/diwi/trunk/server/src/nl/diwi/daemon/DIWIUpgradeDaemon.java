package nl.diwi.daemon;

import org.geotracing.daemon.UpgradeDaemon;
import org.keyworx.oase.api.OaseSession;
import org.keyworx.oase.api.Record;
import org.keyworx.oase.api.Modifier;
import org.keyworx.oase.api.Finder;


public class DIWIUpgradeDaemon extends UpgradeDaemon {

    synchronized public void timerFired() {
		getContext().stopTimer();

		// Check which upgrades to do
		if (getContext().getBoolProperty("relations")) {
			fixRelations();
		}
	}

    protected void fixRelations() {
		try {
			// Start
			log.info("START FIXING RELATIONS table oase_relation");
			OaseSession oase = getContext().createOaseSession().getOaseSession();
			Modifier modifier = oase.getModifier();
			Finder finder = oase.getFinder();

			// 1. get list of Locations
			Record[] recs = finder.readAll("oase_relation");
			if (recs.length == 0) {
				log.info("MIGRATING RELATIONS : NOTHING TODO");
				return;
			}

			log.info("MIGRATING RELATIONS : processing cnt=" + recs.length);

			int cnt=0;
			for (int i=0; i < recs.length; i++) {
                Record rec = recs[i];
				//log.info("FIXING RELATIONS: processing oase_relation id=" + rec.getId());

                int rec1Id = rec.getIntField("rec1");
                int rec2Id = rec.getIntField("rec2");

                try{
                    Record rec1 = finder.read(rec1Id);
                    if(rec1 == null) throw new Exception();
                    Record rec2 = finder.read(rec2Id);
                    if(rec2 == null) throw new Exception();
                }catch(Throwable t){
                    // remove the relation
                    modifier.delete(rec);
                    cnt++;
                    log.info("FIXED RELATION : done id=" + rec.getId());
                }
                
			}
			log.info("FINISHED FIXING RELATIONS OK: cnt=" + cnt + " OUT OF " + recs.length);
		} catch (Throwable t) {
			log.error("ERROR FIXING RELATIONS", t);
		}
	}
}
