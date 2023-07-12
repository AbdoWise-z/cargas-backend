package com.cargas.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.client.*;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

public class MongoBase implements Database {

    public MongoClient client;
    public MongoDatabase base;

    private MongoCollection<Document> LoginsDB;
    private MongoCollection<Document> ActiveLoginsDB;
    private MongoCollection<Document> AccountsDB;
    private MongoCollection<Document> ShopDB;
    private MongoCollection<Document> PendingRequestsDB;
    private MongoCollection<Document> FinishedRequestsDB;

    @Override
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.WARN);

        client = MongoClients.create(Constants.CONNECTION_STR);

        base = client.getDatabase(Constants.BASE_NAME);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.close(); //close the connection on shutdown
        }));

        //prepare records
        LoginsDB           = base.getCollection(Constants.MONGO_LOGIN_DB);
        ActiveLoginsDB     = base.getCollection(Constants.MONGO_ACTIVE_LOGINS_DB);
        AccountsDB         = base.getCollection(Constants.MONGO_ACCOUNTS_DB);
        ShopDB             = base.getCollection(Constants.MONGO_SHOP_DB);
        PendingRequestsDB  = base.getCollection(Constants.MONGO_PENDING_REQUESTS_DB);
        FinishedRequestsDB = base.getCollection(Constants.MONGO_FINISHED_REQUESTS_DB);

    }


    //thx cool guy on stackoverflow :)
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    private static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private static String generateNewToken(int size) {
        byte[] randomBytes = new byte[size];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @Override
    public String login(String name, String password) {

        FindIterable<Document> it = LoginsDB.find(new Document("username", name)
                .append("password", password));

        if (it.first() == null){
            return null;
        }

        FindIterable<Document> at = ActiveLoginsDB.find(new Document("username", name));
        if (at.first() != null){
            return Objects.requireNonNull(at.first()).getString("token");
        }

        String tok = generateNewToken();
        while (ActiveLoginsDB.find(new Document("token", tok)).first() != null){
            tok = generateNewToken();
        }

        ActiveLoginsDB.insertOne(new Document("username" , name).append("token" , tok));
        return tok;
    }

    @Override
    public int register(String username, String password, String name, String phone_number, String address , String email) {
        username      = username.trim();
        password      = password.trim();
        name          = name.trim();
        phone_number  = phone_number.trim();
        address       = address.trim();

        if (username.length() < 8){
            return REGISTER_INVALID_USER;
        }

        if (password.length() < 8){
            return REGISTER_INVALID_PASSWORD;
        }

        if (name.isEmpty() || phone_number.isEmpty() || address.isEmpty() || email.isEmpty()){
            return REGISTER_INVALID_DATA;
        }

        if (LoginsDB.find(new Document("username" , username)).first() != null){
            return REGISTER_TAKEN_USER;
        }

        LoginsDB.insertOne(new Document("username" , username).append("password" , password));
        AccountsDB.insertOne(new Document()
                .append("username" , username)
                .append("name" , name)
                .append("phone" , phone_number)
                .append("address" , address)
                .append("email" , email)
                .append("type" , 0)
                .append("balance" , 0.0)
        );

        return REGISTER_SUCCESS;
    }

    @Override
    public int updateProfile(String token, String name, String phone_number, String address, String email) {
        Document trans = ActiveLoginsDB.find(new Document("token" , token)).first();
        if (trans == null)
            return PROFILE_UPDATE_FAILED;
        String user = trans.getString("username");
        AccountsDB.updateOne(new Document("username" , user) ,
                new Document()
                        .append("name" , name)
                        .append("phone" , phone_number)
                        .append("address" , address));
        return PROFILE_UPDATE_SUCCESS;
    }

    @Override
    public int logout(String token) {
        return ActiveLoginsDB.findOneAndDelete(new Document("token" , token)) != null ? LOGOUT_SUCCESS : LOGOUT_FAILED;
    }

    @Override
    public String getInfo(String token) {
        Document trans = ActiveLoginsDB.find(new Document("token" , token)).first();
        if (trans == null)
            return null;
        Document doc = AccountsDB.find(new Document("username" , trans.getString("username"))).first();
        if (doc == null){
            throw new RuntimeException("Somehow I have active login with invalid username : " + token);
        }

        doc.remove("_id");

        return doc.toJson();
    }

    @Override
    public List<String> getShop(String token) {
        FindIterable<Document> docs =  ShopDB.find();
        List<String> items = new LinkedList<>();

        for (Document d : docs){
            d.remove("_id");
            items.add(d.toJson());
        }

        return items;
    }

    @Override
    public String order(String token, List<ShopRequest> items) {
        Document trans = ActiveLoginsDB.find(new Document("token" , token)).first();
        if (trans == null)
            return null;

        //validate the order first & prepare the docs
        List<Document> docs = new LinkedList<>();
        for (ShopRequest req : items) {
            if (req.Quantity <= 0 || ShopDB.find(new Document("itemCode", req.itemCode)).first() == null) {
                return null; //make sure that all the items exist in the shop first
            }

            docs.add(new Document("itemCode" , req.itemCode).append("Quantity" , req.Quantity));
        }

        String code = generateNewToken(6);
        String userName = trans.getString("username");

        while (PendingRequestsDB.find(new Document("requestCode" , code).append("username" , userName)).first() != null){
            code = generateNewToken(6); //get a unique code-username
                                             //this is NOT the best way to do
                                             //cuz Mongo itself has a way in
                                             //doing it , but yeah its just
                                             //an example
                                             //fixme: unique code
        }

        PendingRequestsDB.insertOne(new Document("username" , userName)
                .append("code" , code)
                .append("items" , docs)
                .append("statues" , ORDER_STATE_WAITING)
                .append("processor" , "none")
                .append("time" , LocalDateTime.now().toString())
        );

        return code;
    }

    @Override
    public List<String> getOrders(String token) {
        Document trans = ActiveLoginsDB.find(new Document("token" , token)).first();
        if (trans == null)
            return null;
        String userName = trans.getString("username");

        FindIterable<Document> pending = PendingRequestsDB.find(new Document("username" , userName));
        FindIterable<Document> finished = FinishedRequestsDB.find(new Document("username" , userName));

        List<String> ret = new LinkedList<>();

        for (Document d : pending){
            d.remove("processor");
            d.remove("username");
            d.remove("_id");
            ret.add(d.toJson());
        }

        for (Document d : finished){
            d.remove("processor");
            d.remove("username");
            d.remove("_id");
            ret.add(d.toJson());
        }

        return ret;
    }

    @Override
    public int deleteOrder(String token, String order) {
        Document trans = ActiveLoginsDB.find(new Document("token" , token)).first();
        if (trans == null)
            return ORDER_FAILED;
        String userName = trans.getString("username");

        Document doc = PendingRequestsDB.findOneAndDelete(new Document("requestCode" , order).append("username" , userName));

        return doc == null ? ORDER_FAILED : ORDER_SUCCESS;
    }
}
