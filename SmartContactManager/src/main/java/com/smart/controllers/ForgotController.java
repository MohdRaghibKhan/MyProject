package com.smart.controllers;

import java.security.Principal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repositories.UserRepository;
import com.smart.service.EmailSenderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	@Autowired
	EmailSenderService emailSenderService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	 BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession httpSession) {
		try {
			// Generate a random 4-digit OTP
			int otp = new Random().nextInt(9000) + 1000;
			String to = email;
			String from = "raghibkhan889@gmail.com";
			String subject = "Testing OTP";
		    String text = ""
			            + "<div style = 'border:1px solid #e2e2e2; padding:20px' >"
					    + "<h3>"
					    + "Your 4 digit OTP is :" 
					    + "<b>" 
					    + otp 
					    + "</b>" 
					    + "</h3>" 
					    + "</div>";
			boolean b = emailSenderService.sendEmail(to, from, subject, text);
			if (b) {
				httpSession.setAttribute("storedOtp", otp);
				httpSession.setAttribute("email", email);

				httpSession.setAttribute("message", new Message("we have sent OTP to your email", "alert-success"));
				return "verify_otp";
			} else {
				new Exception();
			}
		} catch (Exception e) {
			httpSession.setAttribute("message",
					new Message("Something wents wrong please try again!!!", "alert-danger"));
		}
		return "forgot_email_form";
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession httpSession, Principal principal) {
		int storedOtp = (int) httpSession.getAttribute("storedOtp");
		String email = (String) httpSession.getAttribute("email");
		if (otp == storedOtp) {
			User user = userRepository.getUserByUserName(email);
			if(user==null) {
				httpSession.setAttribute("message",new Message("User does not exist", "alert-danger"));
				return "forgot_email_form";
			}
		} else {
			httpSession.setAttribute("message",new Message("please enter correct OTP", "alert-danger"));
		}
		return "password_change_form";
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("password") String password, HttpSession httpSession) {
		String email = (String) httpSession.getAttribute("email");
		User user = userRepository.getUserByUserName(email);
		if(user!=null) {
			user.setPassword(bCryptPasswordEncoder.encode(password));
			userRepository.save(user);
			return "redirect:/signin?change=Your Password is changed successfully...";
		}else {
			return "password_change_form";
		}
		
	}
}
