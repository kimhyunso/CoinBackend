package or.kr.bashboard;

import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class BashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(BashboardApplication.class, args);
    }
}
