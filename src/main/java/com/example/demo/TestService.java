package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Autowired(required=false)
    private TestMapper tm;

    public void tests(){
    }
}
