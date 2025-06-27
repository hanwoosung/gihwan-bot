import db.MemberActivityService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberActivityTracker extends ListenerAdapter {

    private final List<String> _targetChannelIds = List.of(
            "1322512887973023744",
            "1322513617626726513",
            "1173540104413917238",
            "1372910947911598162"
    );

    private final Map<String, MemberSession> _sessionMap = new HashMap<>();
    private final MemberActivityService _activityService;
    private final DateTimeFormatter _dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초");
    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    public MemberActivityTracker(MemberActivityService activityService) {
        this._activityService = activityService;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {

        Member member = event.getEntity();
        String userId = member.getId();
        String globalName = member.getUser().getName();
        String username = member.getEffectiveName();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowKST = ZonedDateTime.now(KST).toLocalDateTime();

        AudioChannel joined = event.getChannelJoined();

        if (joined != null && _targetChannelIds.contains(joined.getId())) {
            _sessionMap.put(userId, new MemberSession(now));
            System.out.println("입장 [" + nowKST.format(_dateTimeFormatter) + "] " + username + " (" + globalName + ")");
            return;
        }

        AudioChannel left = event.getChannelLeft();
        if (left != null && _targetChannelIds.contains(left.getId())) {
            MemberSession session = _sessionMap.remove(userId);

            if (session != null) {
                session.setLeaveTime(now);
                _activityService.saveActivity(userId, username, globalName, session.getJoinTime(), now);
                System.out.println("퇴장 [" + nowKST.format(_dateTimeFormatter) + "] " + username + " (" + globalName + ")");
            }
        }

    }

    public String getTodayLog() {

        var logs = _activityService.getTodayLogs();
        if (logs.isEmpty()) return "- 오늘 입퇴장한 유저가 없슴";

        StringBuilder sb = new StringBuilder("오늘의 출석표\n\n");
        int count = 1;

        for (var entry : logs) {
            long duration = Duration.between(entry.getJoinedAt(), entry.getLeftAt()).getSeconds();
            long minutes = duration / 60, seconds = duration % 60;
            sb.append(count++).append(". ").append(entry.getUsername())
                    .append(" (").append(entry.getGlobalName()).append(")\n")
                    .append(" ㄴ 입장: ").append(formatToKST(entry.getJoinedAt())).append("\n")
                    .append(" ㄴ 퇴장: ").append(formatToKST(entry.getLeftAt())).append("\n")
                    .append(" ㄴ 머문 시간: ").append(minutes).append("분 ").append(seconds).append("초\n\n");
        }
        return sb.toString();
    }

    public String getAllLog() {

        var logs = _activityService.getAllLogs();
        if (logs.isEmpty()) return "- 아직 입퇴장한 유저가 없슴";

        logs.sort(Comparator.comparing(a -> a.getJoinedAt(), Comparator.reverseOrder()));

        StringBuilder sb = new StringBuilder("전체 출석표\n\n");
        String lastDate = "";
        int count = 1;

        for (var entry : logs) {
            LocalDateTime joinKST = _toKST(entry.getJoinedAt());
            LocalDateTime leaveKST = _toKST(entry.getLeftAt());

            LocalDate date = joinKST.toLocalDate();
            String dateStr = date.toString();

            if (!dateStr.equals(lastDate)) {
                sb.append(String.format("--------- %d년 %02d월 %02d일 ---------\n", date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
                lastDate = dateStr;
                count = 1;
            }

            long duration = Duration.between(joinKST, leaveKST).getSeconds();
            long minutes = duration / 60, seconds = duration % 60;

            sb.append(count++).append(". ").append(entry.getUsername())
                    .append(" (").append(entry.getGlobalName()).append(")\n")
                    .append(" ㄴ 입장: ").append(_dateTimeFormatter.format(joinKST)).append("\n")
                    .append(" ㄴ 퇴장: ").append(_dateTimeFormatter.format(leaveKST)).append("\n")
                    .append(" ㄴ 머문 시간: ").append(minutes).append("분 ").append(seconds).append("초\n\n");
        }

        return sb.toString();
    }


    public String getTodayRanking() {

        var ranking = _activityService.getTodayRanking();
        if (ranking.isEmpty()) return "오늘 입퇴장한 유저가 없슴";

        StringBuilder sb = new StringBuilder("오늘 머문 시간 랭킹\n\n");
        int rank = 1;
        for (var entry : ranking) {
            long minutes = entry.getValue() / 60;
            long seconds = entry.getValue() % 60;

            sb.append(rank++).append("위: ").append(entry.getKey())
                    .append(" - ").append(minutes).append("분 ").append(seconds).append("초\n");
        }
        return sb.toString();
    }

    public String getTotalRanking() {

        var ranking = _activityService.getTotalRanking();
        if (ranking.isEmpty()) return "아직 입퇴장한 유저가 없슴";

        StringBuilder sb = new StringBuilder("전체 머문 시간 랭킹\n\n");
        int rank = 1;
        for (var entry : ranking) {
            long minutes = entry.getValue() / 60;
            long seconds = entry.getValue() % 60;

            sb.append(rank++).append("위: ").append(entry.getKey())
                    .append(" - ").append(minutes).append("분 ").append(seconds).append("초\n");
        }
        return sb.toString();
    }

    private String formatToKST(LocalDateTime time) {
        return _dateTimeFormatter.format(_toKST(time));
    }

    private LocalDateTime _toKST(LocalDateTime time) {
        return time.atZone(ZoneId.of("UTC")).withZoneSameInstant(KST).toLocalDateTime();
    }

    public File exportAllLogToFile() {

        String fullLog = getAllLog();
        File file = new File("전체출석표.txt");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(fullLog);
            writer.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return file;
    }
}
