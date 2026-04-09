/*
 * @(#)CommunityDetectionReport.java
 *
 * Copyright 2010 by University of Pittsburgh, released under GPLv3.
 * 
 */
package report;

import java.util.*;

import core.*;
import routing.*;
import routing.community.CentralityDetectionEngine;

/**
 * <p>
 * Reports the local communities at each node whenever the done() method is
 * called. Only those nodes whose router is a DecisionEngineRouter and whose
 * RoutingDecisionEngine implements the
 * routing.community.CommunityDetectionEngine are reported. In this way, the
 * report is able to output the result of any of the community detection
 * algorithms.
 * </p>
 * 
 * @author Daud
 */
public class CentralityDetectionReport extends Report {
	public CentralityDetectionReport() {
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

		// for (Map.Entry<DTNHost, List<Integer>> entry : arrayCentrality.entrySet()) {
		// DTNHost h = entry.getKey();
		// List<Integer> list = entry.getValue();
		// write(entry.getKey() + ": " + entry.getValue());
		// }
		arrayCentrality.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // This requires DTNHost to implement Comparable
				.forEach(entry -> {
					DTNHost h = entry.getKey();
					List<Integer> list = entry.getValue();
					write(h + ": " + list);
				});

		super.done();
	}
}