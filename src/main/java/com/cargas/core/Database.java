package com.cargas.core;

import java.util.List;

public interface Database {

    void init();

    String login(String username , String password);

    int REGISTER_SUCCESS = 0;
    int REGISTER_INVALID_USER = 1;
    int REGISTER_TAKEN_USER = 3;

    int REGISTER_INVALID_PASSWORD = 2;
    int REGISTER_INVALID_DATA = 4;

    int register(String username , String password , String name , String phone_number , String address , String email);

    int PROFILE_UPDATE_SUCCESS = 0;
    int PROFILE_UPDATE_FAILED = 1;
    int updateProfile(String token , String name , String phone_number , String address , String email);

    int LOGOUT_SUCCESS = 0;
    int LOGOUT_FAILED = 1;
    int logout(String token);

    String getInfo(String token);

    List<String> getShop(String token);


    int ORDER_SUCCESS = 0;
    int ORDER_FAILED = 1;


    int ORDER_STATE_WAITING = 0;
    int ORDER_STATE_PROCESSING = 1;
    int ORDER_STATE_DELIVERING = 2;
    int ORDER_STATE_FINISHED = 4;

    int ORDER_STATE_REJECTED = 5;
    int ORDER_STATE_REJECTED_OUT_OF_STOCK = 6;
    int ORDER_STATE_REJECTED_BALANCE = 7;


    class ShopRequest{
        public String itemCode;
        public int Quantity;
    }
    String order(String token , List<ShopRequest> items);

    List<String> getOrders(String token);

    int deleteOrder(String token , String order);
}
