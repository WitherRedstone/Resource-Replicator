package com.chinaex123.resource_replicator.block;

public enum ReplicatorTier {
    TIER_1(1, 20, 4),
    TIER_2(2, 15, 16),
    TIER_3(3, 10, 32),
    TIER_4(4, 5, 64),
    TIER_5(5, 1, 128);

    private final int id;
    private final int processSpeed;
    private final int outputAmount;

    ReplicatorTier(int id, int processSpeed, int outputAmount) {
        this.id = id;
        this.processSpeed = processSpeed;
        this.outputAmount = outputAmount;
    }

    public int getId() {
        return id;
    }

    public int getProcessSpeed() {
        return processSpeed;
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public static ReplicatorTier fromId(int id) {
        for (ReplicatorTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return TIER_1;
    }
}
