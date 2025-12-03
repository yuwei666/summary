package com.example.fastdfs.controller;

import com.example.fastdfs.service.FileService;
import com.example.fastdfs.utils.TestConstants;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @date 2025/10/23
 */
@RestController
public class TestController {

    @Resource
    FileService fileService;

    @RequestMapping("/upload")
    public String upload() throws Exception {
        URL url = TestController.class.getClassLoader().getResource("test.txt");
        if (url == null) {
            throw new FileNotFoundException("classpath 中未找到 test.txt");
        }

        File file = Paths.get(url.toURI()).toFile();
        try (InputStream inputStream = ResourceReader.class.getClassLoader()
                .getResourceAsStream("test.txt")){
            StorePath path = fileService.uploadFile(TestConstants.DEFAULT_GROUP2, inputStream,
                    file.length(), "txt");
            System.out.println(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // http://172.16.1.106/group1/M00/00/00/rBABamj590yAT-3SAAAAIVtTMM4493.txt
        // [group=group1, path=M00/00/00/rBABamj63FaAZzanAAAAIS7cZ5w482.txt]
        return "test";
    }

    @RequestMapping("/delete")
    public String delete()  {
        fileService.delete(TestConstants.DEFAULT_GROUP2 ,"M00/00/00/rBABaWj7JAWAMo95AAAAA9oWDn0847_big.txt");
        return "success";
    }

    @RequestMapping("/queryFileInfo")
    public String queryFileInfo() {
        String info = fileService.queryFileInfo(TestConstants.DEFAULT_GROUP1, "/M00/00/00/rBABaWj7K8qARnvPAAAALTn2Pe0114.txt");
        return info;
    }


}
