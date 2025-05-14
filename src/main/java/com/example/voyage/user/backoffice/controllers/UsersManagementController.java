package com.example.voyage.user.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UsersManagementController {
    @FXML
    private TableView<UserData> usersTable;

    @FXML
    private TableColumn<UserData, Integer> idColumn;

    @FXML
    private TableColumn<UserData, String> usernameColumn;

    @FXML
    private TableColumn<UserData, String> emailColumn;

    @FXML
    private TableColumn<UserData, String> fullNameColumn;

    @FXML
    private TableColumn<UserData, String> phoneColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        configureTableColumns();
        loadUserData();

        // Disable edit and delete buttons until a row is selected
        editUserButton.setDisable(true);
        deleteUserButton.setDisable(true);

        // Add listener for row selection
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editUserButton.setDisable(!hasSelection);
            deleteUserButton.setDisable(!hasSelection);
        });

        // Add listener to search field
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterUserData(newText);
        });
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
    }

    private ObservableList<UserData> masterData = FXCollections.observableArrayList();

    private void loadUserData() {
        // In a real app, this would fetch data from a service/database
        // For now, we'll use dummy data
        masterData.add(new UserData(1, "user1", "user1@example.com", "John Doe", "555-1234"));
        masterData.add(new UserData(2, "user2", "user2@example.com", "Jane Smith", "555-5678"));
        masterData.add(new UserData(3, "user3", "user3@example.com", "Robert Johnson", "555-9012"));
        masterData.add(new UserData(4, "user4", "user4@example.com", "Emily Davis", "555-3456"));

        usersTable.setItems(masterData);
    }

    private void filterUserData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            usersTable.setItems(masterData);
            return;
        }

        searchText = searchText.toLowerCase();

        ObservableList<UserData> filteredData = FXCollections.observableArrayList();
        for (UserData user : masterData) {
            if (user.getUsername().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    user.getFullName().toLowerCase().contains(searchText)) {
                filteredData.add(user);
            }
        }

        usersTable.setItems(filteredData);
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        // Open dialog to add a new user
        showUserForm(null);
    }

    @FXML
    private void handleEditUser(ActionEvent event) {
        UserData selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserForm(selectedUser);
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        UserData selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete User");
            confirmDialog.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Delete the user
                    masterData.remove(selectedUser);
                    usersTable.setItems(masterData);
                }
            });
        }
    }

    private void showUserForm(UserData user) {
        // In a real app, this would open a dialog to add/edit a user
        // For this example, we'll just print to console
        if (user == null) {
            System.out.println("Adding new user");
        } else {
            System.out.println("Editing user: " + user.getUsername());
        }
    }

    // Inner class for user data
    public static class UserData {
        private final int id;
        private final String username;
        private final String email;
        private final String fullName;
        private final String phone;

        public UserData(int id, String username, String email, String fullName, String phone) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPhone() {
            return phone;
        }
    }
}
