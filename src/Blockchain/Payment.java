package Blockchain;

import java.security.*;

/**
 * Class Payment merepresentasikan transaksi
 * antara dua node pada jaringan DTN.
 * 
 * Setiap hop forwarding akan menghasilkan
 * satu objek Payment.
 */
public class Payment {

    /**
     * Pengirim transaksi
     */
    private PublicKey sender;

    /**
     * Penerima transaksi (relay berikutnya)
     */
    private PublicKey receiver;

    /**
     * Nilai reward yang akan diterima relay
     */
    private double value;

    /**
     * Hash transaksi sebelumnya (untuk chain)
     */
    private String prevHash;

    /**
     * EncR1 = bukti bahwa message berasal dari sender
     */
    private String encR1;

    /**
     * EncAck = acknowledgement dari relay
     */
    private String encAck;

    /**
     * EncR2 = bukti bahwa receiver menerima message
     */
    private String encR2;

    /**
     * Signature digital transaksi
     */
    private byte[] signature;

    /**
     * Constructor transaksi payment
     */
    public Payment(PublicKey sender,
            PublicKey receiver,
            double value,
            String prevHash) {

        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
        this.prevHash = prevHash;
    }

    /**
     * Menandatangani transaksi dengan private key
     * milik sender.
     */
    public void sign(PrivateKey privateKey) {

        String data = sender.toString()
                + receiver.toString()
                + value;

        signature = SecureTransaction.sign(privateKey,
                data);
    }

    /**
     * Verifikasi apakah signature transaksi valid.
     * Biasanya dilakukan oleh miner.
     */
    public boolean verify() {

        String data = sender.toString()
                + receiver.toString()
                + value;

        return SecureTransaction.verify(sender,
                data,
                signature);
    }

    /* ===== Setter Proof ===== */

    public void setEncR1(String r) {
        encR1 = r;
    }

    public void setEncAck(String a) {
        encAck = a;
    }

    public void setEncR2(String r) {
        encR2 = r;
    }

    /* ===== Getter ===== */

    public String getEncR1() {
        return encR1;
    }

    public String getEncAck() {
        return encAck;
    }

    public String getEncR2() {
        return encR2;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public double getValue() {
        return value;
    }

    public String getPrevTxHash() {
        return prevHash;
    }

    public String getTxHash() {

        String data = SecureTransaction.keyToString(sender)
                + SecureTransaction.keyToString(receiver)
                + value
                + (prevHash == null ? "" : prevHash);

        return SecureTransaction.sha256(data);
    }

    public boolean verifyACK(PublicKey receiverKey) {

        if (encAck == null)
            return false;

        return true;
    }
}