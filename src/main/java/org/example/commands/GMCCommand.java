package org.example.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GMCCommand extends CustomCommand {
    public GMCCommand() {
        super("gmc");
    }

    @Override
    public void defaultExecutor(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player)) return;
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(Component.text("Set gamemode to creative", NamedTextColor.GREEN));
    }
}
