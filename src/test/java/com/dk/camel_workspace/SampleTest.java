package com.dk.camel_workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.sqs.Sqs2Constants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SampleTest extends CamelTestSupport {
	// create the queue if the localstack is stopped >
	// aws sqs create-queue --queue-name localstack-queue --region us-east-1
	// --profile localstack
	public static final String FROM_SQS = "aws2-sqs://localstack-queue?amazonSQSClient=#amazonSQSClient"; // "direct:toSQS";

	@EndpointInject("mock:received")
	private MockEndpoint receiver;
	
	String msgAttributeNames1 = "&messageAttributeNames=HEADER1";
	String msgAttributeNames2 = "&messageAttributeNames=HEADER-1,HEADER-2,HEADER-3,HEADER-4,HEADER-5,HEADER-6,HEADER-7,HEADER-8,HEADER-9,HEADER-10,HEADER-11,HEADER-12";

	private static SqsClient createSqsClient() {
		return SqsClient.builder().region(Region.US_EAST_1) // Use any region, LocalStack ignores this.
				.endpointOverride(URI.create("http://localhost:4566")) // LocalStack SQS endpoint
				.credentialsProvider(
						StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKey", "secretKey")))// use
																												// any
																												// credentials,
																												// LocalStack
																												// ignores
																												// this.
				.build();
	}

	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
		context.getRegistry().bind("amazonSQSClient", createSqsClient());
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				// Sending a message to SQS
				from("direct:sendToSqs")
					.log("------------- BEFORE --------------- to SQS : ${headers} || ${body}")
//					.process(new Processor() {
						
//						@Override
//						public void process(Exchange exchange) throws Exception {
//							Map<String, Object> headers = exchange.getIn().getHeaders();
//							Map<String, Object> msgAtt = new HashMap<String, Object>();
//							
//							for(Entry<String, Object> entry : headers.entrySet()) {
//								msgAtt.put(entry.getKey(), entry.getValue());
//							}
//							
//							exchange.getIn().setHeader(Sqs2Constants.MESSAGE_ATTRIBUTES, msgAtt);
//								
//						}
//					})
						.to(FROM_SQS).log("***************** Received message: ${headers}");

				// Receiving messages from SQS
				from(FROM_SQS + msgAttributeNames1)
						.log("------------- AFTER --------------- to SQS : ${headers} || ${body}")
						.process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								String header_1 = (String) exchange.getIn().getHeader("HEADER1",
										"CamelAwsSqsMessageAttributes.HEADER1");
								System.out.println("########### HEADER1 : " + header_1);
							}
						})
						.to(receiver);
			}
		};
	}

	@Test
	public void testSendToSqs() throws Exception {
		// Send a test message
		template.sendBodyAndHeader("direct:sendToSqs", "Hello from Camel!", "HEADER1", "Name");

		Thread.sleep(2000);

		receiver.assertIsSatisfied();

		Exchange result = receiver.getExchanges().get(0);
		assertEquals("Hello from Camel!", result.getIn().getBody());

	}

	@Test
	public void testSendToSqsWithMultipleHeaders() throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();

		for (int i = 0; i < 12; i++) {
			headers.put("HEADER-" + i, "Value-" + i);
		}

		// Send a test message
		template.sendBodyAndHeaders("direct:sendToSqs", "Hello from Camel!", headers);

		Thread.sleep(2000);

		receiver.assertIsSatisfied();

		Exchange result = receiver.getExchanges().get(0);
		assertEquals("Hello from Camel!", result.getIn().getBody());

	}
}
