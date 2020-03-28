package com.ammar.myapplication;

import java.util.List;

public class Coordinates {
    private List<Global_plotted_coordinates> global_plotted_coordinates;
    private User_plotted_data user_plotted_data;

    public Coordinates(List<Global_plotted_coordinates> global_plotted_coordinates, User_plotted_data user_plotted_data) {
        this.global_plotted_coordinates = global_plotted_coordinates;
        this.user_plotted_data = user_plotted_data;
    }

    public List<Global_plotted_coordinates> getGlobal_plotted_coordinates() {
        return global_plotted_coordinates;
    }

    public void setGlobal_plotted_coordinates(List<Global_plotted_coordinates> global_plotted_coordinates) {
        this.global_plotted_coordinates = global_plotted_coordinates;
    }

    public User_plotted_data getUser_plotted_data() {
        return user_plotted_data;
    }

    public void setUser_plotted_data(User_plotted_data user_plotted_data) {
        this.user_plotted_data = user_plotted_data;
    }
}
