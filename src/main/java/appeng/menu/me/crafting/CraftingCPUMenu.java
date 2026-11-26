/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.menu.me.crafting;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCpuHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.IncrementalUpdateHelper;

/**
 * @see appeng.client.gui.me.crafting.CraftingCPUScreen
 */
public class CraftingCPUMenu extends AEBaseMenu {

    private static final String ACTION_CANCEL_CRAFTING = "cancelCrafting";
    private static final String ACTION_TOGGLE_SCHEDULING = "toggleScheduling";

    public static final MenuType<CraftingCPUMenu> TYPE = MenuTypeBuilder
            .create(CraftingCPUMenu::new, CraftingBlockEntity.class)
            .withMenuTitle(craftingBlockEntity -> {
                // Use the cluster's custom name instead of the right-clicked block entities one
                CraftingCPUCluster cluster = craftingBlockEntity.getCluster();
                if (cluster != null && cluster.getName() != null) {
                    return cluster.getName();
                }
                return Component.empty();
            })
            .build("craftingcpu");

    private final IncrementalUpdateHelper incrementalUpdateHelper = new IncrementalUpdateHelper();
    private final IGrid grid;
    private CraftingCPUCluster cpu = null;
    private ICraftingCpuHost providerCpu = null;
    private final Consumer<AEKey> cpuChangeListener = incrementalUpdateHelper::addChange;
    private boolean cachedSuspend;

    @GuiSync(0)
    public CpuSelectionMode schedulingMode = CpuSelectionMode.ANY;
    @GuiSync(1)
    public boolean cantStoreItems = false;

    public CraftingCPUMenu(MenuType<?> menuType, int id, Inventory ip, Object te) {
        super(menuType, id, ip, te);
        final IActionHost host = (IActionHost) (te instanceof IActionHost ? te : null);

        if (host != null && host.getActionableNode() != null) {
            this.grid = host.getActionableNode().getGrid();
        } else {
            this.grid = null;
        }

        if (te instanceof CraftingBlockEntity) {
            this.setCPU(((CraftingBlockEntity) te).getCluster());
        }

        if (this.getGrid() == null && isServerSide()) {
            this.setValidMenu(false);
        }

        registerClientAction(ACTION_CANCEL_CRAFTING, this::cancelCrafting);
        registerClientAction(ACTION_TOGGLE_SCHEDULING, this::toggleScheduling);
    }

    protected void setCPU(ICraftingCPU c) {
        if (c == this.cpu) {
            return;
        }

        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(cpuChangeListener);
        }
        if (this.providerCpu != null) {
            this.providerCpu.getCraftingLogic().removeListener(cpuChangeListener);
        }

        this.incrementalUpdateHelper.reset();
        this.cachedSuspend = false;

        if (c instanceof CraftingCPUCluster) {
            this.providerCpu = null;
            this.cpu = (CraftingCPUCluster) c;

            // Initially send all items as a full-update to the client when the CPU changes
            var allItems = new KeyCounter();
            cpu.craftingLogic.getAllItems(allItems);
            for (var entry : allItems) {
                incrementalUpdateHelper.addChange(entry.getKey());
            }

            this.cpu.craftingLogic.addListener(cpuChangeListener);
        } else if (c instanceof ICraftingCpuHost host) {
            this.cpu = null;
            this.providerCpu = host;

            var allItems = new KeyCounter();
            host.getCraftingLogic().getAllItems(allItems);
            for (var entry : allItems) {
                incrementalUpdateHelper.addChange(entry.getKey());
            }

            host.getCraftingLogic().addListener(cpuChangeListener);
        } else {
            this.cpu = null;
            this.providerCpu = null;
            // Clear the crafting status
            sendPacketToClient(new CraftingStatusPacket(containerId, CraftingStatus.EMPTY));
        }
    }

    public void cancelCrafting() {
        if (isClientSide()) {
            sendClientAction(ACTION_CANCEL_CRAFTING);
        } else {
            if (this.cpu != null) {
                this.cpu.cancelJob();
            }
            if (this.providerCpu != null) {
                this.providerCpu.cancelJob();
            }
        }
    }

    public void toggleScheduling() {
        if (isClientSide()) {
            sendClientAction(ACTION_TOGGLE_SCHEDULING);
        } else if (this.cpu != null || this.providerCpu != null) {
            var logic = this.cpu != null ? this.cpu.craftingLogic : this.providerCpu.getCraftingLogic();
            logic.setJobSuspended(!logic.isJobSuspended());
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(cpuChangeListener);
        }
        if (this.providerCpu != null) {
            this.providerCpu.getCraftingLogic().removeListener(cpuChangeListener);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            ICraftingCpuHost active = this.cpu != null ? this.cpu : this.providerCpu;
            if (active != null) {
                this.schedulingMode = active.getSelectionMode();
                this.cantStoreItems = active.getCraftingLogic().isCantStoreItems();

                if (this.incrementalUpdateHelper.hasChanges()
                        || this.cachedSuspend != active.getCraftingLogic().isJobSuspended()) {
                    CraftingStatus status = CraftingStatus.create(this.incrementalUpdateHelper,
                            active.getCraftingLogic());
                    this.incrementalUpdateHelper.commitChanges();
                    this.cachedSuspend = status.isSuspended();

                    sendPacketToClient(new CraftingStatusPacket(containerId, status));
                }
            }
        }

        super.broadcastChanges();
    }

    public CpuSelectionMode getSchedulingMode() {
        return schedulingMode;
    }

    public boolean isCantStoreItems() {
        return cantStoreItems;
    }

    public boolean allowConfiguration() {
        return true;
    }

    IGrid getGrid() {
        return this.grid;
    }

}
