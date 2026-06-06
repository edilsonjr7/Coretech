package coretech.sistemaCoreTech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude ={DataSourceAutoConfiguration.class}) // -> esse comando fala para o spring igonrar o banco de dados por enquanto
public class SistemaCoreTechApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaCoreTechApplication.class, args);
	}

}
