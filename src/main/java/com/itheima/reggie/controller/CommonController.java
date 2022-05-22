package com.itheima.reggie.controller;

import com.itheima.reggie.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传和下载
 * @author Su
 * @create 2022-05-19 13:03
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    //获取配置文件里的值reggie.pat匹配到basePath里
    @Value("${reggie.path}")
    private String basePath;


    /**
     * 页面上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)  {
        //file是一个临时文件,需要转存到指定位置,否则本次请求完成后临时文件会删除
        log.info(file.toString());

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();

        //获取包括.后面的字段
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));


        //使用UUID重新生成文件名,防止文件名称重复造成文件覆盖，UUID后面加上.格式
        String fileName = UUID.randomUUID().toString()+suffix;

        //创建一个目录对象
        File file1 = new File(basePath);

        //判断当前目录是否存在
        if (!file1.exists()){
            file1.mkdirs();
        }


        //将临时文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response 输出流需要response来获得
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)  {
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //加个缓冲流
            bufferedInputStream = new BufferedInputStream(fileInputStream);

            //输出流，通过输出流将文件写回浏览器,在浏览器展示图片了
            ServletOutputStream outstream = response.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outstream);

            //用于设置发送到客户端的响应的内容类型
            response.setContentType("images/jpeg");

            //读取、写入
            byte[] bytes = new byte[1024];
            int len;
            while ((len =bufferedInputStream.read(bytes)) != -1){
                bufferedOutputStream.write(bytes,0,len);
                bufferedOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                //关闭流
                if (bufferedInputStream != null)
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //关闭流
                if (bufferedOutputStream != null)
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





    }

}













