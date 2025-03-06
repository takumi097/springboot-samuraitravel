package com.example.samuraitravel.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.entity.VerificationToken;
import com.example.samuraitravel.event.SignupEventPublisher;
import com.example.samuraitravel.form.SignupForm;
import com.example.samuraitravel.service.UserService;
import com.example.samuraitravel.service.VerificationTokenService;

@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	
	public AuthController(UserService userService, SignupEventPublisher signupEventPublisher, VerificationTokenService verificationTokenService) {
		this.userService = userService;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService = verificationTokenService;
	}
	
	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
	
	/**
	 * 会員登録処理を実行するメソッド
	 * POSTリクエスト: /signup
	 * 
	 * @param signupForm 会員登録フォームのデータ（@Validatedで入力値の検証を実施）
	 * @param bindingResult バリデーション結果を保持するオブジェクト
	 * @param redirectAttributes リダイレクト時にフラッシュメッセージを渡すためのオブジェクト
	 * @param httpServletRequest HTTPリクエスト情報を取得するためのオブジェクト
	 * @param model ビューに渡すデータを格納するオブジェクト
	 */
	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm,
						BindingResult bindingResult, 						//バリデーション結果の格納　BindingResultのインターフェースが提供するhasErrors()でエラーの存在チェック
						RedirectAttributes redirectAttributes,				//リダイレクト内容の格納
						HttpServletRequest httpServletRequest,
						Model model)
	{
		//メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if(userService.isEmailRegistered(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}
	
		//パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
		if(!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
			bindingResult.addError(fieldError);
		}
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("signupForm", signupForm);
			
			return "auth/signup";
		}
	
		// 入力されたフォーム情報をもとに、新しいユーザーを作成
		User createdUser = userService.createUser(signupForm);
		
		// 現在のリクエストURLを取得（メール認証用のベースURLとして使用）
		String requestUrl = new String(httpServletRequest.getRequestURI());
		
		// メール認証イベントを発行
		// このイベントにより、ユーザーに認証メールが送信される
		signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
		redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。 メールに記載されているリンクをクリックし、会員登録を完了してください。");
		
		return "redirect:/";
	}
	
	/**
	 * メール認証を処理するメソッド
	 * GETリクエスト: /signup/verify
	 * 
	 * @param token メール認証用のトークン（URLパラメータから取得）
	 * @param model ビューに渡すデータを格納するオブジェクト
	 */
	@GetMapping("/signup/verify")
	public String verify(@RequestParam(name = "token") String token, Model model) {
		VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
		
		// メール認証用トークンが有効な場合の処理
		if(verificationToken != null) {
			// トークンに関連付けられたユーザー情報を取得
			User user = verificationToken.getUser();
			
			// ユーザーのアカウントを有効化
			// この処理により、ユーザーはログインが可能になる
			userService.enableUser(user);
			
			// 認証完了メッセージを設定
			// このメッセージは認証完了画面に表示される
			String successMessage = "会員登録が完了しました。";
			model.addAttribute("successMessage", successMessage);
		}
		
		return "auth/verify";
	}
}