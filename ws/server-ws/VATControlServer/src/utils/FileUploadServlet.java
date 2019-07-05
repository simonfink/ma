package utils;

import java.io.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet(name = "FileUploadServlet", urlPatterns = { "/FileUploadServlet" })
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private boolean isMultipart;
	private String filePath;
	private int maxFileSize = 512 * 1024 * 1024;
	private int maxMemSize = 4 * 1024;
	private File file ;

    public void init( ){
    	// Get the file location where it would be stored.
    	filePath = getServletContext().getInitParameter("file-upload"); 
    }
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*throw new ServletException("GET method used with " +
	            getClass( ).getName( )+": POST method required.");*/
		String state = request.getHeader("state");
		if(state != null)System.out.println("state:" + state);
		if(state.equals("fileupload")) {
			String data = request.getHeader("file");
			if(data != null) {
				System.out.println("new data: " + data);
				File deleteFile = new File("/home/simon/test/uploads/"+data);
				deleteFile.delete();
			}
			File dir = new File("/home/simon/test/uploads");
			if(!dir.exists())dir.mkdirs();
			File[] files = dir.listFiles();
			java.io.PrintWriter out = response.getWriter( );
			System.out.println("got files");
			if(dir.listFiles().length > 0) {
		        for(File f : files) {
		        	String t = 
		        			"<span style=\"display: inline;\">"
		        			+"<p style=\"display: inline-block;\">"
        					+f.getName()
		        			+"</p>"
		        			+"<Button class=\"removeButton\" "
		        			+"margin-left=\"50px\""
		        			+"onclick=\"getFiles('fileupload','"
		        			+f.getName()
		        			+"')\" "
		        			+ "id=\""
		        			+f.getName()
		        			+"\">Remove"
		        			+"</Button>"
		        			+"</span>"
		        			+"<br>";
		        	out.println(t);
		        }
			} else {
				out.println("<p>no files on server</p>");
			}
		}else {
			File dir = new File("/home/simon/test/uploads");
			if(!dir.exists())dir.mkdirs();
			File[] files = dir.listFiles();
			java.io.PrintWriter out = response.getWriter( );
			if(dir.listFiles().length > 0) {
		        out.println("<ul>");
		        int count = 0;
		        for(File f : files) {
		        	out.println("<li><p>"+f.getName()+"</p></li>");
		        }
		        out.println("</ul>");
			} else {
				out.println("<ul><li>no files on server</li></ul>");
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// Check that we have a file upload request
	      isMultipart = ServletFileUpload.isMultipartContent(request);
	      
	   
	      if( !isMultipart ) {
	    	  response.setContentType("text/html");
		      java.io.PrintWriter out = response.getWriter( );
		      out.println("<html>");
			  out.println("<head>");
	          out.println("<title>FileUpload ERROR</title>");  
	          out.println("</head>");
	          out.println("<body>");
	          out.println("<h2>Could not upload file!!!</h2>");
	          out.println("</body>");
	          out.println("</html>");
	         return;
	      }
	      
	      DiskFileItemFactory factory = new DiskFileItemFactory();
	      
	      // maximum size that will be stored in memory
	      factory.setSizeThreshold(maxMemSize);
	   
	      // Location to save data that is larger than maxMemSize.
	      factory.setRepository(new File("/home/simon/test/uploads/"));

	      // Create a new file upload handler
	      ServletFileUpload upload = new ServletFileUpload(factory);
	   
	      // maximum file size to be uploaded.
	      upload.setSizeMax( maxFileSize );
	      
	      try { 
	          // Parse the request to get file items.
	          List fileItems = upload.parseRequest(new ServletRequestContext(request));
	 	
	          // Process the uploaded file items
	          Iterator i = fileItems.iterator();
	    
	          while ( i.hasNext () ) {
	             FileItem fi = (FileItem)i.next();
	             if ( !fi.isFormField () ) {
	                // Get the uploaded file parameters
	                String fieldName = fi.getFieldName();
	                String fileName = fi.getName();
	                String contentType = fi.getContentType();
	                boolean isInMemory = fi.isInMemory();
	                long sizeInBytes = fi.getSize();
	             
	                // Write the file
	                if( fileName.lastIndexOf("\\") >= 0 ) {
	                   file = new File( filePath + fileName.substring( fileName.lastIndexOf("\\"))) ;
	                } else {
	                   file = new File( filePath + fileName.substring(fileName.lastIndexOf("\\")+1)) ;
	                }
	                fi.write( file );
	                response.getWriter().write("{\"status\":1}");
	             }
	          }
	          } catch(Exception ex) {
	             System.out.println(ex);
	             response.setContentType("text/html");
      		     java.io.PrintWriter out = response.getWriter( );
	             out.println("<html>");
	 	  		 out.println("<head>");
	 	         out.println("<title>FileUpload ERROR</title>");  
	 	         out.println("</head>");
	 	         out.println("<body>");
	 	         out.println("<h2>Could not upload file!!!</h2>");
	 	         out.println("</body>");
	 	         out.println("</html>");
	          }
	       }
	}
