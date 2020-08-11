package com.example.demo;


import com.example.demo.easyExcel.EasyExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;


@Controller
public class TextController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestService ts;

    @Autowired
    private TestMapper tm;
    /*@RequestMapping("/getUsers")
    public List<Map<String, Object>> getDbType(){
        System.out.println("aaa");
        String sql = "select * from mytest";
        List<Map<String, Object>> list =  jdbcTemplate.queryForList(sql);
        for (Map<String, Object> map : list) {
            Set<Map.Entry<String, Object>> entries = map.entrySet( );
            if(entries != null) {
                Iterator<Map.Entry<String, Object>> iterator = entries.iterator( );
                while(iterator.hasNext( )) {
                    Map.Entry<String, Object> entry =(Map.Entry<String, Object>) iterator.next( );
                    Object key = entry.getKey( );
                    Object value = entry.getValue();
                    System.out.println(key+":"+value);
                }
            }
        }
        return list;
    }*/
    /*@Autowired(required=false)
    private TestService ts;

    @RequestMapping("/hello")
    public String index(){
        return "da ge ni hao";
    }
*/
    @RequestMapping("/tea")
    public List<Test> test(){
        /*ts.tests();*/
        return tm.sss();
    }

    @RequestMapping("/hello")
    public String hello() throws Exception {
        List<List<String>> headList=new ArrayList<>();
        List<String> list=new ArrayList<>();
        List<String> list2=new ArrayList<>();
        list.add("one");
        list.add("one");
        list2.add("two11");
        list2.add("two22");
        headList.add(list);
        headList.add(list2);
        List<List<Object>> bodyList=new ArrayList<>();
        List<Object> body=new ArrayList<>();
        List<Object> body2=new ArrayList<>();
        body.add("shuju1");
        body.add("1");
        body2.add("shuju2");
        body2.add("2");
        bodyList.add(body);
        bodyList.add(body2);
        EasyExcelUtil.writeExcel(headList,bodyList,"阿西吧");
        return "success";
    }
}
