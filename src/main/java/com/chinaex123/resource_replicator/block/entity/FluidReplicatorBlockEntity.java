package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class FluidReplicatorBlockEntity extends BlockEntity {
    private static final int INPUT_TANK_CAPACITY = 1000;

    // 输入储罐数据
    private FluidResource inputResource = FluidResource.EMPTY;
    private long inputAmount = 0;

    // 输出储罐数据
    private FluidResource outputResource = FluidResource.EMPTY;
    private long outputAmount = 0;
    private long outputCapacity;

    // 判断是否是玩家插入（通过检查调用栈）
    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("FluidReplicatorBlock") &&
                    element.getMethodName().contains("useItemOn")) {
                return true;
            }
        }

        return false;
    }

    private int tickCounter = 0;
    public FluidReplicatorTier tier = FluidReplicatorTier.FLUID_TIER_1;
    private long energyStored;
    private long energyCapacity;
    private long energyConsumption;

    {
        updateEnergyStats();
        updateOutputCapacity();
        energyStored = 0;
    }

    private void updateEnergyStats() {
        switch (tier) {
            case FLUID_TIER_1:
                energyCapacity = ServerConfig.getFluidTier1EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier1EnergyConsumption();
                break;
            case FLUID_TIER_2:
                energyCapacity = ServerConfig.getFluidTier2EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier2EnergyConsumption();
                break;
            case FLUID_TIER_3:
                energyCapacity = ServerConfig.getFluidTier3EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier3EnergyConsumption();
                break;
            case FLUID_TIER_4:
                energyCapacity = ServerConfig.getFluidTier4EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier4EnergyConsumption();
                break;
            case FLUID_TIER_5:
                energyCapacity = ServerConfig.getFluidTier5EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier5EnergyConsumption();
                break;
        }
    }

    private void updateOutputCapacity() {
        this.outputCapacity = switch (tier) {
            case FLUID_TIER_1 -> ServerConfig.getFluidTier1OutputTankCapacity();
            case FLUID_TIER_2 -> ServerConfig.getFluidTier2OutputTankCapacity();
            case FLUID_TIER_3 -> ServerConfig.getFluidTier3OutputTankCapacity();
            case FLUID_TIER_4 -> ServerConfig.getFluidTier4OutputTankCapacity();
            case FLUID_TIER_5 -> ServerConfig.getFluidTier5OutputTankCapacity();
        };
    }

    /**
     * 使用新 Transfer API 的能量处理器
     */
    private final EnergyHandler energyHandler = new EnergyHandler() {
        @Override
        public int insert(int maxReceive, TransactionContext transaction) {
            int canReceive = (int) Math.min(maxReceive, energyCapacity - energyStored);
            if (canReceive <= 0) {
                return 0;
            }
            energyStored += canReceive;
            markUpdated();
            return canReceive;
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            return 0; // 不支持能量提取
        }

        @Override
        public long getAmountAsLong() {
            return energyStored;
        }

        @Override
        public long getCapacityAsLong() {
            return energyCapacity;
        }
    };

    /**
     * 自定义的流体处理器，支持清空操作和输入罐保护
     */
    private static class ClearableFluidHandler extends net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler {
        private boolean allowClearing = false;
        private final int inputTankCapacity;
        
        public ClearableFluidHandler(int size, int capacity, int inputTankCapacity) {
            super(size, capacity);
            this.inputTankCapacity = inputTankCapacity;
        }
        
        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            // 如果不是在清空模式下，只允许从输出罐（槽位 1）抽取流体
            if (!allowClearing && slot != 1) {
                return 0;
            }
            return super.extract(slot, resource, amount, transaction);
        }
        
        /**
         * 清空所有流体
         */
        public void clearAll() {
            allowClearing = true;
            try {
                // 清空输入罐
                FluidResource inputResource = getResource(0);
                long inputAmount = getAmountAsLong(0);
                if (!inputResource.isEmpty() && inputAmount > 0) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int extracted = extract(0, inputResource, (int) inputAmount, tx);
                        if (extracted > 0) {
                            tx.commit();
                        }
                    }
                }
                
                // 清空输出罐
                FluidResource outputResource = getResource(1);
                long outputAmount = getAmountAsLong(1);
                if (!outputResource.isEmpty() && outputAmount > 0) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int extracted = extract(1, outputResource, (int) outputAmount, tx);
                        if (extracted > 0) {
                            tx.commit();
                        }
                    }
                }
            } finally {
                allowClearing = false;
            }
        }
        
        @Override
        protected int getCapacity(int index, FluidResource resource) {
            if (index == 0) {
                return inputTankCapacity;
            } else if (index == 1) {
                return super.getCapacity(index, resource);
            }
            return super.getCapacity(index, resource);
        }
    }
    
    /**
     * 使用自定义的流体处理器
     */
    private final ClearableFluidHandler fluidHandler = new ClearableFluidHandler(2, (int) outputCapacity, INPUT_TANK_CAPACITY);

    public void clearAllFluids() {
        // 调用内部的 clearAll 方法
        fluidHandler.clearAll();
        markUpdated();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            return;
        }
        
        blockEntity.tickCounter = 0;

        // 从 fluidHandler 获取输入罐的流体
        FluidResource inputResource = (FluidResource) blockEntity.fluidHandler.getResource(0);
        long inputAmount = blockEntity.fluidHandler.getAmountAsLong(0);
        
        if (inputResource.isEmpty() || inputAmount == 0) {
            return;
        }

        // 根据流体类型确定输出量（水和岩浆使用特殊倍率）
        int actualOutput = blockEntity.tier.getActualOutputAmount(inputResource.toStack((int) inputAmount));
        
        long energyPer1000MB = blockEntity.energyConsumption;
        long energyNeeded = (actualOutput * energyPer1000MB) / 1000;
        if (energyNeeded < 1 && actualOutput > 0) {
            energyNeeded = 1;
        }

        if (blockEntity.energyStored < energyNeeded) {
            return;
        }

        int remainingOutput = actualOutput;
        boolean hasUpdated = false;
        int totalOutput = 0;

        boolean autoOutputEnabled = ServerConfig.isFluidReplicatorAutoOutputEnabled();

        if (autoOutputEnabled) {
            Direction outputDirection = ServerConfig.getFluidReplicatorAutoOutputDirection();
            BlockPos neighborPos = pos.relative(outputDirection);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!(neighborState.getBlock() instanceof FluidReplicatorBlock)) {
                var fluidCapability = Capabilities.Fluid.BLOCK;
                ResourceHandler<FluidResource> handler = level.getCapability(fluidCapability, neighborPos, neighborState, level.getBlockEntity(neighborPos), outputDirection.getOpposite());

                if (handler != null) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        long filled = handler.insert(inputResource, remainingOutput, transaction);
                        
                        if (filled > 0) {
                            transaction.commit();
                            totalOutput += (int) filled;
                            remainingOutput -= (int) filled;
                            
                            // 从输入罐提取相应数量的流体
                            try (var extractTx = Transaction.openRoot()) {
                                blockEntity.fluidHandler.extract(0, inputResource, (int) filled, extractTx);
                                extractTx.commit();
                            }
                            
                            hasUpdated = true;
                        }
                    }
                }
            }
        }

        if (remainingOutput > 0) {
            // 将产生的流体"插入"到输出罐
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = (int) blockEntity.fluidHandler.insert(1, inputResource, remainingOutput, transaction);
                if (inserted > 0) {
                    transaction.commit();
                    totalOutput += inserted;
                    hasUpdated = true;
                }
            }
        }

        if (hasUpdated && totalOutput > 0) {
            long actualEnergyNeeded = (totalOutput * energyPer1000MB) / 1000;
            if (actualEnergyNeeded < 1) {
                actualEnergyNeeded = 1;
            }

            if (blockEntity.energyStored >= actualEnergyNeeded) {
                blockEntity.energyStored -= actualEnergyNeeded;
                blockEntity.markUpdated();
            }
        }
    }

    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_REPLICATOR.get(), pos, state);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        
        CompoundTag customData = new CompoundTag();
        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putLong("energyStored", energyStored);

        // 保存流体处理器的数据
        CompoundTag tanksTag = new CompoundTag();
        
        // 保存输入罐（槽位 0）
        FluidResource inputResource = (FluidResource) fluidHandler.getResource(0);
        long inputAmount = fluidHandler.getAmountAsLong(0);
        if (!inputResource.isEmpty() && inputAmount > 0) {
            CompoundTag inputTag = new CompoundTag();
            inputTag.putString("fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(inputResource.toStack(1).getFluid()).toString());
            inputTag.putInt("amount", (int) inputAmount);
            tanksTag.put("inputTank", inputTag);
        }
        
        // 保存输出罐（槽位 1）
        FluidResource outputResource = (FluidResource) fluidHandler.getResource(1);
        long outputAmount = fluidHandler.getAmountAsLong(1);
        if (!outputResource.isEmpty() && outputAmount > 0) {
            CompoundTag outputTag = new CompoundTag();
            outputTag.putString("fluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(outputResource.toStack(1).getFluid()).toString());
            outputTag.putInt("amount", (int) outputAmount);
            tanksTag.put("outputTank", outputTag);
        }
        
        customData.put("tanks", tanksTag);
        
        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        
        CompoundTag customData = (CompoundTag) input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());
        
        tickCounter = customData.getInt("tickCounter").orElse(0);
        if (customData.contains("tier")) {
            this.tier = FluidReplicatorTier.fromId(customData.getInt("tier").orElse(0));
        }
        if (customData.contains("energyStored")) {
            this.energyStored = customData.getLong("energyStored").orElse(0L);
        }

        CompoundTag tanksTag = customData.getCompound("tanks").orElse(new CompoundTag());
        
        // 加载输入罐
        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            String fluidName = inputTag.getString("fluid").orElse("");
            int amount = inputTag.getInt("amount").orElse(0);
            
            if (!fluidName.isEmpty() && amount > 0) {
                net.minecraft.world.level.material.Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.tryParse(fluidName));
                if (fluid != null && !fluid.equals(net.minecraft.world.level.material.Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    try (Transaction tx = Transaction.openRoot()) {
                        fluidHandler.insert(0, resource, amount, tx);
                        tx.commit();
                    }
                }
            }
        }
        
        // 加载输出罐
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            String fluidName = outputTag.getString("fluid").orElse("");
            int amount = outputTag.getInt("amount").orElse(0);
            
            if (!fluidName.isEmpty() && amount > 0) {
                net.minecraft.world.level.material.Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.tryParse(fluidName));
                if (fluid != null && !fluid.equals(net.minecraft.world.level.material.Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    try (Transaction tx = Transaction.openRoot()) {
                        fluidHandler.insert(1, resource, amount, tx);
                        tx.commit();
                    }
                }
            }
        }

        updateEnergyStats();
        updateOutputCapacity();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putLong("energyStored", energyStored);

        CompoundTag tanksTag = new CompoundTag();
        
        if (!inputResource.isEmpty() && inputAmount > 0) {
            CompoundTag inputTag = new CompoundTag();
            FluidStack stack = inputResource.toStack((int) inputAmount);
            inputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            inputTag.putInt("amount", stack.getAmount());
            tanksTag.put("inputTank", inputTag);
        }
        
        if (!outputResource.isEmpty() && outputAmount > 0) {
            CompoundTag outputTag = new CompoundTag();
            FluidStack stack = outputResource.toStack((int) outputAmount);
            outputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            outputTag.putInt("amount", stack.getAmount());
            tanksTag.put("outputTank", outputTag);
        }
        
        tag.put("tanks", tanksTag);

        return tag;
    }

    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputCapacity();
    }

    public FluidStack getInputFluid() {
        FluidResource resource = (FluidResource) fluidHandler.getResource(0);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        long amount = fluidHandler.getAmountAsLong(0);
        return resource.toStack((int) amount);
    }

    public FluidStack getOutputFluid() {
        FluidResource resource = (FluidResource) fluidHandler.getResource(1);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        long amount = fluidHandler.getAmountAsLong(1);
        return resource.toStack((int) amount);
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            if (!level.isClientSide()) {
                var packet = ClientboundBlockEntityDataPacket.create(this);
                var players = level.players();
                for (var player : players) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) {
                            serverPlayer.connection.send(packet);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    public net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getMaxEnergyStored() {
        return energyCapacity;
    }
}
