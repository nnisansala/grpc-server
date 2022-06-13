package com.org.archi.grpc.billing.service.billing;

import com.org.archi.billing.grpc.services.BillingDetails;
import com.org.archi.billing.grpc.services.BillingServiceGrpc;
import com.org.archi.billing.grpc.services.Product;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


@GrpcService
public class BillingServiceImpl extends BillingServiceGrpc.BillingServiceImplBase {


    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class.getName());

    @Override
    public StreamObserver<Product> calculate(StreamObserver<BillingDetails> responseObserver) {

        StreamObserver<Product> streamObserver = new StreamObserver<Product>() {

            final ArrayList<Product> productList = new ArrayList<Product>();

            @Override
            public void onNext(Product product) {
                productList.add(product);
                logger.info("Request Received to calculate bill for :  product : {}, unit price: {}, no of items: {}", product.getProductCode(), product.getUnitPrice(), product.getNoOfItems());

            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal server error")
                        .asRuntimeException());
            }

            @Override
            public void onCompleted() {

                if (productList.size() > 0) {
                    double cartValue = 0;
                    double tax = 0;
                    for (Product product : productList) {
                        cartValue += product.getNoOfItems() * product.getUnitPrice();
                    }
                    logger.info("Amount without tax: {}", cartValue);
                    tax = (cartValue /100) * 10;
                    logger.info("Tax: {}", tax);
                    logger.info("Amount with tax: {}", cartValue + tax);
                    responseObserver
                            .onNext(BillingDetails.newBuilder()
                                    .setBillingAmount(cartValue)
                                    .setTax(tax)
                                    .setTotalAmount(cartValue + tax)
                                    .build());
                    responseObserver.onCompleted();
                } else {
                    responseObserver
                            .onError(Status.NOT_FOUND
                                    .withDescription("Sorry, Bill calculation failed").asRuntimeException());
                }


            }
        };
        return streamObserver;
    }

}
