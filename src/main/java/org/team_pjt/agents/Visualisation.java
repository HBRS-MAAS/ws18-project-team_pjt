package org.team_pjt.agents;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Visualisation extends Application {

    @Override
    public void start(Stage stage) {
        TreeItem<Alert> rootNode = new TreeItem<>(new Alert("dummy", "dummy"));
        rootNode.setExpanded(true);
        TreeItem<Alert> groupNode1 = new TreeItem<>(new Alert("Finished Orders", "Finished Orders"));
        TreeItem<Alert> groupNode2 = new TreeItem<>(new Alert("Current Orders", "Current Orders"));
        TreeItem<Alert> groupNode3 = new TreeItem<>(new Alert("Upcoming Orders", "Upcoming Orders"));

        groupNode1.getChildren().addAll(new TreeItem<>(new Alert("Order 1", "Order 1")),
                new TreeItem<>(new Alert("Order 2", "Order 2")),
                new TreeItem<>(new Alert("Order 3", "Order 3")));
        groupNode2.getChildren().addAll(new TreeItem<>(new Alert("Order 1", "Order 1")),
                new TreeItem<>(new Alert("Order 2", "Order 2")),
                new TreeItem<>(new Alert("Order 3", "Order 3")));
        groupNode3.getChildren().addAll(new TreeItem<>(new Alert("Order 1", "Order 1")),
                new TreeItem<>(new Alert("Order 2", "Order 2")),
                new TreeItem<>(new Alert("Order 3", "Order 3")));

        rootNode.getChildren().addAll(  groupNode1,  groupNode2,  groupNode3);

        VBox box = new VBox();
        final Scene scene = new Scene(box, 400, 300);
        scene.setFill(Color.LIGHTGRAY);

        TreeView<Alert> treeView = new TreeView<>(rootNode);
        treeView.setShowRoot(false);
        treeView.setCellFactory(new Callback<TreeView<Alert>, TreeCell<Alert>>() {
            @Override
            public TreeCell<Alert> call(TreeView<Alert> p) {
                return new AlertTreeCell();
            }
        });

        box.getChildren().add(treeView);
        stage.setScene(scene);
        stage.show();
    }

    private final class AlertTreeCell extends TreeCell<Alert> {

        private final AnchorPane anchorPane;
        private final Label label;
       // private final Button button;

        public AlertTreeCell() {
            anchorPane = new AnchorPane();
            label = new Label();
            //button = new Button();
            //anchorPane.getChildren().addAll(label, button);
            anchorPane.getChildren().addAll(label);
            anchorPane.setStyle("-fx-border-color: gray");
            anchorPane.setPadding(new Insets(5));
            //AnchorPane.setRightAnchor(button, 10.0);
            AnchorPane.setLeftAnchor(label, 15.0);
        }

        @Override
        public void updateItem(Alert item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(null);
                label.setText(item.getStatus());
                //button.setText(item.getName());
                setGraphic(anchorPane);
            }
        }
    }

    public static class Alert {

        private final SimpleStringProperty name;
        private final SimpleStringProperty status;

        private Alert(String name, String department) {
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(department);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String fName) {
            name.set(fName);
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String fName) {
            status.set(fName);
        }
    }

    public static void main(String[] args) {Application.launch(args); }
    //public static void showWindow( ) { launch(new String[] {}); }

}
