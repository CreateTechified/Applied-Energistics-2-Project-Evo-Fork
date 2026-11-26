package appeng.api.networking.crafting;

import appeng.crafting.execution.CraftingCpuLogic;

public interface ICraftingCpuHost extends ICraftingCpuStats, ICraftingCPU {
    CraftingCpuLogic getCraftingLogic();
}
