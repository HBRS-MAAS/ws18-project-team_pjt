package org.team_pjt.agents;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;


public class Visualisation2 extends BaseAgent{


    public static void display(String order)
    {
        Stage popupwindow=new Stage();

        popupwindow.initModality(Modality.APPLICATION_MODAL);
        System.out.println("0");

        popupwindow.setTitle("This is a pop up window");
        System.out.println("1");

        Label label1= new Label(order);
        System.out.println("2");

        Button button1= new Button("Close this pop up window");
        System.out.println("3");

        button1.setOnAction(e -> popupwindow.close());
        System.out.println("4");

        VBox layout= new VBox(10);
        System.out.println("5");

        layout.getChildren().addAll(label1, button1);

        layout.setAlignment(Pos.CENTER);

        Scene scene1= new Scene(layout, 300, 250);

        popupwindow.setScene(scene1);

        popupwindow.showAndWait();
        System.out.println("6");


    }


}
