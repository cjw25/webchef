package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Recipe;
import lombok.Getter;

@Getter
public class RecipeResponse {

    private final Long id;

    private final String title;

    private final String category;

    private final String mainIngredient;

    private final String difficulty;

    private final Integer cookingTime;

    private final String description;

    private final String ingredients;

    private final String thumbnailUrl;

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