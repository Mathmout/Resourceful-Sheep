package com.mathmout.resourcefulsheep.entity.custom;

public class SheepMutation {
    public String MomId;
    public String DadId;
    public String Child;
    public int Chance; // Chance is between 0% and 100%

    public SheepMutation(String mom_Id, String dad_Id, String child, int chance) {
        MomId = mom_Id;
        DadId = dad_Id;
        Child = child;
        Chance = chance;
    }
}
