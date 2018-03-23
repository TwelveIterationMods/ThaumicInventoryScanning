package net.blay09.mods.tcinventoryscan.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ScanningManager;

public class HandlerScanSlot implements IMessageHandler<MessageScanSlot, IMessage> {

    @Override
    public IMessage onMessage(MessageScanSlot message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        Container container = entityPlayer.openContainer;
        if (container != null && message.getSlotNumber() >= 0 && message.getSlotNumber() < container.inventorySlots.size()) {
            Slot slot = container.inventorySlots.get(message.getSlotNumber());
            if (slot.getStack() != null && slot.canTakeStack(entityPlayer) && !(slot instanceof SlotCrafting)) {
                ItemStack itemStack = slot.getStack();
                ScanningManager.scanTheThing(entityPlayer, itemStack);
            }
        }
        return null;
    }

}
