package db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_activity")
@Getter
@Setter
@NoArgsConstructor
public class MemberActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "global_name", nullable = false)
    private String globalName;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at", nullable = false)
    private LocalDateTime leftAt;


    public MemberActivity(String memberId, String username, String globalName,
                          LocalDateTime joinedAt, LocalDateTime leftAt) {
        this.memberId = memberId;
        this.username = username;
        this.globalName = globalName;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

}
