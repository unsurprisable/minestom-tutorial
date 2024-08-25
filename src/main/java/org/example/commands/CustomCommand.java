package org.example.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomCommand extends Command {
    public CustomCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setDefaultExecutor(this::defaultExecutor);
    }

    public abstract void defaultExecutor(@NotNull CommandSender sender, @NotNull CommandContext context);

    public void register() {
        MinecraftServer.getCommandManager().register(this);
    }
}
