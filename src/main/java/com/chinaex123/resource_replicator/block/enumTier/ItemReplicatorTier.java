package com.chinaex123.resource_replicator.block.enumTier;

import com.chinaex123.resource_replicator.config.RRServerConfig;

public enum ItemReplicatorTier {
    ITEM_TIER_1(1),
    ITEM_TIER_2(2),
    ITEM_TIER_3(3),
    ITEM_TIER_4(4),
    ITEM_TIER_5(5);

    private final int id;
    private int processSpeed;
    private int outputAmount;

    ItemReplicatorTier(int id) {
        this.id = id;
        updateFromConfig();
    }

    // 从配置更新参数
    public void updateFromConfig() {
        switch (this) {
            case ITEM_TIER_1:
                this.processSpeed = RRServerConfig.ITEM_TIER1_OUTPUT_TIME.get();
                this.outputAmount = RRServerConfig.ITEM_TIER1_OUTPUT_AMOUNT.get();
                break;
            case ITEM_TIER_2:
                this.processSpeed = RRServerConfig.ITEM_TIER2_OUTPUT_TIME.get();
                this.outputAmount = RRServerConfig.ITEM_TIER2_OUTPUT_AMOUNT.get();
                break;
            case ITEM_TIER_3:
                this.processSpeed = RRServerConfig.ITEM_TIER3_OUTPUT_TIME.get();
                this.outputAmount = RRServerConfig.ITEM_TIER3_OUTPUT_AMOUNT.get();
                break;
            case ITEM_TIER_4:
                this.processSpeed = RRServerConfig.ITEM_TIER4_OUTPUT_TIME.get();
                this.outputAmount = RRServerConfig.ITEM_TIER4_OUTPUT_AMOUNT.get();
                break;
            case ITEM_TIER_5:
                this.processSpeed = RRServerConfig.ITEM_TIER5_OUTPUT_TIME.get();
                this.outputAmount = RRServerConfig.ITEM_TIER5_OUTPUT_AMOUNT.get();
                break;
        }
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

    // 当配置更改时重新加载
    public static void reloadAllFromConfig() {
        for (ItemReplicatorTier tier : values()) {
            tier.updateFromConfig();
        }
    }
}
