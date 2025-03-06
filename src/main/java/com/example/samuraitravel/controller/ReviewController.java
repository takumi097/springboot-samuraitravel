package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewInputForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.HouseService;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses/{houseId}/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final HouseService houseService;
	
	public ReviewController(ReviewService reviewService, HouseService houseService) {
		this.reviewService = reviewService;
		this.houseService = houseService;
	}

	//レビュー一覧を表示するメソッド
	@GetMapping("/index")
	public String index(@PathVariable(name = "houseId") Integer houseId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC)
						Pageable pageable, Model model) {
		
		//民宿の情報を取得
		Optional<House> optionalHouse =houseService.findHouseById(houseId);
		if(optionalHouse.isEmpty()) {
			return "redirect:/houses";
		}
		model.addAttribute("house", optionalHouse.get());
		
		//ユーザーの情報を取得(nullを考慮)
		User user = (userDetailsImpl != null) ? userDetailsImpl.getUser() : null;
		model.addAttribute("user", user);
		
		//レビュー情報を取得
		Page<Review> reviewPage = reviewService.findlReviewByHouseId(houseId, pageable);
		model.addAttribute("reviewPage", reviewPage);
		
		return "reviews/index";
	}
	
	//レビュー投稿フォームを表示するメソッド
	@GetMapping("/input")
	public String input(@PathVariable(name = "houseId") Integer houseId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		
		Optional<House> optionalHouse =houseService.findHouseById(houseId);
		if(optionalHouse.isEmpty()) {
			return "redirect:/houses";
		}
		model.addAttribute("house", optionalHouse.get());
				
		User user = (userDetailsImpl != null) ? userDetailsImpl.getUser() : null;
		model.addAttribute("user", user);
		
		//フォームオブジェクトをセット
		model.addAttribute("reviewInputForm", new ReviewInputForm());
		
		return "reviews/input";
	}
	
	//レビュー編集ページを表示するメソッド
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "houseId") Integer houseId,
					   @PathVariable(name = "reviewId") Integer reviewId,
					   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		
		Optional<House> optionalHouse =houseService.findHouseById(houseId);
		if(optionalHouse.isEmpty()) {
			return "redirect:/houses";
		}
		model.addAttribute("house", optionalHouse.get());
				
		User user = (userDetailsImpl != null) ? userDetailsImpl.getUser() : null;
		model.addAttribute("user", user);
		
		//レビューを取得
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		Review review = optionalReview.get();
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getScore(), review.getComment());
		
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);
		
		return "reviews/edit";
	}
	
	//投稿されたレビューを登録するメソッド
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated ReviewInputForm reviewInputForm,
						 @PathVariable(name = "houseId") Integer houseId,
						 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 BindingResult bindingResult, RedirectAttributes redirectAttributes,
						 Model model) {
		
		Optional<House> optionalHouse = houseService.findHouseById(houseId);
		if(optionalHouse.isEmpty()) {
			return "redirect:/houses";
		}
		
		// バリデーションエラーがある場合
		if(bindingResult.hasErrors()) {
			model.addAttribute("reviewInputForm", reviewInputForm);
			return "reviews/input";
		}
		
		// houseIdをReviewInputFormに設定
		reviewInputForm.setHouseId(houseId);
		
		User user = (userDetailsImpl != null) ? userDetailsImpl.getUser() : null;
		
		// 投稿されたレビューの登録
		reviewService.createReview(reviewInputForm, user);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");
		
		return "redirect:/houses/{houseId}/reviews/index";
	}
	
	//投稿されたレビューを編集するメソッド
	@PostMapping("/{reviewId}/update")
	public String update(@ModelAttribute @Validated ReviewEditForm reviewEditForm,
			 			 @PathVariable(name = "houseId") Integer houseId,
			 			 @PathVariable(name = "reviewId") Integer reviewId,
			 			 BindingResult bindingResult, RedirectAttributes redirectAttributes,
			 			 Model model) {
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("reviewEditFrom", reviewEditForm);
			return "reviews/edit";
		}
		
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		Review review = optionalReview.get();
		reviewService.updateReview(reviewEditForm, review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
		
		return "redirect:/houses/{houseId}/reviews/index";
	}
	
	//レビュー削除するメソッド
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable(name = "houseId") Integer houseId,
						 @PathVariable(name = "reviewId") Integer reviewId,
						 RedirectAttributes redirectAttributes) {
		
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		Review review = optionalReview.get();
		reviewService.deleteReview(review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/houses/{houseId}";
	}
}