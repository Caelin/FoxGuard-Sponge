/*
 * This file is part of FoxGuard, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015. gravityfox - https://gravityfox.net/
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

package net.gravityfox.foxguard.flagsets;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import net.gravityfox.foxguard.pieces.IOwnable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Fox on 10/26/2015.
 * Project: foxguard
 */
abstract public class OwnableFlagSetBase extends FlagSetBase implements IOwnable {

    protected List<User> ownerList = new LinkedList<>();

    public OwnableFlagSetBase(String name, int priority) {
        super(name, priority);
    }

    @Override
    public boolean removeOwner(User user) {
        return ownerList.remove(user);
    }

    @Override
    public boolean addOwner(User user) {
        return ownerList.add(user);
    }

    @Override
    public List<User> getOwners() {
        return ownerList;
    }

    @Override
    public void setOwners(List<User> owners) {
        this.ownerList = owners;
    }

    @Override
    public Text getDetails(String arguments) {
        TextBuilder builder = Texts.builder();
        builder.append(Texts.of(TextColors.GREEN, "Owners: "));
        for (User p : ownerList) {
            builder.append(Texts.of(TextColors.RESET, p.getName() + " "));
        }
        return builder.build();
    }
}