package Blockchain;

import java.util.*;

/**
 * Class RewardDistributor bertugas membagikan
 * reward kepada relay node dan receiver.
 * 
 * Reward diberikan setelah miner memverifikasi
 * payment chain.
 */
public class RewardDistributor {

    /**
     * Fungsi untuk mendistribusikan reward
     * kepada node relay dan receiver.
     *
     * @param chain   payment chain dari message
     * @param wallets daftar wallet seluruh node
     */
    public void distribute(List<Payment> chain,
            Map<String, Wallet> wallets) {

        /*
         * Loop semua transaksi relay
         */
        for (Payment payment : chain) {

            /*
             * Ambil wallet penerima reward
             */
            Wallet wallet = wallets.get(
                    payment.getReceiver().toString());

            /*
             * Tambahkan reward ke saldo wallet
             */
            if (wallet != null) {

                wallet.addBalance(
                        payment.getValue());
            }
        }
    }
}