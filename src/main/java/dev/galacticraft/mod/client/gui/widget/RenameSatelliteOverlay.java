/*
 * Copyright (c) 2019-2026 Team Galacticraft
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

package dev.galacticraft.mod.client.gui.widget;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.impl.universe.celestialbody.type.SatelliteType;
import dev.galacticraft.impl.universe.position.config.SatelliteConfig;
import dev.galacticraft.mod.client.util.Graphics;
import dev.galacticraft.mod.network.c2s.SatelliteUpdatePayload;
import dev.galacticraft.mod.util.DrawableUtil;
import dev.galacticraft.mod.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import static dev.galacticraft.mod.Constant.CelestialScreen.*;

public class RenameSatelliteOverlay implements Renderable, GuiEventListener {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 76;

    private final Minecraft minecraft = Minecraft.getInstance();
    private int centerX;
    private int centerY;
    private int x;
    private int y;

    private @Nullable CelestialBody<SatelliteConfig, SatelliteType> satellite;
    private boolean isVisible = false;
    private String renamingString = "";

    public RenameSatelliteOverlay() {
    }

    public void resize(Minecraft minecraft, int width, int height) {
        this.centerX = width / 2;
        this.centerY = height / 2;
        this.x = this.centerX - WIDTH / 2;
        this.y = this.centerY - HEIGHT / 2;
    }

    public void renameSatellite(CelestialBody<SatelliteConfig, SatelliteType> satellite) {
        this.satellite = satellite;
        this.isVisible = true;
        this.renamingString = satellite.type().getCustomName(satellite.config());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (!this.isVisible) {
            return;
        }

        try (Graphics graphics = Graphics.managed(guiGraphics, this.minecraft.font)) {
            try (Graphics.TextureColor texture = graphics.textureColor(CELESTIAL_SELECTION_1, 512, 512)) {
                texture.blit(this.x, this.y, 179, 67, 159, 0, 179, 67, BLUE);
                texture.blit(this.x + 4, this.y + 2, 171, 10, 159, 92, 171, 10, BLUE);
                texture.blit(this.x + 8, this.y + 18, 161, 13, 159, 67, 161, 13, BLUE);
                texture.blit(this.x + 17, this.y + 59, 72, 12, 159 + 72, 80, -72, 12, BLUE);
                texture.blit(this.centerX, this.y + 59, 72, 12, 159, 80, 72, 12, BLUE);
                texture.drawCenteredText(Component.translatable(Translations.CelestialSelection.ASSIGN_NAME), this.centerX, this.centerY - 35, WHITE);
                texture.drawCenteredText(Component.translatable(Translations.CelestialSelection.APPLY), this.centerX - 36, this.centerY + 23, WHITE);
                texture.drawCenteredText(Component.translatable(Translations.CelestialSelection.CANCEL), this.centerX + 36, this.centerY + 23, WHITE);

                Component text = Component.literal(this.renamingString);
                Component underscore = text.copy().append("_");
                if ((int) (System.currentTimeMillis() / 500) % 2 == 0) {
                    text = underscore;
                }

                texture.drawText(text, this.centerX - this.minecraft.font.width(underscore) / 2, this.centerY - 17, WHITE, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (DrawableUtil.mouseIn(mouseX, mouseY, this.x + 17, this.y + 59, 72, 12) && this.isValid(this.renamingString)) {
            // Apply button pressed
            this.satellite.type().setCustomName(this.renamingString, this.satellite.config());
            ClientPlayNetworking.send(new SatelliteUpdatePayload(this.satellite.config()));

            this.satellite = null;
            this.isVisible = false;
            this.renamingString = "";
            return true;
        } else if (DrawableUtil.mouseIn(mouseX, mouseY, this.centerX, this.y + 59, 72, 12)) {
            // Cancel button pressed
            this.satellite = null;
            this.isVisible = false;
            this.renamingString = "";
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!StringUtil.isNullOrEmpty(this.renamingString)) {
                this.renamingString = this.renamingString.substring(0, this.renamingString.length() - 1);
            }

            return true;
        } else if (Screen.isPaste(key)) {
            assert this.minecraft != null;
            String pasteString = this.minecraft.keyboardHandler.getClipboard();

            if (pasteString.isEmpty()) {
                return false;
            }

            if (this.isValid(this.renamingString + pasteString)) {
                this.renamingString = StringUtil.truncateStringIfNecessary(this.renamingString + pasteString, MAX_SPACE_STATION_NAME_LENGTH, false);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (StringUtil.isAllowedChatCharacter(character)) {
            this.renamingString = StringUtil.truncateStringIfNecessary(this.renamingString + character, MAX_SPACE_STATION_NAME_LENGTH, false);
            return true;
        }

        return false;
    }

    public boolean isValid(String string) {
        if (StringUtil.isNullOrEmpty(string)) {
            return false;
        }

        for (char character : string.toCharArray()) {
            if (!StringUtil.isAllowedChatCharacter(character)) {
                return false;
            }
        }

        return true;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
