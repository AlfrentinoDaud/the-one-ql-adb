package Blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Class Transaction dimana objek transaksi dibuat oleh para miner
 * 
 */
public class Transaction {

    /**
     * Pengirim uang. Pada real world seperti nomor rekening bank,
     * karena itu bertipe PublicKey agar lebih aman
     */
    private PublicKey sender;

    /**
     * Penerima uang, bertipe PublicKey seperti nomor rekening tujan.
     */
    private PublicKey receiver;

    /**
     * Berapa jumlah uang yang akan dikirim
     */
    private double amount;

    /**
     * Kapan transaksi ini dibuat
     */
    private long timestamp;

    /**
     * Hash dari transaksi, hanya sebagai identifier. Pada simulasi tidak
     * terlalu penting.
     */
    private String transactionHash;

    /**
     * Tanda tangan digital dari pengirim untuk memastikan keabsahan
     * transaksi
     */
    private byte[] signature;

    public Transaction(PublicKey sender, PublicKey receiver,
            double amount, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionHash = calculateHash();
    }

    /**
     * Hitung hash transaksi dari sender, receiver, amount, dan timestamp
     */
    private String calculateHash() {
        return SecureTransaction.applySha256(SecureTransaction.getStringFromKey(sender)
                + SecureTransaction.getStringFromKey(receiver)
                + amount + timestamp);
    }

    /**
     * Bikin tanda tangan digital untuk transaksi ini pake private key
     * 
     * @param privateKey - Kunci pribadi si pengirim
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = SecureTransaction.getStringFromKey(sender)
                + SecureTransaction.getStringFromKey(receiver)
                + amount;
        signature = SecureTransaction.applyECDSASig(privateKey, data);
    }

    /**
     * Ngecek apakah tanda tangan transaksi ini valid
     * 
     * @return true kalo tanda tangan cocok
     */
    public boolean verifySignature() {
        String data = SecureTransaction.getStringFromKey(sender)
                + SecureTransaction.getStringFromKey(receiver)
                + amount;
        return SecureTransaction.verifyECDSASig(sender, data, signature);
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
