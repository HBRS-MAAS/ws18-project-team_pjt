package org.team_pjt.doughprep.mas_maas.objects;

public class Product {
    private String guid;
    private Batch batch;
    private Recipe recipe;
    private Packaging packaging;
    private Double salesPrice;
    private Double productionCost;

    public Product(String guid, Batch batch, Recipe recipe, Packaging packaging, Double salesPrice,
            Double productionCost) {
        this.guid = guid;
        this.batch = batch;
        this.recipe = recipe;
        this.packaging = packaging;
        this.salesPrice = salesPrice;
        this.productionCost = productionCost;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }

    public Double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(Double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public Double getProductionCost() {
        return productionCost;
    }

    public void setProductionCost(Double productionCost) {
        this.productionCost = productionCost;
    }

    @Override
    public String toString() {
        return "Product [guid=" + guid + ", batch=" + batch + ", recipe=" + recipe.toString()+ ", packaging=" + packaging
                + ", salesPrice=" + salesPrice + ", productionCost=" + productionCost + "]";
    }

}
