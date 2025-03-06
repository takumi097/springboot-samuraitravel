package com.example.samuraitravel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewInputForm;
import com.example.samuraitravel.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final HouseService houseService;
	
	public ReviewService(ReviewRepository reviewRepository, HouseService houseService) {
		this.reviewRepository = reviewRepository;
		this.houseService = houseService;
	}
	
	//すべてのレビューを作成日時が新しい順に民宿詳細に表示するメソッド
	public List<Review> findReviewByHouseIdOrderByCreatedAtDesc(Integer houseId) {
		return reviewRepository.findByHouseIdOrderByCreatedAtDesc(houseId);
	}

	//すべてのレビューをページングされた状態で取得するメソッド
	public Page<Review> findlReviewByHouseId(Integer houseId, Pageable pageable) {
		return reviewRepository.findByHouseId(houseId,pageable);
	}
	
	//指定したidを持つレビューを取得する
	public Optional<Review> findReviewById(Integer reviewId) {
		return reviewRepository.findById(reviewId);
	}
	
	//データベースからユーザーのレビューを検索し、存在するかどうかを調べる
	public boolean hasUserPostedReview(Integer houseId, Integer userId) {
		return reviewRepository.existsByHouseIdAndUserId(houseId, userId);
	}
	
	//レビューを登録するメソッド
	@Transactional
	public void createReview(ReviewInputForm reviewRegisterForm, User user) {
		Review review = new Review();
		
		review.setScore(reviewRegisterForm.getScore());
		review.setComment(reviewRegisterForm.getComment());
		
		Optional<House> optionalHouse = houseService.findHouseById(reviewRegisterForm.getHouseId());
		if(optionalHouse.isPresent()) {
			review.setHouse(optionalHouse.get());
		} else {
			throw new IllegalArgumentException("Invalid house ID:" + reviewRegisterForm.getHouseId());
		}
		
		review.setUser(user);
		
		reviewRepository.save(review);
	}
	
	//レビューを編集するメソッド
	@Transactional
	public void updateReview(ReviewEditForm reviewEditForm, Review review) {
		review.setScore(reviewEditForm.getScore());
		review.setComment(reviewEditForm.getComment());
		
		reviewRepository.save(review);
	}
	
	//レビューを削除するメソッド
	@Transactional
	public void deleteReview(Review review) {
		reviewRepository.delete(review);
	}
}