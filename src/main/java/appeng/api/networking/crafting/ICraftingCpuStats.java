package appeng.api.networking.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;

public interface ICraftingCpuStats {
    boolean isActive();

    boolean isDestroyed();

    void markDirty();

    void updateOutput(@Nullable GenericStack finalOutput);

    @Nullable
    IGrid getGrid();

    IActionSource getSrc();

    Level getLevel();

    long getAvailableStorage();

    int getCoProcessors();

    net.minecraft.network.chat.Component getName();

    CpuSelectionMode getSelectionMode();

    default boolean canBeAutoSelectedFor(IActionSource source) {
        return true;
    }

    default boolean isPreferredFor(IActionSource source) {
        return false;
    }
}
