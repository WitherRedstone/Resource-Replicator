package com.chinaex123.resource_replicator.block.enumTier;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

// 参数：（id, 普通处理速度, 普通处理输出量, 水处理速度, 水处理输出量, 岩浆处理速度, 岩浆处理输出量）
public enum FluidReplicatorTier {
    FLUID_TIER_1(
            1,
            20, 1000,
            20, 1000,
            20, 10
    ),
    FLUID_TIER_2(
            2,
            15, 2500,
            15, 10000,
            15, 50
    ),
    FLUID_TIER_3(
            3,
            10, 5000,
            10, 100000,
            10, 100
    ),
    FLUID_TIER_4(
            4,
            5, 10000,
            5, 1000000,
            5, 500
    ),
    FLUID_TIER_5(
            5,
            1, 25000,
            1, 10000000,
            1, 1000
    );

    private final int id;
    private final int normalProcessSpeed;
    private final int normalOutputAmount;
    private final int waterProcessSpeed;
    private final int waterOutputAmount;
    private final int lavaProcessSpeed;
    private final int lavaOutputAmount;

    FluidReplicatorTier(int id, int normalSpeed, int normalOutput,
                        int waterSpeed, int waterOutput,
                        int lavaSpeed, int lavaOutput) {
        this.id = id;
        this.normalProcessSpeed = normalSpeed;
        this.normalOutputAmount = normalOutput;
        this.waterProcessSpeed = waterSpeed;
        this.waterOutputAmount = waterOutput;
        this.lavaProcessSpeed = lavaSpeed;
        this.lavaOutputAmount = lavaOutput;
    }

    public int getId() {
        return id;
    }

    public int getNormalProcessSpeed() {
        return normalProcessSpeed;
    }

    public int getNormalOutputAmount() {
        return normalOutputAmount;
    }

    /**
     * 获取实际处理速度（根据流体类型）
     */
    public int getActualProcessSpeed(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return normalProcessSpeed;
        }

        if (fluidStack.getFluid() == Fluids.WATER) {
            return waterProcessSpeed;
        }

        if (fluidStack.getFluid() == Fluids.LAVA) {
            return lavaProcessSpeed;
        }

        return normalProcessSpeed;
    }

    /**
     * 获取实际输出量（根据流体类型）
     */
    public int getActualOutputAmount(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return normalOutputAmount;
        }

        if (fluidStack.getFluid() == Fluids.WATER) {
            return waterOutputAmount;
        }

        if (fluidStack.getFluid() == Fluids.LAVA) {
            return lavaOutputAmount;
        }

        return normalOutputAmount;
    }

    /**
     * 检查是否可以复制指定的流体
     */
    public boolean canReplicateFluid(FluidStack fluidStack) {
        return !fluidStack.isEmpty();
    }

    /**
     * 根据 ID 获取对应的等级
     */
    public static FluidReplicatorTier fromId(int id) {
        for (FluidReplicatorTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return FLUID_TIER_1;
    }
}
