package com.example.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: faker
 * @DATE: 2020/8/11 16:26
 */
@RestController
@Slf4j
public class LogbackController {
    //private static final Logger logger = LoggerFactory.getLogger(LogbackController.class);

    @RequestMapping("/test")
    public String test(){
        /*logger.info("进入test方法");
        logger.debug("进入test方法");
        logger.warn("进入test方法");
        logger.error("进入test方法");*/
        log.info("进入test方法");
        log.debug("进入test方法");
        log.warn("进入test方法");
        log.error("进入test方法");
        String arr[]={"1","2"};
        //arr.length;
        int a=1;
        int b=0;
        int c=a/b;
        return "日志记录";
    }
}
