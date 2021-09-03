package com.scnu.gulimall;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class GulimallGatewayApplicationTests {

    @Test
    public void contextLoads() {
        List<String> props = Arrays.asList("profile=native", "debug=true", "logging=warn", "interval=500");
        Map<String, String> map = props.stream()
                .map(kv -> {
                    String[] split = kv.split("=");
                    return Collections.singletonMap(split[0], split[1]);
                })
                .reduce(new HashMap<>(), (ans, oneMap) -> {
                    ans.putAll(oneMap);
                    return ans;
                });
        map.forEach((k,v) -> {
            System.out.println(k + "--->" + v);
        });
    }

}
