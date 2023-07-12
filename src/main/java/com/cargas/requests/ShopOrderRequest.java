package com.cargas.requests;


import com.cargas.CargasBackendApplication;
import com.cargas.core.Database;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class ShopOrderRequest {
    @PostMapping(value = "/order")
    String order(@RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String token             = doc.getString("token");
            List<Document> it     = doc.getList("items" , Document.class);

            if (token == null || it == null){
                throw new Exception();
            }

            List<Database.ShopRequest> items = new LinkedList<>();
            for (Document d : it){
                Database.ShopRequest req = new Database.ShopRequest();
                req.itemCode = d.getString("itemCode");
                req.Quantity = d.getInteger("Quantity");
                items.add(req);
            }

            String res = CargasBackendApplication.getDatabase().order(
                    token, items
            );

            if (res == null) {
                return new Document(Map.of(
                        "result", Database.ORDER_FAILED
                        )).toJson();
            }else{
                return new Document(Map.of(
                        "result", Database.ORDER_SUCCESS,
                        "requestCode", res
                )).toJson();
            }

        }catch (Exception e){
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }


    @PostMapping(value = "/delete_order")
    String delete_order(@RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String token             = doc.getString("token");
            String code              = doc.getString("code");

            if (token == null || code == null){
                throw new Exception();
            }

            int res = CargasBackendApplication.getDatabase().deleteOrder(
                    token, code
            );

            return new Document(Map.of(
                        "result", res
                )).toJson();
        }catch (Exception e){
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }

    @PostMapping(value = "/orders")
    String getOrders(@RequestBody String body){
        try {
            Document doc = Document.parse(body);

            String token             = doc.getString("token");

            if (token == null)
                throw new Exception();

            List<String> res = CargasBackendApplication.getDatabase().getOrders(
                    token
            );

            if (res == null) {
                return new Document(Map.of(
                        "result", 1
                )).toJson();
            }else{
                return new Document(Map.of(
                        "result", 0,
                        "orders", res
                )).toJson();
            }

        }catch (Exception e){
            return new Document(Map.of(
                    "result" , -1,
                    "error" , "bad request"
            )).toJson();
        }
    }
}
