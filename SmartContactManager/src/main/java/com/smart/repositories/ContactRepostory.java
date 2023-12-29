package com.smart.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entities.Contact;
import com.smart.entities.User;
@Repository
@EnableJpaRepositories
public interface ContactRepostory extends JpaRepository<Contact, Integer> {
   //create new repo for pegination
	@Query("from Contact as c where c.user.uId =:userId ")
	public Page<Contact> findContactByUser(@Param("userId") int userId,Pageable pageable);
	
	public List<Contact> findByNameContainingAndUser(String keyword,User user);
	
}
