import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PresenceListener extends ListenerAdapter {

    private static final String USER_ID = "768557364194312202";
    private static final String GUILD_ID = "1173540104413917234";
    private static final String CHANNEL_ID = "1377506719604805694";


    private OnlineStatus lastKnownStatus = null;
    private long lastStatusChangeTimestamp = 0;

    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        User user = event.getUser();
        if (!user.getId().equals(USER_ID) || user.isBot()) return;

        OnlineStatus newStatus = event.getNewOnlineStatus();

        if (newStatus == lastKnownStatus) return;

        long now = System.currentTimeMillis();
        if (now - lastStatusChangeTimestamp < 1000) return;

        lastKnownStatus = newStatus;
        lastStatusChangeTimestamp = now;

        System.out.println("상태 변경 " + user.getAsTag() + " → " + newStatus);

        Guild guild = Objects.requireNonNull(event.getJDA().getGuildById(GUILD_ID), "서버에러");
        TextChannel textChannel = Objects.requireNonNull(guild.getTextChannelById(CHANNEL_ID), "채널에러");

        String message = switch (newStatus) {
            case ONLINE -> "★☆★속보★☆★ 大 " +user.getEffectiveName() +"(" + user.getAsTag()+")" + " 님 등장. 모두 머리를 조아려라 !!!!";
            case OFFLINE, INVISIBLE -> "★☆★속보★☆★ 大 " +user.getEffectiveName() +"(" + user.getAsTag()+")" +" 님 퇴장. 모두 해산하라 !!!!";
            case IDLE -> "★☆★속보★☆★ 大 " +user.getEffectiveName() +"(" + user.getAsTag()+")" + " 님 잠수중 !!!!";
            case DO_NOT_DISTURB -> "★☆★속보★☆★ 大 " +user.getEffectiveName() +"(" + user.getAsTag()+")" + " 님 방해금지중 모두 방해하지마라 !!!!";
            default -> null;
        };

        if (message != null) {
            textChannel.sendMessage(message).queue();
        }
    }

}
