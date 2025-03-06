package com.example.samuraitravel.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.service.HouseService;

@Controller
public class HomeController {
	private final HouseService houseService;
	
	public HomeController(HouseService houseService) {
		this.houseService = houseService;
	}
	@GetMapping("/")
	public String index(Model model) {
		List<House> newHouses = houseService.findTop8HouseByOrderByCreatedAtDesc();
		List<House> popularHouse = houseService.findTop3HouseByOrderByReservationCountDesc();
		model.addAttribute("newHouses", newHouses); 
		model.addAttribute("popularHouse", popularHouse);
		
		return "index";
	}
}