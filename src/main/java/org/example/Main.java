package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {

        MinecraftServer server = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        });
        instance.setChunkSupplier(LightingChunk::new);


        // Events
        {
            GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

            handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                final Player player = event.getPlayer();
                event.setSpawningInstance(instance);
                player.setRespawnPoint(new Pos(0, 42, 0));
            });

            EventNode<ItemEvent> itemDropsNode = EventNode.type("itemDrops", EventFilter.ITEM);
            handler.addChild(itemDropsNode);

            // Item Pickups
            itemDropsNode.addListener(PickupItemEvent.class, event -> {
                if (!(event.getLivingEntity() instanceof Player player)) return;
                
            });

            // Player Drops
            itemDropsNode.addListener(ItemDropEvent.class, event -> {
                final Player player = event.getPlayer();
                final float itemVelocity = 10f;
                ItemEntity item = new ItemEntity(event.getItemStack());
                item.setInstance(event.getInstance(), player.getPosition().add(0, 1, 0));
                item.setVelocity(player.getPosition().direction().mul(itemVelocity));
                item.setPickupDelay(Duration.ofMillis(500));
            });

            // Block Drops
            itemDropsNode.addListener(PlayerBlockBreakEvent.class, event -> {
                final Material material = event.getBlock().registry().material();
                if (material == null) return;
                final ItemStack itemStack = ItemStack.of(material);
                ItemEntity item = new ItemEntity(itemStack);
                item.setInstance(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
                item.setPickupDelay(Duration.ofMillis(50));
            });
        }

        MojangAuth.init();
        server.start("0.0.0.0", 25565);

    }
}