package com.smart.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	 BCryptPasswordEncoder bCryptPasswordEncoder;
	@RequestMapping("/")
	public String home(Model model) {
		System.out.println("home page");
		model.addAttribute("tittle", "Home - Smart Contact Manager");
		return "home";
	}
	@RequestMapping("/about")
	public String about(Model model) {
		System.out.println("about page");
		model.addAttribute("tittle", "About page");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model model) {
		System.out.println("signup page");
		model.addAttribute("tittle", "Register- Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, @RequestParam("imagePhoto") MultipartFile file,
			BindingResult bindingResult,@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	        Model model, HttpSession httpSession) throws Exception {
	    try {
	    	 if (!agreement) {
	 	        throw new Exception("Please Accept Terms & Conditions");
	 	    }
	 	    if (bindingResult.hasErrors()) {
	 	        // Populate the model with the user object and binding result
	 	        model.addAttribute("user", user);
	 	        return "signup"; // Return the signup page to display errors
	 	    }
//	 	    saving image..
	 	   if (file.isEmpty()) {
				user.setImage_url("contact.png");
				System.out.println(">>>>>>"+user.getImage_url());
//				throw new Exception("File is empty");

			} else {
				user.setImage_url(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is uploaded");

			}
	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	        User save = userRepository.save(user);
	        System.out.println(save);
	        httpSession.setAttribute("title", "Register - Smart Contact Manager");
	        httpSession.setAttribute("user", user);
	        httpSession.setAttribute("message", new Message("Registered Successfully", "alert-success"));
	        return "signup";
	    } catch (Exception e) {
	        e.printStackTrace();
	        model.addAttribute("user", user);
	        httpSession.setAttribute("message", new Message("Please Accept Terms & Conditions", "alert-danger"));
	        return "signup";
	    }
	}
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("tittle","Login page - Smart Contact Manager");
		return "login";
	}
	
}
