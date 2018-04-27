package net.blay09.mods.tcinventoryscan;

import net.blay09.mods.tcinventoryscan.net.NetworkHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod(modid = TCInventoryScanning.MOD_ID, name = "Thaumic Inventory Scanning", acceptableRemoteVersions = "*", dependencies = "required-after:thaumcraft")
public class TCInventoryScanning {

    public static final String MOD_ID = "tcinventoryscan";

    @SidedProxy(clientSide = "net.blay09.mods.tcinventoryscan.client.ClientProxy", serverSide = "net.blay09.mods.tcinventoryscan.CommonProxy")
    public static CommonProxy proxy;

    public static boolean isServerSideInstalled;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        NetworkHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @NetworkCheckHandler
    public boolean checkNetwork(Map<String, String> map, Side side) {
        if (side == Side.SERVER) {
            isServerSideInstalled = map.containsKey(MOD_ID);
        }
        return true;
    }

}
