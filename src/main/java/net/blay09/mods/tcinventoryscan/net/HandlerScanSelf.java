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

public class HandlerScanSelf implements IMessageHandler<MessageScanSelf, IMessage> {

    @Override
    public IMessage onMessage(MessageScanSelf message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        ScanResult scan = new ScanResult((byte) 2, 0, 0, entityPlayer, "");
        ScanManager.completeScan(entityPlayer, scan, "@");
        return null;
    }

}
