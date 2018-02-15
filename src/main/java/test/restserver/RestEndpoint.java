
package test.restserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.net.ssl.SSLContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

/**
 * https://www.gitbook.com/book/wildfly-swarm/wildfly-swarm-users-guide TODO: -
 * package as jar - use SSL - read xml / call ext. EXE - send response - etc.
 *
 */
@Path("/")
public class RestEndpoint {

	private static final URI REST_TEST_ENDPOINT = URI.create("https://httpbin.org/get");


	@GET
	@Path("hello")
	@Produces("text/plain")
	public Response sayHi() throws Exception {
		// obligatory first call
		return Response.ok("world").build();
	}
	@GET
	@Path("remote")
	@Produces("text/plain")
	public Response retrieveData() throws Exception {
		// first make a rest call somewhere else...
		return Response.ok(getRestFromSomewhere()).build();
	}

	@POST
	@Path("generate/{uuid}")
	@Produces("application/json")
	public Response generateKey(@PathParam("uuid") String uuid) throws Exception {
		// actually do real work here... then later on call the rest api to return results
		String result =Json.createObjectBuilder().add("response", "ok").add("uuid", uuid).build().toString();
		return Response.ok(result)
				.build();
	}

	/**
	 * PoC your microservice later on will have to do a Post to return the data
	 * from the request above
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRestFromSomewhere() throws Exception {
		CloseableHttpClient client = newClient();
		HttpGet httpGet = new HttpGet(REST_TEST_ENDPOINT);
		HttpResponse response = client.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			return new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines()
					.collect(Collectors.joining("\n"));
		else
			return "ERROR: " + response.getStatusLine().getStatusCode();
	}

	// https://stackoverflow.com/questions/19517538/ignoring-ssl-certificate-in-apache-httpclient-4-3
	private CloseableHttpClient newClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext context = SSLContexts.custom().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build();

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE)).build();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);

		return HttpClients.custom().setConnectionManager(connectionManager).build();
	}
}