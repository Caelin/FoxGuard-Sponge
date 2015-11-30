/*
 * This file is part of FoxGuard, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2015. gravityfox - https://gravityfox.net/ and contributors.
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

package net.gravityfox.foxguard.factory;

import net.gravityfox.foxguard.FoxGuardMain;
import net.gravityfox.foxguard.commands.util.InternalCommandState;
import net.gravityfox.foxguard.handlers.IHandler;
import net.gravityfox.foxguard.handlers.PassiveHandler;
import net.gravityfox.foxguard.handlers.PermissionHandler;
import net.gravityfox.foxguard.handlers.SimpleHandler;
import net.gravityfox.foxguard.handlers.util.Flags;
import net.gravityfox.foxguard.util.Aliases;
import net.gravityfox.foxguard.util.CallbackHashMap;
import net.gravityfox.foxguard.util.FGHelper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Fox on 10/25/2015.
 * Project: foxguard
 */
public class FGHandlerFactory implements IHandlerFactory {

    String[] simpleAliases = {"simple", "simp"};
    String[] passiveAliases = {"passive", "pass"};
    String[] permissionAliases = {"permission", "permissions", "perm", "perms"};
    String[] types = {"simple", "passive", "permission"};

    @Override
    public String[] getAliases() {
        return FGHelper.concatAll(simpleAliases, passiveAliases, permissionAliases);
    }

    @Override
    public String[] getTypes() {
        return types;
    }

    @Override
    public IHandler createHandler(String name, String type, int priority, String arguments, InternalCommandState state, CommandSource source) {
        if (Aliases.isAlias(simpleAliases, type)) {
            SimpleHandler handler = new SimpleHandler(name, priority);
            if (source instanceof Player) handler.addOwner((Player) source);
            return handler;
        } else if (Aliases.isAlias(passiveAliases, type)) {
            PassiveHandler handler = new PassiveHandler(name, priority);
            if (source instanceof Player) handler.addOwner((Player) source);
            return handler;
        } else if (Aliases.isAlias(permissionAliases, type)) {
            return new PermissionHandler(name, priority);
        } else return null;
    }

    @Override
    public IHandler createHandler(DataSource source, String name, String type, int priority, boolean isEnabled) throws SQLException {
        if (type.equalsIgnoreCase("simple")) {
            List<User> ownerList = new LinkedList<>();
            List<User> memberList = new LinkedList<>();
            SimpleHandler.PassiveOptions po = SimpleHandler.PassiveOptions.DEFAULT;
            CallbackHashMap<Flags, Tristate> ownerFlagMap = new CallbackHashMap<>((key, map) -> Tristate.TRUE);
            CallbackHashMap<Flags, Tristate> memberFlagMap = new CallbackHashMap<>((key, map) -> Tristate.UNDEFINED);
            CallbackHashMap<Flags, Tristate> defaultFlagMap = new CallbackHashMap<>((key, map) -> Tristate.FALSE);
            try (Connection conn = source.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet ownerSet = statement.executeQuery("SELECT * FROM OWNERS")) {
                        while (ownerSet.next()) {
                            Optional<User> user = FoxGuardMain.getInstance().getUserStorage().get((UUID) ownerSet.getObject("USERUUID"));
                            if (user.isPresent() && !FGHelper.isUserOnList(ownerList, user.get()))
                                ownerList.add(user.get());
                        }
                    }
                    try (ResultSet memberSet = statement.executeQuery("SELECT * FROM MEMBERS")) {
                        while (memberSet.next()) {
                            Optional<User> user = FoxGuardMain.getInstance().getUserStorage().get((UUID) memberSet.getObject("USERUUID"));
                            if (user.isPresent() && !FGHelper.isUserOnList(memberList, user.get()))
                                memberList.add(user.get());
                        }
                    }
                    try (ResultSet mapSet = statement.executeQuery("SELECT * FROM MAP")) {
                        while (mapSet.next()) {
                            String key = mapSet.getString("KEY");
                            switch (key) {
                                case "passive":
                                    try {
                                        po = SimpleHandler.PassiveOptions.valueOf(mapSet.getString("VALUE"));
                                    } catch (IllegalArgumentException ignored) {
                                        po = SimpleHandler.PassiveOptions.PASSTHROUGH;
                                    }
                                    break;
                            }
                        }
                    }
                    try (ResultSet passiveMapEntrySet = statement.executeQuery("SELECT * FROM OWNERFLAGMAP")) {
                        while (passiveMapEntrySet.next()) {
                            try {
                                ownerFlagMap.put(Flags.valueOf(passiveMapEntrySet.getString("KEY")),
                                        Tristate.valueOf(passiveMapEntrySet.getString("VALUE")));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    try (ResultSet passiveMapEntrySet = statement.executeQuery("SELECT * FROM MEMBERFLAGMAP")) {
                        while (passiveMapEntrySet.next()) {
                            try {
                                memberFlagMap.put(Flags.valueOf(passiveMapEntrySet.getString("KEY")),
                                        Tristate.valueOf(passiveMapEntrySet.getString("VALUE")));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    try (ResultSet passiveMapEntrySet = statement.executeQuery("SELECT * FROM DEFAULTFLAGMAP")) {
                        while (passiveMapEntrySet.next()) {
                            try {
                                defaultFlagMap.put(Flags.valueOf(passiveMapEntrySet.getString("KEY")),
                                        Tristate.valueOf(passiveMapEntrySet.getString("VALUE")));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }
            SimpleHandler handler = new SimpleHandler(name, priority, ownerFlagMap, memberFlagMap, defaultFlagMap);
            handler.setOwners(ownerList);
            handler.setMembers(memberList);
            handler.setPassiveOption(po);
            handler.setIsEnabled(isEnabled);
            return handler;
        } else if (type.equalsIgnoreCase("passive")) {
            List<User> ownerList = new LinkedList<>();
            CallbackHashMap<Flags, Tristate> flagMap = new CallbackHashMap<>((key, map) -> Tristate.UNDEFINED);
            try (Connection conn = source.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet ownerSet = statement.executeQuery("SELECT * FROM OWNERS")) {
                        while (ownerSet.next()) {
                            Optional<User> user = FoxGuardMain.getInstance().getUserStorage().get((UUID) ownerSet.getObject("USERUUID"));
                            if (user.isPresent() && !FGHelper.isUserOnList(ownerList, user.get()))
                                ownerList.add(user.get());
                        }
                    }
                    try (ResultSet passiveMapEntrySet = statement.executeQuery("SELECT * FROM FLAGMAP")) {
                        while (passiveMapEntrySet.next()) {
                            try {
                                flagMap.put(Flags.valueOf(passiveMapEntrySet.getString("KEY")),
                                        Tristate.valueOf(passiveMapEntrySet.getString("VALUE")));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }
            PassiveHandler handler = new PassiveHandler(name, priority, flagMap);
            handler.setOwners(ownerList);
            handler.setIsEnabled(isEnabled);
            return handler;
        } else if (type.equalsIgnoreCase("permission")) {
            PermissionHandler handler = new PermissionHandler(name, priority);
            handler.setIsEnabled(isEnabled);
            return handler;
        } else return null;
    }
}
