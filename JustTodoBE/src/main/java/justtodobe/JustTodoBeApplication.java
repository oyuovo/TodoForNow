package justtodobe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("justtodobe.mapper")
@EnableScheduling
public class JustTodoBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustTodoBeApplication.class, args);
    }

}
