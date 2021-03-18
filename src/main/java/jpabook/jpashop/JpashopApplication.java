package jpabook.jpashop;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
		return new Hibernate5Module(); // hibernate에서 lazy인 경우 json 라이브러리에 뿌리지 마라고 알려줌.


		// 다른옵션으로 lazy인경우 다 로딩해서 뿌리도록 하는 방법.
	//	Hibernate5Module hibernate5Moduel = new Hibernate5Module();
	//	hibernate5Moduel.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
	//	return hibernate5Moduel;
	}
}
