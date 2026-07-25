package net.werdei.serverhats;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.werdei.serverhats.command.HatsCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerHats implements ModInitializer
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LOG_PREFIX = "[ServerHats]: ";

    private static Set<Item> allowedItems = new HashSet<>();
    private static boolean itemListsInitialized = false;
    private static RegistryWrapper<Item> itemRegistryWrapper;

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
        {
            HatsCommand.register(dispatcher, registryAccess);
            itemRegistryWrapper = registryAccess.getOrThrow(RegistryKeys.ITEM);
            reloadConfig();
        }));
    }

    public static void reloadConfig()
    {
        reloadConfig(null, null);
    }

    public static void reloadConfig(OnOutput info, OnOutput warning)
    {
        if (info == null) info = ServerHats::log;
        if (warning == null) warning = ServerHats::warn;

        Config.load();
        Config.save();

        recalculateItemLists(info, warning);

        String itemCount = Config.allowAllItems ? "all" : Integer.toString(allowedItems.size());
        info.sendMessage("Successfully added ability to equip " + itemCount + " items");
    }

    public static void recalculateItemLists(OnOutput info, OnOutput warning)
    {
        if (info == null) info = ServerHats::log;
        if (warning == null) warning = ServerHats::warn;

        itemListsInitialized = false;
        allowedItems = new HashSet<>();

        if (Config.allowedItems == null)
        {
            warning.sendMessage("allowedItems is missing or null; no custom hats will be added");
            itemListsInitialized = true;
            return;
        }

        if (itemRegistryWrapper == null)
        {
            warning.sendMessage("Item registry is not ready yet; allowed item list will be built when commands initialize");
            return;
        }

        List.of(Config.allowedItems).forEach(string ->
        {
            try
            {
                parseAllowedEntry(string, warning);
            }
            catch (Exception e)
            {
                warning.sendMessage("Skipping \"" + string + "\": " + e.getMessage());
            }
        });
        itemListsInitialized = true;
    }

    private static void parseAllowedEntry(String string, OnOutput warning)
    {
        boolean isTag = string.startsWith("#");
        String rawId = isTag ? string.substring(1) : string;
        Identifier id = Identifier.of(rawId);

        if (isTag)
        {
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, id);
            var tag = itemRegistryWrapper.getOptional(tagKey);
            if (tag.isEmpty())
            {
                warning.sendMessage("Skipping \"" + string + "\": Unknown item tag");
                return;
            }
            tag.get().forEach(entry -> addAllowedItem(entry.value(), warning));
        }
        else
        {
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
            var entry = itemRegistryWrapper.getOptional(itemKey);
            if (entry.isEmpty())
            {
                warning.sendMessage("Skipping \"" + string + "\": Unknown item");
                return;
            }
            addAllowedItem(entry.get().value(), warning);
        }
    }

    private static void addAllowedItem(Item item, OnOutput warning)
    {
        if (isItemRestricted(item))
            warning.sendMessage("Skipping \"" + item.getName().getString() + "\": The item can already be equipped in a helmet slot");
        else
            allowedItems.add(item);
    }

    public static boolean isItemAllowed(ItemStack stack)
    {
        if (!itemListsInitialized) return false;
        if (Config.allowAllItems) return !isItemRestricted(stack);
        return allowedItems.contains(stack.getItem());
    }

    private static boolean isItemRestricted(Item item)
    {
        return isItemRestricted(new ItemStack(item));
    }

    private static boolean isItemRestricted(ItemStack stack)
    {
        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.HEAD;
    }


    // Logging

    public static void log(Object message)
    {
        LOGGER.info(LOG_PREFIX + message);
    }

    public static void warn(Object message)
    {
        LOGGER.warn(LOG_PREFIX + message);
    }

    public interface OnOutput
    {
        void sendMessage(String message);
    }
}
