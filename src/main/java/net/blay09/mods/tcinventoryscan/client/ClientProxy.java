package net.blay09.mods.tcinventoryscan.client;

import net.blay09.mods.tcinventoryscan.CommonProxy;
import net.blay09.mods.tcinventoryscan.net.MessageScanSelf;
import net.blay09.mods.tcinventoryscan.net.MessageScanSlot;
import net.blay09.mods.tcinventoryscan.net.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanningManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClientProxy extends CommonProxy {

    private static final int SCAN_TICKS = 25;
    private static final int SOUND_TICKS = 3;

    private static final int INVENTORY_PLAYER_X = 26;
    private static final int INVENTORY_PLAYER_Y = 8;
    private static final int INVENTORY_PLAYER_WIDTH = 52;
    private static final int INVENTORY_PLAYER_HEIGHT = 70;

    private static final int HELLO_TIMEOUT = 20 * 60;

    private int helloTimeout;
    private boolean isEnabled;

    private Item thaumometer;
    private SoundEvent researchSound;

    private Slot mouseSlot;
    private Slot lastScannedSlot;
    private int ticksHovered;
    private Object currentScan;
    private boolean isHoveringPlayer;

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        thaumometer = Item.REGISTRY.getObject(new ResourceLocation("thaumcraft", "thaumometer"));
        researchSound = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "scan"));
    }

    @SubscribeEvent
    public void connectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        helloTimeout = HELLO_TIMEOUT;
        isEnabled = false;
    }

    private boolean isHoldingThaumometer() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entityPlayer = mc.player;
        ItemStack mouseItem = entityPlayer.inventory.getItemStack();
        return mouseItem != null && mouseItem.getItem() == thaumometer;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entityPlayer = mc.player;
        if (entityPlayer != null) {
            if (helloTimeout > 0) {
                helloTimeout--;
                if (helloTimeout <= 0) {
                    entityPlayer.sendStatusMessage(new TextComponentTranslation("tcinventoryscan:serverNotInstalled"));
                    isEnabled = false;
                }
            }
            if (!isEnabled) {
                return;
            }

            if (isHoldingThaumometer()) {
                if ((isHoveringPlayer && currentScan != null) || (mouseSlot != null && mouseSlot.getStack() != null && mouseSlot.canTakeStack(entityPlayer) && mouseSlot != lastScannedSlot && !(mouseSlot instanceof SlotCrafting))) {
                    ticksHovered++;

                    if (currentScan == null) {
                        currentScan = mouseSlot.getStack();
                    }

                    if (ScanningManager.isThingStillScannable(entityPlayer, currentScan)) {
                        if (ticksHovered > SOUND_TICKS && ticksHovered % 4 == 0) {
                            entityPlayer.world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, researchSound, SoundCategory.NEUTRAL, 0.2f, 0.45f + entityPlayer.world.rand.nextFloat() * 0.1f, false);
                        }

                        if (ticksHovered >= SCAN_TICKS) {
                            try {
                                if (currentScan instanceof EntityPlayer) {
                                    NetworkHandler.instance.sendToServer(new MessageScanSelf());
                                } else {
                                    NetworkHandler.instance.sendToServer(new MessageScanSlot(mouseSlot.slotNumber));
                                }
                            } catch (StackOverflowError e) {
                                // Can't do anything about Thaumcraft freaking out except for calming it down if it does.
                                // If Thaumcraft happens to get into a weird recipe loop, we just ignore that and assume the item unscannable.
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
    public void onTooltip(ItemTooltipEvent event) {
        if (isEnabled && event.getItemStack().getItem() == thaumometer) {
            event.getToolTip().add(TextFormatting.GOLD + I18n.format("tcinventoryscan:thaumometerTooltip"));
            if (GuiScreen.isShiftKeyDown()) {
                String[] lines = I18n.format("tcinventoryscan:thaumometerTooltipMore").split("\\\\n");
                for (String line : lines) {
                    event.getToolTip().add(TextFormatting.DARK_AQUA + line);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTooltipPostText(RenderTooltipEvent.PostText event) {
        if (isHoldingThaumometer() && !GuiScreen.isShiftKeyDown()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen instanceof GuiContainer && !ScanningManager.isThingStillScannable(mc.player, event.getStack())) {
                renderAspectsInGui((GuiContainer) mc.currentScreen, mc.player, event.getStack(), 0, event.getX(), event.getY());
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (isEnabled && event.getGui() instanceof GuiContainer && !(event.getGui() instanceof GuiContainerCreative)) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer entityPlayer = mc.player;
            boolean oldHoveringPlayer = isHoveringPlayer;
            isHoveringPlayer = isHoveringPlayer((GuiContainer) event.getGui(), event.getMouseX(), event.getMouseY());
            if (!isHoveringPlayer) {
                Slot oldMouseSlot = mouseSlot;
                mouseSlot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
                if (oldMouseSlot != mouseSlot) {
                    ticksHovered = 0;
                    currentScan = null;
                }
            }
            if (oldHoveringPlayer != isHoveringPlayer) {
                ticksHovered = 0;
                if (isHoveringPlayer) {
                    currentScan = entityPlayer;
                    if (!ScanningManager.isThingStillScannable(entityPlayer, currentScan)) {
                        currentScan = null;
                    }
                }
            }

            ItemStack mouseItem = entityPlayer.inventory.getItemStack();
            if (mouseItem != null && mouseItem.getItem() == thaumometer) {
                if (mouseSlot != null && mouseSlot.getStack() != null) {
                    if (currentScan != null) {
                        renderScanningProgress(event.getGui(), event.getMouseX(), event.getMouseY(), ticksHovered / (float) SCAN_TICKS);
                    }
                    event.getGui().renderToolTip(mouseSlot.getStack(), event.getMouseX(), event.getMouseY());
                } else if (isHoveringPlayer) {
                    if (currentScan != null) {
                        renderScanningProgress(event.getGui(), event.getMouseX(), event.getMouseY(), ticksHovered / (float) SCAN_TICKS);
                    }
                    if (!ScanningManager.isThingStillScannable(entityPlayer, entityPlayer)) {
                        renderPlayerAspects(event.getGui(), event.getMouseX(), event.getMouseY());
                    }
                }
            }
        }
    }

    private boolean renderAspectsInGuiHasErrored;
    private Object hudHandlerInstance;
    private Method renderAspectsInGuiMethod;

    @SuppressWarnings("unchecked")
    private void renderAspectsInGui(GuiContainer guiContainer, EntityPlayer player, ItemStack itemStack, int d, int x, int y) {
        if (renderAspectsInGuiHasErrored) {
            return;
        }

        if (hudHandlerInstance == null) {
            try {
                Class renderEventHandler = Class.forName("thaumcraft.client.lib.events.RenderEventHandler");
                Object instance = renderEventHandler.getField("INSTANCE").get(null);
                hudHandlerInstance = renderEventHandler.getField("hudHandler").get(instance);
                Class hudHandler = Class.forName("thaumcraft.client.lib.events.HudHandler");
                renderAspectsInGuiMethod = hudHandler.getMethod("renderAspectsInGui", GuiContainer.class, EntityPlayer.class, ItemStack.class, int.class, int.class, int.class);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
                renderAspectsInGuiHasErrored = true;
                e.printStackTrace();
                return;
            }
        }

        try {
            renderAspectsInGuiMethod.invoke(hudHandlerInstance, guiContainer, player, itemStack, d, x, y);
        } catch (IllegalAccessException | InvocationTargetException e) {
            renderAspectsInGuiHasErrored = true;
            e.printStackTrace();
        }
    }

    private boolean drawTagHasErrored;
    private Method drawTagMethod;

    @SuppressWarnings("unchecked")
    private void drawTag(int x, int y, Aspect aspect, float amount, int bonus, double zLevel) {
        if (drawTagHasErrored) {
            return;
        }

        if (drawTagMethod == null) {
            try {
                Class utilsFX = Class.forName("thaumcraft.client.lib.UtilsFX");
                drawTagMethod = utilsFX.getMethod("drawTag", int.class, int.class, Aspect.class, float.class, int.class, double.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                drawTagHasErrored = true;
                e.printStackTrace();
                return;
            }
        }

        try {
            drawTagMethod.invoke(null, x, y, aspect, amount, bonus, zLevel);
        } catch (IllegalAccessException | InvocationTargetException e) {
            drawTagHasErrored = true;
            e.printStackTrace();
        }
    }

    private void renderPlayerAspects(GuiScreen gui, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GL11.glPushAttrib(1048575);
        GlStateManager.disableLighting();
        int x = mouseX + 17;
        int y = mouseY + 7 - 33;
        EntityPlayer entityPlayer = FMLClientHandler.instance().getClientPlayerEntity();
        AspectList aspectList = AspectHelper.getEntityAspects(entityPlayer);
        if (aspectList != null && aspectList.size() > 0) {
            GlStateManager.disableDepth();
            Aspect[] sortedAspects = aspectList.getAspectsSortedByAmount();
            for (Aspect aspect : sortedAspects) {
                if (aspect != null) {
                    drawTag(x, y, aspect, aspectList.getAmount(aspect), 0, gui.zLevel);
                    x += 18;
                }
            }
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }

    private void renderScanningProgress(GuiScreen gui, int mouseX, int mouseY, float progress) {
        StringBuilder sb = new StringBuilder("\u00a76");
        sb.append(I18n.format("tcinventoryscan:scanning"));
        if (progress >= 0.75f) {
            sb.append("...");
        } else if (progress >= 0.5f) {
            sb.append("..");
        } else if (progress >= 0.25f) {
            sb.append(".");
        }
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        float oldZLevel = gui.zLevel;
        gui.zLevel = 300;
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(sb.toString(), mouseX, mouseY - 30, 0xFFFFFFFF);
        gui.zLevel = oldZLevel;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    private boolean isHoveringPlayer(GuiContainer gui, int mouseX, int mouseY) {
        return gui instanceof GuiInventory && mouseX >= gui.guiLeft + INVENTORY_PLAYER_X && mouseX < gui.guiLeft + INVENTORY_PLAYER_X + INVENTORY_PLAYER_WIDTH && mouseY >= gui.guiTop + INVENTORY_PLAYER_Y && mouseY < gui.guiTop + INVENTORY_PLAYER_Y + INVENTORY_PLAYER_HEIGHT;
    }

    @Override
    public void receivedHello(EntityPlayer entityPlayer) {
        super.receivedHello(entityPlayer);
        helloTimeout = 0;
        isEnabled = true;
    }
}
