package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.form.HouseEditForm;
import com.example.samuraitravel.form.HouseRegisterForm;
import com.example.samuraitravel.service.HouseService;

@Controller
@RequestMapping("/admin/houses") //ルートパスの基準値の設定　各メソッドに共通のパスを繰り返し記述する必要がなくなる
public class AdminHouseController {
	private final HouseService houseService;
	
	public AdminHouseController(HouseService houseService) {
		this.houseService = houseService;
	}
	
	// 民宿一覧を表示するメソッド
	@GetMapping	
	public String index(@RequestParam(name = "keyword", required = false) String keyword,  // 検索キーワード（任意）
					    @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, // ページネーション設定
					    Model model) {
		Page<House> housePage;
		
		// キーワードが存在する場合は検索結果を、存在しない場合は全件を取得
		if (keyword != null && !keyword.isEmpty()) {
			housePage = houseService.findHousesByNameLike(keyword, pageable);
		} else {
			housePage = houseService.findAllHouses(pageable);
		}
		model.addAttribute("housePage", housePage);
		model.addAttribute("keyword", keyword);
		
		return "admin/houses/index";
	}
	
	// 民宿詳細を表示するメソッド
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,  // URLから民宿IDを取得
					    RedirectAttributes redirectAttributes,    // リダイレクト時のフラッシュメッセージ用
					    Model model) {
		Optional<House> optionalHouse = houseService.findHouseById(id);
		
		// 指定されたIDの民宿が存在しない場合
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/admin/houses";
		}
		
		House house = optionalHouse.get();
		model.addAttribute("house", house);
		
		return "admin/houses/show";
	}
	
	// 民宿登録フォームを表示するメソッド
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("houseRegisterForm", new HouseRegisterForm());
		
		return "admin/houses/register";
	}
	
	//民宿を登録するメソッド
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated HouseRegisterForm houseRegisterForm, // フォームから送信されたデータを受け取る
						 BindingResult bindingResult, // バリデーション結果を格納するオブジェクト
						 RedirectAttributes redirectAttributes, // リダイレクト時にメッセージを渡すためのオブジェクト
						 Model model) // ビューにデータを渡すためのオブジェクト
	{
		if(bindingResult.hasErrors()) { // バリデーションエラーがある場合
			model.addAttribute("houseRegisterForm", houseRegisterForm); // エラーメッセージと共にフォームを再表示
			return "admin/houses/register"; // 登録ページに戻る
		}
		
		houseService.createHouse(houseRegisterForm); // 民宿を登録するサービスメソッドを呼び出す
		redirectAttributes.addFlashAttribute("successMessage","民宿を登録しました。"); // 成功メッセージをリダイレクト先に渡す
		
		return "redirect:/admin/houses"; // 民宿一覧ページにリダイレクト
	}
	
	//指定されたIDの民宿の編集ページを表示するメソッド
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,  // URLから民宿IDを取得
					    RedirectAttributes redirectAttributes,    // リダイレクト時のフラッシュメッセージ用
					    Model model) { // ビューにデータを渡すためのオブジェクト
		Optional<House> optionalHouse = houseService.findHouseById(id); // IDで民宿を検索
		
		if(optionalHouse.isEmpty()) { // 指定されたIDの民宿が存在しない場合
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。"); // エラーメッセージを設定
			return "redirect:/admin/houses"; // 民宿一覧ページにリダイレクト
		}
		
		House house = optionalHouse.get(); // 民宿情報を取得
		HouseEditForm houseEditForm = new HouseEditForm(house.getName(), null, house.getDescription(), house.getPrice(),
														  house.getCapacity(),house.getPostalCode(), house.getAddress(), house.getPhoneNumber()); // 編集フォームを作成
		
		model.addAttribute("house", house); // モデルに民宿情報を追加
		model.addAttribute("houseEditForm", houseEditForm); // モデルに編集フォームを追加
		
		return "admin/houses/edit"; // 編集ページを表示
	}	
	
	//民宿詳細を更新するメソッド
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated HouseEditForm houseEditForm, // フォームから送信されたデータを受け取る
						 BindingResult bindingResult, // バリデーション結果を格納するオブジェクト
						 @PathVariable(name = "id") Integer id, // URLから民宿IDを取得
						 RedirectAttributes redirectAttributes, // リダイレクト時にメッセージを渡すためのオブジェクト
						 Model model) // ビューにデータを渡すためのオブジェクト
	{
		Optional<House> optionalHouse = houseService.findHouseById(id); // IDで民宿を検索
		
		if(optionalHouse.isEmpty()) { // 指定されたIDの民宿が存在しない場合
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。"); // エラーメッセージを設定
			
			return "redirect:/admin/houses"; // 民宿一覧ページにリダイレクト
		}
		
		House house = optionalHouse.get(); // 民宿情報を取得
		
		if(bindingResult.hasErrors()) { // バリデーションエラーがある場合
			model.addAttribute("house", house); // モデルに民宿情報を追加
			model.addAttribute("houseEditForm", houseEditForm); // モデルに編集フォームを追加
			return "admin/houses/edit"; // 編集ページに戻る
		}
		
		houseService.updateHouse(houseEditForm, house); // 民宿情報を更新するサービスメソッドを呼び出す
		redirectAttributes.addFlashAttribute("successMessage","民宿情報を編集しました。"); // 成功メッセージをリダイレクト先に渡す
		
		return "redirect:/admin/houses"; // 民宿一覧ページにリダイレクト
	}
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<House> optionalHouse = houseService.findHouseById(id);
		
		if(optionalHouse.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "民宿が存在しません。");
			
			return "redirect:/admin/houses"; 
		}
		
		House house = optionalHouse.get();
		houseService.deleteHouse(house);
		redirectAttributes.addFlashAttribute("successMessage", "民宿を削除しました。");
		
		return "redirect:/admin/houses";
	}
}