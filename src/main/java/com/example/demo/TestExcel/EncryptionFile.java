package com.example.demo.TestExcel;

import java.io.*;

/**
 * @author faker
 * @date 2020/2/21 15:36
 * 文件加密
 */
public class EncryptionFile {
    //加密解密秘钥
    private static final int numOfEncAndDec = 0x99;
    //文件字节内容
    private static int dataOfFile = 0;

    public static void main(String[] args) {
        //初始文件
        File srcFile = new File("D:\\pictureTest\\test1\\word.docx");
        //加密文件
        File encFile = new File("D:\\pictureTest\\jiami\\encFile.tif");
        //解密文件
        File decFile = new File("D:\\pictureTest\\jiemi\\decFile.bmp");

        try {
            //加密操作
            EncFile(srcFile, encFile);
            //解密
            DecFile(encFile,decFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void EncFile(File srcFile, File encFile) throws Exception {
        if (!srcFile.exists()) {
            System.out.println("source file not exixt");
            return;
        }

        if (!encFile.exists()) {
            System.out.println("encrypt file created");
            encFile.createNewFile();
        }
        InputStream fis = new FileInputStream(srcFile);
        OutputStream fos = new FileOutputStream(encFile);

        while ((dataOfFile = fis.read()) > -1) {
            fos.write(dataOfFile ^ numOfEncAndDec);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    private static void DecFile(File encFile, File decFile) throws Exception {
        if (!encFile.exists()) {
            System.out.println("encrypt file not exixt");
            return;
        }

        if (!decFile.exists()) {
            System.out.println("decrypt file created");
            decFile.createNewFile();
        }

        InputStream fis = new FileInputStream(encFile);
        OutputStream fos = new FileOutputStream(decFile);
        while ((dataOfFile = fis.read()) > -1) {
            fos.write(dataOfFile ^ numOfEncAndDec);
        }

        fis.close();
        fos.flush();
        fos.close();
    }
}
