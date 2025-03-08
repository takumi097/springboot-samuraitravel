package com.example.samuraitravel.controller;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses")
public class HouseController {
	private final HouseService houseService;
	private final ReviewService reviewService;
	private final FavoriteService favoriteService;
	private boolean hasPostedFavorite;
	
	
	public HouseController(HouseService houseService, ReviewService reviewService, FavoriteService favoriteService) {
		this.houseService = houseService;
		this.reviewService = reviewService;
		this.favoriteService = favoriteService;
		
	}

	
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
						@RequestParam(name = "area", required = false) String area,
						@RequestParam(name = "price", required = false) Integer price,
						@RequestParam(name = "order", required = false) String order,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
						Model model) {
		Page<House> housePage;
		
		if(keyword != null && !keyword.isEmpty()) {
			if(order != null && order.equals("priceAsc")) {
				housePage = houseService.findHousesByNameLikeOrAddressLikeOrderByPriceAsc(keyword, keyword, pageable);
			} else {
				housePage = houseService.findHousesByNameLikeOrAddressLikeOrderByCreatedAtDesc(keyword, keyword, pageable);
			}
		} else if(area != null && !area.isEmpty()) {
			if(order != null && order.equals("priceAsc")) {
				housePage = houseService.findHousesByAddressLikeOrderByPriceAsc(area, pageable);
			} else {
				housePage = houseService.findHousesByAddressLikeOrderByCreatedAtDesc(area, pageable);
			}
		} else if(price != null) {
			if(order != null && order.equals("priceAsc")) {
				housePage = houseService.findHousesByPriceLessThanEqualOrderByPriceAsc(price, pageable);
			} else {
				housePage = houseService.findHousesByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		} else {
			if(order != null && order.equals("priceAsc")) {
				housePage = houseService.findAllHousesByOrderByPriceAsc(pageable);
			} else {
				housePage = houseService.findAllHousesByOrderByCreatedAtDesc(pageable);
			}
		}
		
		model.addAttribute("housePage", housePage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("area", area);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		
		return "houses/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, 
					   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
					   RedirectAttributes redirectAttributes, Model model) {
		Optional<House> optionalHouse = houseService.findHouseById(id);
		List<Review> reviews = reviewService.findReviewByHouseIdOrderByCreatedAtDesc(id);
		
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/houses";
		}
		
		House house = optionalHouse.get();
		model.addAttribute("house", house);
		model.addAttribute("reservationInputForm", new ReservationInputForm());
		model.addAttribute("reviews", reviews);
		
		//ユーザーの情報を取得
		User user = (userDetailsImpl != null) ? userDetailsImpl.getUser() : null;
		
		//ユーザーがすでにレビューを投稿しているかの確認
		boolean hasPostedReview = false;
		if(user != null) {
			hasPostedReview = reviewService.hasUserPostedReview(id, user.getId());
		}
		
		boolean hasPostedFavorite = false;
		if (user != null) {
			hasPostedFavorite = favoriteService.hasUserPostedFavorite(id, user.getId());
		}
		
		model.addAttribute("hasPostedReview", hasPostedReview);
		model.addAttribute("hasPostedFavorite", hasPostedFavorite);
		
		return "houses/show";
	}
	
	//レビューを削除
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Review review) {
		Optional<Review> optionalReview = reviewService.findReviewById(review.getId());
		
		if(optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "レビューが存在しません。");
			
			return "redirect:/houses/" + id;
		}
		
		reviewService.deleteReview(review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/houses/" + id;
	}
}