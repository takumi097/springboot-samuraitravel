package com.example.samuraitravel.service;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.UserRepository;
import com.example.samuraitravel.security.UserDetailsImpl;

@Service
public class FavoriteService {
	@Autowired
	private final FavoriteRepository favoriteRepository;
	private final UserRepository userRepository;
	private final HouseRepository houseRepository;
	
	
	public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, HouseRepository houseRepository) {
		this.favoriteRepository = favoriteRepository;
		this.userRepository = userRepository;
		this.houseRepository = houseRepository;
	}
	
	//お気に入りリストを取得
	public Page<Favorite> findFavoritesByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
		
		return favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
	}
	
	public boolean isFavorite(Integer userId, Integer houseId) {
		return favoriteRepository.existsByUserIdAndHouseId(userId, houseId);
	}
	
	//指定したIDを持つお気に入りを取得する
	public Optional<Favorite> findFavoriteById(Integer favoriteId) {
		return favoriteRepository.findById(favoriteId);
	}
	
	//ユーザーのレビューを検索し、存在するかどうかを調べる
	public boolean hasUserPostedFavorite(Integer houseId, Integer userId) {
		return favoriteRepository.existsByUserIdAndHouseId(userId, houseId);
	}
			
	
	//お気に入りを登録
	@Transactional
	public void addFavorite(@PathVariable Integer houseId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Integer userId) {
		Favorite favorite = new Favorite();
		
		
		House house = houseRepository.findById(houseId).orElseThrow(() -> new EntityNotFoundException("指定されたIDの民宿が存在しません。"));
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("指定されたIDのユーザーが存在しません。"));
		
		favorite.setHouse(house);
		favorite.setUser(user);
		
		favoriteRepository.save(favorite);
		}
	 
	//お気に入りの削除
	public void deleteFavorite(Integer houseId, Integer userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
		House house = houseRepository.findById(houseId).orElseThrow(() -> new IllegalArgumentException("Inavalid house ID"));
		List<Favorite> favorite = favoriteRepository.findByHouseAndUser(house, user);
		if(favorite != null) {
			favoriteRepository.deleteAll(favorite);
		}
	}
}
