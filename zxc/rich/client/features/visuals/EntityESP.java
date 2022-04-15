package zxc.rich.client.features.visuals;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import zxc.rich.Main;
import zxc.rich.api.event.EventTarget;
import zxc.rich.api.event.events.impl.Event2D;
import zxc.rich.api.event.events.impl.Event3D;
import zxc.rich.api.utils.combat.KillAuraHelper;
import zxc.rich.api.utils.combat.RotationHelper;
import zxc.rich.api.utils.math.MathematicHelper;
import zxc.rich.api.utils.render.ClientHelper;
import zxc.rich.api.utils.render.ColorUtils;
import zxc.rich.api.utils.render.RenderUtils;
import zxc.rich.client.features.Feature;
import zxc.rich.client.features.FeatureCategory;
import zxc.rich.client.ui.settings.impl.BooleanSetting;
import zxc.rich.client.ui.settings.impl.ColorSetting;
import zxc.rich.client.ui.settings.impl.ListSetting;
import zxc.rich.client.ui.settings.impl.NumberSetting;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

public class EntityESP extends Feature {

    public ListSetting espMode;
    public ListSetting rectMode;
    private final int black = Color.BLACK.getRGB();
    private final ColorSetting colorEsp;
    private final ColorSetting triangleColor;
    private final BooleanSetting fullBox;
    private final BooleanSetting heathPercentage;
    private final BooleanSetting border;
    private final BooleanSetting healRect = new BooleanSetting("Health Rect", true, () -> true);
    private BooleanSetting triangleESP;
    private final BooleanSetting ignoreInvisible = new BooleanSetting("Ignore Invisible", true, () -> true);
    private final ListSetting healcolorMode = new ListSetting("Color Health Mode", "Custom", () -> healRect.getBoolValue(), "Astolfo", "Health", "Rainbow", "Client", "Custom");
    private final ColorSetting healColor = new ColorSetting("Health Color", 0xffffffff, () -> healcolorMode.currentMode.equals("Custom"));
    private final ListSetting csgoMode;
    private final ListSetting colorMode = new ListSetting("Color Box Mode", "Custom", () -> espMode.currentMode.equals("Box") || espMode.currentMode.equals("2D"), "Astolfo", "Rainbow", "Client", "Custom");
    private final ListSetting triangleMode = new ListSetting("Triangle Mode", "Custom", () -> triangleESP.getBoolValue(), "Astolfo", "Rainbow", "Client", "Custom");
    private final NumberSetting xOffset;
    private final NumberSetting triangleFov;
    private final NumberSetting size;

    public EntityESP() {
        super("EntityESP", "���������� �������, ��� � �� �������� ������ �����", FeatureCategory.VISUALS);
        espMode = new ListSetting("ESP Mode", "2D", () -> true, "2D", "Box");
        rectMode = new ListSetting("Rect Mode", "Default", () -> espMode.currentMode.equalsIgnoreCase("2D"), "Default", "Smooth");
        border = new BooleanSetting("Border Rect", true, () -> espMode.currentMode.equals("2D"));
        csgoMode = new ListSetting("Border Mode", "Box", () -> espMode.currentMode.equals("2D") && border.getBoolValue(), "Box", "Corner");
        colorEsp = new ColorSetting("ESP Color", new Color(0xFFFFFF).getRGB(), () -> !colorMode.currentMode.equals("Client") && (espMode.currentMode.equals("2D") || espMode.currentMode.equals("Box") || espMode.currentMode.equals("Glow") || colorMode.currentMode.equalsIgnoreCase("Custom")));
        fullBox = new BooleanSetting("Full Box", false, () -> espMode.currentMode.equals("Box"));
        heathPercentage = new BooleanSetting("Health Percentage", false, () -> espMode.currentMode.equals("2D"));
        triangleESP = new BooleanSetting("Triangle ESP", true, () -> true);
        triangleColor = new ColorSetting("Triangle Color", Color.PINK.getRGB(), () -> triangleESP.getBoolValue() && triangleMode.currentMode.equals("Custom"));
        xOffset = new NumberSetting("Triangle XOffset", 10, 0, 50, 5, () -> triangleESP.getBoolValue());
        triangleFov = new NumberSetting("Triangle FOV", 100, 0, 180, 1, () -> triangleESP.getBoolValue());
        size = new NumberSetting("Triangle Size", 5, 0, 20, 1, () -> triangleESP.getBoolValue());
        addSettings(espMode, csgoMode, rectMode, colorMode, healcolorMode, healColor, colorEsp, border, fullBox, healRect, heathPercentage, ignoreInvisible, triangleESP, triangleMode, triangleColor, triangleFov, xOffset, size);
    }

    @EventTarget
    public void onRender3D(Event3D event3D) {
        if (!isEnabled())
            return;

        int color = 0;

        switch (colorMode.currentMode) {
            case "Client":
                color = ClientHelper.getClientColor().getRGB();
                break;
            case "Custom":
                color = colorEsp.getColorValue();
                break;
            case "Astolfo":
                color = ColorUtils.astolfo(false, 10).getRGB();
                break;
            case "Rainbow":
                color = ColorUtils.rainbow(300, 1, 1).getRGB();
                break;
        }

        if (espMode.currentMode.equals("Box")) {
            GlStateManager.pushMatrix();
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityPlayer && entity != mc.player) {
                    RenderUtils.drawEntityBox(entity, new Color(color), fullBox.getBoolValue(), fullBox.getBoolValue() ? 0.15F : 0.90F);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onRenderTriangle(Event2D eventRender2D) {
        if (!isEnabled())
            return;

        if (triangleESP.getBoolValue()) {
            int color = 0;
            switch (triangleMode.currentMode) {
                case "Client":
                    color = ClientHelper.getClientColor().getRGB();
                    break;
                case "Custom":
                    color = triangleColor.getColorValue();
                    break;
                case "Astolfo":
                    color = ColorUtils.astolfo(false, 1).getRGB();
                    break;
                case "Rainbow":
                    color = ColorUtils.rainbow(300, 1, 1).getRGB();
                    break;
            }

            ScaledResolution sr = new ScaledResolution(mc);
            float size = 50;
            float xOffset = sr.getScaledWidth() / 2F - 24.5F;
            float yOffset = sr.getScaledHeight() / 2F - 25.2F;
            for (EntityPlayer entity : mc.world.playerEntities) {

                if ((ignoreInvisible.getBoolValue() && entity.isInvisible()))
                    continue;

                if (entity != null && entity != mc.player) {
                    GlStateManager.pushMatrix();
                    GlStateManager.disableBlend();
                    double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
                    double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
                    double cos = Math.cos(mc.player.rotationYaw * (Math.PI * 2 / 360));
                    double sin = Math.sin(mc.player.rotationYaw * (Math.PI * 2 / 360));
                    double rotY = -(z * cos - x * sin);
                    double rotX = -(x * cos + z * sin);
                    if (MathHelper.sqrt(rotX * rotX + rotY * rotY) < size) {
                        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);
                        double xPos = ((size / 2) * Math.cos(Math.toRadians(angle))) + xOffset + size / 2;
                        double y = ((size / 2) * Math.sin(Math.toRadians(angle))) + yOffset + size / 2;
                        GlStateManager.translate(xPos, y, 0);
                        GlStateManager.rotate(angle, 0, 0, 1);
                        GlStateManager.scale(1.5, 1, 1);
                        if (KillAuraHelper.canSeeEntityAtFov(entity, triangleFov.getNumberValue())) {
                            if (Main.instance.friendManager.isFriend(entity.getName())) {
                                RenderUtils.drawFillTriangle(this.xOffset.getNumberValue(), 0, this.size.getNumberValue() + 0.5F, 90, new Color(0, 255, 0).getRGB());
                            } else {
                                RenderUtils.drawFillTriangle(this.xOffset.getNumberValue(), 0, this.size.getNumberValue() + 0.5F, 90, new Color(color).getRGB());

                            }
                        }
                    }
                    GlStateManager.resetColor();
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @EventTarget
    public void onRender2D(Event2D event) {
        if (!isEnabled())
            return;

        String mode = espMode.getOptions();
        setSuffix(mode);

        float partialTicks = mc.timer.renderPartialTicks;
        int scaleFactor = event.getResolution().getScaleFactor();
        double scaling = scaleFactor / Math.pow(scaleFactor, 2);
        GL11.glPushMatrix();
        GlStateManager.scale(scaling, scaling, scaling);

        int color = 0;
        switch (colorMode.currentMode) {
            case "Client":
                color = ClientHelper.getClientColor().getRGB();
                break;
            case "Custom":
                color = colorEsp.getColorValue();
                break;
            case "Astolfo":
                color = ColorUtils.astolfo(false, 1).getRGB();
                break;
            case "Rainbow":
                color = ColorUtils.rainbow(300, 1, 1).getRGB();
                break;
        }

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != mc.player) {


                if ((ignoreInvisible.getBoolValue() && entity.isInvisible()))
                    continue;

                if (isValid(entity) && RenderUtils.isInViewFrustum(entity)) {
                    double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks();
                    double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks();
                    double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks();
                    double width = entity.width / 1.5;
                    double height = entity.height + ((entity.isSneaking() || (entity == mc.player && mc.player.isSneaking()) ? -0.3D : 0.2D));
                    AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
                    List<Vector3d> vectors = Arrays.asList(new Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ),
                            new Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ),
                            new Vector3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vector3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ),
                            new Vector3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vector3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ));

                    mc.entityRenderer.setupCameraTransform(partialTicks, 0);

                    Vector4d position = null;
                    for (Vector3d vector : vectors) {
                        vector = vectorRender2D(scaleFactor, vector.x - mc.getRenderManager().renderPosX, vector.y - mc.getRenderManager().renderPosY, vector.z - mc.getRenderManager().renderPosZ);
                        if (vector != null && vector.z > 0 && vector.z < 1) {

                            if (position == null) {
                                position = new Vector4d(vector.x, vector.y, vector.z, 0);
                            }

                            position.x = Math.min(vector.x, position.x);
                            position.y = Math.min(vector.y, position.y);
                            position.z = Math.max(vector.x, position.z);
                            position.w = Math.max(vector.y, position.w);
                        }
                    }

                    if (position != null) {
                        mc.entityRenderer.setupOverlayRendering();
                        double posX = position.x;
                        double posY = position.y;
                        double endPosX = position.z;
                        double endPosY = position.w;
                        if (border.getBoolValue()) {
                            if (mode.equalsIgnoreCase("2D") && csgoMode.currentMode.equalsIgnoreCase("Box") && rectMode.currentMode.equalsIgnoreCase("Smooth")) {

                                //top
                                RenderUtils.drawSmoothRect(posX - 0.5, posY - 0.5, endPosX + 0.5, posY + 0.5 + 1, black);

                                //button
                                RenderUtils.drawSmoothRect(posX - 0.5, endPosY - 0.5 - 1, endPosX + 0.5, endPosY + 0.5, black);

                                //left
                                RenderUtils.drawSmoothRect(posX - 1.5, posY, posX + 0.5, endPosY + 0.5, black);

                                //right
                                RenderUtils.drawSmoothRect(endPosX - 0.5 - 1, posY, endPosX + 0.5, endPosY + 0.5, black);

                                /* Main ESP */

                                //left
                                RenderUtils.drawSmoothRect(posX - 1, posY, posX + 0.5 - 0.5, endPosY, color);

                                //Button
                                RenderUtils.drawSmoothRect(posX, endPosY - 1, endPosX, endPosY, color);

                                //Top
                                RenderUtils.drawSmoothRect(posX - 1, posY, endPosX, posY + 1, color);

                                //Right
                                RenderUtils.drawSmoothRect(endPosX - 1, posY, endPosX, endPosY, color);
                            } else if (mode.equalsIgnoreCase("2D") && csgoMode.currentMode.equalsIgnoreCase("Corner") && rectMode.currentMode.equalsIgnoreCase("Smooth")) {

                                //Top Left
                                RenderUtils.drawSmoothRect(posX + 1, posY, posX - 1, posY + (endPosY - posY) / 4 + 0.5, black);

                                //Button Left
                                RenderUtils.drawSmoothRect(posX - 1, endPosY, posX + 1, endPosY - (endPosY - posY) / 4.0 - 0.5, black);

                                //Top Left Corner
                                RenderUtils.drawSmoothRect(posX - 1, posY - 0.5, posX + (endPosX - posX) / 3, posY + 1, black);

                                //Top Corner
                                RenderUtils.drawSmoothRect(endPosX - (endPosX - posX) / 3 - 0, posY - 0.5, endPosX, posY + 1.5, black);

                                //Top Right Corner
                                RenderUtils.drawSmoothRect(endPosX - 1.5, posY, endPosX + 0.5, posY + (endPosY - posY) / 4 + 0.5, black);

                                //Right Button Corner
                                RenderUtils.drawSmoothRect(endPosX - 1.5, endPosY, endPosX + 0.5, endPosY - (endPosY - posY) / 4 - 0.5, black);

                                //Left Button
                                RenderUtils.drawSmoothRect(posX - 1, endPosY - 1.5, posX + (endPosX - posX) / 3 + 0.5, endPosY + 0.5, black);

                                //Right Button
                                RenderUtils.drawSmoothRect(endPosX - (endPosX - posX) / 3 - 0.5, endPosY - 1.5, endPosX + 0.5, endPosY + 0.5, black);

                                RenderUtils.drawSmoothRect(posX + 0.5, posY, posX - 0.5, posY + (endPosY - posY) / 4, color);

                                RenderUtils.drawSmoothRect(posX + 0.5, endPosY, posX - 0.5, endPosY - (endPosY - posY) / 4, color);

                                RenderUtils.drawSmoothRect(posX - 0.5, posY, posX + (endPosX - posX) / 3, posY + 1, color);
                                RenderUtils.drawSmoothRect(endPosX - (endPosX - posX) / 3 + 0.5, posY, endPosX, posY + 1, color);
                                RenderUtils.drawSmoothRect(endPosX - 1, posY, endPosX, posY + (endPosY - posY) / 4 + 0.5, color);
                                RenderUtils.drawSmoothRect(endPosX - 1, endPosY, endPosX, endPosY - (endPosY - posY) / 4, color);
                                RenderUtils.drawSmoothRect(posX, endPosY - 1, posX + (endPosX - posX) / 3, endPosY, color);
                                RenderUtils.drawSmoothRect(endPosX - (endPosX - posX) / 3, endPosY - 1, endPosX - 0.5, endPosY, color);
                            } else if (mode.equalsIgnoreCase("2D") && csgoMode.currentMode.equalsIgnoreCase("Box") && rectMode.currentMode.equalsIgnoreCase("Default")) {

                                //top
                                RenderUtils.drawRect(posX - 0.5, posY - 0.5, endPosX + 0.5, posY + 0.5 + 1, black);

                                //button
                                RenderUtils.drawRect(posX - 0.5, endPosY - 0.5 - 1, endPosX + 0.5, endPosY + 0.5, black);

                                //left
                                RenderUtils.drawRect(posX - 1.5, posY, posX + 0.5, endPosY + 0.5, black);

                                //right
                                RenderUtils.drawRect(endPosX - 0.5 - 1, posY, endPosX + 0.5, endPosY + 0.5, black);

                                /* Main ESP */

                                //left
                                RenderUtils.drawRect(posX - 1, posY, posX + 0.5 - 0.5, endPosY, color);

                                //Button
                                RenderUtils.drawRect(posX, endPosY - 1, endPosX, endPosY, color);

                                //Top
                                RenderUtils.drawRect(posX - 1, posY, endPosX, posY + 1, color);

                                //Right
                                RenderUtils.drawRect(endPosX - 1, posY, endPosX, endPosY, color);
                            } else if (mode.equalsIgnoreCase("2D") && csgoMode.currentMode.equalsIgnoreCase("Corner") && rectMode.currentMode.equalsIgnoreCase("Default")) {

                                //Top Left
                                RenderUtils.drawRect(posX + 1, posY, posX - 1, posY + (endPosY - posY) / 4 + 0.5, black);

                                //Button Left
                                RenderUtils.drawRect(posX - 1, endPosY, posX + 1, endPosY - (endPosY - posY) / 4.0 - 0.5, black);

                                //Top Left Corner
                                RenderUtils.drawRect(posX - 1, posY - 0.5, posX + (endPosX - posX) / 3, posY + 1, black);

                                //Top Corner
                                RenderUtils.drawRect(endPosX - (endPosX - posX) / 3 - 0, posY - 0.5, endPosX, posY + 1.5, black);

                                //Top Right Corner
                                RenderUtils.drawRect(endPosX - 1.5, posY, endPosX + 0.5, posY + (endPosY - posY) / 4 + 0.5, black);

                                //Right Button Corner
                                RenderUtils.drawRect(endPosX - 1.5, endPosY, endPosX + 0.5, endPosY - (endPosY - posY) / 4 - 0.5, black);

                                //Left Button
                                RenderUtils.drawRect(posX - 1, endPosY - 1.5, posX + (endPosX - posX) / 3 + 0.5, endPosY + 0.5, black);

                                //Right Button
                                RenderUtils.drawRect(endPosX - (endPosX - posX) / 3 - 0.5, endPosY - 1.5, endPosX + 0.5, endPosY + 0.5, black);

                                RenderUtils.drawRect(posX + 0.5, posY, posX - 0.5, posY + (endPosY - posY) / 4, color);

                                RenderUtils.drawRect(posX + 0.5, endPosY, posX - 0.5, endPosY - (endPosY - posY) / 4, color);

                                RenderUtils.drawRect(posX - 0.5, posY, posX + (endPosX - posX) / 3, posY + 1, color);
                                RenderUtils.drawRect(endPosX - (endPosX - posX) / 3 + 0.5, posY, endPosX, posY + 1, color);
                                RenderUtils.drawRect(endPosX - 1, posY, endPosX, posY + (endPosY - posY) / 4 + 0.5, color);
                                RenderUtils.drawRect(endPosX - 1, endPosY, endPosX, endPosY - (endPosY - posY) / 4, color);
                                RenderUtils.drawRect(posX, endPosY - 1, posX + (endPosX - posX) / 3, endPosY, color);
                                RenderUtils.drawRect(endPosX - (endPosX - posX) / 3, endPosY - 1, endPosX - 0.5, endPosY, color);
                            }
                        }
                        boolean living = entity instanceof EntityLivingBase;
                        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                        float targetHP = entityLivingBase.getHealth();
                        targetHP = MathHelper.clamp(targetHP, 0F, 24F);
                        float maxHealth = entityLivingBase.getMaxHealth();
                        double hpPercentage = (targetHP / maxHealth);
                        double hpHeight2 = (endPosY - posY) * hpPercentage;


                        if (living && healRect.getBoolValue() && (!(espMode.currentMode.equals("Box")))) {
                            int colorHeal = 0;

                            switch (healcolorMode.currentMode) {
                                case "Client":
                                    colorHeal = ClientHelper.getClientColor().getRGB();
                                    break;
                                case "Custom":
                                    colorHeal = healColor.getColorValue();
                                    break;
                                case "Astolfo":
                                    colorHeal = ColorUtils.astolfo(false, (int) entity.height).getRGB();
                                    break;
                                case "Rainbow":
                                    colorHeal = ColorUtils.rainbow(300, 1, 1).getRGB();
                                    break;
                                case "Health":
                                    colorHeal = RenderUtils.getHealthColor(((EntityLivingBase) entity).getHealth(), ((EntityLivingBase) entity).getMaxHealth());
                                    break;
                            }
                            if (targetHP > 0) {
                                String string2 = "" + MathematicHelper.round(entityLivingBase.getHealth() / entityLivingBase.getMaxHealth() * 20, 1);
                                if (living && heathPercentage.getBoolValue() && (!(espMode.currentMode.equals("Box")))) {
                                    if (heathPercentage.getBoolValue()) {
                                        GlStateManager.pushMatrix();
                                        mc.sfui18.drawStringWithOutline(string2, (float) posX - 30, (float) ((float) endPosY - hpHeight2), -1);
                                        GlStateManager.popMatrix();
                                    }
                                }
                                RenderUtils.drawRect(posX - 5, posY - 0.5, posX - 2.5, endPosY + 0.5, new Color(0, 0, 0, 125).getRGB());
                                RenderUtils.drawRect(posX - 4.5, endPosY, posX - 3, endPosY - hpHeight2, colorHeal);
                            }
                        }
                    }
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GL11.glPopMatrix();
        mc.entityRenderer.setupOverlayRendering();
    }

    private boolean isValid(Entity entity) {
        if (mc.gameSettings.thirdPersonView == 0 && entity == mc.player)
            return false;
        if (entity.isDead)
            return false;
        if ((entity instanceof net.minecraft.entity.passive.EntityAnimal))
            return false;
        if ((entity instanceof EntityPlayer))
            return true;
        if ((entity instanceof EntityArmorStand))
            return false;
        if ((entity instanceof IAnimals))
            return false;
        if ((entity instanceof EntityItemFrame))
            return false;
        if (entity instanceof EntityArrow)
            return false;
        if ((entity instanceof EntityMinecart))
            return false;
        if ((entity instanceof EntityBoat))
            return false;
        if ((entity instanceof EntityDragonFireball))
            return false;
        if ((entity instanceof EntityXPOrb))
            return false;
        if ((entity instanceof EntityTNTPrimed))
            return false;
        if ((entity instanceof EntityExpBottle))
            return false;
        if ((entity instanceof EntityLightningBolt))
            return false;
        if ((entity instanceof EntityPotion))
            return false;
        if ((entity instanceof Entity))
            return false;
        if (((entity instanceof net.minecraft.entity.monster.EntityMob || entity instanceof net.minecraft.entity.monster.EntitySlime || entity instanceof net.minecraft.entity.boss.EntityDragon
                || entity instanceof net.minecraft.entity.monster.EntityGolem)))
            return false;
        return entity != mc.player;
    }

    private Vector3d vectorRender2D(int scaleFactor, double x, double y, double z) {
        float xPos = (float) x;
        float yPos = (float) y;
        float zPos = (float) z;
        IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
        FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
        FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
        FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        if (GLU.gluProject(xPos, yPos, zPos, modelview, projection, viewport, vector))
            return new Vector3d((vector.get(0) / scaleFactor), ((Display.getHeight() - vector.get(1)) / scaleFactor), vector.get(2));
        return null;
    }
}