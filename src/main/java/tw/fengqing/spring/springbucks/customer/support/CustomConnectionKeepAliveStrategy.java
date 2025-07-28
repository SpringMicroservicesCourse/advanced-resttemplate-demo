package tw.feingqing.spring.springbucks.customer.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.TimeValue;

import java.util.Arrays;

/**
 * 自訂 Keep-Alive 策略，支援 httpclient5
 * 會解析 Keep-Alive header 的 timeout 參數，若無則預設 30 秒
 * 注意：必須 implements org.apache.hc.client5.http.ConnectionKeepAliveStrategy
 */
public class CustomConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
    private static final long DEFAULT_SECONDS = 30;

    @Override
    public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
        Header[] keepAliveHeaders = response.getHeaders("Keep-Alive");
        if (keepAliveHeaders != null) {
            for (Header header : keepAliveHeaders) {
                String value = header.getValue();
                if (value != null) {
                    for (String param : value.split(",")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && pair[0].trim().equalsIgnoreCase("timeout")) {
                            if (NumberUtils.isCreatable(pair[1].trim())) {
                                return TimeValue.ofSeconds(Long.parseLong(pair[1].trim()));
                            }
                        }
                    }
                }
            }
        }
        return TimeValue.ofSeconds(DEFAULT_SECONDS);
    }
}
