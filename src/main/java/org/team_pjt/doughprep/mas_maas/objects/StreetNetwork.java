package org.team_pjt.doughprep.mas_maas.objects;

import java.util.Vector;

public class StreetNetwork {
    private boolean directed;
    private Vector<StreetNode> nodes;
    private Vector<StreetLink> links;

    public StreetNetwork(boolean directed, Vector<StreetNode> nodes, Vector<StreetLink> links) {
        this.directed = directed;
        this.nodes = nodes;
        this.links = links;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public Vector<StreetNode> getNodes() {
        return nodes;
    }

    public void setNodes(Vector<StreetNode> nodes) {
        this.nodes = nodes;
    }

    public Vector<StreetLink> getLinks() {
        return links;
    }

    public void setLinks(Vector<StreetLink> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "StreetNetwork [directed=" + directed + ", nodes=" + nodes + ", links=" + links + "]";
    }
}
