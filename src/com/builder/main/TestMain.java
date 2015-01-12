package com.builder.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.EvaluatorException;

import com.builder.file.HTMLParser;

public class TestMain {

	public static void main(String[] args) {
		HTMLParser hp = new HTMLParser();
		String file = "C:/Users/Tivon/Desktop/tmp.js";
		try {
			String rs = hp.compressJavascript(new FileReader(file));
			System.out.println(rs);
		} catch (EvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
