package com.example.samuraitravel.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.UserEditForm;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}

	// ユーザーのホームページを表示するメソッド
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		
		model.addAttribute("user", user);
		
		return "user/index";
	}
	
	// ユーザー情報編集ページを表示するメソッド
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		// 現在のユーザー情報を取得
		User user = userDetailsImpl.getUser();
		// ユーザー情報をフォームに設定
		UserEditForm userEditForm = new UserEditForm(user.getName(), user.getFurigana(), user.getPostalCode(), user.getAddress(), user.getPhoneNumber(), user.getEmail());
		
		// モデルにフォームを追加
		model.addAttribute("userEditForm", userEditForm);
		
		return "user/edit";
	}
	
	// ユーザー情報を更新するメソッド
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm,
						 BindingResult bindingResult,
						 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						 RedirectAttributes redirectAttributes,
						 Model model)
	{
		// 現在のユーザー情報を取得
		User user = userDetailsImpl.getUser();
		
		//メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if(userService.isEmailChanged(userEditForm, user) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		// バリデーションエラーがある場合、編集ページに戻る
		if(bindingResult.hasErrors()) {
			model.addAttribute("userEditForm", userEditForm);
			
			return "user/edit";
		}
		
		// ユーザー情報を更新
		userService.updateUser(userEditForm, user);
		redirectAttributes.addFlashAttribute("successMessage", " 会員情報を編集しました。");
		
		return "redirect:/user";
	}
}