package com.emc.ecd.spt.restfulsamples;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestfulSampleNoGUI {
	
	
	/**
	* Restful Java Class sample for InfoArchive 4.
	* 
	* @author EMEA SPT
	* @version 1.0
	*/

	public static volatile String accessToken = null;
	public static volatile Client client = null;
	
	
	public static volatile String serverIp = null;
	public static volatile String clientPort = null;
	public static volatile String restAPIPort = null;
	public static volatile String userName = null;
	public static volatile String password = null;
	
	
	public static String executeStaticQuery ( String customerID, String relativePath ) throws Exception
	{
			
			//Get config paramaters to connect to InfoArchive
			ResourceBundle inputStream = ResourceBundle.getBundle( "com.emc.ecd.spt.restfulsamples.config");
			serverIp = inputStream.getString( "serverip" );
			clientPort = inputStream.getString( "client_port" );
			restAPIPort = inputStream.getString( "restapiproxy_port" );
			userName = inputStream.getString( "username" );
			password = inputStream.getString( "password" );
			


		    String customerIDInput = ( customerID != null ? customerID : "" );
			
		    //Create new Jersey client instance
			log ( "Creating Jersey client instance ..." );
			client = ClientBuilder.newClient();
			
			//Retrieve access token from oauth authentication engine for the user indicated in the properties file
			accessToken = getAccessToken ( );
			
			//Retrieve IA instance runtime information 
			getInstanceInformation ( );	
			
			
			//Get existing tenants and the URL pointer to the first Tenant found
			String applicationsURL = getTenantApplicationURI ( );
			log ( "Applications URL for Tenant is " + applicationsURL );
			
			
			//Get root URL pointer to the HealthCare application
			String applicationRootURL = getApplicationRootURI ( applicationsURL, "HealthCare" );
			log ( "Root URI for Application HealthCare is " + applicationRootURL );

			//Get URL pointer to the HealthCare application
			String searchesURL = getApplicationSearchURI ( applicationsURL, "HealthCare" );
			log ( "Searchs URL for Application HealthCare is " + searchesURL );
			
			//Get URL pointer to the HealthCare search screen called DefaultSearch
			String searchURL  = getSearchURI ( searchesURL, "Patient" );
			log ( "HealthCare DefaultSearch URL is " + searchURL );
			
			//Once all the URL pointers have been retrieved the query is executed using the filter typed by the user
			return getResults ( applicationRootURL, searchURL, customerIDInput, relativePath );
			
		
	}
	public static void main ( String[] args  )
	{
		
		
	}
	
	
	public static String getResults ( String applicationRootURL, String searchURL, String customerIDFilter, String relativePath )throws Exception
	{ 
		StringBuffer data = new StringBuffer ( );
		log ( "Executing the query ..." );
		WebTarget target = client.target(searchURL).path("");
		long currentMS = System.currentTimeMillis( );
		
		//Set maximum results to 10000 to avoid handling pages
		WebTarget targetWithParams = target.queryParam("size", "10000");

		Invocation.Builder invocationBuilder3 = targetWithParams
				.request(new String[] { "application/hal+json" });
		invocationBuilder3
				.header(
						"Authorization",
						"Bearer " + accessToken );
		
		invocationBuilder3.header("content-type", "application/xml");

		//Compose the search criteria filtering by CustomerID
		String input = "<data><criterion><name>PatientID</name><operator>EQUAL</operator><value>"
				+ customerIDFilter + "</value></criterion></data>";

		Response response = invocationBuilder3.post(Entity.xml(input));
		
		log ( "Query executed in " + ( System.currentTimeMillis( ) - currentMS ) + "ms." );


		final JSONObject obj = new JSONObject(response
				.readEntity(String.class));

		final JSONObject jsonMain = (JSONObject) obj.get("_embedded");


		final JSONArray results = jsonMain.getJSONArray("results");
		final int n = results.length();

		System.out.println("\n");
		System.out.println("Results");
		System.out
				.println("------------------------------------------------------------------------------------------------------------------");
		
		
		//Go over the results to retrieve rows and columns
		for (int i = 0; i < n; ++i) {
			final JSONObject objects = results.getJSONObject(i);
			final JSONArray rows = objects.getJSONArray("rows");
			final int n1 = rows.length();
			for (int y = 0; y < n1; ++y) {
				final JSONObject row = rows.getJSONObject(y);

				final JSONArray columns = row.getJSONArray("columns");
				final int n2 = columns.length();
				StringBuffer line = new StringBuffer();
				for (int z = 0; z < n2; ++z) {
					final JSONObject fields = columns.getJSONObject(z);
					
					//Fields printed in the results table are filtered to avoid too much information in the screen
					if (fields.getString("name").equals("PatientID")
							|| fields.getString("name").equals(
									"PatientLastName")
							|| fields.getString("name").equals(
									"PatientFirstName")
							|| fields.getString("name").equals("Attachment1")) 
					{
						if ( fields.getString("name").equals("Attachment1") ) {
							line.append("@FB@<a href='" + fields.getString("value").replaceAll( ":", "_" ) + ".pdf" + "'>" + fields.getString("value") + "</a>@FE@");
						} else {
							line.append("@FB@" + fields.getString("value") + "@FE@");
						}
						
						//If the field is the attachment, retrieve the file and store it in the disk
						if (fields.getString("name").equals("Attachment1")
								&& fields.getString("value") != null
								&& fields.getString("value").trim().length() > 0) {
							
							
							getAttachment(applicationRootURL, fields
									.getString("value"), 
								relativePath);
						}
					}

				}
				
				data.append( "@LB@" + line.toString( ) + "@LE@" );

			}

			System.out
					.println("------------------------------------------------------------------------------------------------------------------");
			
			System.out.println((n1) + " Records found");
			System.out
					.println("All the attachments from the records have been stored in "
							+ System.getProperty("user.home"));
			System.out
			.println( "\n");
			
			

		}
		
		return data.toString( );
	}	

	
	public static String getTenantApplicationURI ( )throws Exception
	{ 
		log ( "Looking for an InfoArchive tenant ...");	
		
		WebTarget target = client.target(
				"http://" + serverIp + ":" + restAPIPort
						+ "/systemdata/tenants").path("");

		WebTarget targetQry = target.queryParam(
				"grant_type", new Object[] { "password" });

		Invocation.Builder invocationBuilder = targetQry
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder.header("Authorization", "Bearer " + accessToken);

		Response response = invocationBuilder.get();

		final JSONObject obj = new JSONObject(response.readEntity(String.class));

		final JSONObject data = (JSONObject) obj.get("_embedded");
		

		JSONObject links = null;

		final JSONArray tenants = data.getJSONArray("tenants");
		final int n = tenants.length();
		for (int i = 0; i < n; ++i) {
			final JSONObject tenant = tenants.getJSONObject(i);
			log ( "Tenant selected is '" + tenant.getString("name") + "'" );
			links = tenant.getJSONObject("_links");
		}

		log ( "Looking for applications URL ...");	

		return links.getJSONObject("http://identifiers.emc.com/applications")
				.get("href").toString();
	}
	
	
	public static String getApplicationSearchURI ( String appsURI, String applicationName )throws Exception
	{ 
		
		log ( "Looking for application searchs URL ...");	
		WebTarget target = client.target(appsURI).path("");

		Invocation.Builder invocationBuilder = target
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder.header("Authorization", "Bearer " + accessToken);

		Response response = invocationBuilder.get();

		final JSONObject obj = new JSONObject(response.readEntity(String.class));

		final JSONObject data = (JSONObject) obj.get("_embedded");

		JSONObject links = null;

		final JSONArray apps = data.getJSONArray("applications");
		final int n = apps.length();
		for (int i = 0; i < n; ++i) {
			final JSONObject app = apps.getJSONObject(i);
			if (app.getString("name").equals(applicationName)) {
				links = app.getJSONObject("_links");
				break;

			}
		}

		return links.getJSONObject("http://identifiers.emc.com/searches").get(
				"href").toString();
	}
	
	
	
	public static String getApplicationRootURI ( String appsURI, String applicationName )throws Exception
	{ 
		
		log ( "Looking for HealthCare application ...");	

		WebTarget target = client.target(appsURI).path("");


		Invocation.Builder invocationBuilder = target
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder
				.header(
						"Authorization",
						"Bearer " + accessToken );

		Response response = invocationBuilder.get();


		final JSONObject obj = new JSONObject(response
				.readEntity(String.class));

		final JSONObject data = (JSONObject) obj.get("_embedded");
		

		JSONObject links = null;

		final JSONArray apps = data.getJSONArray("applications");
		final int n = apps.length();
		for (int i = 0; i < n; ++i) {
			final JSONObject app = apps.getJSONObject(i);
			if (app.getString("name").equals(applicationName)) {
				links = app.getJSONObject("_links");
				break;

			}
		}

		return links.getJSONObject("self").get("href").toString();
	}
	
	
	public static String getSearchURI ( String searchesURI, String searchName )throws Exception
	{ 
		
		log ( "Looking for Healthcare default search ...");	

		WebTarget target = client.target(searchesURI).path("");


		Invocation.Builder invocationBuilder = target
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder
				.header(
						"Authorization",
						"Bearer " + accessToken );

		Response response = invocationBuilder.get();


		final JSONObject obj = new JSONObject(response
				.readEntity(String.class));

		final JSONObject data = (JSONObject) obj.get("_embedded");
		

		JSONObject links = null;

		final JSONArray searches = data.getJSONArray("searches");
		final int n = searches.length();
		for (int i = 0; i < n; ++i) {
			final JSONObject search = searches.getJSONObject(i);
			if (search.getString("name").equals(searchName)) {
				links = search.getJSONObject("_links");
				break;

			}
		}

		return links.getJSONObject("http://identifiers.emc.com/execute").get(
				"href").toString();
	}
	
	
	public static void getAttachment ( String applicationRootURI, String attachmentID, String relativePath )throws Exception
	{ 
		String filename = relativePath
		+ "/" + attachmentID.replaceAll(":", "_") + ".pdf";
		
		if (  !new File ( filename ).exists( ) ) {
			WebTarget target = client
					.target(
							applicationRootURI + "/ci")
					.path("");
	
			WebTarget targetQry = target.queryParam(
					"cid", new Object[] { attachmentID }).queryParam(
					"Accept-Encoding", "gzip, deflate, sdch");
			
	
			Invocation.Builder invocationBuilder = targetQry
					.request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
			invocationBuilder.header("Authorization", "Bearer " + accessToken);
	
			InputStream in = invocationBuilder.get(InputStream.class);
			
			
				
				OutputStream outputStream = new FileOutputStream( filename );
		
				byte[] buffer = new byte[10 * 1024];
		
				for (int length; (length = in.read(buffer)) != -1;) {
		
					outputStream.write(buffer, 0, length);
					outputStream.flush();
				}
		}
		
	}
	
	
	public static String getAccessToken ( )throws Exception
	{ 
		log("Getting access token ... ");

		WebTarget target = client.target(
				"http://" + serverIp + ":" + clientPort + "/oauth/token").path(
				"");

		
		//client_secret param depends on the configuration of spring
		WebTarget targetQry = target.queryParam( "grant_type", new Object[] { "password" })
														.queryParam( "username", new Object[] { userName } )
														.queryParam("password", new Object[] { password })
														.queryParam("client_id", new Object[] { "infoarchive.iawa" })
														.queryParam( "client_secret", new Object[] { "secret" })
														.queryParam("scope", new Object[] { "search compliance administration" });

		Invocation.Builder invocationBuilder = targetQry
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder.header("Authorization",
				"Basic aW5mb2FyY2hpdmUuaWF3YTpzZWNyZXQ=");

		Response response = invocationBuilder.post(null);

		final JSONObject obj = new JSONObject(response.readEntity(String.class));

		log("Access token is: " + obj.getString("access_token"));
		log("Refresh token is: " + obj.getString("refresh_token"));
		log("JTI identifier is: " + obj.getString("jti"));
		
		
		return obj.getString("access_token");
	}
	
	
	public static void getInstanceInformation ( )throws Exception
	{ 
		log ( "Retrieving InfoArchive instance product information ...");	

		WebTarget target = client.target(
				"http://" + serverIp + ":" + restAPIPort + "/product-info")
				.path("");

		Invocation.Builder invocationBuilder = target
				.request(new String[] { MediaType.APPLICATION_JSON_TYPE
						.toString() });
		invocationBuilder.header("Authorization", "Bearer " + accessToken);

		Response response = invocationBuilder.get();

		final JSONObject obj = new JSONObject(response
				.readEntity(String.class));

		final JSONObject props = (JSONObject) obj.get("buildProperties");

		log("InfoArchive " + props.getString("ia.server.version.label")
				+ " has been detected ... ");
		log("Extracting additional instance information ... ");

		Iterator<?> iterator = props.keys();
		while (iterator.hasNext()) {
			String obj2 = iterator.next().toString();

			if (!(props.get(obj2) instanceof JSONArray)) {

				if (!(props.get(obj2) instanceof JSONObject)) {

					log(" - " + obj2 + ": " + props.getString(obj2));
				}
			}

		}
	    
	}
	
	public static void log ( String text )
	{
		System.out.println( "(" +  System.currentTimeMillis( ) + "m.) => " + text );
	}
}
