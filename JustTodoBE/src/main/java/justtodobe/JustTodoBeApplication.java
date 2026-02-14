package justtodobe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("justtodobe.mapper")
public class JustTodoBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustTodoBeApplication.class, args);
    }

}
