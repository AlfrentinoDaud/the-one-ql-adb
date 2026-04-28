package Blockchain;

import java.util.*;

/**
 * Class Blockchain mengelola seluruh block
 * dalam sistem reward DTN.
 * 
 * Fungsi utama:
 * - menyimpan block
 * - menambahkan block baru
 * - menjaga integritas chain
 */
public class Blockchain {

    /**
     * List seluruh block dalam chain
     */
    private List<Block> chain = new ArrayList<>();

    /**
     * Difficulty mining
     */
    private int difficulty = 3;

    /**
     * Constructor blockchain
     * otomatis membuat genesis block
     */
    public Blockchain() {

        chain.add(createGenesisBlock());
    }

    /**
     * Genesis block adalah block pertama
     * dalam blockchain
     */
    private Block createGenesisBlock() {

        return new Block(
                "0",
                new ArrayList<>());
    }

    /**
     * Mengambil block terakhir
     */
    public Block getLatestBlock() {

        return chain.get(chain.size() - 1);
    }

    /**
     * Menambahkan block baru ke chain
     */
    public void addBlock(Block block) {

        block.mine(difficulty);

        chain.add(block);
    }

    /**
     * Mengambil jumlah block dalam chain
     */
    public int size() {
        return chain.size();
    }
}