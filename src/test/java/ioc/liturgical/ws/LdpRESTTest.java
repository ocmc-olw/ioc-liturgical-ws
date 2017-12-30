package ioc.liturgical.ws;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Suite;

import io.restassured.http.ContentType;
import ioc.liturgical.test.framework.TestConstants;
import ioc.liturgical.test.framework.TestUsers;
import ioc.liturgical.ws.app.ServiceProvider;
import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.constants.HTTP_RESPONSE_CODES;

public class LdpRESTTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ServiceProvider.main(new String[] {
				System.getenv("pwd")
				, TestConstants.DISABLE_EXTERNAL_DB
				});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	   public void testLDPGregorianValidDate() {
		    io.restassured.RestAssured
		    	.given()
				.baseUri(TestConstants.BASE_URL)
				.basePath(Constants.INTERNAL_LITURGICAL_DAY_PROPERTIES_API_PATH + "/ldp")
				.auth(). preemptive().basic(
						TestUsers.WS_ADMIN.id
						, System.getenv("pwd")
						)
			     .accept(ContentType.JSON)
			     .queryParam("t", "g")
			     .queryParam("d", "2016-11-19T12:00:00.000Z")
			     .expect().statusCode(HTTP_RESPONSE_CODES.OK.code)
		    	.when().get("/");
	    }
	
}
