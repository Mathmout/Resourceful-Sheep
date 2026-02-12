package com.mathmout.resourcefulsheep.config.dnacrossbreeding;

import java.util.List;

public record SheepCrossbreeding(String MomId, String DadId, String ChildId, List<String> ResultsIfFail, int Chance) {
}
