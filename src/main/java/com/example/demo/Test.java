package com.example.demo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Data
public class Test {
    @Setter
    @Getter
    private int tid;
    @Setter
    @Getter
    private String tname;

    public Test() {
    }
}
