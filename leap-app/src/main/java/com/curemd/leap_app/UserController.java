package com.curemd.leap_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leap")

public class UserController {

	@GetMapping("/")
	public String getUser() {
		return "this is user";
	}

}
