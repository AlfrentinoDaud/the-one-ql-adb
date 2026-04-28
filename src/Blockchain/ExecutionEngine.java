package Blockchain;

import java.util.List;

public class ExecutionEngine {

    public static int totalSuccess = 0;
    public static int totalFail = 0;
    public static int totalRefund = 0;
    public static int totalPaymentTx = 0;
    public static double totalReward = 0;

    public static void execute(
            Deposit deposit,
            List<Payment> chain,
            String R1,
            String R2,
            long now) {

        boolean valid = Validator.validateMultiHop(
                deposit, chain, R1, R2, now);

        /*
         * SUCCESS → payout
         */
        if (valid) {

            totalSuccess++;

            double paid = 0;

            System.out.println("SUCCESS: payout");

            for (Payment p : chain) {

                totalPaymentTx++;
                totalReward += p.getValue();
                paid += p.getValue();

                System.out.println(
                        "Transfer "
                                + p.getValue()
                                + " -> relay");
            }

            double remaining = deposit.getAmount() - paid;

            System.out.println(
                    "Remaining deposit = " + remaining);

            return;
        }

        /*
         * FAIL + expired → refund
         */
        if (deposit.canRefund(now)) {

            totalRefund++;

            System.out.println(
                    "REFUND -> sender : "
                            + deposit.getAmount());

            return;
        }

        /*
         * FAIL + belum expired
         */
        totalFail++;

        System.out.println(
                "FAIL but timelock active");
    }
}