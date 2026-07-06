package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Recipe;
import lombok.Getter;

@Getter
public class RecipeResponse {

    private Long id;

    private String title;

    private String category;

    private String mainIngredient;

    private String difficulty;

    private Integer cookingTime;

    private String description;

    private String ingredients;

    private String thumbnailUrl;

    public RecipeResponse(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.category = recipe.getCategory();
        this.mainIngredient = recipe.getMainIngredient();
        this.difficulty = recipe.getDifficulty();
        this.cookingTime = recipe.getCookingTime();
        this.description = recipe.getDescription();
        this.ingredients = recipe.getIngredients();
        this.thumbnailUrl = recipe.getThumbnailUrl();
    }
}