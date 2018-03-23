package net.blay09.mods.tcinventoryscan;

import net.blay09.mods.tcinventoryscan.net.NetworkHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = TCInventoryScanning.MOD_ID, name = "TC Inventory Scanning", acceptableRemoteVersions = "*", dependencies = "required-after:thaumcraft")
public class TCInventoryScanning {

    public static final String MOD_ID = "tcinventoryscan";

    @SidedProxy(clientSide =  "net.blay09.mods.tcinventoryscan.client.ClientProxy", serverSide = "net.blay09.mods.tcinventoryscan.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        NetworkHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
