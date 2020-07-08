package ru.guhar4k.ilfumoclient.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.guhar4k.ilfumoclient.common.DataProtocol;
import ru.guhar4k.ilfumoclient.common.Library;
import ru.guhar4k.ilfumoclient.network.SocketThread;
import ru.guhar4k.ilfumoclient.network.SocketThreadListener;
import ru.guhar4k.ilfumoclient.presenter.PresenterListener;
import ru.guhar4k.ilfumoclient.product.Product;

public class Model implements PresenterListener.Model, SocketThreadListener {
    private ModelListener listener;
    private String ip = "109.111.178.130";
    private int port = 5277;
    private SocketThread socketThread;
    private ExecutorService threadPool;
    private final String LOG_TAG = "Model";
    private ImageDownloader imageDownloader;

    public Model(ModelListener listener) {
        this.listener = listener;
        imageDownloader = new ImageDownloader();
        threadPool = Executors.newFixedThreadPool(2, Thread::new);
    }

    @Override
    public void onUIReady() {
        if (socketThread != null && socketThread.isAlive()) return;
        threadPool.execute(this::connect);
    }

    @Override
    public void onAppClosed() {
        if (socketThread != null && socketThread.isAlive()) socketThread.close();
    }

    @Override
    public void onAppPaused() {

    }

    void connect() {
        try {
            Socket socket = new Socket(ip, port);
            socketThread = new SocketThread(this, "client", socket);
            Log.i(LOG_TAG, "Successful connected");
        } catch (IOException e) {
            Log.e(LOG_TAG, String.valueOf(e.getCause()));
        }
    }

    void sendProductRequest() {
        String msg = "{\n" +
                "  \"header\": [\n" +
                "    30\n" +
                "  ],\n" +
                "  \"dataLength\": 176,\n" +
                "  \"data\": \"{\\n  \\\"stock\\\": false,\\n  \\\"regionID\\\": -1,\\n  \\\"storeID\\\": -1,\\n  \\\"strengthStart\\\": -1,\\n  \\\"strengthEnd\\\": -1,\\n  \\\"volumeStart\\\": -1,\\n  \\\"volumeEnd\\\": -1,\\n  \\\"priceStart\\\": 0,\\n  \\\"priceEnd\\\": -1\\n}\"\n" +
                "} ";
        socketThread.sendMessage(msg);
    }

    void sendMessage(String msg) {
        socketThread.sendMessage(msg);
    }

    //socket events
    @Override
    public void onSocketThreadStart(SocketThread thread, Socket socket) {
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        sendProductRequest();
    }

    @Override
    public void onSocketThreadStop(SocketThread thread) {

    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMsg(thread, msg);
    }

    private void handleMsg(SocketThread thread, String msg) {
        DataProtocol receivedData = Library.jsonToObject(msg);

        byte[] header = receivedData.getHeader();
        switch (header[0]) {
            case Library.PRODUCT_REQUEST:
                if (header[1] == Library.EMPTY) {
                    Log.w(LOG_TAG, "No products found by user request");
                    //listener.onProductsNotFound();
                }
                break;
            case Library.PRODUCT_LIST:
                //products.add(Library.productFromJson(receivedData.getData()));
                Product product = Library.productFromJson(receivedData.getData());
                listener.onProductFound(product);
                getImage(product.getId());
                break;
            case Library.IMAGE:
                if (header.length > 1) {
                    switch (header[1]) {
                        case Library.EXCEPTION:
                            Log.e(LOG_TAG, "Failed to get image from the server. Product id: " + receivedData.getData());
                            break;
                        case Library.NO_IMAGE:
                            //noImageForProduct(receivedData.getData());
                            listener.noImageForProduct(Integer.parseInt(receivedData.getData()));
                            break;
                        case Library.FIRST_CHUNK:
                            imageDownloader.storeImageFirstChunk(receivedData.getData().split(Library.DELIMITER));
                            break;
                        case Library.TRANSIT_CHUNK:
                            /*TODO Check Map for contains an imageView*/ //if (!images.containsKey(Integer.valueOf(messageParts[0])));
                            imageDownloader.storeImageTransitChunk(receivedData.getData().split(Library.DELIMITER));
                            break;
                        case Library.LAST_CHUNK:
                            String[] messageParts = receivedData.getData().split(Library.DELIMITER);
                            Bitmap image = imageDownloader.storeImageLastChunk(messageParts);
                            int productID = Integer.parseInt(messageParts[0]);
                            listener.onImageDownload(productID, image);
                            break;
                        case Library.FULL:
                            String[] messageParts2 = receivedData.getData().split(Library.DELIMITER);
                            Bitmap image2 = imageDownloader.storeFullImage(messageParts2);
                            int productID2 = Integer.parseInt(messageParts2[0]);
                            listener.onImageDownload(productID2, image2);
                            break;
                    }
                }
                break;
        }
    }


    @Override
    public void onSocketThreadException(SocketThread thread, Exception e) {

    }

    //Getting an imageView from server or HW
    void getImage(int id) {
        Log.i(LOG_TAG, "Getting an image for product with id: " + id);
        socketThread.sendMessage(msgOf(header(Library.IMAGE), String.valueOf(id)));
    }

    private String msgOf(byte[] header, String... data) {
        return Library.makeJsonString(header, data);
    }

    private byte[] header(byte... header) {
        return header;
    }

}
