import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BotSlashResponse extends ListenerAdapter {

    private final Map<String, SlashCommandInfo> _commandMap = new LinkedHashMap<>();
    private final List<CommandData> _commandDataList = new ArrayList<>();

    public BotSlashResponse(MemberActivityTracker _tracker) {
        register("기환출석", "기환씨가 오늘의 출석표를 가져올거에요", event -> {
            String message = _tracker.getTodayLog();
            replyException(event, message);
        });

        register("기환전체출석", "기환씨가 전체출석표를 가져올거에요", event -> {
            File logFile = _tracker.exportAllLogToFile();
            if (logFile != null && logFile.exists()) {
                event.deferReply().queue(hook -> {
                    hook.sendMessage("전체 출석 기록(너무 길어서 파일로 드림요)")
                            .addFiles(FileUpload.fromData(logFile))
                            .queue();
                }, error -> {
                    System.out.println("실패: " + error.getMessage());
                });
            } else {
                replyException(event, "파일생성 실패 ");
            }
        });

        register("기환랭킹", "기환씨가 오늘의 랭킹을 가져올거에요", event -> {
            String message = _tracker.getTodayRanking();
            replyException(event, message);
        });

        register("기환전체랭킹", "기환씨가 전체 랭킹을 가져올거에요", event -> {
            String message = _tracker.getTotalRanking();
            replyException(event, message);
        });

        register("기환씨부르기", "기환씨를 불러보아요", event -> {
            replyException(event, "나는 최기환 왜 불렀냐!");
        });
    }

    public void register(String name, String description, Consumer<SlashCommandInteractionEvent> action) {
        if (!_commandMap.containsKey(name)) {
            SlashCommandInfo info = new SlashCommandInfo(name, description, action);
            _commandMap.put(name, info);
            _commandDataList.add(info.toCommandData());
        } else {
            System.out.println("중복 명령어임: " + name);
        }
    }

    private void replyException(SlashCommandInteractionEvent event, String message) {
        if (!event.isAcknowledged()) {
            event.deferReply().queue(
                    hook -> hook.sendMessage(message).queue(
                            success -> {},
                            error -> System.err.println("메시지 전송 실패: " + error.getMessage())
                    ),
                    error -> System.err.println("deferReply 실패: " + error.getMessage())
            );
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        SlashCommandInfo info = _commandMap.get(event.getName());
        if (info != null) {
            info.run(event);
        } else {
            event.reply("없는 명령어임").setEphemeral(true).queue();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("명령어 등록 시도");

        event.getJDA().updateCommands()
                .addCommands(_commandDataList)
                .queue(success -> System.out.println("등록 성공"));

    }



    private static class SlashCommandInfo {
        private final String name;
        private final String description;
        private final Consumer<SlashCommandInteractionEvent> action;

        public SlashCommandInfo(String name, String description, Consumer<SlashCommandInteractionEvent> action) {
            this.name = name;
            this.description = description;
            this.action = action;
        }

        public CommandData toCommandData() {
            return Commands.slash(name, description);
        }

        public void run(SlashCommandInteractionEvent event) {
            try {
                action.accept(event);
            } catch (Exception e) {
                event.reply("응답 실패 : " + e.getMessage()).setEphemeral(true).queue();
                System.err.println("run 에러 : " + e.getMessage());
            }
        }
    }
}