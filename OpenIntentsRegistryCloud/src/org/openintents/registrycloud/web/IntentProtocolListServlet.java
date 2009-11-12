package org.openintents.registrycloud.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openintents.registrycloud.data.IntentProtocol;

import com.google.gwt.user.client.ui.Panel;

@SuppressWarnings("serial")
public class IntentProtocolListServlet extends HttpServlet {

	private String action;
	private int id;
	private String title;
	private String description;
	private String intentaction;
	
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private String msg;
	

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		init(req, resp);

		// delete intent by id
		if (action.equals("delete") && id > 0) {
			IntentProtocol deleteSchedule = IntentProtocol.find(id);
			deleteSchedule.delete();
			id = 0;
		}
		
		

		displayContent();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		init(req, resp);

		msg = action + " ";
		
		// add a new intent
		if (action.equals("add") && !action.equals("")) {
			IntentProtocol intent = new IntentProtocol();
			intent.setAction(intentaction);
			intent.setTitle(title);
			intent.setDescription(description);
			intent.persist();
		}
		
		// edit intent by id - set name
		if (action.equals("edit") && id > 0 && intentaction != null && !intentaction.equals("")) {
			IntentProtocol editIntent = IntentProtocol.find(id);
			editIntent.setAction(intentaction);
			editIntent.setTitle(title);
			editIntent.persist();
			editIntent = null;
			id = 0;
		}
		
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

		resp.setContentType("text/html");

		// set name
		intentaction = "";
		if (req.getParameter("intentaction") != null
				&& !req.getParameter("intentaction").equals("")) {
			intentaction = req.getParameter("intentaction");
		}
		
		// set alias
		description = "";
		if (req.getParameter("description") != null
				&& !req.getParameter("description").equals("")) {
			description = req.getParameter("description");
		}
		
		// set description
		description = "";
		if (req.getParameter("description") != null
				&& !req.getParameter("description").equals("")) {
			description = req.getParameter("description");
		}
		
		// set id
		id = 0;
		if (req.getParameter("id") != null) {
			id = Integer.parseInt(req.getParameter("id"));
		}
		
		

		// set action
		action = "";
		if (req.getParameter("action") != null
				&& !req.getParameter("action").equals("")) {
			action = req.getParameter("action");
		}
	}

	private void displayContent() throws IOException {
		// create XHTML page
		WebPage page = new WebPage("Schedule Database");
		
		page.addContent(msg);
		
		page.addContent("<h1>Schedule Database</h1>");
		// add list with schedules
		List<IntentProtocol> schedules = IntentProtocol.findAll();
		WebPage list = new WebPage("Intent Protocols");
		page.addContent(list.display());
		// add form to add and edit schedules
		IntentProtocol editIntentProtocol = null;
		
		if (id > 0) {
			IntentProtocol editIntent = IntentProtocol.find(id);
			
		}
		IntentProtocolForm form = new IntentProtocolForm(editIntentProtocol);
		page.addContent(form.display());
		
		
		// print generated XHTML page
		PrintWriter out = resp.getWriter();
		out.println(page.display());
	}
}
