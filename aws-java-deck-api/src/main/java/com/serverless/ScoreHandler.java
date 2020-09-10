package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.dal.CardConverter;
import com.serverless.dal.Game;
import com.serverless.dal.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScoreHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(ScoreHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			String[] pieces = ((String) input.get("path")).split("/");
			String code = pieces[3];
			String name = pieces[4];
			String points = pieces[5];
			Game g = (new Game()).getGame(code);
			if (g != null) {
				int pointsInt = unconvertInt(points);
				g.score(g.getPlayer(name), pointsInt);
				g.save(g);
				Response responseBody = new Response("added score successfully", input);
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
			String message = "error in adding score: ";
			LOG.error(message, e);
			Response responseBody = new Response(message + e + " " + e.getCause(), input);
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

	private int unconvertInt(String s) {
		int value = 0;
		char[] digits = s.toCharArray();
		for (char digit: digits) {
			value = value * 10 + Character.getNumericValue(digit);
		}
		return value;
	}
}
