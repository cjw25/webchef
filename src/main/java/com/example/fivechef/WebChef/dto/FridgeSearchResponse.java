package com.example.fivechef.WebChef.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FridgeSearchResponse {

    private String keyword;

    private List<RecipeResponse> recipes;

    private List<FridgeCourseResponse> courses;

    public boolean hasResult() {
        boolean hasRecipes = recipes != null && !recipes.isEmpty();
        boolean hasCourses = courses != null && !courses.isEmpty();

        return hasRecipes || hasCourses;
    }
}