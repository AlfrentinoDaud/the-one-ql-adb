package Blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;

public class Payment {

    private PublicKey sender;
    private PublicKey receiver;

    private double value;

    private String prevTxHash;

    // proof
    private String encR1;
    private String encAck;
    private String encR2;

    private byte[] signature;

    public Payment(PublicKey sender, PublicKey receiver,
            double value, String prevTxHash) {

        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
        this.prevTxHash = prevTxHash;
    }

    public void sign(PrivateKey sk) {
        String data = sender.toString() + receiver.toString() + value;
        signature = SecureTransaction.applyECDSASig(sk, data);
    }

    public boolean verify() {
        String data = sender.toString() + receiver.toString() + value;
        return SecureTransaction.verifyECDSASig(sender, data, signature);
    }

    // ===== setters =====
    public void setEncR1(String v) {
        encR1 = v;
    }

    public void setEncAck(String v) {
        encAck = v;
    }

    public void setEncR2(String v) {
        encR2 = v;
    }

    // ===== getters =====
    public String getEncR1() {
        return encR1;
    }

    public String getEncAck() {
        return encAck;
    }

    public String getEncR2() {
        return encR2;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public double getValue() {
        return value;
    }

    public String getPrevTxHash() {
        return prevTxHash;
    }
}
