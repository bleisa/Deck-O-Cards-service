package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.dal.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class GetPlayersHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(GetPlayersHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			String[] pieces = ((String) input.get("path")).split("/");
			String code = pieces[2];
			Game g = new Game().getGame(code);
			if (g != null) {
				return ApiGatewayResponse.builder()
						.setStatusCode(200)
						.setObjectBody(g.getPlayerNames())
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
			LOG.info("error in updating game: " + e);
			Response responseBody = new Response("error in updating game: " + e, input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
					.build();
		}
	}
}
