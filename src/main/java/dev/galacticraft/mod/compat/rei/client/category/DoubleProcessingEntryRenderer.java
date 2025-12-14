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

package dev.galacticraft.mod.compat.rei.client.category;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class DoubleProcessingEntryRenderer implements EntryRenderer<ItemStack> {
    public static final EntryRenderer INSTANCE = new DoubleProcessingEntryRenderer();
    private final Minecraft minecraft = Minecraft.getInstance();
    private long lastCycleTime = -1;

    @Override
    public void render(EntryStack<ItemStack> entryStack, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (entryStack.isEmpty()) {
            return;
        }
        ItemStack itemStack = entryStack.getValue();

        long currentTime = System.currentTimeMillis();
        if (this.lastCycleTime == -1) {
            this.lastCycleTime = currentTime;
        }
        if (currentTime > this.lastCycleTime + 2000) {
            this.lastCycleTime = currentTime;
        } else if (currentTime > this.lastCycleTime + 500 && currentTime <= this.lastCycleTime + 1500) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() * 2);
        }

        graphics.renderItem(itemStack, bounds.x, bounds.y);
        graphics.renderItemDecorations(this.minecraft.font, itemStack, bounds.x, bounds.y);
    }

    @Override
    public Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context) {
        return Tooltip.create(entry.getValue().getTooltipLines(
                Item.TooltipContext.of(this.minecraft.level), this.minecraft.player, context.getFlag()
        ));
    }
}