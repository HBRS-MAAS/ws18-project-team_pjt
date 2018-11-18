package org.team_pjt.agents;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.control.Label;


public class Visualisation extends Application {
/*
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void showWindow( ) { launch(new String[] {}); }

*/


    @Override
    public void start(Stage stage) {
        TreeItem<Alert> rootNode = new TreeItem<>(new Alert("dummy", "dummy"));
        rootNode.setExpanded(true);
        TreeItem<Alert> groupNode = new TreeItem<>(new Alert("group item", "group item"));
        groupNode.getChildren().addAll(new TreeItem<>(new Alert("sub item 1", "sub item 1")),
                new TreeItem<>(new Alert("sub item 2", "sub item 2")),
                new TreeItem<>(new Alert("sub item 3", "sub item 3")));

        rootNode.getChildren().addAll(new TreeItem<>(new Alert("item 1", "item 1")),
                groupNode,
                new TreeItem<>(new Alert("item 2", "item 2")),
                new TreeItem<>(new Alert("item 3", "item 3")));

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
        private final Button button;

        public AlertTreeCell() {
            anchorPane = new AnchorPane();
            label = new Label();
            button = new Button();
            anchorPane.getChildren().addAll(label, button);
            anchorPane.setStyle("-fx-border-color: gray");
            anchorPane.setPadding(new Insets(5));
            AnchorPane.setRightAnchor(button, 10.0);
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
                button.setText(item.getName());
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

    //public static void main(String[] args) {
    // Application.launch(args);
    //}
    public static void showWindow( ) { launch(new String[] {}); }

}
