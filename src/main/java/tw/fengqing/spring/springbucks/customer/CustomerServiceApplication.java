package tw.fengqing.spring.springbucks.customer;

import tw.fengqing.spring.springbucks.customer.model.Coffee;
import tw.fengqing.spring.springbucks.customer.support.CustomConnectionKeepAliveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import java.time.Duration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Optional;

import java.net.URI;
import java.util.List;

@SpringBootApplication
@Slf4j
public class CustomerServiceApplication implements ApplicationRunner {

	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.sources(CustomerServiceApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.web(WebApplicationType.NONE)
				.run(args);
	}

	@Bean
	public CloseableHttpClient httpClient() {
		// 整合連線池管理器和 HttpClient 配置
		return HttpClients.custom()
				.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
						.setMaxConnTotal(200) // 最大連線數
						.setMaxConnPerRoute(20) // 每個路由最大連線數
						.setDefaultConnectionConfig(ConnectionConfig.custom()
								.setTimeToLive(TimeValue.ofSeconds(30)) // 連線存活時間
								.build())
						.build())
				.evictIdleConnections(TimeValue.ofSeconds(30)) // 空閒連線清理
				.disableAutomaticRetries() // 停用自動重試
				.setKeepAliveStrategy(new CustomConnectionKeepAliveStrategy()) // 自定義 Keep-Alive 策略
				.build();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
		requestFactory.setConnectTimeout(Duration.ofSeconds(5));
		requestFactory.setReadTimeout(Duration.ofSeconds(1));
		return builder
				.requestFactory(() -> requestFactory)
				.build();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 直接取得 RestTemplate bean
		RestTemplate restTemplate = restTemplate(new RestTemplateBuilder());
		URI uri = UriComponentsBuilder
				.fromUriString("http://localhost:8080/coffee/?name={name}")
				.build("mocha");
		RequestEntity<Void> req = RequestEntity.get(uri)
				.accept(MediaType.APPLICATION_XML)
				.build();
		ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
		log.info("Response Status: {}, Response Headers: {}", resp.getStatusCode(), resp.getHeaders().toString());
		log.info("Coffee: {}", resp.getBody());

		String coffeeUri = "http://localhost:8080/coffee/";
		Coffee request = Coffee.builder()
				.name("Americano")
				.price(Money.of(CurrencyUnit.of("TWD"), 125.00))
				.build();
		Coffee response = restTemplate.postForObject(coffeeUri, request, Coffee.class);
		log.info("New Coffee: {}", response);

		ParameterizedTypeReference<List<Coffee>> ptr =
				new ParameterizedTypeReference<List<Coffee>>() {};
		ResponseEntity<List<Coffee>> list = restTemplate
				.exchange(coffeeUri, HttpMethod.GET, null, ptr);
		Optional.ofNullable(list.getBody())
				.ifPresentOrElse(
					body -> body.forEach(c -> log.info("Coffee: {}", c)),
					() -> log.warn("No coffee data received from server")
				);
	}
}
