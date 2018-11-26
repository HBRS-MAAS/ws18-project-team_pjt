package org.team_pjt.doughprep.mas_maas.objects;

import java.awt.geom.Point2D;
import java.util.Vector;

public class Bakery {
        private String guid;
        private String name;
        private Point2D location;
        private Vector<Product> products;
        private Vector<Equipment> equipment;

        public Bakery() {}

        public Bakery(String guid, String name, Point2D location, Vector<Product> products,
                Vector<Equipment> equipment) {
            this.guid = guid;
            this.name = name;
            this.location = location;
            this.products = products;
            this.equipment = equipment;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Point2D getLocation() {
            return location;
        }

        public void setLocation(Point2D location) {
            this.location = location;
        }

        public Vector<Product> getProducts() {
            return products;
        }

        public void setProducts(Vector<Product> products) {
            this.products = products;
        }

        public Vector<Equipment> getEquipment() {
            return equipment;
        }

        public void setEquipment(Vector<Equipment> equipment) {
            this.equipment = equipment;
        }
        
        public Product findProduct(String productGuid) {
            Product matchedProduct = null;
            
            for(Product product : products) {
                if (product.getGuid().equals(productGuid)) {
                    matchedProduct = product;
                    break;
                }
            }
            
            return matchedProduct;
        }

        @Override
        public String toString() {
            return "Bakery [guid=" + guid + ", name=" + name + ", location=" + location + ", products=" + products
                    + ", equipment=" + equipment + "]";
        }
}