package gregtech.integration.jei.utils;

import gregtech.loaders.recipe.CustomItemReturnShapedOreRecipeRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.gui.CraftingGridHelper;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeWrapper;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class CustomItemReturnRecipeWrapper extends ShapedOreRecipeWrapper implements ICustomCraftingRecipeWrapper {

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    private CraftingGridHelper craftingGridHelper;
    private CustomItemReturnShapedOreRecipeRecipe customRecipe;

    public CustomItemReturnRecipeWrapper(IJeiHelpers jeiHelpers, CustomItemReturnShapedOreRecipeRecipe recipe) {
        super(jeiHelpers, recipe);
        this.customRecipe = recipe;
        this.craftingGridHelper = new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    //straight copy-pasted from JEI CraftingRecipeCategory because JEI
    //doesn't allow just adding tooltip to ingredients without fucking overwriting most of it's setRecipe code
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<List<ItemStack>> outputs = ingredients.getOutputs(ItemStack.class);

        craftingGridHelper.setInputs(guiItemStacks, inputs, getWidth(), getHeight());
        guiItemStacks.set(craftOutputSlot, outputs.get(0));

        ResourceLocation registryName = getRegistryName();
        guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (slotIndex == craftOutputSlot && registryName != null) {
                String recipeModId = registryName.getResourceDomain();

                boolean modIdDifferent = false;
                ResourceLocation itemRegistryName = ingredient.getItem().getRegistryName();
                if (itemRegistryName != null) {
                    String itemModId = itemRegistryName.getResourceDomain();
                    modIdDifferent = !recipeModId.equals(itemModId);
                }

                if (modIdDifferent) {
                    String modName = ForgeModIdHelper.getInstance().getFormattedModNameForModId(recipeModId);
                    tooltip.add(TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
                }

                boolean showAdvanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips || GuiScreen.isShiftKeyDown();
                if (showAdvanced) {
                    tooltip.add(TextFormatting.GRAY + registryName.getResourcePath());
                }
            }

            if(slotIndex != craftOutputSlot) {
                if(customRecipe.shouldItemReturn(ingredient)) {
                    tooltip.add(I18n.format("gregtech.recipe.not_consumed"));
                }
            }
        });
    }
}
