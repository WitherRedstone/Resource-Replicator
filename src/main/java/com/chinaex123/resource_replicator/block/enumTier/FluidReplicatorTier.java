package com.chinaex123.resource_replicator.block.enumTier;

import com.chinaex123.resource_replicator.config.RRServerConfig;
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
                this.processSpeed = RRServerConfig.getFluidTier1OutputTime();
                this.outputAmount = RRServerConfig.getFluidTier1OutputAmount();
                this.waterOutputAmount = RRServerConfig.getFluidTier1WaterAmount();
                this.lavaOutputAmount = RRServerConfig.getFluidTier1LavaAmount();
                break;
            case FLUID_TIER_2:
                this.processSpeed = RRServerConfig.getFluidTier2OutputTime();
                this.outputAmount = RRServerConfig.getFluidTier2OutputAmount();
                this.waterOutputAmount = RRServerConfig.getFluidTier2WaterAmount();
                this.lavaOutputAmount = RRServerConfig.getFluidTier2LavaAmount();
                break;
            case FLUID_TIER_3:
                this.processSpeed = RRServerConfig.getFluidTier3OutputTime();
                this.outputAmount = RRServerConfig.getFluidTier3OutputAmount();
                this.waterOutputAmount = RRServerConfig.getFluidTier3WaterAmount();
                this.lavaOutputAmount = RRServerConfig.getFluidTier3LavaAmount();
                break;
            case FLUID_TIER_4:
                this.processSpeed = RRServerConfig.getFluidTier4OutputTime();
                this.outputAmount = RRServerConfig.getFluidTier4OutputAmount();
                this.waterOutputAmount = RRServerConfig.getFluidTier4WaterAmount();
                this.lavaOutputAmount = RRServerConfig.getFluidTier4LavaAmount();
                break;
            case FLUID_TIER_5:
                this.processSpeed = RRServerConfig.getFluidTier5OutputTime();
                this.outputAmount = RRServerConfig.getFluidTier5OutputAmount();
                this.waterOutputAmount = RRServerConfig.getFluidTier5WaterAmount();
                this.lavaOutputAmount = RRServerConfig.getFluidTier5LavaAmount();
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
