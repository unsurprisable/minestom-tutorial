package org.example;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.example.commands.GMACommand;
import org.example.commands.GMCCommand;
import org.example.commands.GMSCommand;
import org.example.commands.GMSPCommand;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

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
        EventNode<PlayerEvent> playerMovementNode;
        {
            //region Player Join Configuration
            GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

            handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                final Player player = event.getPlayer();
                event.setSpawningInstance(instance);
                player.setRespawnPoint(new Pos(0, 42, 0));
            });
            handler.addListener(PlayerSpawnEvent.class, event -> {
                final Player player = event.getPlayer();
                player.setAllowFlying(true);
            });
            //endregion

            //region Item Entity Interactions
            EventNode<Event> itemDropsNode = EventNode.all("itemDrops");
            handler.addChild(itemDropsNode);

            // Item Pickups
            itemDropsNode.addListener(PickupItemEvent.class, event -> {
                if (!(event.getLivingEntity() instanceof Player player)) return;
                player.getInventory().addItemStack(event.getItemStack());
            });

            // Player Drops
            itemDropsNode.addListener(ItemDropEvent.class, event -> {
                final Player player = event.getPlayer();

                final float itemVelocity = 6f;
                ItemEntity item = new ItemEntity(event.getItemStack());
                item.setInstance(event.getInstance(), player.getPosition().add(0, 1.42, 0));
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

                final float horzVelocity = 1.45f;
                final float upVelocityMin = 1.4f;
                final float upVelocityExtra = 1.85f;
                item.setVelocity(new Vec(ThreadLocalRandom.current().nextFloat(2), 0, ThreadLocalRandom.current().nextFloat(2))
                    .sub(1, 0, 1) // range of -1 to 1
                    .mul(horzVelocity)
                    .add(0, upVelocityMin + ThreadLocalRandom.current().nextFloat(upVelocityExtra), 0));
            });
            //endregion

            //region Custom Player Movement
            playerMovementNode = EventNode.value("playerMovement", EventFilter.PLAYER, (player) -> player.getGameMode() != GameMode.CREATIVE);
            handler.addChild(playerMovementNode);

            // Horizontal Dash
            playerMovementNode.addListener(PlayerSwapItemEvent.class, event -> {
                final Player player = event.getPlayer();
                event.setCancelled(true);

                final float speed = 45f;
                final float vertInfluence = 0.06f;
                final float extraVertVelocity = 4.5f;
                player.setVelocity(player.getPosition().direction()
                    .mul(1, vertInfluence, 1)
                    .normalize()
                    .mul(speed)
                    .add(0, extraVertVelocity, 0));
            });

            // Double Jump
            playerMovementNode.addListener(PlayerStartFlyingEvent.class, event -> {
                final Player player = event.getPlayer();
                player.setFlying(false);

                final float speed = 2f;
                final float horzInfluence = 0.5f;
                final float extraVertVelocity = 20.5f;
                player.setVelocity(player.getVelocity().withY(0)
                    .add(player.getPosition().direction()
                        .mul(horzInfluence, 1, horzInfluence)
                        .normalize()
                        .mul(speed)
                        .add(0, extraVertVelocity, 0)));
            });
            //endregion
        }

        //region Commands
        new GMCCommand().register();
        new GMSCommand().register();
        new GMACommand().register();
        new GMSPCommand().register();
        //endregion

//        MojangAuth.init();
        server.start("0.0.0.0", 25565);
    }
}