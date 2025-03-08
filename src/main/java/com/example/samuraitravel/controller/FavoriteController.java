package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
public class FavoriteController {
	private final FavoriteService favoriteService;
	
	public FavoriteController(FavoriteService favoriteService) {
		this.favoriteService = favoriteService;
	}
	
	//お気に入り追加メソッド
	@PostMapping("/houses/{houseId}/addfavorite")
	public String addFavorite(@PathVariable Integer houseId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
	
		if (userDetailsImpl != null) {
			Integer userId = userDetailsImpl.getUser().getId();
			favoriteService.addFavorite(houseId,  userDetailsImpl, userId);
	}
	
		return "redirect:/houses/" + houseId;
}
	
	//お気に入り削除メソッド
	@PostMapping("/houses/{houseId}/deleteFavorite")
	public String deleteFavorite(@PathVariable Integer houseId,  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl ) {
		
		if (userDetailsImpl != null) {
			Integer userId = userDetailsImpl.getUser().getId();
			favoriteService.deleteFavorite(houseId, userId);
	}
		return "redirect:/houses/" + houseId;
	}
	
	@GetMapping("/favorites")
	public String indec(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model)
	{
		User user = userDetailsImpl.getUser();
		Page<Favorite> favoritePage = favoriteService.findFavoritesByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("favoritePage", favoritePage);
		
		return "favorites/index";
	}
}
