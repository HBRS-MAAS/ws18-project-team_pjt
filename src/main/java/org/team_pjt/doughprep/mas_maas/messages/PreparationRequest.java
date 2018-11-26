package org.team_pjt.doughprep.mas_maas.messages;

import java.util.Vector;

import org.team_pjt.doughprep.mas_maas.objects.Step;

public class PreparationRequest extends GenericGuidMessage {
    private Vector<Integer> productQuantities;
    private Vector<Step> steps;

    public PreparationRequest(Vector<String> guids, String productType, Vector<Integer> productQuantities,
            Vector<Step> steps) {
        super(guids, productType);
        this.productQuantities = productQuantities;
        this.steps = steps;
    }

    public Vector<Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Vector<Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public Vector<Step> getSteps() {
        return steps;
    }

    public void setSteps(Vector<Step> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "PreparationRequest [productQuantities=" + productQuantities + ", steps=" + steps + "]";
    }
}
