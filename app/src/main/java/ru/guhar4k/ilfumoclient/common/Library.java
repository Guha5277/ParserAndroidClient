package ru.guhar4k.ilfumoclient.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.guhar4k.ilfumoclient.product.DailyOffer;
import ru.guhar4k.ilfumoclient.product.Product;
import ru.guhar4k.ilfumoclient.product.Warehouse;

public class Library {
    public static final int ADMIN = 1;
    public static final int MODERATOR = 2;


    public static final String DELIMITER = "§";
    public static final byte AUTH = 1;
    public static final byte REQUEST = 2;
    public static final byte ACCEPTED = 3;
    public static final byte DENIED = 4;
    public static final byte MULTIPLY_SESSION = 5;
    public static final byte MESSAGE_FORMAT_ERROR = -1;
    public static final byte SERVER_INFO = 6;
//    public static final byte START_TIME = 7;
//    public static final byte PRODUCTS_COUNT = 8;
//    public static final byte WAREHOUSES_COUNT = 9;
    public static final byte USERS = 10;
    public static final byte LIST = 11;
    public static final byte COUNT = 12;
    public static final byte DISCONNECT = 13;
    public static final byte BAN = 14;
    public static final byte UPDATER = 15;
    public static final byte RESEARCHER = 16;
    public static final byte START = 17;
    public static final byte STOP = 18;
    public static final byte FOUND = 19;
    public static final byte FAILED = 20;
    public static final byte CURRENT = 21;
    public static final byte PRODUCTS_TOTAL = 22;
    public static final byte PROCESS_END = 23;
    public static final byte LAST_RUN = 24;
    public static final byte LAST_POSITION = 25;
    public static final byte EXCEPTION = 26;
    public static final byte CURRENT_CATEGORY = 27;
    public static final byte WAREHOUSE_LIST = 28;
    public static final byte WAREHOUSE_LIST_END = 29;
    public static final byte PRODUCT_REQUEST = 30;
    public static final byte PRODUCT_LIST = 31;
    public static final byte PRODUCT_LIST_START = 32;
    public static final byte PRODUCT_LIST_END = 33;
    public static final byte EMPTY = 34;
    public static final byte AUTOSTART = 35;
    public static final byte AUTOSTART_INTERVAL = 36;
    public static final byte AUTOSTART_TIME = 37;
    public static final byte IMAGE = 38;
    public static final byte FIRST_CHUNK = 39;
    public static final byte TRANSIT_CHUNK = 40;
    public static final byte LAST_CHUNK = 41;
    public static final byte NO_IMAGE = 42;
    public static final byte FULL = 43;
    public static final byte INFO = 44;
    public static final byte NEW = 45;
    public static final byte NEXT = 46;
    public static final byte REMAINS = 47;
    public static final byte SORT = 48;
    public static final byte DAILY_OFFER = 49;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String getAuthRequest(String login, String password){
        return makeJsonString(new byte[]{Library.AUTH, Library.REQUEST}, login + DELIMITER + password);
    }

    public static DataProtocol jsonToObject(String json){
        return GSON.fromJson(json, DataProtocol.class);
    }

    public static String makeJsonString(byte[] header, String... data) {
        String dataResult;

        if (data.length == 0){
            dataResult = "";
        } else {
            StringBuilder sb = new StringBuilder();
            if (data.length == 1){
                sb.append(data[0]);
            } else {
                for (int i = 0; i <data.length ; i++) {
                    sb.append(data[i]);
                    if (i == data.length - 1) break;
                    sb.append(DELIMITER);
                }
            }
            dataResult = sb.toString();
        }

        DataProtocol message;
        if (dataResult.equals("")){
            message = new DataProtocol(header);
        } else {
            message = new DataProtocol(header, dataResult);
        }
        return GSON.toJson(message);
    }

    public static String warehouseToJson(Warehouse warehouse){
        String data = GSON.toJson(warehouse);
        DataProtocol message = new DataProtocol(new byte[]{WAREHOUSE_LIST}, data);
        return GSON.toJson(message);
    }

    public static Warehouse warehouseFromJson(String data){
        return GSON.fromJson(data, Warehouse.class);
    }

    public static String productRequestToJson(ProductRequest request){
        String data = GSON.toJson(request);
        DataProtocol message = new DataProtocol(new byte[]{PRODUCT_REQUEST, NEW}, data);
        return GSON.toJson(message);

    }

    public static ProductRequest productRequestFromJson(String request){
        return GSON.fromJson(request, ProductRequest.class);
    }

    public static String productToJson(Product product){
        String data = GSON.toJson(product);
        DataProtocol message = new DataProtocol(new byte[]{PRODUCT_LIST}, data);
        return GSON.toJson(message);
    }

    public static Product productFromJson(String data){
        return GSON.fromJson(data, Product.class);
    }

    public static String dailyOfferToJson(DailyOffer dailyOffer){
        String data = GSON.toJson(dailyOffer);
        DataProtocol message = new DataProtocol(new byte[]{PRODUCT_LIST}, data);
        return GSON.toJson(message);
    }

    public static DailyOffer dailyOfferFromJson(String data){
        return GSON.fromJson(data, DailyOffer.class);
    }
}
