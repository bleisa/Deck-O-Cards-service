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
			String[] pieces = ((String) input.get("path")).split("/");
			String code = pieces[3];
			String cardString = pieces[4];
			String howString = pieces[5];
			String pass = pieces[6];
			String player = pieces[7];
			Game g = new Game().getGame(code);
			if (g != null) {
				Card c;
				if (!cardString.equals("null")) {
					c = (new CardConverter()).unconvert(URLDecoder.decode(cardString, StandardCharsets.UTF_8));
				} else {
					c = null;
				}
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
