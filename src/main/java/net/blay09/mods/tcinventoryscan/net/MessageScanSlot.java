package net.blay09.mods.tcinventoryscan.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageScanSlot implements IMessage {
    private int slotNumber;

    public MessageScanSlot() {
    }

    public MessageScanSlot(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slotNumber = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slotNumber);
    }

    public int getSlotNumber() {
        return slotNumber;
    }
}
