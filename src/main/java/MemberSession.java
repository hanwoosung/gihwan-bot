import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class MemberSession {
    private final LocalDateTime joinTime;
    @Setter
    private LocalDateTime leaveTime;

    public MemberSession(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
}
