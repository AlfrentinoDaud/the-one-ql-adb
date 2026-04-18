package Blockchain;

import java.security.PublicKey;

public class Deposit {

    private PublicKey owner; // sender
    private double amount;

    private String h1; // H(R1)
    private String h2; // H(R2)

    private long timelock; // expiry time

    public Deposit(PublicKey owner, double amount,
            String h1, String h2, long timelock) {

        this.owner = owner;
        this.amount = amount;
        this.h1 = h1;
        this.h2 = h2;
        this.timelock = timelock;
    }

    public boolean isExpired(long currentTime) {
        return currentTime > timelock;
    }

    public boolean canRefund(long now) {
        return now > timelock;
    }

    public String getH1() {
        return h1;
    }

    public String getH2() {
        return h2;
    }

    public double getAmount() {
        return amount;
    }

    public PublicKey getOwner() {
        return owner;
    }
}
