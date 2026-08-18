package or.kr.bashboard;

import lombok.extern.slf4j.Slf4j;
import or.kr.bashboard.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class BashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(BashboardApplication.class, args);
    }

    @Bean
    CommandLineRunner init(MemberRepository memberRepository) {
        return args -> {
            var members = memberRepository.findAll();
            log.info("=== DB Member 목록 ===");
            members.forEach(m ->
                    log.info("email={}, provider={}", m.getEmail(), m.getProvider())
            );
            log.info("총 {}명", members.size());
        };
    }
}
