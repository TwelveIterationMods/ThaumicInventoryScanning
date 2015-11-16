package net.blay09.mods.tcinventoryscan.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.lib.research.ScanManager;

public class HandlerScanSlot implements IMessageHandler<MessageScanSlot, IMessage> {

    @Override
    public IMessage onMessage(MessageScanSlot message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        Container container = entityPlayer.openContainer;
        if(container != null) {
            Slot slot = (Slot) container.inventorySlots.get(message.getSlotNumber());
            if(slot.getStack() != null && slot.canTakeStack(entityPlayer)) {
                ItemStack itemStack = slot.getStack();
                ScanResult scan = new ScanResult((byte) 1, Item.getIdFromItem(itemStack.getItem()), itemStack.getItemDamage(), null, "");
//                if(ScanManager.isValidScanTarget(entityPlayer, scan, "@")) {
                    ScanManager.completeScan(entityPlayer, scan, "@");
//                }
            }
        }
        return null;
    }

}
