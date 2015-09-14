package com.example.medionchou.tobacco.DataContainer;

/**
 * Created by Medion on 2015/9/3.
 */
public class Recipe {

    private String bucket;
    private String recipeId;
    private String recipeName;


    public Recipe(String bucket, String recipeId, String recipeName) {
        this.bucket = bucket;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
    }

    public String getBucketNum() {
        return bucket;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String toString() {
        return bucket + " " + recipeId + " " + recipeName;
    }

    public boolean isBucketMatch(Recipe recipe) {
        return bucket.equals(recipe.getBucketNum());
    }

}
