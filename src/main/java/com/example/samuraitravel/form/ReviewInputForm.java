package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ReviewInputForm {
	@NotNull(message = "評価を選択してください。")
	private Integer score;
	
	private String comment;
	
	@NotNull
	private Integer houseId;
}