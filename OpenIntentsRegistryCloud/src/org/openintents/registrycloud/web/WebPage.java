package org.openintents.registrycloud.web;

public class WebPage {

	private String foot;
	private String head;
	private String content = "";

	public WebPage(String title) {
		head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!--DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"-->"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + "<head>"
				+ "<title>" + title + "</title>" + "</head>" + "<body>";
		foot = "</body>" + "</html>";
	}

	public String display() {
		String output = head + content + foot;
		return output;
	}

	public void addContent(String content) {
		this.content += content;
	}

}