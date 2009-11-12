package org.openintents.registrycloud.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.annotations.Persistent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openintents.registrycloud.data.IntentProtocol;

@SuppressWarnings("serial")
public class IntentsXmlServlet extends HttpServlet {

	
	private HttpServletRequest req;
	private HttpServletResponse resp;
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		init(req, resp);
		

		displayContent();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		init(req, resp);

		displayContent();
	}

	/**
	 * Sets the content type and inits all given parameters
	 * 
	 * @param _req
	 * @param _resp
	 * @throws IOException
	 */
	private void init(HttpServletRequest _req, HttpServletResponse _resp)
			throws IOException {

		req = _req;
		resp = _resp;

		resp.setContentType("text/xml");
				

	}

	private void displayContent() throws IOException {
		// create XHTML page
		String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		
		// add list with schedules
		List<IntentProtocol> intents = IntentProtocol.findAll();
		if (intents.size() == 0){
			IntentProtocol i = new IntentProtocol();
			i.setAction("android.intent.CALL");
			i.setTitle("Call");
			i.persist();
			
		}
		intents = IntentProtocol.findAll();
		
		IntentProtocolListXml list = new IntentProtocolListXml(intents);
		output +=(list.display());
		// print generated XHTML page
		PrintWriter out = resp.getWriter();
		out.println(output);
	}
}
