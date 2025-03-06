package com.example.samuraitravel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.form.HouseEditForm;
import com.example.samuraitravel.form.HouseRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;

@Service
public class HouseService {
	private final HouseRepository houseRepository;
	
	public HouseService(HouseRepository houseRepository) {
		this.houseRepository = houseRepository;
	}
	
	//すべての民宿をページングされた状態で所得する
	//Pageableを使用することで、ページングされた状態でデータを取得することができる
	public Page<House> findAllHouses(Pageable pageable) {
		return houseRepository.findAll(pageable);
	}
	
	//指定されたキーワードを民宿名に含む民宿を、ページングされた状態で取得する
	public Page<House> findHousesByNameLike(String keyword, Pageable pageable) {
		return houseRepository.findByNameLike("%" + keyword + "%", pageable);
	}

	//指定したidを持つ民宿を取得する
	public Optional<House> findHouseById(Integer id) {
		return houseRepository.findById(id);
	}
	
	//民宿のレコード数を取得する
	public long countHouses() {
		return houseRepository.count();
	}
	
	//idが最も大きい民宿を取得する
	public House findFirstHouseByOrderByIdDesc() {
		return houseRepository.findFirstByOrderByIdDesc();
	}
	
	//指定されたキーワードを民宿名または住所に含む民宿を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByNameLikeOrAddressLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Pageable pageable) {
		return houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + nameKeyword + "%", "%" + addressKeyword + "%", pageable);
	}
	
	//指定されたキーワードを民宿名または住所に含む民宿を宿泊料金が安い順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByNameLikeOrAddressLikeOrderByPriceAsc(String nameKeyword, String addressKeyword, Pageable pageable) {
		return houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + nameKeyword + "%", "%" + addressKeyword + "%", pageable);
	}
	
	//指定されたキーワードを住所に含む民宿を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByAddressLikeOrderByCreatedAtDesc(String area, Pageable pageable) {
		return houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
	}
	
	//指定されたキーワードを住所に含む民宿を宿泊料金が安い順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByAddressLikeOrderByPriceAsc(String area, Pageable pageable) {
		return houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
	}
	
	//指定された宿泊料金以下の民宿を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable) {
		return houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
	}
	
	//指定された宿泊料金以下の民宿を宿泊料金が安い順に並べ替え、ページングされた状態で取得する
	public Page<House> findHousesByPriceLessThanEqualOrderByPriceAsc(Integer price, Pageable pageable) {
		return houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
	}
	
	//指定された宿泊料金以下の民宿を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<House> findAllHousesByOrderByCreatedAtDesc(Pageable pageable) {
		return houseRepository.findAllByOrderByCreatedAtDesc(pageable);
	}
		
	//指定された宿泊料金以下の民宿を宿泊料金が安い順に並べ替え、ページングされた状態で取得する
	public Page<House> findAllHousesByOrderByPriceAsc(Pageable pageable) {
		return houseRepository.findAllByOrderByPriceAsc(pageable);
	}
	
	//作成日時が新しい順に8件の民宿を取得する
	public List<House> findTop8HouseByOrderByCreatedAtDesc() {
		return houseRepository.findTop8ByOrderByCreatedAtDesc();
	}
	
	//予約数が多い順に3件の民宿を取得する
	public List<House> findTop3HouseByOrderByReservationCountDesc() {
		return houseRepository.findAllByOrderByReservationCountDesc(PageRequest.of(0, 3));
	}
	
	@Transactional  //民宿新規登録メソッド
	public void createHouse(HouseRegisterForm houseRegisterForm) {
		House house = new House(); // 新しいHouseオブジェクトを作成
		MultipartFile imageFile = houseRegisterForm.getImageFile(); // フォームから画像ファイルを取得
		
		if(!imageFile.isEmpty()) { // 画像ファイルが空でない場合
			String imageName = imageFile.getOriginalFilename(); // 画像の元のファイル名を取得
			String hashedImageName = generateNewFileName(imageName); // 新しいファイル名を生成
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName); // ファイルの保存先パスを設定
			copyImageFile(imageFile, filePath); // 画像ファイルを指定したパスにコピー
			house.setImageName(hashedImageName); // Houseオブジェクトに画像名を設定
		}
		
		house.setName(houseRegisterForm.getName());
		house.setDescription(houseRegisterForm.getDescription());
		house.setPrice(houseRegisterForm.getPrice());
		house.setCapacity(houseRegisterForm.getCapacity());
		house.setPostalCode(houseRegisterForm.getPostalCode());
		house.setAddress(houseRegisterForm.getAddress());
		house.setPhoneNumber(houseRegisterForm.getPhoneNumber());
		
		houseRepository.save(house);
	}
	
	@Transactional  //民宿更新メソッド
	public void updateHouse(HouseEditForm houseEditForm, House house) { //第2引数で更新対象のエンティティを受け取る
		MultipartFile imageFile = houseEditForm.getImageFile();
		
		if(!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			house.setImageName(hashedImageName);
		}
		
		house.setName(houseEditForm.getName());
		house.setDescription(houseEditForm.getDescription());
		house.setPrice(houseEditForm.getPrice());
		house.setCapacity(houseEditForm.getCapacity());
		house.setPostalCode(houseEditForm.getPostalCode());
		house.setAddress(houseEditForm.getAddress());
		house.setPhoneNumber(houseEditForm.getPhoneNumber());
		
		houseRepository.save(house);
	}
	
	@Transactional
	public void deleteHouse(House house) {
		houseRepository.delete(house);
	}
		
	// UUIDを使って生成したファイル名を返す
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\."); // ファイル名をドットで分割し、拡張子を含む配列を作成
		
		for(int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString(); // 拡張子以外の各部分にUUIDを設定
		}
		
		String hashedFileName = String.join(".", fileNames); // 分割した部分をドットで再結合し、新しいファイル名を生成
		
		return hashedFileName; // 生成した新しいファイル名を返す
	}
	
	//画像ファイルを指定したファイルにコピーする
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath); // 画像ファイルの入力ストリームを指定したパスにコピー
		} catch (IOException e) {
			e.printStackTrace(); // 入出力例外が発生した場合、スタックトレースを出力
		}
	}
}