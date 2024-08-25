package org.example.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

public class GiveCommand extends CustomCommand {

    private final ArgumentEntity playerArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

    private final ArgumentItemStack itemArgument = ArgumentType.ItemStack("item");
    private final ArgumentInteger amountArgument = ArgumentType.Integer("amount");

    public GiveCommand() {
        super("give");

        addSyntax(this::syntaxExecutor, itemArgument);
        addSyntax(this::syntaxExecutor, itemArgument, amountArgument);
        addSyntax(this::syntaxExecutor, playerArgument, itemArgument);
        addSyntax(this::syntaxExecutor, playerArgument, itemArgument, amountArgument);
    }

    @Override
    public void defaultExecutor(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player)) return;
        player.sendMessage(Component.text("Incorrect usage: /give <target> <item> <amount>"));
    }

    private void syntaxExecutor(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player)) return;

        EntityFinder finder = context.get(playerArgument);
        Player target;

        if (finder == null) {
            target = player;
        } else {
            target = (Player) finder.findFirstPlayer(sender);
            if (target == null) target = player;
        }

        ItemStack itemStack = context.get(itemArgument);

        Integer amount = context.get(amountArgument);
        if (amount == null) amount = 1;

        target.getInventory().addItemStack(itemStack.withAmount(amount));
    }
}
