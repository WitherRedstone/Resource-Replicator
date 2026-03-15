package com.chinaex123.resource_replicator.block.enumTier;

import com.chinaex123.resource_replicator.config.ServerConfig;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public enum FluidReplicatorTier {
    FLUID_TIER_1(1),
    FLUID_TIER_2(2),
    FLUID_TIER_3(3),
    FLUID_TIER_4(4),
    FLUID_TIER_5(5);

    private final int id;
    private int processSpeed;
    private int outputAmount;
    private int waterOutputAmount;
    private int lavaOutputAmount;

    FluidReplicatorTier(int id) {
        this.id = id;
        updateFromConfig();
    }

    // 从配置更新参数
    public void updateFromConfig() {
        switch (this) {
            case FLUID_TIER_1:
                this.processSpeed = ServerConfig.getFluidTier1OutputTime();
                this.outputAmount = ServerConfig.getFluidTier1OutputAmount();
                this.waterOutputAmount = ServerConfig.getFluidTier1WaterAmount();
                this.lavaOutputAmount = ServerConfig.getFluidTier1LavaAmount();
                break;
            case FLUID_TIER_2:
                this.processSpeed = ServerConfig.getFluidTier2OutputTime();
                this.outputAmount = ServerConfig.getFluidTier2OutputAmount();
                this.waterOutputAmount = ServerConfig.getFluidTier2WaterAmount();
                this.lavaOutputAmount = ServerConfig.getFluidTier2LavaAmount();
                break;
            case FLUID_TIER_3:
                this.processSpeed = ServerConfig.getFluidTier3OutputTime();
                this.outputAmount = ServerConfig.getFluidTier3OutputAmount();
                this.waterOutputAmount = ServerConfig.getFluidTier3WaterAmount();
                this.lavaOutputAmount = ServerConfig.getFluidTier3LavaAmount();
                break;
            case FLUID_TIER_4:
                this.processSpeed = ServerConfig.getFluidTier4OutputTime();
                this.outputAmount = ServerConfig.getFluidTier4OutputAmount();
                this.waterOutputAmount = ServerConfig.getFluidTier4WaterAmount();
                this.lavaOutputAmount = ServerConfig.getFluidTier4LavaAmount();
                break;
            case FLUID_TIER_5:
                this.processSpeed = ServerConfig.getFluidTier5OutputTime();
                this.outputAmount = ServerConfig.getFluidTier5OutputAmount();
                this.waterOutputAmount = ServerConfig.getFluidTier5WaterAmount();
                this.lavaOutputAmount = ServerConfig.getFluidTier5LavaAmount();
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

    public static FluidReplicatorTier fromId(int id) {
        for (FluidReplicatorTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return FLUID_TIER_1;
    }

    // 当配置更改时重新加载
    public static void reloadAllFromConfig() {
        for (FluidReplicatorTier tier : values()) {
            tier.updateFromConfig();
        }
    }

    public int getActualProcessSpeed(FluidStack fluid) {
        return processSpeed;
    }

    public int getActualOutputAmount(FluidStack fluid) {
        if (fluid.getFluid() == Fluids.WATER) {
            return waterOutputAmount;
        } else if (fluid.getFluid() == Fluids.LAVA) {
            return lavaOutputAmount;
        }
        return outputAmount;
    }

    public boolean canReplicateFluid(FluidStack fluid) {
        return true;
    }
}
