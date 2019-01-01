package org.team_pjt.messages;

import java.util.Vector;

public abstract class GenericGuidMessage {

    private Vector<String> guids;
    private String productType;

    public GenericGuidMessage(Vector<String> guids, String productType) {
        this.guids = guids;
        this.productType = productType;
    }

    public Vector<String> getGuids() {
        return guids;
    }

    public void setGuids(Vector<String> guids) {
        this.guids = guids;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Override
    public String toString() {
        return "DoughMessage [guids=" + guids + ", productType=" + productType + "]";
    }
}
