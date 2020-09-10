package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.DeckType;
import com.serverless.dal.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CreateGameHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(CreateGameHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			Game g = new Game();
			g.save(g);
			Map<String, String> headers = new HashMap<>();
			headers.put("X-Powered-By", "AWS Lambda & Serverless");
			headers.put("Content-Type", "application/json");
			return ApiGatewayResponse.builder()
					.setStatusCode(200)
					.setRawBody(g.getCode())
					.setHeaders(headers)
					.build();
		} catch (Exception e) {
			String message = "error in creating game: ";
			LOG.error(message, e);
			Response responseBody = new Response(message + e, input);
			Map<String, String> headers = new HashMap<>();
			headers.put("X-Powered-By", "AWS Lambda & Serverless");
			headers.put("Content-Type", "application/json");
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(headers)
					.build();
		}
	}
}
