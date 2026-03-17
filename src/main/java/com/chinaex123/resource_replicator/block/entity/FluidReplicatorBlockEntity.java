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
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidReplicatorBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidReplicatorBlockEntity.class);
    private static final int INPUT_TANK_CAPACITY = 1000;
    
    private static final ThreadLocal<Boolean> IS_PLAYER_OPERATION = ThreadLocal.withInitial(() -> false);
    
    private static final ThreadLocal<Boolean> IS_INTERNAL_REPLICATION = ThreadLocal.withInitial(() -> false);

    private long outputCapacity;

    public static void setPlayerOperation(boolean isPlayer) {
        IS_PLAYER_OPERATION.set(isPlayer);
    }
    
    public static boolean isPlayerOperation() {
        return IS_PLAYER_OPERATION.get();
    }
    
    public static void setInternalReplication(boolean isInternal) {
        IS_INTERNAL_REPLICATION.set(isInternal);
    }
    
    public static boolean isInternalReplication() {
        return IS_INTERNAL_REPLICATION.get();
    }

    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("FluidReplicatorBlock")) {
                String methodName = element.getMethodName();
                if (methodName.contains("use") || methodName.contains("useItemOn")) {
                    return true;
                }
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

    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputCapacity();
    }

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
            return 0;
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

    private class ClearableFluidHandler extends FluidStacksResourceHandler {
        private boolean allowClearing = false;
        private final int inputTankCapacity;
        
        public ClearableFluidHandler(int size, int capacity, int inputTankCapacity) {
            super(size, capacity);
            this.inputTankCapacity = inputTankCapacity;
        }
        
        @Override
        public int insert(int slot, FluidResource resource, int maxAmount, TransactionContext transaction) {
            if (slot == 1) {
                if (!isInternalReplication()) {
                    return 0;
                }
                
                FluidResource currentResource = getResource(1);
                long currentAmount = getAmountAsLong(1);
                
                if (currentResource.isEmpty() || currentAmount == 0) {
                    set(1, resource, maxAmount);
                    markUpdated();
                    return maxAmount;
                } else if (currentResource.equals(resource)) {
                    int canAdd = Math.min((int) outputCapacity - (int)currentAmount, maxAmount);
                    if (canAdd > 0) {
                        set(1, resource, (int)(currentAmount + canAdd));
                        markUpdated();
                    }
                    return canAdd;
                }
                return 0;
            }
            
            if (slot != 0) {
                return 0;
            }
            
            boolean isPipeInsertion = !isPlayerOperation();
            
            if (isPipeInsertion && ServerConfig.isFluidReplicatorDestroyEnabled()) {
                return maxAmount;
            }
            
            if (isPipeInsertion) {
                return 0;
            }
            
            FluidResource currentResource = getResource(0);
            long currentAmount = getAmountAsLong(0);
            
            if (currentResource.isEmpty() || currentAmount == 0) {
                set(0, resource, maxAmount);
                markUpdated();
                return maxAmount;
            } else if (currentResource.equals(resource)) {
                int canAdd = Math.min(inputTankCapacity - (int)currentAmount, maxAmount);
                if (canAdd > 0) {
                    set(0, resource, (int)(currentAmount + canAdd));
                    markUpdated();
                }
                return canAdd;
            }
            
            return 0;
        }
        
        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (!allowClearing && slot != 1) {
                return 0;
            }
            return super.extract(slot, resource, amount, transaction);
        }
        
        public void clearAll() {
            allowClearing = true;
            try {
                FluidResource inputResource = getResource(0);
                long inputAmount = getAmountAsLong(0);
                if (!inputResource.isEmpty() && inputAmount > 0) {
                    set(0, FluidResource.EMPTY, 0);
                }
                
                FluidResource outputResource = getResource(1);
                long outputAmount = getAmountAsLong(1);
                if (!outputResource.isEmpty() && outputAmount > 0) {
                    set(1, FluidResource.EMPTY, 0);
                }
            } finally {
                allowClearing = false;
            }
        }
        
        @Override
        protected int getCapacity(int index, FluidResource resource) {
            if (index == 0) return inputTankCapacity;
            if (index == 1) return (int) outputCapacity;
            return super.getCapacity(index, resource);
        }
    }
    
    private final ClearableFluidHandler fluidHandler = new ClearableFluidHandler(2, (int) outputCapacity, INPUT_TANK_CAPACITY);

    public void clearAllFluids() {
        fluidHandler.clearAll();
        markUpdated();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            return;
        }
        
        blockEntity.tickCounter = 0;

        FluidResource inputResource = (FluidResource) blockEntity.fluidHandler.getResource(0);
        long inputAmount = blockEntity.fluidHandler.getAmountAsLong(0);
        
        if (inputResource.isEmpty() || inputAmount == 0) {
            return;
        }

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
                            
                            blockEntity.fluidHandler.extract(0, inputResource, (int) filled, transaction);
                            
                            hasUpdated = true;
                        }
                    }
                }
            }
        }

        if (remainingOutput > 0) {
            setInternalReplication(true);
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = (int) blockEntity.fluidHandler.insert(1, inputResource, remainingOutput, transaction);
                if (inserted > 0) {
                    transaction.commit();
                    totalOutput += inserted;
                    hasUpdated = true;
                }
            } finally {
                setInternalReplication(false);
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

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && level.isClientSide()) {
            markUpdated();
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

        CompoundTag tanksTag = new CompoundTag();
        
        FluidResource inputResource = (FluidResource) fluidHandler.getResource(0);
        long inputAmount = fluidHandler.getAmountAsLong(0);
        
        if (!inputResource.isEmpty() && inputAmount > 0) {
            CompoundTag inputTag = new CompoundTag();
            inputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(inputResource.toStack(1).getFluid()).toString());
            inputTag.putInt("amount", (int) inputAmount);
            tanksTag.put("inputTank", inputTag);
        }
        
        FluidResource outputResource = (FluidResource) fluidHandler.getResource(1);
        long outputAmount = fluidHandler.getAmountAsLong(1);
        
        if (!outputResource.isEmpty() && outputAmount > 0) {
            CompoundTag outputTag = new CompoundTag();
            outputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(outputResource.toStack(1).getFluid()).toString());
            outputTag.putInt("amount", (int) outputAmount);
            tanksTag.put("outputTank", outputTag);
        }
        
        customData.put("tanks", tanksTag);
        
        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putLong("energyStored", energyStored);

        CompoundTag tanksTag = new CompoundTag();
        
        FluidResource inputRes = (FluidResource) fluidHandler.getResource(0);
        long inputAmt = fluidHandler.getAmountAsLong(0);
        
        if (!inputRes.isEmpty() && inputAmt > 0) {
            CompoundTag inputTag = new CompoundTag();
            FluidStack stack = inputRes.toStack((int) inputAmt);
            inputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            inputTag.putInt("amount", stack.getAmount());
            tanksTag.put("inputTank", inputTag);
        }
        
        FluidResource outputRes = (FluidResource) fluidHandler.getResource(1);
        long outputAmt = fluidHandler.getAmountAsLong(1);
        
        if (!outputRes.isEmpty() && outputAmt > 0) {
            CompoundTag outputTag = new CompoundTag();
            FluidStack stack = outputRes.toStack((int) outputAmt);
            outputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            outputTag.putInt("amount", stack.getAmount());
            tanksTag.put("outputTank", outputTag);
        }
        
        tag.put("tanks", tanksTag);

        return tag;
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
        
        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            String fluidName = inputTag.getString("fluid").orElse("");
            int amount = inputTag.getInt("amount").orElse(0);
            
            if (!fluidName.isEmpty() && amount > 0) {
                net.minecraft.world.level.material.Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.tryParse(fluidName));
                if (fluid != null && !fluid.equals(net.minecraft.world.level.material.Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(0, resource, amount);
                }
            }
        }
        
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            String fluidName = outputTag.getString("fluid").orElse("");
            int amount = outputTag.getInt("amount").orElse(0);
            
            if (!fluidName.isEmpty() && amount > 0) {
                net.minecraft.world.level.material.Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.tryParse(fluidName));
                if (fluid != null && !fluid.equals(net.minecraft.world.level.material.Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(1, resource, amount);
                }
            }
        }

        updateEnergyStats();
        updateOutputCapacity();
    }

    public void loadFluidFromPacket(CompoundTag tag) {
        if (!getLevel().isClientSide()) {
            return;
        }
        
        CompoundTag tanksTag = tag.getCompound("tanks").orElse(new CompoundTag());
        
        clearFluidHandlerDirectly();
        
        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                net.minecraft.resources.Identifier.tryParse(inputTag.getString("fluid").orElse(""))
            );
            int amount = inputTag.getInt("amount").orElse(0);
            
            if (fluid.isPresent() && amount > 0) {
                var resource = net.neoforged.neoforge.transfer.fluid.FluidResource.of(fluid.get());
                fluidHandler.set(0, resource, amount);
            }
        }
        
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                net.minecraft.resources.Identifier.tryParse(outputTag.getString("fluid").orElse(""))
            );
            int amount = outputTag.getInt("amount").orElse(0);
            
            if (fluid.isPresent() && amount > 0) {
                var resource = net.neoforged.neoforge.transfer.fluid.FluidResource.of(fluid.get());
                fluidHandler.set(1, resource, amount);
            }
        }
    }
    
    private void clearFluidHandlerDirectly() {
        fluidHandler.set(0, FluidResource.EMPTY, 0);
        fluidHandler.set(1, FluidResource.EMPTY, 0);
    }
    
    private void insertFluidDirectly(int slot, FluidResource resource, int amount) {
        fluidHandler.set(slot, resource, amount);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            
            if (!level.isClientSide()) {
                var tag = getUpdateTag(level.registryAccess());
                var packet = new com.chinaex123.resource_replicator.network.FluidSyncPacket(getBlockPos(), tag);
                
                for (var player : level.players()) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) {
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, packet);
                        }
                    }
                }
            }
        }
    }


    public FluidStack getInputFluid() {
        FluidResource resource = (FluidResource) fluidHandler.getResource(0);
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        
        long amount = fluidHandler.getAmountAsLong(0);
        
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        
        return resource.toStack((int) amount);
    }

    public FluidStack getOutputFluid() {
        FluidResource resource = (FluidResource) fluidHandler.getResource(1);
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        
        long amount = fluidHandler.getAmountAsLong(1);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        
        return resource.toStack((int) amount);
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
