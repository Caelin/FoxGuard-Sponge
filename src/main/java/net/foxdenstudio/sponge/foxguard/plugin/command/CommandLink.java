/*
 * This file is part of FoxGuard, licensed under the MIT License (MIT).
 *
 * Copyright (c) gravityfox - https://gravityfox.net/
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.foxdenstudio.sponge.foxguard.plugin.command;

import com.google.common.collect.ImmutableList;
import net.foxdenstudio.sponge.foxcore.plugin.command.FCCommandBase;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.AdvCmdParser;
import net.foxdenstudio.sponge.foxcore.plugin.command.util.FlagMapper;
import net.foxdenstudio.sponge.foxcore.plugin.state.FCStateManager;
import net.foxdenstudio.sponge.foxguard.plugin.FGManager;
import net.foxdenstudio.sponge.foxguard.plugin.handler.GlobalHandler;
import net.foxdenstudio.sponge.foxguard.plugin.handler.IHandler;
import net.foxdenstudio.sponge.foxguard.plugin.region.IRegion;
import net.foxdenstudio.sponge.foxguard.plugin.state.HandlersStateField;
import net.foxdenstudio.sponge.foxguard.plugin.state.RegionsStateField;
import net.foxdenstudio.sponge.foxguard.plugin.util.FGUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

import static net.foxdenstudio.sponge.foxcore.plugin.util.Aliases.WORLD_ALIASES;
import static net.foxdenstudio.sponge.foxcore.plugin.util.Aliases.isIn;

public class CommandLink extends FCCommandBase {

    private static final FlagMapper mapper = map -> key -> value -> {
        map.put(key, value);
        if (isIn(WORLD_ALIASES, key) && !map.containsKey("world")) {
            map.put("world", value);
        }
    };

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            source.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this command!"));
            return CommandResult.empty();
        }
        AdvCmdParser.ParseResult parse = AdvCmdParser.builder().arguments(arguments).flagMapper(mapper).parse();

        if (parse.args.length == 0) {
            if (FGUtil.getSelectedRegions(source).size() == 0 &&
                    FGUtil.getSelectedHandlers(source).size() == 0)
                throw new CommandException(Text.of("You don't have any regions or handlers in your state buffer!"));
            if (FGUtil.getSelectedRegions(source).size() == 0)
                throw new CommandException(Text.of("You don't have any regions in your state buffer!"));
            if (FGUtil.getSelectedHandlers(source).size() == 0)
                throw new CommandException(Text.of("You don't have any handlers in your state buffer!"));
            int[] successes = {0};
            FGUtil.getSelectedRegions(source).forEach(
                    region -> FGUtil.getSelectedHandlers(source).stream()
                            .filter(handler -> !(handler instanceof GlobalHandler))
                            .forEach(handler -> successes[0] += FGManager.getInstance().link(region, handler) ? 1 : 0));
            source.sendMessage(Text.of(TextColors.GREEN, "Successfully formed " + successes[0] + " links!"));
            FCStateManager.instance().getStateMap().get(source).flush(RegionsStateField.ID, HandlersStateField.ID);
            return CommandResult.builder().successCount(successes[0]).build();
        } else {
            if (parse.args.length < 1) throw new CommandException(Text.of("Must specify items to link!"));
            IRegion region = FGManager.getInstance().getRegion(parse.args[0]);
            World world = null;
            if (region == null) {
                String worldName = parse.flags.get("world");
                if (source instanceof Player) world = ((Player) source).getWorld();
                if (!worldName.isEmpty()) {
                    Optional<World> optWorld = Sponge.getGame().getServer().getWorld(worldName);
                    if (optWorld.isPresent()) {
                        world = optWorld.get();
                    } else {
                        if (world == null)
                            throw new CommandException(Text.of("No world exists with name \"" + worldName + "\"!"));
                    }
                }
                if (world == null) throw new CommandException(Text.of("Must specify a world!"));
                region = FGManager.getInstance().getWorldRegion(world, parse.args[0]);
            }
            if (region == null)
                throw new CommandException(Text.of("No region with name \"" + parse.args[0] + "\" in world \"" + world.getName() + "\"!"));
            if (parse.args.length < 2) throw new CommandException(Text.of("Must specify a handler!"));
            IHandler handler = FGManager.getInstance().gethandler(parse.args[1]);
            if (handler == null)
                throw new CommandException(Text.of("No handler with name \"" + parse.args[1] + "\"!"));
            if (region.getHandlers().contains(handler))
                throw new CommandException(Text.of("Already linked!"));
            boolean success = FGManager.getInstance().link(region, handler);
            if (success) {
                source.sendMessage(Text.of(TextColors.GREEN, "Successfully linked!"));
                return CommandResult.success();
            } else {
                source.sendMessage(Text.of(TextColors.RED, "There was an error while trying to link."));
                return CommandResult.empty();
            }
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("foxguard.command.modify.link.link");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("link [ [--w:<worldname>] <region name> <handler name> ]");
    }
}
