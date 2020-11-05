package com.umbrella.rayaliyunoss.controller;

import com.alibaba.fastjson.JSONObject;
import com.umbrella.rayaliyunoss.service.AliyunOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class AliyunOss {

    @Autowired
    private AliyunOssService aliyunOssService;

    @RequestMapping("upload")
    @CrossOrigin
    public JSONObject upload(MultipartFile file, String extData, String fileName) throws IOException{
        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(file)) {
            result.put("code", 12);
            result.put("msg", "上传文件不能为空");
            return result;
        }

        Map<String, String> map = new HashMap<>(2);
        map.put("url", aliyunOssService.uploadFile(fileName,"",file.getInputStream() ));
        map.put("extData", extData);

        result.put("code", 0);
        result.put("msg", "成功");
        result.put("data", map);
        return result;
    }
}
