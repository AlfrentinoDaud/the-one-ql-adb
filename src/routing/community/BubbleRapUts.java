package routing.community;

import java.util.*;

import core.*;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.AverageWinCentrality1;
import routing.community.Centrality;
import routing.community.CommunityDetection;
import routing.community.CommunityDetectionEngine;
import routing.community.Duration;
import routing.community.SimpleCommunityDetection;

// tambahkan interface CentralityDetectionEngine (buat sendiri, method 1) terinspirasi dari CommunityDetectionEngine
public class BubbleRapUts implements RoutingDecisionEngine, CommunityDetectionEngine, CentralityDetectionEngine {
    // Start-initialisation
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg"; // added
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";

    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    protected CommunityDetection community; // added
    protected CentralityKu centrality;

    // End-initialisation
    // Constructor based on the settings
    public BubbleRapUts(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) // added
        {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }

        if (s.contains(CENTRALITY_ALG_SETTING)) {
            this.centrality = (CentralityKu) s.createIntializedObject(s.getSetting(CENTRALITY_ALG_SETTING));
        }
        // else {
        // this.centrality = new AverageWinCentrality1(s);
        // }
    }

    // Constructor based on the argument prototype
    public BubbleRapUts(BubbleRapUts proto) {
        this.community = proto.community.replicate(); // added
        this.centrality = proto.centrality.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        BubbleRapUts de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, de.community); // added
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        // double time = startTimestamps.get(peer);
        double time = cek(thisHost, peer);
        double etime = SimClock.getTime();

        // Find or create the connection history list
        List<Duration> history;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        // add this connection to the list
        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community; // added
        community.connectionLost(thisHost, peer, peerCD, history); // added

        startTimestamps.remove(peer);
    }

    public double cek(DTNHost thisHost, DTNHost peer) {
        if (startTimestamps.containsKey(thisHost)) {
            startTimestamps.get(peer);
        }
        return 0;
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;

    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true; // deliver to final destination
        }
        // now we decide where to forward a message to relay node
        DTNHost dest = m.getTo();
        BubbleRapUts de = getOtherDecisionEngine(otherHost);

        boolean peerInCommunity = de.commumesWithHost(dest); // Is peer in dest'community
        boolean meInCommunity = this.commumesWithHost(dest); // Is THIS in dest'community

        if (peerInCommunity && !meInCommunity) // peer is in dest's community, but THIS is not
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // THIS is in dest'community, but peer is not
        {
            return false;
        } else if (peerInCommunity) // We're both in dest'community
        {
            // Forward to the one with the higher local centrality (in dest'community)
            if (de.getLocalCentrality() > this.getLocalCentrality()) {
                return true;
            } else {
                return false;
            }
        } else if (de.getGlobalCentrality() > this.getGlobalCentrality()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // delete the message once it is forwarded to the node in the dest'community

        BubbleRapUts de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        // BubbleRap de = this.getOtherDecisionEngine(hostReportingOld);
        // return de.commumesWithHost(m.getTo()) &&
        // !this.commumesWithHost(m.getTo());

        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new BubbleRapUts(this);
    }

    protected boolean commumesWithHost(DTNHost h) {
        return community.isHostInCommunity(h);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    protected double getGlobalCentrality() {
        return this.centrality.getGlobalCentrality(connHistory);
    }

    private BubbleRapUts getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (BubbleRapUts) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    // for REPORT purpose: CommunityDetectionReport
    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    @Override
    public void update(DTNHost thisHost) {
    }

    protected int[] getGlobalArrayCentrality() {
        return this.centrality.getArrayCentrality(connHistory);
    }

    // // public int [] getArrayCentrality();
    // protected int[] getGlobalArrayCentralityTag() {
    // return getGlobalArrayCentrality();
    // }

    @Override
    public int[] getArrayCentrality() {
        return getGlobalArrayCentrality();
    }
}