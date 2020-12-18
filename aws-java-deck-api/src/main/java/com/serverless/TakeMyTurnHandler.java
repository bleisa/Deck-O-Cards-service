package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class TakeMyTurnHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(TakeMyTurnHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		try {
			Map<String, String> query = (Map<String, String>) input.get("pathParameters");
			JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
			String code = query.get("code");
			JsonNode cardNode = body.get("cardPlayed");
			Card c;
			if (!cardNode.isNull()) {
				c = new Card(Suit.valueOf(cardNode.get("suit").asText()), cardNode.get("value").asInt());
			} else {
				c = null;
			}
			String howString = body.get("how").asText();
			String pass = body.get("passedTo").asText();
			String player = body.get("playedBy").asText();
			Game g = new Game().getGame(code);
			if (g != null) {
				WayToPlay how = (new WayToPlayConverter()).unconvert(howString);
				g.takeTurn(player, c, how, pass);
				g.save(g);
				return ApiGatewayResponse.builder()
						.setStatusCode(200)
						.setObjectBody(g)
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
			LOG.error("error in taking turn: " + e);
			Response responseBody = new Response("error in taking turn: " + e, input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
					.build();
		}
	}
}
