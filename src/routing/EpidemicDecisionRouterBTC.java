package routing;

import core.*;
import Blockchain.*;

import java.util.*;

public class EpidemicDecisionRouterBTC implements RoutingDecisionEngine {

    // =============================
    // SETTINGS
    // =============================
    public static final String NS = "EpidemicDecisionRouterBTC";
    public static final String ALPHA = "alpha";
    public static final String BETA = "beta";
    public static final String MAX_HOP = "maxHop";
    public static final String SELFISH = "selfishThreshold";

    private double alpha;
    private double beta;
    private int maxHop;
    private double selfishThreshold;

    private PaymentChain paymentChain;
    private Map<String, Deposit> deposits;
    private WalletRegistry walletRegistry;

    // =============================
    public EpidemicDecisionRouterBTC(Settings s) {

        Settings settings = new Settings(NS);

        this.alpha = settings.contains(ALPHA)
                ? settings.getDouble(ALPHA)
                : 1.0;

        this.beta = settings.contains(BETA)
                ? settings.getDouble(BETA)
                : 0.5;

        this.maxHop = settings.contains(MAX_HOP)
                ? settings.getInt(MAX_HOP)
                : 10;

        this.selfishThreshold = settings.contains(SELFISH)
                ? settings.getDouble(SELFISH)
                : 0.3;

        // 🔥 INIT WALLET REGISTRY
        this.walletRegistry = new WalletRegistry();

        this.paymentChain = new PaymentChain(alpha, beta, maxHop);
        this.deposits = new HashMap<>();

        System.out.println("BTC Router loaded: alpha=" + alpha +
                " beta=" + beta +
                " maxHop=" + maxHop +
                " selfish=" + selfishThreshold);
    }

    protected EpidemicDecisionRouterBTC(EpidemicDecisionRouterBTC r) {
        this.alpha = r.alpha;
        this.beta = r.beta;
        this.maxHop = r.maxHop;
        this.selfishThreshold = r.selfishThreshold;

        this.walletRegistry = r.walletRegistry;

        this.paymentChain = new PaymentChain(alpha, beta, maxHop);
        this.deposits = new HashMap<>();
    }

    // =============================
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    // =============================
    // NEW MESSAGE (SOURCE)
    // =============================
    public boolean newMessage(Message m) {

        String msgId = m.getId();

        String R1 = UUID.randomUUID().toString();
        String R2 = UUID.randomUUID().toString();

        String h1 = SecureTransaction.applySha256(R1);
        String h2 = SecureTransaction.applySha256(R2);

        DTNHost sender = m.getFrom();
        DTNHost dest = m.getTo();

        Deposit deposit = new Deposit(
                walletRegistry.getWallet(sender).getPublicKey(),
                100,
                h1,
                h2,
                (long) SimClock.getTime() + 20000);

        deposits.put(msgId, deposit);
        paymentChain.registerDestination(msgId, dest);

        m.addProperty("R1", R1);
        m.addProperty("R2", R2);
        m.addProperty("deposit", deposit);

        List<DTNHost> path = new ArrayList<>();
        path.add(sender);

        m.addProperty("path", path);

        return true;
    }

    // =============================
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return true;
    }

    // =============================
    // 🔥 CORE LOGIC: INCENTIVE-AWARE
    // =============================
    public boolean shouldSendMessageToHost(Message m,
            DTNHost otherHost,
            DTNHost thisHost) {

        List<DTNHost> path = (List<DTNHost>) m.getProperty("path");

        // avoid loop
        if (path.contains(otherHost))
            return false;

        // hop limit (soft constraint)
        if (path.size() >= maxHop) {
            // fallback: allow if near destination
            if (!otherHost.equals(m.getTo()))
                return false;
        }

        // =========================
        // 🔥 INCENTIVE DECISION
        // =========================
        int hopIndex = path.size();

        double alphaPrime = alpha / (maxHop - 1);

        double reward;

        if (hopIndex == maxHop - 1) {
            reward = beta;
        } else {
            reward = (maxHop - 1 - hopIndex) * alphaPrime + beta;
        }

        // =========================
        // 🔥 SELFISH NODE BEHAVIOR
        // =========================
        if (reward < selfishThreshold) {
            return false; // node tidak mau forward
        }

        // =========================
        // ADD BLOCKCHAIN TRANSACTION
        // =========================
        paymentChain.addHop(
                m.getId(),
                thisHost,
                otherHost,
                (String) m.getProperty("R1"),
                (String) m.getProperty("R2"));

        // path.add(otherHost);
        List<DTNHost> newPath = new ArrayList<>(path);
        newPath.add(otherHost);
        m.updateProperty("path", newPath);

        return true;
    }

    // =============================
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    // =============================
    // DESTINATION EXECUTION
    // =============================
    public void update(DTNHost thisHost) {

        for (Message m : thisHost.getMessageCollection()) {

            if (isFinalDest(m, thisHost)) {

                String msgId = m.getId();

                Deposit deposit = (Deposit) m.getProperty("deposit");
                String R1 = (String) m.getProperty("R1");
                String R2 = (String) m.getProperty("R2");

                List<Payment> chain = paymentChain.getChain(msgId);

                if (chain == null)
                    continue;

                System.out.println("✅ Delivered with blockchain validation");

                ExecutionEngine.execute(
                        deposit,
                        chain,
                        R1,
                        R2,
                        (long) SimClock.getTime());
            }
        }
    }

    // =============================
    public RoutingDecisionEngine replicate() {
        return new EpidemicDecisionRouterBTC(this);
    }

}
