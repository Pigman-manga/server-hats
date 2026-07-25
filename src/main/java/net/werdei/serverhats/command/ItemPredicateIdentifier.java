package net.werdei.serverhats.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemPredicateIdentifier
{
    private static final SimpleCommandExceptionType UNKNOWN_ITEM =
            new SimpleCommandExceptionType(Text.literal("Unknown item or item tag"));

    public Identifier id;
    public boolean isTag;

    public ItemPredicateIdentifier(Identifier id, boolean isTag)
    {
        this.id = id;
        this.isTag = isTag;
    }

    public static ItemPredicateIdentifier fromString(String string, RegistryWrapper<Item> registryWrapper) throws CommandSyntaxException
    {
        boolean isTag = string.startsWith("#");
        String rawId = isTag ? string.substring(1) : string;
        Identifier id = Identifier.tryParse(rawId);
        if (id == null)
            throw UNKNOWN_ITEM.create();

        if (isTag)
        {
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, id);
            if (registryWrapper.getOptional(tagKey).isEmpty())
                throw UNKNOWN_ITEM.create();
            return new ItemPredicateIdentifier(id, true);
        }

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        if (registryWrapper.getOptional(itemKey).isEmpty())
            throw UNKNOWN_ITEM.create();
        return new ItemPredicateIdentifier(id, false);
    }
}
