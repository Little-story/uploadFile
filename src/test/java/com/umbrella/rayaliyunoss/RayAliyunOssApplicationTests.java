package com.umbrella.rayaliyunoss;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RayAliyunOssApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(RayAliyunOssApplicationTests.class);

	@Test
	void contextLoads() {
		logger.info("debug");
		logger.warn("warm");
		logger.error("error");
	}

}
