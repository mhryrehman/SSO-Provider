package com.curemd.leap_app;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class LeapController {

	@Value("${keycloak.introspectURl}")
	private String introspectURl;

	@GetMapping("/")
	public RedirectView getUser(HttpServletRequest request) {
		String token = getTokenFromCookie(request);
		boolean isValid = validateToken(token);
		if(isValid) {
			return new RedirectView("success.html");
		}else {
			return new RedirectView("failed.html");
		}
	}

	@PostMapping("/login")
	public RedirectView login(@RequestParam String username, @RequestParam String password) {
		// write down logic to validate userName and password
		return new RedirectView("success.html");
	}

	@GetMapping("/getTokenFromCookie")
	public String getTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("keycloakToken")) {
					return cookie.getValue();
				}
			}
		}
		return "No token found in cookies";
	}
	
    @GetMapping("/getRefreshTokenFromCookie")
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return "No refresh token found in cookies";
    }


	@GetMapping("/leap-logout")
	public RedirectView mylogout(HttpServletRequest servletRequest) {
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.getForEntity("http://172.16.102.238:8090/auth/logout-leap?token=" + getRefreshTokenFromCookie(servletRequest), String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				return new RedirectView("/index.html");
			} else {
				return new RedirectView("/success.html");
			}
		} catch (HttpClientErrorException e) {
			return new RedirectView("/index.html");
		}		
	}
	
	@GetMapping("/welcome")
	public String welcome() {
		return "welcome";
	}

	private boolean validateToken(String token) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("client_id", "test");
		request.add("client_secret", "g0Z2bIgiDs2BB18HsYbD5DnfxKUYoX4w");
		request.add("token", token);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(request, headers);        
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(introspectURl, entity, Map.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				boolean isActive = (boolean) response.getBody().get("active");
				return isActive;
			} else {
				System.out.println("Failed to get token from Keycloak. Status: " + response.getStatusCode());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
