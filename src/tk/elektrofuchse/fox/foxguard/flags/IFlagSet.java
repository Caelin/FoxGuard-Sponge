package tk.elektrofuchse.fox.foxguard.flags;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import tk.elektrofuchse.fox.foxguard.flags.util.ActiveFlags;
import tk.elektrofuchse.fox.foxguard.flags.util.FlagState;
import tk.elektrofuchse.fox.foxguard.flags.util.PassiveFlags;

/**
 * Created by Fox on 8/17/2015.
 */
public interface IFlagSet extends Comparable<IFlagSet> {

    FlagState hasPermission(Player player, ActiveFlags flag);

    FlagState isFlagAllowed(PassiveFlags flag);

    boolean isEnabled();

    void setIsEnabled(boolean state);

    int getPriority();

    void setPriority(int priority);

    String getName();

    void setName(String name);

    String getType();

    Text getDetails(String[] args);

}
