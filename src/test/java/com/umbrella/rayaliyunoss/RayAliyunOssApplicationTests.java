package com.umbrella.rayaliyunoss;

import com.umbrella.rayaliyunoss.service.AliyunOssService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RayAliyunOssApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(RayAliyunOssApplicationTests.class);

	@Autowired
	private AliyunOssService aliyunOssService;

	@Test
	void contextLoads() {
		aliyunOssService.deleteFile("http://umbrella-ray.oss-cn-shenzhen.aliyuncs.com/dev/20201105/test.txt");
	}

}
