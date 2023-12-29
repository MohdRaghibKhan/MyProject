package com.smart.controllers;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.repositories.ContactRepostory;
import com.smart.repositories.UserRepository;

@RestController
public class SearchController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepostory contactRepostory;
	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal){
		System.out.println(query);
		User user = this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts = this.contactRepostory.findByNameContainingAndUser(query, user);
//	 for (Contact contact : contacts) {
//		System.out.println(contact.getName());
//		System.out.println(contact.getSecond_name());
//		System.out.println(contact.getC_id());
//	}
		return ResponseEntity.ok(contacts);
		
	}

}
