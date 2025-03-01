package com.game.controller;

import com.game.other.Result;
import com.game.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("file")
public class UploadController {

    @PostMapping("upload")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        String s = FileUtil.uploadFile(image);
        return Result.ok(s);
    }

}
