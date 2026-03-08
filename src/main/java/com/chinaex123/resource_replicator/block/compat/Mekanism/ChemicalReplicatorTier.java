package com.chinaex123.resource_replicator.block.compat.Mekanism;

import com.chinaex123.resource_replicator.config.ServerConfig;

public enum ChemicalReplicatorTier {
    CHEMICAL_TIER_1(1),
    CHEMICAL_TIER_2(2),
    CHEMICAL_TIER_3(3),
    CHEMICAL_TIER_4(4),
    CHEMICAL_TIER_5(5);

    private final int id;
    private int processSpeed;
    private int outputAmount;

    ChemicalReplicatorTier(int id) {
        this.id = id;
        updateFromConfig();
    }

    /**
     * 从配置文件更新复制机参数
     */
    public void updateFromConfig() {
        switch (this) {
            case CHEMICAL_TIER_1:
                this.processSpeed = ServerConfig.getChemicalTier1OutputTime();
                this.outputAmount = ServerConfig.getChemicalTier1OutputAmount();
                break;
            case CHEMICAL_TIER_2:
                this.processSpeed = ServerConfig.getChemicalTier2OutputTime();
                this.outputAmount = ServerConfig.getChemicalTier2OutputAmount();
                break;
            case CHEMICAL_TIER_3:
                this.processSpeed = ServerConfig.getChemicalTier3OutputTime();
                this.outputAmount = ServerConfig.getChemicalTier3OutputAmount();
                break;
            case CHEMICAL_TIER_4:
                this.processSpeed = ServerConfig.getChemicalTier4OutputTime();
                this.outputAmount = ServerConfig.getChemicalTier4OutputAmount();
                break;
            case CHEMICAL_TIER_5:
                this.processSpeed = ServerConfig.getChemicalTier5OutputTime();
                this.outputAmount = ServerConfig.getChemicalTier5OutputAmount();
                break;
        }
    }

    // 用于 NBT 数据序列化
    public int getId() {
        return id;
    }

    // 用于控制复制速度
    public int getProcessSpeed() {
        return processSpeed;
    }

    // 用于控制复制产量
    public int getOutputAmount() {
        return outputAmount;
    }

    /**
     * 根据 ID 获取对应的复制机等级
     * <p>
     * 此方法用于将整数 ID 映射到对应的化学品复制机等级枚举值。
     * 通过遍历所有枚举值来匹配指定的 ID，如果找不到匹配的 ID 则返回默认的 T1 等级。
     * </p>
     * 
     * @param id 复制机等级 ID（有效范围：1-5）
     * @return ChemicalReplicatorTier 匹配的复制机等级枚举值，如果 ID 无效则返回 CHEMICAL_TIER_1
     */
    public static ChemicalReplicatorTier fromId(int id) {
        for (ChemicalReplicatorTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return CHEMICAL_TIER_1;
    }

    /**
     * 重新加载所有等级的配置参数
     * <p>
     * 此方法在服务器启动或配置重新加载时被调用，用于遍历所有等级枚举并调用各自的 updateFromConfig() 方法，
     * 确保所有复制机等级的处理速度和输出量都与配置文件保持一致。
     * </p>
     */
    public static void reloadAllFromConfig() {
        for (ChemicalReplicatorTier tier : values()) {
            tier.updateFromConfig();
        }
    }

    /**
     * 获取实际处理速度
     * <p>
     * 此方法返回当前等级复制机的处理速度（tick 数）。
     * 该方法接受一个 Object 参数以便将来扩展为根据化学品种类调整速度的功能，
     * 但目前实现中所有化学品的处理速度相同。
     * </p>
     * 
     * @param chemical 化学品对象（目前未使用，保留用于未来扩展）
     * @return int 处理速度（tick 数），数值越大表示处理越慢
     */
    public int getActualProcessSpeed(Object chemical) {
        return processSpeed;
    }

    /**
     * 获取实际输出数量
     * <p>
     * 此方法返回当前等级复制机每次操作产生的化学品数量（单位：mB）。
     * 该方法接受一个 Object 参数以便将来扩展为根据化学品种类调整产量的功能，
     * 但目前实现中所有化学品的输出量相同。
     * </p>
     * 
     * @param chemical 化学品对象（目前未使用，保留用于未来扩展）
     * @return int 输出数量（单位：mB）
     */
    public int getActualOutputAmount(Object chemical) {
        return outputAmount;
    }

    /**
     * 检查是否可以复制该化学品
     * <p>
     * 此方法用于判断当前等级的复制机是否能够复制指定的化学品。
     * 当前实现默认允许复制所有化学品，未来可以扩展为限制低等级机器无法复制某些高级化学品。
     * </p>
     * 
     * @param chemical 待检查的化学品对象
     * @return boolean 始终返回 true，表示所有等级的复制机都可以复制任何化学品
     */
    public boolean canReplicateChemical(Object chemical) {
        return true;
    }
}
