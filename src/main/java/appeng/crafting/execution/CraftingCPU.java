package appeng.crafting.execution;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCpuHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;

/**
 * Reusable {@link ICraftingCPU} implementation that owns a {@link CraftingCpuLogic} and delegates its mutable stats to
 * a backing host. Addons can wrap their own host implementation with this class instead of reimplementing the CPU.
 */
public final class CraftingCPU implements ICraftingCpuHost {

    private final ICraftingCpuHost statsHost;
    public final CraftingCpuLogic craftingLogic;

    public CraftingCPU(ICraftingCpuHost statsHost) {
        this.statsHost = statsHost;
        this.craftingLogic = new CraftingCpuLogic(this);
    }

    @Override
    public boolean isBusy() {
        return statsHost.isBusy();
    }

    @Override
    public appeng.api.networking.crafting.CraftingJobStatus getJobStatus() {
        return statsHost.getJobStatus();
    }

    @Override
    public void cancelJob() {
        statsHost.cancelJob();
    }

    @Override
    public long getAvailableStorage() {
        return statsHost.getAvailableStorage();
    }

    @Override
    public int getCoProcessors() {
        return statsHost.getCoProcessors();
    }

    @Override
    public net.minecraft.network.chat.Component getName() {
        return statsHost.getName();
    }

    @Override
    public appeng.api.config.CpuSelectionMode getSelectionMode() {
        return statsHost.getSelectionMode();
    }

    @Override
    public boolean isActive() {
        return statsHost.isActive();
    }

    @Override
    public boolean isDestroyed() {
        return statsHost.isDestroyed();
    }

    @Override
    public void markDirty() {
        statsHost.markDirty();
    }

    @Override
    public void updateOutput(@Nullable GenericStack finalOutput) {
        statsHost.updateOutput(finalOutput);
    }

    @Override
    public @Nullable IGrid getGrid() {
        return statsHost.getGrid();
    }

    @Override
    public IActionSource getSrc() {
        return statsHost.getSrc();
    }

    @Override
    public Level getLevel() {
        return statsHost.getLevel();
    }

    @Override
    public CraftingCpuLogic getCraftingLogic() {
        return this.craftingLogic;
    }

    @Override
    public boolean canBeAutoSelectedFor(IActionSource source) {
        return statsHost.canBeAutoSelectedFor(source);
    }

    @Override
    public boolean isPreferredFor(IActionSource source) {
        return statsHost.isPreferredFor(source);
    }
}
