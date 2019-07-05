package webclients;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import webutils.User;
import webutils.WebUtils;
/**
 * Servlet implementation class LoginController
 */
public class LoginController extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String un = request.getParameter("username");
		String header = request.getHeader("X-Forwarded-For");
		String ipAddr = "";
		if(header == null)
			ipAddr = request.getRemoteAddr();
		else
			ipAddr = new StringTokenizer(header, ",").nextToken().trim();
		
		response.sendRedirect("WebSocketClient.html?user="+un);
		
		User newUser = new User(un, ipAddr);
		
		WebUtils.users.add(newUser.id, newUser);
	}
}