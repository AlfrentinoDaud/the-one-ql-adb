package Blockchain;

import java.util.*;
import core.DTNHost;

/**
 * Class PaymentChain mengelola seluruh transaksi
 * yang terjadi selama message melewati beberapa hop.
 * 
 * Setiap message memiliki satu chain transaksi.
 */
public class PaymentChain {

    /**
     * Map untuk menyimpan chain transaksi
     * key = messageID
     */
    private Map<String, List<Payment>> chains = new HashMap<>();

    /**
     * Registry untuk mengetahui node tujuan message
     */
    private DestinationRegistry registry;

    /**
     * Reward relay
     */
    private double alpha;

    /**
     * Reward receiver
     */
    private double beta;

    /**
     * Jumlah hop maksimum
     */
    private int maxHop;

    private WalletRegistry walletRegistry;

    /**
     * Constructor PaymentChain
     */
    public PaymentChain(double alpha,
            double beta,
            int maxHop,
            DestinationRegistry reg,
            WalletRegistry walletRegistry) {

        this.alpha = alpha;
        this.beta = beta;
        this.maxHop = maxHop;
        this.registry = reg;
        this.walletRegistry = walletRegistry;
    }

    public PaymentChain(double alpha,
            double beta,
            int maxHop) {

        this.alpha = alpha;
        this.beta = beta;
        this.maxHop = maxHop;

        this.registry = new DestinationRegistry();
        this.walletRegistry = new WalletRegistry();
    }

    /**
     * Fungsi dipanggil setiap kali message
     * berpindah dari satu node ke node lain.
     *
     * @param msgId ID message
     * @param from  node pengirim
     * @param to    node penerima
     */
    public Payment addHop(String msgId,
            DTNHost from,
            DTNHost to,
            String R1,
            String R2) {

        /*
         * Ambil chain transaksi message
         */
        List<Payment> chain = chains.computeIfAbsent(
                msgId,
                k -> new ArrayList<>());

        int hop = chain.size();

        /*
         * Hitung reward relay
         */
        double value = reward(hop);

        String prevHash;

        if (chain.isEmpty()) {
            prevHash = "GENESIS";
        } else {
            prevHash = chain.get(chain.size() - 1).getTxHash();
        }

        Payment payment = new Payment(
                walletRegistry.getWallet(from).getPublicKey(),
                walletRegistry.getWallet(to).getPublicKey(),
                value,
                prevHash);

        /*
         * Jika hop pertama
         * maka sertakan bukti R1
         */
        if (hop == 0) {

            payment.setEncR1(
                    SecureTransaction.commutativeEncrypt(
                            walletRegistry.getWallet(to).getPublicKey(),
                            R1));
        }

        /*
         * ACK dari relay
         */
        String ack = "ACK_" + msgId + "_" + to;

        payment.setEncAck(
                SecureTransaction
                        .commutativeEncrypt(
                                walletRegistry.getWallet(to).getPublicKey(),
                                ack));

        /*
         * Jika node ini adalah destination
         * maka tambahkan proof R2
         */
        DTNHost dest = registry.get(msgId);

        if (dest != null &&
                to.equals(dest)) {

            payment.setEncR2(
                    SecureTransaction
                            .commutativeEncrypt(
                                    walletRegistry.getWallet(to).getPublicKey(),
                                    R2));
        }

        /*
         * Sender menandatangani transaksi
         */
        payment.sign(
                walletRegistry
                        .getWallet(from)
                        .getPrivateKey());

        chain.add(payment);

        chains.put(msgId, chain);

        return payment;
    }

    /**
     * Rumus reward sesuai paper
     */
    private double reward(int hop) {

        if (hop >= maxHop)
            return beta;

        double alphaPrime = alpha / (maxHop - 1);

        if (hop == maxHop - 1)
            return beta;

        return (maxHop - 1 - hop) * alphaPrime + beta;
        // double alphaPrime = alpha / (maxHop - 1);

        // if (hop == maxHop - 1)
        // return beta;

        // return (maxHop - 1 - hop)
        // * alphaPrime
        // + beta;
    }

    /**
     * Mengambil seluruh chain transaksi
     * untuk sebuah message
     */
    public List<Payment> getChain(String msgId) {
        return chains.get(msgId);
    }

    public void registerDestination(String msgId, DTNHost dest) {

        if (registry != null) {
            registry.register(msgId, dest);
        }
    }
}