package com.builder.main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.builder.file.FileUtile;
import com.builder.file.HTMLParser;
import com.builder.util.Logger;

public class MainBuilder {

	public static void main(String[] args) {
		String in = null,out = null;
		if(args.length >= 2){
			in = args[0];
			out = args[1];
		}
		doMerge(in,out);
	}
	
	private static void doMerge(String src,String exp){
		String pathIn = null,pathOut = null;
		if(null == src || "".equals(src) || null == exp || "".equals(exp)){
			Logger.log("源文件目录：");
			Logger.log("");
			pathIn = new Scanner(System.in).nextLine().replace("\\", "/");
			Logger.log("目标文件输出目录：");
			Logger.newLine();
			pathOut = new Scanner(System.in).nextLine().replace("\\", "/");
			if("".equals(pathIn) || "".equals(pathOut)){
				Logger.newLine();
				Logger.log("请填写相关文件信息");
				return;
			}
			if(pathIn.equals(pathOut)){
				Logger.newLine();
				Logger.log("输入路径和输出路径不能相同!");
				return;
			}
		}else{
			pathIn = src;
			pathOut = exp;
		}
		
		File in = new File(pathIn);
		File out = new File(pathOut);
		FileUtile fu = new FileUtile();
		fu.deleteFolder(pathOut);
		HTMLParser htmlParser = new HTMLParser(in,out);
		fu.setHtmlParser(htmlParser);
		try {
			fu.copyFile(in, out);
			Logger.log("merge file is complated!");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			htmlParser.clearExportCompressedFile();
			fu.clearEmptyDir(out);
		}
	}
}
