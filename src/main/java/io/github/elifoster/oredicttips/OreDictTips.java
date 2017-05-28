package io.github.elifoster.oredicttips;

import net.minecraft.client.settings.KeyBinding;
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

@Mod(modid = "oredicttips", name = "OreDictTips", version = "1.0.0", clientSideOnly = true, acceptedMinecraftVersions = "[1.10,1.11.2]")
public class OreDictTips {
    private ConfigRequirement configRequirement;
    private KeyBinding key = null;

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

        if (configRequirement.requiresKeybind()) {
            key = new KeyBinding("key.oredicttips.desc", Keyboard.KEY_LSHIFT, "key.oredicttips.category");
            ClientRegistry.registerKeyBinding(key);
        }
    }

    @SubscribeEvent
    public void addOreDictTips(ItemTooltipEvent event) {
        if (canShowTooltips(event.isShowAdvancedItemTooltips())) {
            int[] ids = OreDictionary.getOreIDs(event.getItemStack());
            for (int id : ids) {
                event.getToolTip().add(" * " + TextFormatting.DARK_GREEN + OreDictionary.getOreName(id));
            }
        }
    }

    private boolean canShowTooltips(boolean debug) {
        if (configRequirement == ConfigRequirement.ALWAYS) {
            return true;
        }

        boolean keyBind = true;

        if (configRequirement.requiresKeybind()) {
            // Cannot use KeyBinding#isKeyDown
            keyBind = Keyboard.isKeyDown(key.getKeyCode());
        }

        if (!configRequirement.requiresDebug()) {
            debug = true;
        }

        return keyBind && debug;
    }

    private enum ConfigRequirement {
        ALWAYS("Always show the entries"),
        DEBUG("Show when the F3+H debug mode is enabled"),
        KEYBIND("Show when holding a keybind"),
        DEBUG_KEYBIND("Show when the F3+H debug mode is enabled and holding a keybind");

        private final String configurationDescription;

        ConfigRequirement(String configurationDescription) {
            this.configurationDescription = configurationDescription;
        }

        private String getConfigurationDescription() {
            return configurationDescription;
        }

        private boolean requiresKeybind() {
            return this == KEYBIND || this == DEBUG_KEYBIND;
        }

        private boolean requiresDebug() {
            return this == DEBUG || this == DEBUG_KEYBIND;
        }
    }
}
