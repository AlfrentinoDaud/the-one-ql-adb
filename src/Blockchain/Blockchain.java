package Blockchain;

import java.util.ArrayList;
import java.util.List;

/**
 * Blockchain digunakan sebagai rantai utama yang tersimpan di Internet
 */
public class Blockchain {

    /**
     * List berisi blok-blok yang telah ditambang
     */
    private List<Block> chain;

    /**
     * Target kesuitan para mining untuk menambang
     */
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
    }

    /**
     * Ambil block terakhir di chain. Kalo kosong, return block kosong
     * 
     * @return objek blok paling terakhir
     */
    public Block getLatestBlock() {
        if (chain.isEmpty()) {
            return new Block();
        }
        return chain.get(chain.size() - 1);
    }

    /**
     * Menambahkahkan blok baru ke blockchain
     * 
     * @param newBlock Blok yang akan ditambah
     */
    public void addBlock(Block newBlock) {
        chain.add(newBlock);
    }

    public int chainSize() {
        return chain.size();
    }
}
