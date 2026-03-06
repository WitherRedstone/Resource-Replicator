package com.chinaex123.resource_replicator.block.enumTier;

public enum ItemReplicatorTier {
    ITEM_TIER_1(1, 20, 4),
    ITEM_TIER_2(2, 15, 16),
    ITEM_TIER_3(3, 10, 32),
    ITEM_TIER_4(4, 5, 64),
    ITEM_TIER_5(5, 1, 128);

    private final int id;
    private final int processSpeed;
    private final int outputAmount;

    ItemReplicatorTier(int id, int processSpeed, int outputAmount) {
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

    public static ItemReplicatorTier fromId(int id) {
        for (ItemReplicatorTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return ITEM_TIER_1;
    }
}
