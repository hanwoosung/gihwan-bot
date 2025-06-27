package db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class MemberActivityService {

    private final EntityManager em;

    public MemberActivityService(EntityManager em) {
        this.em = em;
    }

    // 저장
    public void saveActivity(String memberId, String username, String globalName,
                             LocalDateTime joinedAt, LocalDateTime leftAt) {
        em.getTransaction().begin();
        MemberActivity activity = new MemberActivity(memberId, username, globalName, joinedAt, leftAt);
        em.persist(activity);
        em.getTransaction().commit();
    }

    // 오늘 로그 조회
    public List<MemberActivity> getTodayLogs() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        TypedQuery<MemberActivity> query = em.createQuery(
                "SELECT m FROM MemberActivity m WHERE m.joinedAt BETWEEN :start AND :end",
                MemberActivity.class
        );
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    // 전체 로그 조회
    public List<MemberActivity> getAllLogs() {
        TypedQuery<MemberActivity> query = em.createQuery(
                "SELECT m FROM MemberActivity m ORDER BY m.joinedAt", MemberActivity.class
        );
        return query.getResultList();
    }

    // 오늘 랭킹
    public List<Map.Entry<String, Long>> getTodayRanking() {
        List<MemberActivity> logs = getTodayLogs();
        return calculateRanking(logs);
    }

    // 전체 랭킹
    public List<Map.Entry<String, Long>> getTotalRanking() {
        List<MemberActivity> logs = getAllLogs();
        return calculateRanking(logs);
    }

    // 랭킹 계산 로직
    private List<Map.Entry<String, Long>> calculateRanking(List<MemberActivity> logs) {
        Map<String, Long> durationMap = new HashMap<>();

        for (MemberActivity entry : logs) {
            long seconds = java.time.Duration.between(entry.getJoinedAt(), entry.getLeftAt()).getSeconds();
            String key = entry.getUsername() + " (" + entry.getGlobalName() + ")";
            durationMap.put(key, durationMap.getOrDefault(key, 0L) + seconds);
        }

        List<Map.Entry<String, Long>> sorted = new ArrayList<>(durationMap.entrySet());
        sorted.sort((a, b) -> Long.compare(a.getValue(), b.getValue()));
        Collections.reverse(sorted);
        return sorted;
    }
}
