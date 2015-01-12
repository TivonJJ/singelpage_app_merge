package com.builder.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.builder.util.Logger;
import yahoo.platform.yui.compressor.CssCompressor;
import yahoo.platform.yui.compressor.JavaScriptCompressor;


public class HTMLParser {

	private File res;
	private File export;

	private List<String> cssFiles = new ArrayList<String>();
	private List<String> jsFiles = new ArrayList<String>();
	
	private String tempPath = System.getProperty("user.dir")+File.separator + "tmp.js";

	public HTMLParser() {

	}

	public HTMLParser(File res, File export) {
		this.res = res;
		this.export = export;
	}

	public String mergeRes(Document document, File in, File out) throws IOException {
		Elements links = document.getElementsByTag("link");
		StringBuffer cssText = new StringBuffer();
		String path = in.getAbsolutePath().replace(in.getName(), "");
		String outPath = out.getAbsolutePath().replace(out.getName(), "");
		Elements styles = document.getElementsByTag("style");
		for(Element style : styles){
			StringBuffer html = new StringBuffer(style.html());
			String compressedCss = new CssCompressor(html).compress(0);
			style.html(compressedCss);
			continue;
		};
		for (Element link : links) {
			if(!"stylesheet".equals(link.attr("rel"))) continue;
			String linkHref = link.attr("href");
			if(null==link || "".equals(link) || checkIsAbsPath(linkHref))continue;
			File cssFile = new File(path + linkHref);
			String css = readCss(cssFile) + " \r\n ";
			cssText.append(formartCss(css, out, linkHref ,cssFile.getName()));
			link.remove();
			cssFiles.add(outPath + linkHref);
		}
		document.outputSettings().escapeMode().getMap().clear();
		String css = new CssCompressor(cssText).compress(0);
		Element head = document.head();
		if(css.length()>0){
			Element cssTag = document.createElement("style");
			cssTag.attr("type", "text/css");
			cssTag.appendChild(new TextNode(css, ""));
			head.appendChild(cssTag);
		}
		this.mergeJavascript(document, path,outPath);
		return document.html();
	}
	
	private void mergeJavascript(Document document,String path,String outPath) throws EvaluatorException, FileNotFoundException, IOException{
		Elements scripts = document.getElementsByTag("script");
		for(Element script : scripts){
			String type = script.attr("type").toLowerCase();
			if(!"".equals(script.attr("type")) && !"text/javascript".equals(type))continue;
			String src = script.attr("src");
			if(src.endsWith("xface.js"))continue;
			if(null==src || "".equals(src)){
				String html = script.html();
				if(html.length()<=0)continue;
				File tempFile = FileUtile.writeFileTxt(tempPath, html,"utf-8");
				String javascript = this.compressJavascript(new FileReader(tempFile));
				script.html(javascript);
				tempFile.deleteOnExit();
				continue;
			};
			if(checkIsAbsPath(src))continue;
			File jsFile = new File(path + src);
			String code = this.compressJavascript(new FileReader(jsFile));
			Element scriptTag = document.createElement("script");
			scriptTag.attr("type","text/javascript");
			scriptTag.html(code);
			if(code.length()>0)document.head().appendChild(scriptTag);
			script.remove();
			jsFiles.add(outPath + src);
		}
	}
	
	private boolean checkIsAbsPath(String path){
		return path.matches("^(\\w)+:\\/\\/.+$");
	}
	
	public String compressJavascript(Reader in) throws EvaluatorException, IOException{
		JavaScriptCompressor compressor = new JavaScriptCompressor(in,
				new ErrorReporter() {
					public void warning(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						if (line < 0) {
							Logger.log("\n[WARNING] " + message);
						} else {
							Logger.log("\n[WARNING] " + line + ':'
									+ lineOffset + ':' + message);
						}
					}

					public void error(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						if (line < 0) {
							Logger.log("\n[ERROR] " + message);
						} else {
							Logger.log("\n[ERROR] " + line + ':'
									+ lineOffset + ':' + message);
						}
					}

					public EvaluatorException runtimeError(String message,
							String sourceName, int line, String lineSource,
							int lineOffset) {
						error(message, sourceName, line, lineSource, lineOffset);
						return new EvaluatorException(message);
					}
				});
		return compressor.compress(0, true, false, false, false);
	}

	public void clearExportCompressedFile() {
		for (int i = 0; i < cssFiles.size(); i++) {
			File file = new File(cssFiles.get(i));
			if (file.exists()) {
				boolean deleted = file.delete();
				Logger.log("del" + deleted + ":"+file.getAbsolutePath());
			}else {
				Logger.log("del-not exist:"+file.getAbsolutePath());
			}
		}
		
		for (int i = 0; i < jsFiles.size(); i++) {
			File file = new File(jsFiles.get(i));
			if (file.exists()) {
				boolean deleted = file.delete();
				Logger.log("del" + deleted + ":"+file.getAbsolutePath());
			}else {
				Logger.log("del-not exist:"+file.getAbsolutePath());
			}
		}
	}

	private String readCss(File cssFile) {
		if(!cssFile.exists()){
			Logger.log(cssFile.getName() + " is not found");
			return "";
		}
		String cssText = FileUtile.readFileTxt(cssFile);
		return cssText;
	}

	private String formartCss(String css, File out,String cssHref,String cssName) {
		String expPath = export.getAbsolutePath();
		String realPath = out.getAbsolutePath().replace(out.getName(), "");
		realPath = realPath.replace(expPath, "").replace("\\", "/");
		String regex = "background(-image)?:[\\s|\\w|#]*url\\([\"|']*(.*)[\"|']*\\)";
		Pattern pa = Pattern.compile(regex);
		Matcher ma = pa.matcher(css);
		while (ma.find()) {
			String text = ma.group(0);
			Pattern p = Pattern.compile("\\([\"|']*(.*)[\"|']*\\)");
			Matcher m = p.matcher(text);
			if (m.find()) {
				String temp = m.group(0);
				String cssImage = temp.replace("\"", "").replace("(", "")
						.replace("'", "").replace(")", "");
				String hrefPath = cssHref.replace(cssName, "");
				String image = realPath + hrefPath + cssImage;
				if (image.startsWith("/"))image = image.substring(1, image.length());
				String newText = text.replace(temp, "(\"" + simplifyPath(image) + "\")");
				css = css.replace(text, newText);
			}
		}

		return css;
	}
	
	private String simplifyPath(String path){
		String[] dirs = path.split("/");
		String newPath = "";
		for(int i=0;i<dirs.length;i++){
			String dir = dirs[i];
			if(dir.equals("..")){
				newPath = newPath.substring(0,newPath.lastIndexOf("/"));
			}else{
				newPath += "/"+dir;
			}
		}
		newPath = newPath.replaceAll("^(/)?", "");
		return dirs.length > 0 ? newPath : path;
	}

	public File getRes() {
		return res;
	}

	public void setRes(File res) {
		this.res = res;
	}

	public File getExport() {
		return export;
	}

	public void setExport(File export) {
		this.export = export;
	}
}
