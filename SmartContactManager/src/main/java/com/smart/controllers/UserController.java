package com.smart.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repositories.ContactRepostory;
import com.smart.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepostory contactRepostory;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	// method to adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
//		System.out.println(name+" >>>>>>>>>>>>>>>>>> ");
		User user = userRepository.getUserByUserName(name);
		model.addAttribute("user", user);
//		System.out.println(user);
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("tittle", "User DashBoard");
		return "normal/user_dashboard";
	}

//	open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, User user) {
		model.addAttribute("tittle", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// process add contact form
	@PostMapping("/process-contact") // @ModelAttribute this is will set all field data to contact object from Html
										// form
	public String processContact(@ModelAttribute Contact contact, @RequestParam("imagePhoto") MultipartFile file, // image
																													// field
																													// can't
																													// be
																													// same
			Model model, Principal principal, HttpSession httpSession) {
		try {

			User user = this.userRepository.getUserByUserName(principal.getName());
			System.out.println(user.toString());
			// processing & uploading file
			if (file.isEmpty()) {
				contact.setImage("contact.png");
				System.out.println(">>>>>>" + contact.getImage());
//				throw new Exception("File is empty");

			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is uploaded");

			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("DATA " + contact);
			model.addAttribute("tittle", "process Contact form");
			httpSession.setAttribute("message", new Message("Registered Successfully", "alert-success"));

		} catch (Exception e) {
			System.out.println("Error  :" + e.getMessage());
			httpSession.setAttribute("message", new Message("Something wents wrong!!! try again", "alert-danger"));
		}
		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("tittle", "show user contacts");
		User user = userRepository.getUserByUserName(principal.getName());

		Pageable pageRequest = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepostory.findContactByUser(user.getuId(), pageRequest);
//		for(Contact c:  contacts) {
//			System.out.println(c.getImage());
//		}
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cid, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepostory.findById(cid);
		Contact contact = contactOptional.get();
		// validating cId
		User user = userRepository.getUserByUserName(principal.getName());
		if (user.getuId() == contact.getUser().getuId()) {
			model.addAttribute("contact", contact);
		}
		return "/normal/contact_detail";
	}

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cid, Model model, Principal principal,
			HttpSession httpSession) {
		try {
			Optional<Contact> optionalContact = contactRepostory.findById(cid);
			Contact contact = optionalContact.get();
			// validating cId
			User user = userRepository.getUserByUserName(principal.getName());
			// unlinking user from contact obj before delete
			if (user.getuId() == contact.getUser().getuId()) {
				contact.setUser(null);
				// deleting image from target folder
				File storeImgPath = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(storeImgPath.getAbsolutePath() + File.separator + contact.getImage());
				// preventing deletion of default image
				if (!contact.getImage().equals("contact.png"))
					Files.delete(path);
				contactRepostory.delete(contact);
				httpSession.setAttribute("message", new Message("contact deleted successfully!!!", "alert-success"));
			} else {
				// if user id is not matched with contact
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			httpSession.setAttribute("message", new Message("something wents wrong", "alert-danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	@GetMapping("/deleteUser/{uId}")
	public String deleteUser(@PathVariable("uId") Integer uId, Model model, Principal principal,
			HttpSession httpSession) {
		try {
			User user = userRepository.findById(uId).get();
			// validating cId
			User loginUser = userRepository.getUserByUserName(principal.getName());

			// unlinking user from contact obj before delete
			if (user.getuId() == loginUser.getuId()) {
//				contactRepostory.deleteAllById();
				user.getContacts().forEach(contact -> contact.setUser(null));
				user.getContacts().clear();
				// deleting image from target folder
				File storeImgPath = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(storeImgPath.getAbsolutePath() + File.separator + user.getImage_url());
				// preventing deletion of default image
				if (!user.getImage_url().equals("contact.png"))
					Files.delete(path);
				userRepository.delete(user);
				httpSession.setAttribute("message", new Message("contact deleted successfully!!!", "alert-success"));
			} else {
				// if user id is not matched with contact
				throw new Exception();
			}
		} catch (Exception e) {
			e.printStackTrace();
			httpSession.setAttribute("message", new Message("something wents wrong", "alert-danger"));
		}

		return "redirect:/signin";
	}

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("tittle", "update form");
		Contact contact = this.contactRepostory.findById(cid).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("imagePhoto") MultipartFile file,
			Principal principal, HttpSession httpSession) {
		try {
			System.out.println("Contact Name " + contact.getName());
			System.out.println("contact id " + contact.getC_id());
			Contact oldContactDetails = this.contactRepostory.findById(contact.getC_id()).get();
			// image...
			if (!file.isEmpty()) {
				// deleting old image from target
				File saveFilepath = new ClassPathResource("static/img").getFile();
				File file2 = new File(saveFilepath, oldContactDetails.getImage());
				// updating image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				file2.delete();
			} else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepostory.save(contact);
			httpSession.setAttribute("message", new Message("contact Updated successfully!!!", "alert-success"));

		} catch (Exception e) {
			httpSession.setAttribute("message", new Message("Something wents wrong !!!", "alert-danger"));

		}

		return "redirect:/user/contact/" + contact.getC_id();
	}

	@GetMapping("/profile")
	public String userProfile(Model model, Principal principal) {
		model.addAttribute("tittle", "User Profile");
		User user = userRepository.getUserByUserName(principal.getName());
		return "normal/profile";
	}

	// open setting handler
	@GetMapping("/setting")
	public String openSetting() {

		return "normal/setting";
	}

	// open setting handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession httpSession) {
		User currentUser = userRepository.getUserByUserName(principal.getName());
		if(this.bCryptPasswordEncoder.matches(newPassword, currentUser.getPassword())) {
			//change Password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			httpSession.setAttribute("message", new Message("Your Password is successfully changed!!!", "alert-success"));

		}else {
			httpSession.setAttribute("message", new Message("Incorrect OldPassword,please enter correct password", "alert-danger"));
			return "redirect:/user/setting";
		}
		return "redirect:/user/index";
	}
}
