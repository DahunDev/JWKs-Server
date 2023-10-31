package com.daniel.Main;


import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

//Handler for /jwks endpoint
	//ChatGPT prompt: what should I write in JWKSHandler 

	public class JWKSEndpoint implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			//String requestPath = exchange.getRequestURI().getPath();
			String para = exchange.getRequestURI().getQuery();
			//System.out.println("requestPath in JWKS: " + requestPath);
			//System.out.println("MEthod in JWKS: " + exchange.getRequestMethod());
			System.out.println("Parameter in JWKS: " + para);

			if ("GET".equals(exchange.getRequestMethod())) {
				// Filter keys based on expiration

		            // Convert the JWKS JSON object to a string
		            String jwksJson = JwtsServer.generateJwksJson();
		            
		       //     System.out.println("jwksJson: " + jwksJson);
		            // Send JWKS response with a status code and the JWKS JSON in the response body
		            exchange.getResponseHeaders().set("Content-Type", "application/json");

		            byte[] responseBytes = jwksJson.getBytes("UTF-8");
		            exchange.sendResponseHeaders(200, responseBytes.length);
		            OutputStream os = exchange.getResponseBody();
		            os.write(responseBytes);
		            os.close();
		            exchange.close();
	            
			} else {
				// Method not allowed
				System.out.println("Method for JWT: " + exchange.getRequestMethod());
		
				exchange.sendResponseHeaders(405, -1);
				exchange.getResponseBody().close();
				exchange.close();
			}
		}
	}	