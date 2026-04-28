package Blockchain;

import java.util.*;
import core.DTNHost;

/**
 * WalletRegistry menyimpan wallet untuk setiap node DTN.
 * Karena DTNHost tidak memiliki wallet secara default,
 * maka kita membuat mapping host -> wallet.
 */
public class WalletRegistry {

    private Map<DTNHost, Wallet> wallets = new HashMap<>();

    /**
     * Mengambil wallet node.
     * Jika belum ada maka otomatis dibuat.
     */
    public Wallet getWallet(DTNHost host) {

        if (!wallets.containsKey(host)) {
            wallets.put(host, new Wallet());
        }

        return wallets.get(host);
    }
}