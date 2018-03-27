package io.github.elifoster.oredicttips;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mod(modid = "oredicttips", name = "OreDictTips", version = "2.0.1", clientSideOnly = true, acceptedMinecraftVersions = "[1.12,1.12.2]")
public class OreDictTips {
    private ConfigRequirement configRequirement;
    private static KeyBinding key = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        Configuration configuration = new Configuration(configFile);

        List<String> descriptions = new ArrayList<>();
        for (ConfigRequirement req : ConfigRequirement.values()) {
            descriptions.add(String.format("%d: %s", req.ordinal(), req.getConfigurationDescription()));
        }

        String configDesc = String.join(", ", descriptions);

        configRequirement = ConfigRequirement.values()[
          configuration.getInt("Toggle", "OreDictTips", 0, 0, ConfigRequirement.values().length - 1, configDesc)];

        configuration.save();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        key = new KeyBinding("key.oredicttips.desc", Keyboard.KEY_LSHIFT, "key.oredicttips.category");
        ClientRegistry.registerKeyBinding(key);
    }

    @SubscribeEvent
    public void addOreDictTips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (configRequirement.canShowTooltips(event.getFlags().isAdvanced()) && !stack.isEmpty()) {
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids) {
                event.getToolTip().add(" * " + TextFormatting.DARK_GREEN + OreDictionary.getOreName(id));
            }
        }
    }

    private enum ConfigRequirement {
        ALWAYS("Always show the entries", (isDebugMode) -> true),
        DEBUG("Show when the F3+H debug mode is enabled", (isDebugMode) -> isDebugMode),
        KEYBIND("Show when holding a keybind", (isDebugMode) -> Keyboard.isKeyDown(key.getKeyCode())),
        DEBUG_KEYBIND("Show when the F3+H debug mode is enabled and holding a keybind",
          (isDebugMode) -> DEBUG.canShowTooltips(isDebugMode) && KEYBIND.canShowTooltips(isDebugMode));

        private final String configurationDescription;
        private final Predicate<Boolean> canShowTooltipsPredicate;

        ConfigRequirement(String configurationDescription, Predicate<Boolean> canShowTooltipsPredicate) {
            this.configurationDescription = configurationDescription;
            this.canShowTooltipsPredicate = canShowTooltipsPredicate;
        }

        private boolean canShowTooltips(boolean isDebugMode) {
            return canShowTooltipsPredicate.test(isDebugMode);
        }

        private String getConfigurationDescription() {
            return configurationDescription;
        }
    }
}
