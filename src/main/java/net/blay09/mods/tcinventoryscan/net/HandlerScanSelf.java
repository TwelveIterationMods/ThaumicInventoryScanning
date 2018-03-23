package net.blay09.mods.tcinventoryscan.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ScanningManager;

public class HandlerScanSelf implements IMessageHandler<MessageScanSelf, IMessage> {

    @Override
    public IMessage onMessage(MessageScanSelf message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        ScanningManager.scanTheThing(entityPlayer, entityPlayer);
        return null;
    }

}
