/*
 * Copyright (c) 2019-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.client.api.util.GraphicsUtil;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.GCFluids;
import dev.galacticraft.mod.content.block.entity.machine.FuelLoaderBlockEntity;
import dev.galacticraft.mod.screen.FuelLoaderMenu;
import dev.galacticraft.mod.util.DrawableUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FuelLoaderScreen extends MachineScreen<FuelLoaderBlockEntity, FuelLoaderMenu> {
    public static final int RED_CROSS_X = 158;
    public static final int RED_CROSS_Y = 72;
    public static final int RED_CROSS_U = 190;
    public static final int RED_CROSS_V = 49;
    public static final int RED_CROSS_WIDTH = 11;
    public static final int RED_CROSS_HEIGHT = 11;

    public static final int ROCKET_FACE_X = 145;
    public static final int ROCKET_FACE_Y = 28;
    public static final int ROCKET_FACE_U = 176;
    public static final int ROCKET_FACE_V = 49;
    public static final int ROCKET_FACE_WIDTH = 14;
    public static final int ROCKET_FACE_HEIGHT = 34;

    public static final int TANK_OVERLAY_X = 69;
    public static final int TANK_OVERLAY_Y = 21;
    public static final int TANK_OVERLAY_U = 177;
    public static final int TANK_OVERLAY_V = 1;
    public static final int TANK_OVERLAY_WIDTH = 38;
    public static final int TANK_OVERLAY_HEIGHT = 47;

    public static final int PRIMARY_PROGRESS_X = 86;
    public static final int PRIMARY_PROGRESS_Y = 69;
    public static final int PRIMARY_PROGRESS_U = 176;
    public static final int PRIMARY_PROGRESS_V = 83;
    public static final int PRIMARY_PROGRESS_WIDTH = 3;
    public static final int PRIMARY_PROGRESS_HEIGHT = 7;
    public static final int SECONDARY_PROGRESS_X = 86;
    public static final int SECONDARY_PROGRESS_Y = 74;
    public static final int SECONDARY_PROGRESS_U = 176;
    public static final int SECONDARY_PROGRESS_V = 101;
    public static final int SECONDARY_PROGRESS_WIDTH = 38;
    public static final int SECONDARY_PROGRESS_HEIGHT = 3;
    public static final int TERTIARY_PROGRESS_X = 121;
    public static final int TERTIARY_PROGRESS_Y = 56;
    public static final int TERTIARY_PROGRESS_U = 211;
    public static final int TERTIARY_PROGRESS_V = 83;
    public static final int TERTIARY_PROGRESS_WIDTH = 3;
    public static final int TERTIARY_PROGRESS_HEIGHT = 21;
    public static final int QUATERNARY_PROGRESS_X = 121;
    public static final int QUATERNARY_PROGRESS_Y = 56;
    public static final int QUATERNARY_PROGRESS_U = 211;
    public static final int QUATERNARY_PROGRESS_V = 83;
    public static final int QUATERNARY_PROGRESS_WIDTH = 21;
    public static final int QUATERNARY_PROGRESS_HEIGHT = 3;

    private float progress;

    public FuelLoaderScreen(FuelLoaderMenu handler, Inventory inv, Component title) {
        super(handler, title, Constant.ScreenTexture.FUEL_LOADER_SCREEN);
        this.progress = -80.0F;
    }

    @Override
    protected void renderMachineBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderMachineBackground(graphics, mouseX, mouseY, delta);

        if (this.menu.rocketCapacity == 0) {
            graphics.blit(Constant.ScreenTexture.FUEL_LOADER_SCREEN, this.leftPos + RED_CROSS_X, this.topPos + RED_CROSS_Y, RED_CROSS_U, RED_CROSS_V, RED_CROSS_WIDTH, RED_CROSS_HEIGHT);
        }

        GraphicsUtil.drawFluid(graphics, this.leftPos + ROCKET_FACE_X, this.topPos + ROCKET_FACE_Y, ROCKET_FACE_WIDTH, ROCKET_FACE_HEIGHT, this.menu.rocketCapacity, FluidVariant.of(GCFluids.FUEL), this.menu.rocketAmount);

        if (!DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + ROCKET_FACE_X, this.topPos + ROCKET_FACE_Y, ROCKET_FACE_WIDTH, ROCKET_FACE_HEIGHT)) {
            graphics.blit(Constant.ScreenTexture.FUEL_LOADER_SCREEN, this.leftPos + ROCKET_FACE_X, this.topPos + ROCKET_FACE_Y, ROCKET_FACE_U, ROCKET_FACE_V, ROCKET_FACE_WIDTH, ROCKET_FACE_HEIGHT);
        }

        this.drawProgressBar(graphics.pose());
    }

    private void drawProgressBar(PoseStack matrices) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.FUEL_LOADER_SCREEN);
        progress += 0.2F;
        if (progress < 0.0F) {
            this.increaseProgressBar(matrices, progress + 80.0F);
        } else {
            this.decreaseProgressBar(matrices, progress);
        }
    }

    private void increaseProgressBar(PoseStack matrices, float progress) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.FUEL_LOADER_SCREEN);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + PRIMARY_PROGRESS_X, this.topPos + PRIMARY_PROGRESS_Y, PRIMARY_PROGRESS_U, PRIMARY_PROGRESS_V, PRIMARY_PROGRESS_WIDTH, Math.min(progress, PRIMARY_PROGRESS_HEIGHT));
        float startNext = PRIMARY_PROGRESS_HEIGHT - 1.0F;
        if (progress > startNext) {
            DrawableUtil.drawProgressTexture(matrices, this.leftPos + SECONDARY_PROGRESS_X, this.topPos + SECONDARY_PROGRESS_Y, SECONDARY_PROGRESS_U, SECONDARY_PROGRESS_V, Math.min(progress - startNext, SECONDARY_PROGRESS_WIDTH), SECONDARY_PROGRESS_HEIGHT);
            startNext += SECONDARY_PROGRESS_WIDTH - 1.0F;
            if (progress > startNext) {
                startNext += TERTIARY_PROGRESS_HEIGHT - 1.0F;
                DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_U, TERTIARY_PROGRESS_V, TERTIARY_PROGRESS_WIDTH, TERTIARY_PROGRESS_HEIGHT);
                DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_X, TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_WIDTH, Mth.clamp(startNext - progress, 0.0F, TERTIARY_PROGRESS_HEIGHT - 3.0F));
                if (progress > startNext) {
                    DrawableUtil.drawProgressTexture(matrices, this.leftPos + QUATERNARY_PROGRESS_X, this.topPos + QUATERNARY_PROGRESS_Y, QUATERNARY_PROGRESS_U, QUATERNARY_PROGRESS_V, Math.min(progress - startNext, QUATERNARY_PROGRESS_WIDTH), QUATERNARY_PROGRESS_HEIGHT);
                }
            }
        }
    }

    private void staticProgressBar(PoseStack matrices) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.FUEL_LOADER_SCREEN);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + PRIMARY_PROGRESS_X, this.topPos + PRIMARY_PROGRESS_Y, PRIMARY_PROGRESS_U, PRIMARY_PROGRESS_V, PRIMARY_PROGRESS_WIDTH, PRIMARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + SECONDARY_PROGRESS_X, this.topPos + SECONDARY_PROGRESS_Y, SECONDARY_PROGRESS_U, SECONDARY_PROGRESS_V,SECONDARY_PROGRESS_WIDTH, SECONDARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_X, TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_WIDTH, TERTIARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + QUATERNARY_PROGRESS_X, this.topPos + QUATERNARY_PROGRESS_Y, QUATERNARY_PROGRESS_U, QUATERNARY_PROGRESS_V, QUATERNARY_PROGRESS_WIDTH, QUATERNARY_PROGRESS_HEIGHT);
    }

    private void decreaseProgressBar(PoseStack matrices, float progress) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.FUEL_LOADER_SCREEN);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + PRIMARY_PROGRESS_X, this.topPos + PRIMARY_PROGRESS_Y, PRIMARY_PROGRESS_U, PRIMARY_PROGRESS_V, PRIMARY_PROGRESS_WIDTH, PRIMARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + SECONDARY_PROGRESS_X, this.topPos + SECONDARY_PROGRESS_Y, SECONDARY_PROGRESS_U, SECONDARY_PROGRESS_V,SECONDARY_PROGRESS_WIDTH, SECONDARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_U, TERTIARY_PROGRESS_V, TERTIARY_PROGRESS_WIDTH, TERTIARY_PROGRESS_HEIGHT);
        DrawableUtil.drawProgressTexture(matrices, this.leftPos + QUATERNARY_PROGRESS_X, this.topPos + QUATERNARY_PROGRESS_Y, QUATERNARY_PROGRESS_U, QUATERNARY_PROGRESS_V, QUATERNARY_PROGRESS_WIDTH, QUATERNARY_PROGRESS_HEIGHT);

        DrawableUtil.drawProgressTexture(matrices, this.leftPos + PRIMARY_PROGRESS_X, this.topPos + PRIMARY_PROGRESS_Y, PRIMARY_PROGRESS_X, PRIMARY_PROGRESS_Y, PRIMARY_PROGRESS_WIDTH, Math.min(progress, PRIMARY_PROGRESS_HEIGHT - 2.0F));
        float startNext = PRIMARY_PROGRESS_HEIGHT - 1.0F;
        if (progress > startNext) {
            DrawableUtil.drawProgressTexture(matrices, this.leftPos + SECONDARY_PROGRESS_X, this.topPos + SECONDARY_PROGRESS_Y, SECONDARY_PROGRESS_X, SECONDARY_PROGRESS_Y, Math.min(progress - startNext, SECONDARY_PROGRESS_WIDTH), SECONDARY_PROGRESS_HEIGHT);
            startNext += SECONDARY_PROGRESS_WIDTH - 1.0F;
            if (progress > startNext) {
                startNext += TERTIARY_PROGRESS_HEIGHT - 1.0F;
                DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_X, TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_WIDTH, TERTIARY_PROGRESS_HEIGHT);
                DrawableUtil.drawProgressTexture(matrices, this.leftPos + TERTIARY_PROGRESS_X, this.topPos + TERTIARY_PROGRESS_Y, TERTIARY_PROGRESS_U, TERTIARY_PROGRESS_V, TERTIARY_PROGRESS_WIDTH, Mth.clamp(startNext - progress, 0.0F, TERTIARY_PROGRESS_HEIGHT - 3.0F));
                if (progress > startNext) {
                    DrawableUtil.drawProgressTexture(matrices, this.leftPos + QUATERNARY_PROGRESS_X, this.topPos + QUATERNARY_PROGRESS_Y, QUATERNARY_PROGRESS_X, QUATERNARY_PROGRESS_Y, Math.min(progress - startNext, QUATERNARY_PROGRESS_WIDTH), QUATERNARY_PROGRESS_HEIGHT);
                }
            }
        }
    }

    @Override
    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        List<Component> list = new ArrayList<>();
        if (DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + TANK_OVERLAY_X, this.topPos + TANK_OVERLAY_Y, TANK_OVERLAY_WIDTH, TANK_OVERLAY_HEIGHT)) {
            FluidResourceSlot slot = this.menu.fluidStorage.slot(FuelLoaderBlockEntity.FUEL_TANK);
            DisplayUtil.createFluidTooltip(list, slot.getResource(), slot.getComponents(), slot.getAmount(), slot.getCapacity());
        } else if (DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + ROCKET_FACE_X, this.topPos + ROCKET_FACE_Y, ROCKET_FACE_WIDTH, ROCKET_FACE_HEIGHT)) {
            DisplayUtil.createFluidTooltip(list, GCFluids.FUEL, null, this.menu.rocketAmount, this.menu.rocketCapacity);
        }

        if (!list.isEmpty()) {
            setTooltipForNextRenderPass(Lists.transform(list, Component::getVisualOrderText));
        }
    }

    @Override
    protected void drawTanks(GuiGraphics graphics, int mouseX, int mouseY) {
        super.drawTanks(graphics, mouseX, mouseY);
        
        FluidResourceSlot slot = this.menu.fluidStorage.slot(FuelLoaderBlockEntity.FUEL_TANK);
        if (!slot.isEmpty()) {
            graphics.blit(Constant.ScreenTexture.FUEL_LOADER_SCREEN, this.leftPos + TANK_OVERLAY_X, this.topPos + TANK_OVERLAY_Y, TANK_OVERLAY_U, TANK_OVERLAY_V, TANK_OVERLAY_WIDTH, TANK_OVERLAY_HEIGHT);
        }
    }
}