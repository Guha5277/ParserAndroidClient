package ru.guhar4k.ilfumoclient.presenter;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ru.guhar4k.ilfumoclient.model.Model;
import ru.guhar4k.ilfumoclient.model.ModelListener;
import ru.guhar4k.ilfumoclient.product.Product;
import ru.guhar4k.ilfumoclient.product.Warehouse;
import ru.guhar4k.ilfumoclient.view.ViewListener;

public class Presenter implements ModelListener, ViewListener {
    private static final String LOG_TAG = "Presenter";
    PresenterListener.View viewListener;
    PresenterListener.Model modelListener;

    private boolean isAllProductsLoaded;
    private boolean readyToGetProduct;
    private List<Warehouse> warehouses = new ArrayList<>();
    private List<String> cities = new ArrayList<>();
    private HashMap<String, List<String>> citiesAndStores = new HashMap<>();

    public Presenter(PresenterListener.View listener) {
        viewListener = listener;
        modelListener = new Model(this);
    }

    //UI evetns
    @Override
    public void onAppReady() {
        modelListener.onUIReady();
    }

    @Override
    public void onAppClosed() {
        modelListener.onAppClosed();
    }

    @Override
    public void onAppPaused() {
        modelListener.onAppPaused();
    }

    @Override
    public void getMoreProducts() {
        if (readyToGetProduct && !isAllProductsLoaded){
            readyToGetProduct = false;
            modelListener.getMoreProducts();
        }
    }

    @Override
    public void availableProducts(boolean hasNextPage) {
        isAllProductsLoaded = !hasNextPage;
        readyToGetProduct = true;
    }

    @Override
    public List<String> getCitiesList() {
        return cities;
    }

    //Model events
    @Override
    public void onProductFound(Product product) {
        viewListener.onProductFound(product);
    }

    @Override
    public void onImageDownload(int productID, Bitmap image) {
        viewListener.onProductImageDownload(productID, image);
    }

    @Override
    public void noImageForProduct(int productID) {
        viewListener.noImageForProduct(productID);
    }

    @Override
    public void warehouseReceived(Warehouse warehouse) {
        warehouses.add(warehouse);
    }

    @Override
    public void warehouseListEnd() {
        Log.i(LOG_TAG,"Received warehouses list");
        ArrayList<Warehouse> list = new ArrayList<>(warehouses);
        int index = 0;

        cities.add("Все города");

        while (list.size() > 0) {
            int region = list.get(index).getRegion();
            String city = list.get(index).getCity();

            cities.add(city);
            Iterator<Warehouse> iterator = list.iterator();

            while (iterator.hasNext()) {
                Warehouse warehouse = iterator.next();
                if (warehouse.getRegion() == region) {
                    if (!citiesAndStores.containsKey(city)) {
                        List<String> tempList = new ArrayList<>();
                        tempList.add("Все магазины");
                        tempList.add(warehouse.getAddress());
                        citiesAndStores.put(city, tempList);
                    } else {
                        citiesAndStores.get(city).add(warehouse.getAddress());
                    }
                    iterator.remove();
                    index = 0;
                }
            }
        }

        viewListener.onWarehousesInfoReceived();
    }
}
