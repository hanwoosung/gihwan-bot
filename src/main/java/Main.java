import db.MemberActivityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.dv8tion.jda.api.entities.Activity;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        try {
            makeLogFile();

            DiscordGihwanBot gihwanBot = DiscordGihwanBot.getGihwanInstance();

            System.out.println("JDA 상태: " + gihwanBot.getJDA().getStatus());

            gihwanBot.getJDA().getPresence().setActivity(
                    Activity.playing("기환씨가 대기")
            );

            gihwanBot.getJDA().awaitReady();

            System.out.println("작동 고고");

        } catch (Exception e) {
            System.out.println("봇 실행 에러 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void makeLogFile() {
        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path logDir = Path.of("/home/ubuntu/jar/logs");
           // Path logDir = Path.of("D:/log");
            if (!Files.exists(logDir)) Files.createDirectories(logDir);

            String logFilePath = logDir.resolve("gihwan_log_" + time + ".txt").toString();
            PrintStream logStream = new PrintStream(new FileOutputStream(logFilePath, true), true, "UTF-8");
            System.setOut(logStream);
            System.setErr(logStream);

            System.out.println("로그 파일 : " + logFilePath);
        } catch (Exception e) {
            System.out.println("로그 파일 error: " + e.getMessage());
        }
    }
}
