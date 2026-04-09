/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.SimError;
import core.SimScenario;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.CentralityDetectionEngine;

/**
 * Abstract superclass for all reports. All settings defined in this class
 * can be used for all Report classes. Some reports don't implement intervalled
 * reports ({@link #INTERVAL_SETTING}) and will ignore that setting. Most of
 * the reports implement warm up feature ({@link #WARMUP_S}) but the
 * implementations are always report specific.
 */
public abstract class BubbleRapUTSReport extends Report {
	public BubbleRapUTSReport() {
		init();
	}

	@Override
	public void done() {
		List<DTNHost> nodes = SimScenario.getInstance().getHosts();
		Map<DTNHost, List<Integer>> arrayCentrality = new HashMap<DTNHost, List<Integer>>();

		for (DTNHost h : nodes) {
			MessageRouter r = h.getRouter();
			if (!(r instanceof DecisionEngineRouter))
				continue;
			RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
			if (!(de instanceof CentralityDetectionEngine))
				continue;

			CentralityDetectionEngine ctd = (CentralityDetectionEngine) de;
			int[] arrayku = ctd.getArrayCentrality();

			List<Integer> myarray = new ArrayList<Integer>();

			for (int cent : arrayku) {
				myarray.add(cent);
			}
			arrayCentrality.put(h, myarray);
		}

		for (Map.Entry<DTNHost, List<Integer>> entry : arrayCentrality.entrySet()) {
			DTNHost h = entry.getKey();
			List<Integer> list = entry.getValue();
			write(entry.getKey() + ": " + entry.getValue());
		}
		super.done();
	}

}