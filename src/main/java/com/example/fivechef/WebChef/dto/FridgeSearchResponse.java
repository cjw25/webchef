package com.example.fivechef.WebChef.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FridgeSearchResponse {

    private final String keyword;

    private final List<RecipeResponse> recipes;

    private final List<FridgeCourseResponse> courses;

    public FridgeSearchResponse(String keyword,
                                List<RecipeResponse> recipes,
                                List<FridgeCourseResponse> courses) {
        this.keyword = keyword;
        this.recipes = recipes;
        this.courses = courses;
    }

    public boolean hasRecipeResult() {
        return recipes != null && !recipes.isEmpty();
    }

    public boolean hasCourseResult() {
        return courses != null && !courses.isEmpty();
    }

    public boolean hasResult() {
        return hasRecipeResult() || hasCourseResult();
    }
}