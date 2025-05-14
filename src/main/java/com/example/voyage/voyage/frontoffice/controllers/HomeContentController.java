package com.example.voyage.voyage.frontoffice.controllers;

import com.example.voyage.user.models.User;
import com.example.voyage.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class HomeContentController {

    @FXML
    private Button exploreButton;

    @FXML
    private void handleExploreVoyages(ActionEvent event) {
        try {
            // Get parent node and find the dashboard controller
            Node sourceNode = (Node) event.getSource();
            if (sourceNode.getScene() == null || sourceNode.getScene().getRoot() == null) {
                System.err.println("Scene or root is null");
                return;
            }

            // Try to find the StackPane that contains our content
            Node parent = sourceNode.getScene().getRoot();
            while (parent != null) {
                if (parent.getUserData() instanceof ClientDashboardController) {
                    ClientDashboardController controller = (ClientDashboardController) parent.getUserData();
                    controller.handleExploreButton(null);
                    return;
                }
                if (parent.getParent() == null)
                    break;
                parent = parent.getParent();
            }

            // If we get here, we couldn't find the controller through the scene graph
            // Let's try the contentArea's userData
            if (sourceNode.getScene().lookup("#contentArea") != null) {
                Node contentArea = sourceNode.getScene().lookup("#contentArea");
                if (contentArea.getUserData() instanceof ClientDashboardController) {
                    ClientDashboardController controller = (ClientDashboardController) contentArea.getUserData();
                    controller.handleExploreButton(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error navigating to explore page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
