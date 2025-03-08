package com.example.samuraitravel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	//特定のユーザーのお気に入りを取得
	public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	
	public List<Favorite> findByHouseAndUser(House house, User user);

	boolean existsByUserIdAndHouseId(Integer userId, Integer houseId);
}
