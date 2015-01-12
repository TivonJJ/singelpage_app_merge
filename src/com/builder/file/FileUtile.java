package com.builder.file;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.builder.util.Logger;

public class FileUtile {
	
	private HTMLParser htmlParser;
	private static final String CHARSET = "utf-8";
	private static String[] textFiles = {"html","js","css","xml","txt","json","jsp","xhtml","htm","php"};
	
	public void copyFile(File in, File out) throws IOException {
		if (!in.exists()) {
			throw new FileNotFoundException();
		}
		if (!out.exists()) {
			out.mkdirs();
		}
		File[] files = in.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if(file.getName().startsWith("."))continue;
			if (file.isFile()) {
				File outFile = new File(out.getAbsolutePath() + "/"+ file.getName());
				if(isParseFile(file)){
					parseHTML(htmlParser,file, outFile);
				}
//				else if(isImageFile(file)){
//					compressImage(file.getAbsolutePath(), outFile.getAbsolutePath());
//				}
				else{
					copyUnParseFile(file,outFile);
				}
			} else
				this.copyFile(new File(in.getAbsolutePath() + "/" + file.getName()),
						new File(out.getAbsolutePath() + "/" + file.getName()));
		}
	}
	
	public void clearEmptyDir(File dir){
		if(dir.isFile())return;
		File[] files = dir.listFiles();  
	    for (int i = 0; i < files.length; i++) {
	    	File file = files[i];
	        if(file.isFile())continue;
	        if(file.listFiles().length<=0){
	        	file.delete();
	        }else{
	        	clearEmptyDir(file);
	        }
	    }  
	}
	
	private boolean isParseFile(File in){
		if(in.isDirectory())return false;
		Logger.log(in.getAbsolutePath());
		String exName = getFileExpandedName(in);
		if("".equals(exName) || null == exName)return false;
		if(in.getName().equals("index.html"))return false;
		if(exName.matches("html|htm|jsp|xhtml|php"))return true;
		return false;
	}
	
	private boolean isImageFile(File in){
		if(in.isDirectory())return false;
		String exName = getFileExpandedName(in);
		if(exName.matches("jpg|jpeg|png|gif|ico"))return true;
		return false;
	}
	
	private void parseHTML(HTMLParser htmlParser,File in, File out) {
		Document document;
		try {
			document = Jsoup.parse(in,CHARSET);
			String outHTML = htmlParser.mergeRes(document,in,out);
			FileUtile.writeFileTxt(new FileOutputStream(out), outHTML, CHARSET);
			Logger.log("merge > " + in.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean compressImage(String srcFilePath, String descFilePath) {
		File file = null;
		BufferedImage src = null;
		FileOutputStream out = null;
		ImageWriter imgWrier;
		ImageWriteParam imgWriteParams;

		// 指定写图片的方式为 jpg
		imgWrier = ImageIO.getImageWritersByFormatName("jpg").next();
		imgWriteParams = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(
				null);
		// 要使用压缩，必须指定压缩方式为MODE_EXPLICIT
		imgWriteParams.setCompressionMode(imgWriteParams.MODE_EXPLICIT);
		// 这里指定压缩的程度，参数qality是取值0~1范围内，
		imgWriteParams.setCompressionQuality((float) 0.1);
		imgWriteParams.setProgressiveMode(imgWriteParams.MODE_DISABLED);
		ColorModel colorModel = ColorModel.getRGBdefault();
		// 指定压缩时使用的色彩模式
		imgWriteParams.setDestinationType(new javax.imageio.ImageTypeSpecifier(
				colorModel, colorModel.createCompatibleSampleModel(32, 32)));

		try {
			if (null == srcFilePath || "".equals(srcFilePath)) {
				return false;
			} else {
				file = new File(srcFilePath);
				src = ImageIO.read(file);
				out = new FileOutputStream(descFilePath);

				imgWrier.reset();
				// 必须先指定 out值，才能调用write方法, ImageOutputStream可以通过任何
				// OutputStream构造
				imgWrier.setOutput(ImageIO.createImageOutputStream(out));
				// 调用write方法，就可以向输入流写图片
				imgWrier.write(null, new IIOImage(src, null, null),
						imgWriteParams);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static String readFileTxt(String path){
		File in = new File(path);
		return readFileTxt(in);
	};
	
	public static String readFileTxt(File in){
		try {
			FileReader fr = new FileReader(in);
			BufferedReader br = new BufferedReader(fr);
			String line;
			StringBuffer text = new StringBuffer();
			while ((line = br.readLine()) != null) {
				text.append(line + "\r\n");
			}
			return new String(text);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	};
	
	private void copyUnParseFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
	
	private static void writeOutputstream(File file,File outFile) throws IOException{
		FileInputStream fin = new FileInputStream(file);
		FileOutputStream fout = new FileOutputStream(outFile);
		String name = getFileExpandedName(file);
		List<String> list = Arrays.asList(textFiles);
		if(list.indexOf(name) != -1){
			String fileText = readFileTxt(file);
			writeFileTxt(new FileOutputStream(outFile), fileText, CHARSET);
		}else{
			int c;
			byte[] b = new byte[1024 * 5];
			while ((c = fin.read(b)) != -1) {
				fout.write(b, 0, c);
			}
			fin.close();
			fout.flush();
			fout.close();
		}
	}
	
	public static File writeFileTxt(String path,String text){
		try {
			FileOutputStream out = new FileOutputStream(path);
			writeFileTxt(out, text,CHARSET);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new File(path);
	}
	
	
	public static File writeFileTxt(String path,String text,String charset){
		try {
			FileOutputStream out = new FileOutputStream(path);
			writeFileTxt(out, text,charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(path);
	}
	
	public static void writeFileTxt(FileOutputStream out,String text,String charset){
		try {
			OutputStreamWriter writer = new OutputStreamWriter(out,charset);
			writer.write(text);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	public static String getFileExpandedName(File file){
		if(null == file || !file.exists())return null;
		String name = file.getName();
		int indexP = name.lastIndexOf(".");
		if(indexP < 0)return null;
		String exName = name.substring(indexP+1, name.length());
		return exName;
	}
	
	public boolean delete(String path){
		File file = new File(path);
		return file.delete();
	}
	
	public void deleteFolder(String dir){
	    File file = new File(dir);  
	    // 判断目录或文件是否存在  
	    if (!file.exists()) {  // 不存在返回 false  
	        return;
	    } else {  
	        // 判断是否为文件  
	        if (file.isFile()) {  // 为文件时调用删除文件方法  
	            file.delete();
	        } else {  // 为目录时调用删除目录方法  
	            this.deleteDirectory(dir);
	        }
	    }
	}
	
	public boolean deleteDirectory(String sPath) {  
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) {  
	            flag = this.delete(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //删除当前目录  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}

	public HTMLParser getHtmlParser() {
		return htmlParser;
	}

	public void setHtmlParser(HTMLParser htmlParser) {
		this.htmlParser = htmlParser;
	}
}
