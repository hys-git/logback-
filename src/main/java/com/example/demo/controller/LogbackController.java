package com.example.demo.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: faker
 * @DATE: 2020/8/11 16:26
 */
@RestController

public class LogbackController {
    private static final Logger logger = LoggerFactory.getLogger(LogbackController.class);

    @RequestMapping("/test")
    public String test(){
        logger.info("进入test方法");
        logger.debug("进入test方法");
        logger.warn("进入test方法");
        logger.error("进入test方法");
        /*logger.info("进入test方法");
        logger.debug("进入test方法");
        logger.warn("进入test方法");
        logger.error("进入test方法");*/
        int a=1;
        int b=0;
        int c=a/b;
        return "日志记录";
    }
}
