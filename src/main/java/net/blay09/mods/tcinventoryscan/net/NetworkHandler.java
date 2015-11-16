package net.blay09.mods.tcinventoryscan.net;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.blay09.mods.tcinventoryscan.TCInventoryScanning;

public class NetworkHandler {

    public static final SimpleNetworkWrapper instance = new SimpleNetworkWrapper(TCInventoryScanning.MOD_ID);

    public static void init() {
        instance.registerMessage(HandlerScanSlot.class, MessageScanSlot.class, 0, Side.SERVER);
        instance.registerMessage(HandlerScanSelf.class, MessageScanSelf.class, 1, Side.SERVER);
    }

}
