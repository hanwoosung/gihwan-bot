import db.MemberActivityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordGihwanBot {

    private static DiscordGihwanBot gihwanBot;
    private static JDA jda;

    private DiscordGihwanBot() {
        try {
            String gihwanToken = GiHwanTokenManage.getGiHwanToken();

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("member-activity");
            EntityManager em = emf.createEntityManager();
            MemberActivityService activityService = new MemberActivityService(em);

            MemberActivityTracker tracker = new MemberActivityTracker(activityService);
            BotSlashResponse botSlashResponse = new BotSlashResponse(tracker);
            PresenceListener presenceListener = new PresenceListener();

            jda = JDABuilder.createDefault(
                            gihwanToken,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.ONLINE_STATUS)
                    .addEventListeners(tracker)
                    .addEventListeners(botSlashResponse)
                    .addEventListeners(presenceListener)
                    .build();
        } catch (Exception e) {
            System.out.println("DiscordGihwanBot 초기화 에러 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DiscordGihwanBot getGihwanInstance() {
        if (gihwanBot == null) {
            gihwanBot = new DiscordGihwanBot();
        }
        return gihwanBot;
    }

    public JDA getJDA() {
        return jda;
    }
}
