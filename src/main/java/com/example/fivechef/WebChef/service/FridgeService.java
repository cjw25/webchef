package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.FridgeCourseResponse;
import com.example.fivechef.WebChef.dto.FridgeSearchResponse;
import com.example.fivechef.WebChef.dto.RecipeResponse;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Recipe;
import com.example.fivechef.WebChef.repository.CourseRepository;
import com.example.fivechef.WebChef.repository.RecipeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FridgeService {

    private final RecipeRepository recipeRepository;
    private final CourseRepository courseRepository;

    public FridgeSearchResponse getEmptyResult() {
        return new FridgeSearchResponse(
                "",
                List.of(),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public FridgeSearchResponse search(String keyword) {
        String cleanKeyword = normalizeKeyword(keyword);

        if (cleanKeyword.isEmpty()) {
            return getEmptyResult();
        }

        List<String> keywords = splitKeywords(cleanKeyword);

        List<Recipe> recipes = recipeRepository.findAll(
                recipeAutoSearch(keywords),
                PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createDate"))
        ).getContent();

        List<Course> courses = courseRepository.findAll(
                courseAutoSearch(keywords),
                PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();

        List<RecipeResponse> recipeResponses = recipes.stream()
                .map(RecipeResponse::new)
                .toList();

        List<FridgeCourseResponse> courseResponses = courses.stream()
                .map(FridgeCourseResponse::new)
                .toList();

        return new FridgeSearchResponse(
                cleanKeyword,
                recipeResponses,
                courseResponses
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }

        return keyword.trim()
                .replaceAll("\\s+", " ");
    }

    private List<String> splitKeywords(String keyword) {
        return Arrays.stream(keyword.split("[,\\s]+"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }

    private Specification<Recipe> recipeAutoSearch(List<String> keywords) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (String keyword : keywords) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likeKeyword
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("category")),
                        likeKeyword
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("mainIngredient")),
                        likeKeyword
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("ingredients")),
                        likeKeyword
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likeKeyword
                ));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Course> courseAutoSearch(List<String> keywords) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String keyword : keywords) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";

                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likeKeyword
                ));

                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likeKeyword
                ));
            }

            Predicate approvedStatus = criteriaBuilder.equal(
                    root.get("status"),
                    CourseStatus.APPROVED
            );

            Predicate keywordSearch = criteriaBuilder.or(
                    keywordPredicates.toArray(new Predicate[0])
            );

            return criteriaBuilder.and(approvedStatus, keywordSearch);
        };
    }
}