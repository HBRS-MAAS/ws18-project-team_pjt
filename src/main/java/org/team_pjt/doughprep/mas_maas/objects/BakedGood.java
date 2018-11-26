package org.team_pjt.doughprep.mas_maas.objects;


// TODO this is just a shell for testing json
public class BakedGood
{
    private String name;
    private int amount;
    // TODO this will have to change when the real BakedGood class is created
    // e.g. this is a temp hack
    public static final String bakedGoodNames[] =
        {
                "Bagel",
                "Donut",
                "Berliner",
                "Muffin",
                "Bread"
        };

    public BakedGood() {}

    public BakedGood(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "BakedGood [name=" + name + ", amount=" + amount + "]";
    }
}
