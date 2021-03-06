package de.photon.aacadditionpro.modules.checks.packetanalysis;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.photon.aacadditionpro.AACAdditionPro;
import de.photon.aacadditionpro.modules.ModuleType;
import de.photon.aacadditionpro.modules.PacketListenerModule;
import de.photon.aacadditionpro.user.User;
import de.photon.aacadditionpro.user.UserManager;
import de.photon.aacadditionpro.util.mathematics.MathUtils;
import de.photon.aacadditionpro.util.messaging.VerboseSender;
import de.photon.aacadditionpro.util.packetwrappers.client.IWrapperPlayClientLook;
import lombok.Getter;

public class IllegalPitchPattern extends PacketAdapter implements PacketListenerModule
{
    @Getter
    private static final IllegalPitchPattern instance = new IllegalPitchPattern();

    IllegalPitchPattern()
    {
        super(AACAdditionPro.getInstance(), ListenerPriority.LOW,
              PacketType.Play.Client.LOOK, PacketType.Play.Client.POSITION_LOOK);
    }


    @Override
    public void onPacketReceiving(final PacketEvent packetEvent)
    {
        final User user = UserManager.safeGetUserFromPacketEvent(packetEvent);

        if (User.isUserInvalid(user, this.getModuleType())) {
            return;
        }

        final IWrapperPlayClientLook lookWrapper = packetEvent::getPacket;
        PacketAnalysis.getInstance().getViolationLevelManagement().flag(user.getPlayer(), MathUtils.inRange(-90, 90, lookWrapper.getPitch()) ?
                                                                                          0 :
                                                                                          20,
                                                                        -1, () -> {},
                                                                        () -> VerboseSender.getInstance().sendVerboseMessage("PacketAnalysisData-Verbose | Player: " + user.getPlayer().getName() + " sent illegal pitch value."));
    }

    @Override
    public boolean isSubModule()
    {
        return true;
    }

    @Override
    public String getConfigString()
    {
        return this.getModuleType().getConfigString() + ".parts.IllegalPitch";
    }

    @Override
    public ModuleType getModuleType()
    {
        return ModuleType.PACKET_ANALYSIS;
    }
}
