# advanced-resttemplate-demo

> Advanced RestTemplate customization with Apache HttpClient5 connection pool and Keep-Alive strategy

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Apache HttpClient](https://img.shields.io/badge/HttpClient5-5.3-blue.svg)](https://hc.apache.org/)
[![Commons Lang3](https://img.shields.io/badge/Commons%20Lang3-3.17-green.svg)](https://commons.apache.org/proper/commons-lang/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive demonstration of **advanced RestTemplate customization** with Apache HttpClient5, featuring connection pool management, custom Keep-Alive strategy, timeout configuration, and Money type serialization.

## Features

- Apache HttpClient5 integration (NOT default JDK HttpURLConnection)
- Connection pool management with PoolingHttpClientConnectionManager
- Custom Keep-Alive strategy based on HTTP headers
- Configurable connection and read timeouts
- Automatic retry disabled for business safety
- XML and JSON response handling
- Money type serialization/deserialization (TWD currency)
- Connection lifecycle management
- WebApplicationType.NONE (client-only mode)

## Tech Stack

- Spring Boot 3.4.5
- Apache HttpClient5
- Apache Commons Lang3
- Java 21
- Joda Money 2.0.2
- Lombok
- Maven 3.8+

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.8+ (or use included Maven Wrapper)
- **springbucks2** server running on port 8080 (provides `/coffee` REST API)

### Quick Start

**Step 1: Start REST API Server (springbucks2)**

```bash
# Navigate to springbucks2 project
cd ../../Chapter\ 6\ Spring\ MVC實踐/springbucks2

# Start the server
mvn spring-boot:run
```

**Step 2: Verify REST API**

```bash
# Test coffee endpoint
curl http://localhost:8080/coffee/?name=mocha

# Expected: Coffee JSON response
# {"id":4,"name":"mocha","price":150.00,...}
```

**Step 3: Run advanced-resttemplate-demo**

```bash
# Navigate back to advanced-resttemplate-demo
cd ../../Chapter\ 7\ 訪問Web資源/advanced-resttemplate-demo

# Using Maven
mvn spring-boot:run

# Or using compiled JAR
mvn clean package
java -jar target/advanced-resttemplate-demo-0.0.1-SNAPSHOT.jar
```

**Project Relationship:**
- **Server**: springbucks2 (Chapter 6) - Provides `/coffee` REST API
- **Client**: advanced-resttemplate-demo (Chapter 7) - Consumes `/coffee` API

## Configuration

### Application Properties

```properties
# No configuration required in application.properties
# All settings are configured programmatically in Java code
```

### HttpClient Configuration

```java
@Bean
public CloseableHttpClient httpClient() {
    return HttpClients.custom()
        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(200)           // Max total connections
            .setMaxConnPerRoute(20)         // Max per route
            .setDefaultConnectionConfig(ConnectionConfig.custom()
                .setTimeToLive(TimeValue.ofSeconds(30))  // TTL
                .build())
            .build())
        .evictIdleConnections(TimeValue.ofSeconds(30))   // Evict idle
        .disableAutomaticRetries()          // No auto-retry
        .setKeepAliveStrategy(new CustomConnectionKeepAliveStrategy())
        .build();
}
```

**Configuration Details:**
- **MaxConnTotal**: 200 - Maximum total connections across all routes
- **MaxConnPerRoute**: 20 - Maximum connections per target host
- **TimeToLive**: 30s - Connection maximum lifetime
- **EvictIdleConnections**: 30s - Remove idle connections after 30s
- **DisableAutomaticRetries**: Prevent duplicate operations (e.g., payments)
- **CustomKeepAliveStrategy**: Dynamic Keep-Alive based on server response

### RestTemplate Configuration

```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    HttpComponentsClientHttpRequestFactory requestFactory = 
        new HttpComponentsClientHttpRequestFactory(httpClient());
    
    requestFactory.setConnectTimeout(Duration.ofSeconds(5));  // Connect timeout
    requestFactory.setReadTimeout(Duration.ofSeconds(1));     // Read timeout
    
    return builder
        .requestFactory(() -> requestFactory)
        .build();
}
```

## Usage

### Application Flow

```
1. Spring Boot starts (WebApplicationType.NONE)
   ↓
2. HttpClient configured with custom connection pool
   ↓
3. RestTemplate bean created with HttpClient
   ↓
4. ApplicationRunner executes:
   ├─ GET /coffee/?name=mocha (XML format)
   ├─ POST /coffee/ (Create Americano)
   └─ GET /coffee/ (List all coffees)
   ↓
5. Connection pool manages HTTP connections
   ↓
6. Application completes and exits
```

### Code Example

```java
@SpringBootApplication
@Slf4j
public class CustomerServiceApplication implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RestTemplate restTemplate = restTemplate(new RestTemplateBuilder());
        
        // 1. GET request with XML response
        URI uri = UriComponentsBuilder
            .fromUriString("http://localhost:8080/coffee/?name={name}")
            .build("mocha");
        RequestEntity<Void> req = RequestEntity.get(uri)
            .accept(MediaType.APPLICATION_XML)  // Request XML format
            .build();
        ResponseEntity<String> resp = restTemplate.exchange(req, String.class);
        log.info("Response Status: {}, Response Headers: {}", 
            resp.getStatusCode(), resp.getHeaders().toString());
        log.info("Coffee: {}", resp.getBody());

        // 2. POST request to create coffee
        String coffeeUri = "http://localhost:8080/coffee/";
        Coffee request = Coffee.builder()
            .name("Americano")
            .price(Money.of(CurrencyUnit.of("TWD"), 125.00))
            .build();
        Coffee response = restTemplate.postForObject(coffeeUri, request, Coffee.class);
        log.info("New Coffee: {}", response);

        // 3. GET request to list all coffees
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
```

### Sample Output

```
Response Status: 200 OK, Response Headers: [Content-Type:"application/xml", ...]
Coffee: <?xml version="1.0" encoding="UTF-8"?><coffee><id>4</id><name>mocha</name><price>150.00</price>...</coffee>

New Coffee: Coffee(id=6, name=Americano, price=TWD 125.00, createTime=..., updateTime=...)

Coffee: Coffee(id=1, name=espresso, price=TWD 100.00, ...)
Coffee: Coffee(id=2, name=latte, price=TWD 125.00, ...)
Coffee: Coffee(id=3, name=capuccino, price=TWD 125.00, ...)
Coffee: Coffee(id=4, name=mocha, price=TWD 150.00, ...)
Coffee: Coffee(id=5, name=macchiato, price=TWD 150.00, ...)
Coffee: Coffee(id=6, name=Americano, price=TWD 125.00, ...)
```

**Output Analysis:**
- **XML Response**: Successfully retrieved mocha coffee in XML format
- **POST Success**: Created Americano with ID=6
- **List Query**: Retrieved all 6 coffees including the newly created one
- **Connection Pool**: All requests reused connections from the pool
- **Timeout**: All requests completed within configured timeout

## Key Components

### Custom Keep-Alive Strategy

```java
public class CustomConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
    private final long DEFAULT_SECONDS = 30;

    @Override
    public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
        // Parse timeout from Connection header
        long milliseconds = Arrays.stream(response.getHeaders("Connection"))
            .filter(h -> StringUtils.equalsIgnoreCase(h.getName(), "timeout")
                    && StringUtils.isNumeric(h.getValue()))
            .findFirst()
            .map(h -> NumberUtils.toLong(h.getValue(), DEFAULT_SECONDS))
            .orElse(DEFAULT_SECONDS) * 1000;
        
        return TimeValue.ofMilliseconds(milliseconds);
    }
}
```

**Strategy Details:**
- Parses `timeout` value from `Connection` header
- Uses Apache Commons Lang3 for string/number utilities
- Default 30 seconds if no header or parsing fails
- Prevents indefinite connection persistence

**Example HTTP Headers:**
```
Connection: keep-alive, timeout=60
→ Keep-Alive duration: 60 seconds

Connection: keep-alive
→ Keep-Alive duration: 30 seconds (default)
```

### Money Serializer

```java
@JsonComponent
public class MoneySerializer extends StdSerializer<Money> {
    @Override
    public void serialize(Money money, JsonGenerator jsonGenerator, 
                         SerializerProvider serializerProvider) throws IOException {
        // Serialize Money to number
        jsonGenerator.writeNumber(money.getAmount());
    }
}
```

**Serialization Flow:**
```
TWD 125.00 → getAmount() → BigDecimal 125.00
           → writeNumber() → JSON: 125.00
```

### Money Deserializer

```java
@JsonComponent
public class MoneyDeserializer extends StdDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        // Deserialize number to Money (default currency: TWD)
        return Money.of(CurrencyUnit.of("TWD"), p.getDecimalValue());
    }
}
```

**Deserialization Flow:**
```
JSON: 125.00 → getDecimalValue() → BigDecimal 125.00
             → Money.of(TWD, ...) → TWD 125.00
```

### Coffee Entity

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coffee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private Money price;        // Joda Money type
    private Date createTime;
    private Date updateTime;
}
```

## Connection Pool Management

### Pool Configuration Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| `MaxConnTotal` | 200 | Maximum total connections |
| `MaxConnPerRoute` | 20 | Max connections per host |
| `TimeToLive` | 30s | Connection maximum lifetime |
| `EvictIdleConnections` | 30s | Idle connection cleanup interval |

### Connection Lifecycle

```
1. Request arrives
   ↓
2. Check connection pool
   ├─ Available? → Reuse connection
   └─ Full? → Wait or create new (if under max)
   ↓
3. Execute HTTP request
   ↓
4. Return connection to pool
   ↓
5. Idle > 30s? → Evict
6. Age > 30s? → Close
```

### Pool Monitoring

```java
@Component
public class ConnectionPoolMonitor {
    
    private final PoolingHttpClientConnectionManager connectionManager;
    
    @Scheduled(fixedRate = 30000)
    public void monitorPool() {
        PoolStats totalStats = connectionManager.getTotalStats();
        log.info("Pool - Max: {}, Available: {}, Leased: {}", 
            totalStats.getMax(), 
            totalStats.getAvailable(), 
            totalStats.getLeased());
    }
}
```

## Timeout Configuration

### Timeout Types

**1. Connect Timeout (5 seconds)**
```java
requestFactory.setConnectTimeout(Duration.ofSeconds(5));
```
- Time to establish TCP connection
- Throws `ConnectTimeoutException` if exceeded
- Protects against unreachable servers

**2. Read Timeout (1 second)**
```java
requestFactory.setReadTimeout(Duration.ofSeconds(1));
```
- Time waiting for response data
- Throws `SocketTimeoutException` if exceeded
- Protects against slow responses

### Timeout by Use Case

| Use Case | Connect | Read | Rationale |
|----------|---------|------|-----------|
| **Financial Transaction** | 3s | 2s | Strict timing for safety |
| **General Query** | 5s | 10s | Moderate tolerance |
| **File Upload** | 10s | 5min | Large data transfer |
| **Reporting** | 10s | 30s | Complex computation |

## Best Practices

### 1. Connection Pool Sizing

**Development Environment:**
```java
.setMaxConnTotal(50)
.setMaxConnPerRoute(10)
.setTimeToLive(TimeValue.ofSeconds(30))
```

**Production Environment:**
```java
.setMaxConnTotal(500)
.setMaxConnPerRoute(50)
.setTimeToLive(TimeValue.ofMinutes(5))
```

**Calculation Formula:**
```
MaxConnTotal = (Expected Concurrent Users) × (Requests per User)
MaxConnPerRoute = MaxConnTotal / (Number of Target Hosts)
```

### 2. Disable Automatic Retries

```java
.disableAutomaticRetries()  // ✅ Recommended for financial operations
```

**Why disable?**
- Prevents duplicate transactions (e.g., double payment)
- Network errors might occur after server processing
- Manual retry logic provides better control

**When to enable?**
- Read-only operations (GET requests)
- Idempotent operations (safe to retry)

### 3. Custom Keep-Alive Strategy

```java
// ✅ Recommended: Parse from server response
.setKeepAliveStrategy(new CustomConnectionKeepAliveStrategy())

// ❌ Not recommended: Fixed value
.setKeepAliveStrategy((response, context) -> TimeValue.ofSeconds(60))
```

**Benefits:**
- Respects server preferences
- Reduces connection overhead
- Prevents connection leaks

### 4. Error Handling

```java
try {
    Coffee coffee = restTemplate.getForObject(uri, Coffee.class);
} catch (ResourceAccessException ex) {
    // Connection or read timeout
    log.error("Timeout: {}", ex.getMessage());
    throw new ServiceUnavailableException("Service temporarily unavailable");
} catch (HttpClientErrorException ex) {
    // 4xx client errors
    log.error("Client error: {}", ex.getStatusCode());
    throw new BadRequestException("Invalid request");
} catch (HttpServerErrorException ex) {
    // 5xx server errors
    log.error("Server error: {}", ex.getStatusCode());
    throw new InternalServerException("Server error");
}
```

## Performance Optimization

### Connection Reuse

**Without Connection Pool:**
```
Request 1: TCP handshake (50ms) + Data transfer (100ms) = 150ms
Request 2: TCP handshake (50ms) + Data transfer (100ms) = 150ms
Total: 300ms
```

**With Connection Pool:**
```
Request 1: TCP handshake (50ms) + Data transfer (100ms) = 150ms
Request 2: Reuse connection (0ms) + Data transfer (100ms) = 100ms
Total: 250ms (17% faster)
```

### Keep-Alive Impact

```
Without Keep-Alive:
└─ Every request: TCP handshake + TLS handshake + HTTP request

With Keep-Alive:
└─ First request: TCP + TLS + HTTP
└─ Subsequent: HTTP only (60-80% faster)
```

## Monitoring

### Enable Debug Logging

```properties
# application.properties
logging.level.org.apache.hc.client5.http=DEBUG
logging.level.org.springframework.web.client.RestTemplate=DEBUG
```

### Key Metrics to Monitor

| Metric | Description | Alert Threshold |
|--------|-------------|-----------------|
| **Pool Utilization** | Leased / MaxTotal | > 80% |
| **Wait Time** | Time waiting for connection | > 100ms |
| **Timeout Rate** | Timeout exceptions / Total requests | > 1% |
| **Retry Rate** | Retries / Total requests | > 5% |

### Health Check

```java
@Component
public class RestTemplateHealthIndicator implements HealthIndicator {
    
    private final PoolingHttpClientConnectionManager connectionManager;
    
    @Override
    public Health health() {
        PoolStats stats = connectionManager.getTotalStats();
        double utilization = (double) stats.getLeased() / stats.getMax();
        
        if (utilization > 0.9) {
            return Health.down()
                .withDetail("utilization", utilization)
                .withDetail("leased", stats.getLeased())
                .withDetail("max", stats.getMax())
                .build();
        }
        
        return Health.up()
            .withDetail("available", stats.getAvailable())
            .withDetail("leased", stats.getLeased())
            .build();
    }
}
```

## Testing

### Unit Test Example

```java
@SpringBootTest
class CustomerServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verify application context loads successfully
    }
    
    @Test
    void testRestTemplateConfiguration() {
        RestTemplate restTemplate = new RestTemplate(
            new HttpComponentsClientHttpRequestFactory(httpClient())
        );
        
        assertNotNull(restTemplate);
    }
}
```

### Integration Test with MockRestServiceServer

```java
@SpringBootTest
class RestTemplateIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;
    
    private MockRestServiceServer mockServer;
    
    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
    
    @Test
    void testGetCoffee() {
        mockServer.expect(requestTo("http://localhost:8080/coffee/?name=mocha"))
            .andRespond(withSuccess("{\"name\":\"mocha\",\"price\":150.00}", 
                        MediaType.APPLICATION_JSON));
        
        Coffee coffee = restTemplate.getForObject(
            "http://localhost:8080/coffee/?name=mocha", Coffee.class);
        
        assertEquals("mocha", coffee.getName());
        mockServer.verify();
    }
}
```

## Common Issues

### Issue 1: Connection Pool Exhausted

**Error:**
```
org.apache.hc.core5.http.ConnectionClosedException: 
Connection is closed
```

**Causes:**
- Connections not returned to pool
- `MaxConnTotal` too small
- Connection leaks

**Solutions:**
```java
// Increase pool size
.setMaxConnTotal(500)
.setMaxConnPerRoute(100)

// Ensure proper connection lifecycle
try (CloseableHttpResponse response = httpClient.execute(request)) {
    // Process response
}  // Auto-closes connection
```

### Issue 2: Read Timeout

**Error:**
```
java.net.SocketTimeoutException: Read timed out
```

**Solutions:**
```java
// Increase timeout for specific operations
requestFactory.setReadTimeout(Duration.ofSeconds(10));

// Or handle gracefully
try {
    response = restTemplate.getForObject(uri, String.class);
} catch (ResourceAccessException ex) {
    if (ex.getCause() instanceof SocketTimeoutException) {
        log.warn("Request timed out, using cached data");
        return getCachedData();
    }
    throw ex;
}
```

### Issue 3: Keep-Alive Not Working

**Symptoms:**
- High connection creation rate
- Poor performance

**Solutions:**
```bash
# Verify server supports Keep-Alive
curl -v http://localhost:8080/coffee/1 2>&1 | grep -i "connection: keep-alive"

# Check HttpClient logs
logging.level.org.apache.hc.client5.http=DEBUG
```

## Apache HttpClient vs Default

| Feature | Default (JDK) | Apache HttpClient5 |
|---------|---------------|-------------------|
| Connection Pool | ❌ No | ✅ Yes |
| Keep-Alive Strategy | Limited | ✅ Customizable |
| Timeout Control | Basic | ✅ Advanced |
| Retry Mechanism | ❌ No | ✅ Configurable |
| Connection Lifecycle | Simple | ✅ Full control |
| Performance | Moderate | ✅ High |
| Use Case | Simple apps | Enterprise apps |

## Dependencies

```xml
<properties>
    <java.version>21</java.version>
    <joda-money.version>2.0.2</joda-money.version>
</properties>

<dependencies>
    <!-- Spring Boot Web (includes RestTemplate) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Apache HttpClient5 -->
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
    </dependency>
    
    <!-- Apache Commons Lang3 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    
    <!-- Joda Money -->
    <dependency>
        <groupId>org.joda</groupId>
        <artifactId>joda-money</artifactId>
        <version>${joda-money.version}</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Best Practices Demonstrated

1. **Connection Pool Management**: Proper pooling configuration
2. **Custom Keep-Alive Strategy**: Dynamic based on server response
3. **Timeout Configuration**: Both connect and read timeouts
4. **Automatic Retry Disabled**: Safe for financial transactions
5. **Money Serialization**: Efficient money type handling
6. **Error Handling**: Comprehensive exception handling
7. **Resource Management**: Proper connection lifecycle

## References

- [Apache HttpClient5 Documentation](https://hc.apache.org/httpcomponents-client-5.3.x/)
- [Spring RestTemplate Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
- [Connection Pool Best Practices](https://hc.apache.org/httpcomponents-client-5.3.x/examples.html)
- [Joda Money Documentation](https://www.joda.org/joda-money/)

## License

MIT License - see [LICENSE](LICENSE) file for details.

## About Us

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。近來也積極結合 AI 技術，推動自動化工作流，讓開發與運維更有效率、更智慧。持續學習與分享，希望能一起推動軟體開發的創新和進步。

## Contact

**風清雲談** - 專注於敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。

- 🌐 官方網站：[風清雲談部落格](https://blog.fengqing.tw/)
- 📘 Facebook：[風清雲談粉絲頁](https://www.facebook.com/profile.php?id=61576838896062)
- 💼 LinkedIn：[Chu Kuo-Lung](https://www.linkedin.com/in/chu-kuo-lung)
- 📺 YouTube：[雲談風清頻道](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- 📧 Email：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**⭐ If this project helps you, please give it a star!**
