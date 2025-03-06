package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewEditForm {
	@NotNull(message = "評価を選択してください。")
	private Integer score;
	
	private String comment;
}