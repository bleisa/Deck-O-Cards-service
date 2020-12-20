package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class SetSettingsHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(SetSettingsHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode body = mapper.readTree((String) input.get("body"));
			Map<String, String> query = (Map<String, String>) input.get("pathParameters");
			String code = query.get("code");
			Settings s = mapper.readValue(mapper.writeValueAsString(body.get("settings")), Settings.class);
			Game g = new Game().getGame(code);
			if (g != null) {
				g.setUp(s);
				g.save(g);
				Response responseBody = new Response("settings saved successfully: ", input);
				return ApiGatewayResponse.builder()
						.setStatusCode(200)
						.setObjectBody(responseBody)
						.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
						.build();
			} else {
				Response responseBody = new Response("game with code " + code + " not found", input);
				return ApiGatewayResponse.builder()
						.setStatusCode(404)
						.setObjectBody(responseBody)
						.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
						.build();
			}
		} catch (Exception e) {
			LOG.error("error in saving settings: " + e);
			Response responseBody = new Response("error in saving settings: " + e, input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
					.build();
		}
	}
}
