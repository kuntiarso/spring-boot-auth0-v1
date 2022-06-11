package com.kuntia.demo.springauth0.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.JSONValue;

@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class APIController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @GetMapping(value = "/public")
    public ResponseEntity<Object> publicEndpoint() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(Map.of(
                "status", 200,
                "message", "success",
                "result", "public access is called successfully"
            ));
    }

    @GetMapping(value = "/private")
    public ResponseEntity<Object> privateEndpoint(JwtAuthenticationToken jwt) throws JsonProcessingException {
        String access_token = jwt.getToken().getTokenValue();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ".concat(access_token));
        HttpEntity<String> request = new HttpEntity<>("headers", headers);

        RestTemplate template = new RestTemplate();
        Object user = template.exchange(issuer.concat("userinfo"), HttpMethod.POST, request, Object.class);
        JSONObject jsonObject = (JSONObject) JSONValue.parse(new ObjectMapper().writeValueAsString(user));

        JSONObject result = new JSONObject();
        result.put("status", 200);
        result.put("message", "success");
        result.put("result", jsonObject.get("body"));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
