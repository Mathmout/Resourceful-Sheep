package com.mathmout.resourcefulsheep.config.mutations;

public record SheepMutation(String MomId, String DadId, String Child, int Chance) {
    // Chance is between 0% and 100%
}