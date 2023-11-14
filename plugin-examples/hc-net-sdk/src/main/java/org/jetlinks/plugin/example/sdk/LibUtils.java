package org.jetlinks.plugin.example.sdk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.sun.jna.Platform.isLinux;
import static com.sun.jna.Platform.isWindows;

/**
 * 库文件工具类.
 *
 * @author zhangji 2023/10/16
 */
@Slf4j
public class LibUtils {

    // 库文件缓存目录
    public static final String ABSOLUTE_LIB_TEMP_PATH = System.getProperty("jetlinks.plugin.lib.path", "./data/plugins/lib");

    public static void copyLibFile() {
        if (isLinux()) {
            batchCopyFile("lib/linux/", ABSOLUTE_LIB_TEMP_PATH, true);
            batchCopyFile("lib/linux/HCNetSDKCom/", ABSOLUTE_LIB_TEMP_PATH + File.separator + "HCNetSDKCom", true);
        } else if (isWindows()) {
            batchCopyFile("lib/win/", ABSOLUTE_LIB_TEMP_PATH, true);
            batchCopyFile("lib/win/ClientDemoDll/", ABSOLUTE_LIB_TEMP_PATH + File.separator + "ClientDemoDll", true);
            batchCopyFile("lib/win/HCNetSDKCom/", ABSOLUTE_LIB_TEMP_PATH + File.separator + "HCNetSDKCom", true);
        }

    }

    /**
     * 复制path目录下所有文件
     * @param path  文件目录 不能以/开头
     * @param newpath 新文件目录
     */
    public static void batchCopyFile(String path,String newpath, boolean cover) {
        if (!new File(newpath).exists()){
            new File(newpath).mkdir();
        }
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(LibUtils.class.getClassLoader());
        try {
            //获取所有匹配的文件
            Resource[] resources = resolver.getResources(path+"*");
            //打印有多少文件
            for(int i=0;i<resources.length;i++) {
                Resource resource=resources[i];
                try {
                    //以jar运行时，resource.getFile().isFile() 无法获取文件类型，会报异常，抓取异常后直接生成新的文件即可；以非jar运行时，需要判断文件类型，避免如果是目录会复制错误，将目录写成文件。
                    if(resource.getFile().isFile()) {
                        makeFile(newpath+"/"+resource.getFilename());
                        InputStream inputStream = resource.getInputStream();
                        File tempFile = new File(newpath + File.separator + resource.getFilename());
                        if (cover || !tempFile.exists()) {
                            log.info("复制文件{}到目录{}", resource.getFilename(), tempFile.getParentFile().getAbsolutePath());
                            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }catch (Exception e) {
                    makeFile(newpath+"/"+resource.getFilename());
                    InputStream inputStream = resource.getInputStream();
                    File tempFile = new File(newpath + File.separator + resource.getFilename());
                    if (cover || !tempFile.exists()) {
                        log.info("复制文件{}到目录{}", resource.getFilename(), tempFile.getParentFile().getAbsolutePath());
                        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建文件
     * @param path  全路径 指向文件
     * @return
     */
    public static boolean makeFile(String path) {
        File file = new File(path);
        if(file.exists()) {
            return false;
        }
        if (path.endsWith(File.separator)) {
            return false;
        }
        if(!file.getParentFile().exists()) {
            if(!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        try {
            if (file.createNewFile()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
