package com.umbrella.rayaliyunoss.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import com.umbrella.rayaliyunoss.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
* @Project: captain-supply-chain
* @PackageName: com.captain.supply-chain.common.file
* @Author: Captain&D
* @cnblogs: https://www.cnblogs.com/captainad
* @DateTime: 2019/5/10 18:12.
* @Description: Upload file to Aliyun OSS.
*/

@Component
public class AliyunOssService {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(AliyunOssService.class);

    /**
     * 斜杠
     */
    private final String FLAG_SLANTING_ROD = "/";
    /**
     * http://
     */
    private final String FLAG_HTTP = "http://";
    /**
     * https://
     */
    private final String FLAG_HTTPS = "https://";
    /**
     * 空字符串
     */
    private final String FLAG_EMPTY_STRING = "";
    /**
     * 点号
     */
    private final String FLAG_DOT = ".";
    /**
     * 横杠
     */
    private final String FLAG_CROSSBAR = "-";

    /**
     * 缺省的最大上传文件大小：20M
     */
    private final int DEFAULT_MAXIMUM_FILE_SIZE = 20;

    /**
     * endpoint
     */
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * access key id
     */
    @Value("${aliyun.oss.keyid}")
    private String accessKeyId;

    /**
     * access key secret
     */
    @Value("${aliyun.oss.keysecret}")
    private String accessKeySecret;

    /**
     * bucket name (namespace)
     */
    @Value("${aliyun.oss.bucketname}")
    private String bucketName;

    /**
     * file host (dev/test/prod)
     */
    @Value("${aliyun.oss.filehost}")
    private String fileHost;

    /**
     * 以文件流的方式上传文件
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @param fileName 文件名称
     * @param filePath 文件路径
     * @param inputStream 文件输入流
     * @return
     */
    public String uploadFile(String fileName, String filePath, InputStream inputStream) {
        return coreUpload(fileName, filePath, inputStream);
    }

    /**
     * 核心上传功能
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param inputStream 文件输入流
     * @return
     */
    private String coreUpload(String fileName, String filePath, InputStream inputStream) {
        log.info("Start to upload file....");
        if(StringUtils.isEmpty(filePath)) {
            log.warn("File path is lack when upload file but we automatically generated");
            String dateCategory = DateUtil.getFormatDate(new Date(), "yyyyMMdd");
            filePath = FLAG_SLANTING_ROD.concat(dateCategory).concat(FLAG_SLANTING_ROD);
        }
        String fileUrl;
        OSSClient ossClient = null;
        try{

            // If the upload file size exceeds the limit
            long maxSizeAllowed = getMaximumFileSizeAllowed();
            if(Long.valueOf(inputStream.available()) > maxSizeAllowed) {
                log.error("Uploaded file is too big.");
                return null;
            }

            // Create OSS instance
            ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

            // Create bucket if not exists
            if (!ossClient.doesBucketExist(bucketName)) {
                log.info("Bucket '{}' is not exists and create it now.", bucketName);
                ossClient.createBucket(bucketName);
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
            }

            /*********************************/
            // List the bucket in my account
            //listBuckets(ossClient);
            /*********************************/

            // File path format
            if(!filePath.startsWith(FLAG_SLANTING_ROD)) {
                filePath = FLAG_SLANTING_ROD.concat(filePath);
            }
            if(!filePath.endsWith(FLAG_SLANTING_ROD)) {
                filePath = filePath.concat(FLAG_SLANTING_ROD);
            }

            // File url
            StringBuilder buffer = new StringBuilder();
            buffer.append(fileHost).append(filePath).append(fileName);
            fileUrl = buffer.toString();
            log.info("After format the file url is {}", fileUrl);

            // Upload file and set ACL
            PutObjectResult result = ossClient.putObject(new PutObjectRequest(bucketName, fileUrl, inputStream));
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if(result != null) {
                log.info("Upload result:{}", result.getETag());
                log.info("Upload file {} successfully.", fileName);
            }
            fileUrl = getHostUrl().concat(fileUrl);
            log.info("Call path is {}", fileUrl);

            /***********************************/
            // List objects in your bucket
            //listObjects(ossClient);
            /***********************************/

        }catch (Exception e){
            log.error("Upload file failed.", e);
            fileUrl = null;
        }finally {
            if(ossClient != null) {
                ossClient.shutdown();
            }
        }
        return fileUrl;
    }

    /**
     * 列出buckets下的所有文件
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     */
    private void listObjects() {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")"+"\t"+objectSummary.getLastModified().toLocaleString());

        }
        System.out.println();
    }

    /**
     * 列出当前用户下的所有bucket
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @param ossClient
     */
    private void listBuckets(OSSClient ossClient) {
        System.out.println("Listing buckets");
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        listBucketsRequest.setMaxKeys(500);
        for (Bucket bucket : ossClient.listBuckets()) {
            System.out.println(" - " + bucket.getName());
        }
        System.out.println();
    }

    /**
     * 以文件的形式上传文件
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @param fileName
     * @param filePath
     * @param file
     * @return
     */
    public String uploadFile(String fileName, String filePath, File file) {
        if(file == null) {
            log.warn("File is lack when upload.");
            return null;
        }
        if(StringUtils.isEmpty(fileName)) {
            log.warn("File name is lack when upload file but we automatically generated");
            String uuidFileName = UUID.randomUUID().toString().replace(FLAG_CROSSBAR, FLAG_EMPTY_STRING);
            String fname = file.getName();
            String suffix = fname.substring(fname.lastIndexOf(FLAG_DOT), fname.length());
            fileName = uuidFileName.concat(suffix);
        }
        InputStream inputStream = null;
        String fileUrl = null;
        try{
            inputStream = new FileInputStream(file);
            fileUrl = uploadFile(fileName, filePath, inputStream);
        }catch (Exception e){
            log.error("Upload file error.", e);
        }finally {
            IOUtils.safeClose(inputStream);
        }
        return fileUrl;
    }

    /**
     * 获取访问的base地址
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @return
     */
    private String getHostUrl() {
        String hostUrl = null;
        if(this.endpoint.startsWith(FLAG_HTTP)) {
            hostUrl = FLAG_HTTP.concat(this.bucketName).concat(FLAG_DOT)
                    .concat(this.endpoint.replace(FLAG_HTTP, FLAG_EMPTY_STRING)).concat(FLAG_SLANTING_ROD);
        } else if (this.endpoint.startsWith(FLAG_HTTPS)) {
            return FLAG_HTTPS.concat(this.bucketName).concat(FLAG_DOT)
                    .concat(this.endpoint.replace(FLAG_HTTPS, FLAG_EMPTY_STRING)).concat(FLAG_SLANTING_ROD);
        }
        return hostUrl;
    }

    /**
     * 获取最大允许上传文件的大小
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @return
     */
    private long getMaximumFileSizeAllowed() {
        // 缓存单位是M
        String maxConfigVal = "500";
        if(StringUtils.isEmpty(maxConfigVal)) {
            return DEFAULT_MAXIMUM_FILE_SIZE * 1024L * 1024L;
        }else {
            return Long.valueOf(maxConfigVal.trim()) * 1024L * 1024L;
        }
    }

    /**
     * 删除文件
     * @Author: Captain&D
     * @cnblogs: https://www.cnblogs.com/captainad
     * @param fileUrl 文件访问的全路径
     */
    public void deleteFile(String fileUrl) {
        log.info("Start to delete file from OSS.{}", fileUrl);
        if(StringUtils.isEmpty(fileUrl)
                || (!fileUrl.startsWith(FLAG_HTTP)
                && !fileUrl.startsWith(FLAG_HTTPS))) {
            log.error("Delete file failed because the invalid file address. -> {}", fileUrl);
            return;
        }
        OSSClient ossClient = null;
        try{
            /**
             * http:// bucketname                                dev/test/pic/abc.jpg = key
             * http:// captainad.oss-ap-southeast-1.aliyuncs.com/dev/test/pic/abc.jpg
             */
            String key = fileUrl.replace(getHostUrl(), FLAG_EMPTY_STRING);
            if(log.isDebugEnabled()) {
                log.debug("Delete file key is {}", key);
            }
            ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, key);
        }catch (Exception e){
            log.error("Delete file error.", e);
        } finally {
            if(ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
