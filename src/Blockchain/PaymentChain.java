package Blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;

public class PaymentChain {

    private Map<String, List<Payment>> chains = new HashMap<>();

    private double alpha;
    private double beta;
    private int maxHop;

    public PaymentChain(double alpha, double beta, int maxHop) {
        this.alpha = alpha;
        this.beta = beta;
        this.maxHop = maxHop;
    }

    public Payment addHop(String msgId,
            DTNHost from,
            DTNHost to,
            String R1,
            String R2) {

        List<Payment> chain = chains.getOrDefault(msgId, new ArrayList<>());

        Payment prev = chain.isEmpty()
                ? null
                : chain.get(chain.size() - 1);

        int hopIndex = chain.size();

        double value = calculateReward(hopIndex);

        Payment p = new Payment(
                from.getWallet().getPublicKey(),
                to.getWallet().getPublicKey(),
                value,
                prev == null ? null : prev.getPrevTxHash());

        // ========================
        // R1 hanya di hop pertama
        // ========================
        if (hopIndex == 0) {
            p.setEncR1(SecureTransaction.encrypt(
                    to.getWallet().getPublicKey(), R1));
        }

        // ========================
        // ACK semua hop
        // ========================
        String ack = "ACK_" + msgId + "_" + to;
        p.setEncAck(SecureTransaction.encrypt(
                to.getWallet().getPublicKey(), ack));

        // ========================
        // R2 hanya di receiver
        // ========================
        if (to.equals(getDestination(msgId))) {
            p.setEncR2(SecureTransaction.encrypt(
                    to.getWallet().getPublicKey(), R2));
        }

        p.sign(from.getWallet().getPrivateKey());

        chain.add(p);
        chains.put(msgId, chain);

        return p;
    }

    private double calculateReward(int hopIndex) {

        double alphaPrime = alpha / (maxHop - 1);

        if (hopIndex == maxHop - 1) {
            return beta;
        }

        return (maxHop - 1 - hopIndex) * alphaPrime + beta;
    }

    public List<Payment> getChain(String msgId) {
        return chains.get(msgId);
    }

    private DTNHost getDestination(String msgId) {
        // optional mapping
        return null;
    }
}
