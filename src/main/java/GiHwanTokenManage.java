import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GiHwanTokenManage {

    public static String getGiHwanToken() {
        String token = null;

         String tokenFilePath = "/home/ubuntu/jar/gihwan-token";
       // String tokenFilePath = "gihwan-token";

        try (BufferedReader br = new BufferedReader(new FileReader(tokenFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("GIHWAN-TOKEN=")) {
                    token = line.split("=")[1].trim();
                    System.out.println("기환토큰: " + token);
                }
            }
        } catch (IOException e) {
            System.out.println(" 파일 읽기 실패: " + e.getMessage());
        }

        if (token == null || token.isEmpty()) {
            System.err.println(" 봇 실행 실패 토큰없음");
        }

        return token;
    }
}
