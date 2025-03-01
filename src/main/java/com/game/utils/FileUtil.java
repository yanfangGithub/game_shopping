package com.game.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.game.utils.SystemConstants.IMAGE_UPLOAD_DIR;

/**
 * @author yanfang
 * &#064;date  2024/6/13 14:34
 * @version 1.0
 */

@Slf4j
public class FileUtil {
    /**
     * 上传文件
     *
     * @param file MultipartFile 类型的文件
     * @return 系统目录下的文件夹
     */
    public static String uploadFile(MultipartFile file) {
        try {
            // 获取原始文件名称
            String originalFilename = file.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            file.transferTo(new File(IMAGE_UPLOAD_DIR, fileName));
            log.debug("文件上传成功:{}", fileName);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @return 路径名称
     */
    public static String[] uploadFiles(MultipartFile[] files) {
        String[] pathArr = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            pathArr[i] = uploadFile(files[i]);
        }
        return pathArr;
    }


    /**
     * 生成文件名称以及路径
     *
     * @param originalFilename 原本的文件名称
     * @return 处理后的文件名称和路径
     */
    private static String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        String d1 = createDirectory();
        // 判断目录是否存在
        File dir = new File(IMAGE_UPLOAD_DIR, StrUtil.format("/{}", d1));
        if (!dir.exists()) {
            //创建新的目录
            boolean b = dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("{}/{}.{}", d1, name, suffix);
    }

    /**
     * 生成目录
     *
     * @return 目录
     */
    private static String createDirectory() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd");
        return now.format(formatter);
    }
}
