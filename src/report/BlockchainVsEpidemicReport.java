package report;

import core.*;
import java.util.*;
import Blockchain.Validator;

/**
 * Blockchain vs Epidemic Report
 * Mengukur:
 * - Delivery Ratio
 * - Latency
 * - Overhead Ratio
 * - Reward Distribution (multi-hop forwarding)
 */
public class BlockchainVsEpidemicReport extends Report implements MessageListener {

    private int created = 0;
    private int delivered = 0;
    private int relayed = 0;

    private List<Double> latencies = new ArrayList<>();
    private Map<Integer, Double> rewards = new HashMap<>();

    public BlockchainVsEpidemicReport() {
        super();
        SimScenario.getInstance().addMessageListener(this);
    }

    @Override
    public void newMessage(Message m) {
        if (isWarmup())
            return;
        created++;
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(
            Message m,
            DTNHost from,
            DTNHost to,
            boolean firstDelivery) {

        if (isWarmup())
            return;

        relayed++;

        /*
         * reward mengikuti formula paper:
         *
         * reward = α'*(H-i)+β
         *
         * α=1
         * β=0.5
         * H=10
         */

        int hop = m.getHopCount();

        double alpha = 1.0;
        double beta = 0.5;
        int H = 10;

        double alphaPrime = alpha / (H - 1);

        double reward;

        if (hop >= H - 1) {
            reward = beta;
        } else {
            reward = ((H - 1) - hop) * alphaPrime + beta;
        }

        int node = from.getAddress();

        rewards.put(
                node,
                rewards.getOrDefault(node, 0.0) + reward);

        if (firstDelivery) {
            delivered++;

            double latency = SimClock.getTime() - m.getCreationTime();

            latencies.add(latency);
        }
    }

    @Override
    public void messageDeleted(
            Message m,
            DTNHost where,
            boolean dropped) {
    }

    @Override
    public void messageTransferAborted(
            Message m,
            DTNHost from,
            DTNHost to) {
    }

    @Override
    public void done() {

        write("==================================");
        write(" BLOCKCHAIN vs EPIDEMIC REPORT ");
        write("==================================");

        write("Messages Created  : " + created);
        write("Messages Delivered: " + delivered);
        write("Messages Relayed  : " + relayed);

        double deliveryRatio = created == 0 ? 0 : (double) delivered / created;

        double overhead = delivered == 0 ? 0 : (double) (relayed - delivered) / delivered;

        double avgLatency = avg(latencies);

        write("");
        write("NETWORK PERFORMANCE");
        write("----------------------------");
        write("Delivery Ratio : " + format(deliveryRatio));
        write("Latency Avg    : " + format(avgLatency));
        write("Overhead Ratio : " + format(overhead));

        write("");
        write("REWARD DISTRIBUTION");
        write("----------------------------");

        double total = 0;
        double max = 0;
        double min = Double.MAX_VALUE;

        for (Integer host : rewards.keySet()) {

            double reward = rewards.get(host);

            total += reward;
            max = Math.max(max, reward);
            min = Math.min(min, reward);

            write("Node " + host + " : " + format(reward));
        }

        double avgReward = rewards.isEmpty() ? 0 : total / rewards.size();

        /*
         * Jain Fairness Index
         */

        double sum = 0;
        double sumSq = 0;

        for (double r : rewards.values()) {
            sum += r;
            sumSq += r * r;
        }

        double fairness = 0;

        if (sumSq != 0) {
            fairness = (sum * sum) /
                    (rewards.size() * sumSq);
        }

        write("");
        write("SUMMARY");
        write("----------------------------");
        write("Total Reward : " + format(total));
        write("Avg Reward   : " + format(avgReward));
        write("Max Reward   : " + format(max));
        write("Min Reward   : " + format(min == Double.MAX_VALUE ? 0 : min));
        write("Fairness     : " + format(fairness));

        write("");
        write("BLOCKCHAIN VALIDATION");
        write("----------------------------");
        write("Validation Success : " + Blockchain.Validator.successCount);
        write("Validation Failed  : " + Blockchain.Validator.failCount);

        super.done();
    }

    private double avg(List<Double> list) {
        if (list.isEmpty())
            return 0;

        double sum = 0;

        for (double d : list) {
            sum += d;
        }

        return sum / list.size();
    }
}