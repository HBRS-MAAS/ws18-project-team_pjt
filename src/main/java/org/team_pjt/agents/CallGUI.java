package org.team_pjt.agents;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CallGUI extends Application {
    String order2 = "sadfger";
    public CallGUI(String order){
        this.order2 = order;

    }

        public static void showWindow(){

            System.out.println("Called CallGUI - now returning order");
            //System.out.println(order2);
            //String order2 = order.toString();
            //System.out.println(order2);
            launch(new String[] {});
        }

        @Override
        public void start(Stage primaryStage) {

            primaryStage.setTitle("JavaFX GUI");

            // Dows not work !! CustomerAgent.GetCurrentOrder.getGlobalOrder();
            Button mainButton= new Button("Click to show current orders");
            Button closeButton= new Button("Click to close window");
            mainButton.setOnAction(e -> Visualisation2.display(order2));
            closeButton.setOnAction(e -> primaryStage.close());
            Pane layout= new Pane();

            mainButton.setLayoutX(50);
            mainButton.setLayoutY(70);
            closeButton.setLayoutX(70);
            closeButton.setLayoutY(140);

            layout.getChildren().add(mainButton);
            layout.getChildren().add(closeButton);

            Scene scene1= new Scene(layout, 300, 250);
            primaryStage.setScene(scene1);

            primaryStage.show();
            primaryStage.close();
        }

        // public static void main(String[] args) {         launch(args);       }

    }



