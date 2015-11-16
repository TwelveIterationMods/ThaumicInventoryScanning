package net.blay09.mods.tcinventoryscan.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.blay09.mods.tcinventoryscan.CommonProxy;
import net.blay09.mods.tcinventoryscan.net.MessageScanSlot;
import net.blay09.mods.tcinventoryscan.net.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import thaumcraft.api.research.ScanResult;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.common.lib.research.ScanManager;

public class ClientProxy extends CommonProxy {

    private static final int SCAN_TICKS = 50;
    private static final int SOUND_TICKS = 5;
    private Item thaumometer;
    private Slot mouseSlot;
    private Slot lastScannedSlot;
    private int ticksHovered;
    private ClientTickEventsFML effectRenderer;
    private ScanResult currentScan;

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);

        effectRenderer = new ClientTickEventsFML();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        thaumometer = GameRegistry.findItem("Thaumcraft", "ItemThaumometer");
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entityPlayer = mc.thePlayer;
        if(entityPlayer != null) {
            ItemStack mouseItem = entityPlayer.inventory.getItemStack();
            if(mouseItem != null && mouseItem.getItem() == thaumometer) {
                if(mouseSlot != null && mouseSlot.getStack() != null && mouseSlot.canTakeStack(entityPlayer) && mouseSlot != lastScannedSlot) {
                    ticksHovered++;
                    ItemStack itemStack = mouseSlot.getStack();
                    if(currentScan == null) {
                        currentScan = new ScanResult((byte) 1, Item.getIdFromItem(itemStack.getItem()), itemStack.getItemDamage(), null, "");
                    }
                    if(ScanManager.isValidScanTarget(entityPlayer, currentScan, "@")) {
                        if (ticksHovered > SOUND_TICKS && ticksHovered % 2 == 0) {
                            entityPlayer.worldObj.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, "thaumcraft:cameraticks", 0.2F, 0.45F + entityPlayer.worldObj.rand.nextFloat() * 0.1F, false);
                        }
                        if (ticksHovered >= SCAN_TICKS) {
                            if (ScanManager.completeScan(entityPlayer, currentScan, "@")) {
                                NetworkHandler.instance.sendToServer(new MessageScanSlot(mouseSlot.slotNumber));
                            }
                            ticksHovered = 0;
                            lastScannedSlot = mouseSlot;
                            currentScan = null;
                        }
                    } else {
                        currentScan = null;
                        lastScannedSlot = mouseSlot;
                    }
                }
            } else {
                ticksHovered = 0;
                currentScan = null;
                lastScannedSlot = null;
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(event.gui instanceof GuiContainer) {
            Slot oldMouseSlot = mouseSlot;
            mouseSlot = ((GuiContainer) event.gui).getSlotAtPosition(event.mouseX, event.mouseY);
            if(oldMouseSlot != mouseSlot) {
                ticksHovered = 0;
                currentScan = null;
            }
            if(mouseSlot != null && mouseSlot.getStack() != null) {
                Minecraft mc = Minecraft.getMinecraft();
                EntityPlayer entityPlayer = mc.thePlayer;
                if (entityPlayer != null) {
                    ItemStack mouseItem = entityPlayer.inventory.getItemStack();
                    if (mouseItem != null && mouseItem.getItem() == thaumometer) {
                        if(currentScan != null) {
                            String s;
                            if(ticksHovered >= SCAN_TICKS * 0.75f) {
                                s = "\u00a76Scanning...";
                            } else if(ticksHovered >= SCAN_TICKS * 0.5f) {
                                s = "\u00a76Scanning..";
                            } else if(ticksHovered >= SCAN_TICKS * 0.25f) {
                                s = "\u00a76Scanning.";
                            } else {
                                s = "\u00a76Scanning";
                            }
                            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                            RenderHelper.disableStandardItemLighting();
                            GL11.glDisable(GL11.GL_LIGHTING);
                            GL11.glDisable(GL11.GL_DEPTH_TEST);
                            mc.fontRenderer.drawStringWithShadow(s, event.mouseX, event.mouseY - 30, Integer.MAX_VALUE);
                            float oldZLevel = event.gui.zLevel;
                            event.gui.zLevel = 300;
                            event.gui.zLevel = oldZLevel;
                            GL11.glEnable(GL11.GL_LIGHTING);
                            GL11.glEnable(GL11.GL_DEPTH_TEST);
                            RenderHelper.enableStandardItemLighting();
                            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                        }
                        event.gui.renderToolTip(mouseSlot.getStack(), event.mouseX, event.mouseY);
                        effectRenderer.renderAspectsInGui((GuiContainer) event.gui, entityPlayer);
                    }
                }
            }
        }
    }

}
