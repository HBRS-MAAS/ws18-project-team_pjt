package org.team_pjt.messages;

import java.util.Vector;

public class ProofingRequest extends GenericGuidMessage {
    private float proofingTime;
    private Vector<Integer> productQuantities;

    public ProofingRequest(String productType, Vector<String> guids, float proofingTime, Vector<Integer> productQuantities) {
        super(guids, productType);
        this.proofingTime = proofingTime;
        this.productQuantities = productQuantities;
    }

    public Float getProofingTime() {
        return proofingTime;
    }

    public void setProofingTime(Float proofingTime) {
        this.proofingTime = proofingTime;
    }

    public Vector<Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Vector<Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public void setProofingTime(float proofingTime) {
        this.proofingTime = proofingTime;
    }

    public ProofingRequest(Vector<String> guids, String productType, float proofingTime,
                           Vector<Integer> productQuantities) {
        super(guids, productType);
        this.proofingTime = proofingTime;
        this.productQuantities = productQuantities;
    }
}

