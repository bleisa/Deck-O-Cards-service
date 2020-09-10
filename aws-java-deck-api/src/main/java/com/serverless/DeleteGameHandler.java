package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DeleteGameHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(DeleteGameHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			String[] pieces = ((String) input.get("path")).split("/");
			String code = pieces[2];
			boolean success = new Game().delete(code);
			Map<String, String> headers = new HashMap<>();
			headers.put("X-Powered-By", "AWS Lambda & Serverless");
			headers.put("Content-Type", "application/json");
			Response responseBody;
			if (success) {
				responseBody = new Response("game deleted successfully: ", input);
				return ApiGatewayResponse.builder()
						.setStatusCode(200)
						.setObjectBody(responseBody)
						.setHeaders(headers)
						.build();
			} else {
				responseBody = new Response("game with code " + code + " not found", input);
				return ApiGatewayResponse.builder()
						.setStatusCode(404)
						.setObjectBody(responseBody)
						.setHeaders(headers)
						.build();
			}
		} catch (Exception e) {
			LOG.error("error in deleting game: " + e);
			Response responseBody = new Response("error in deleting game: " + e, input);
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
