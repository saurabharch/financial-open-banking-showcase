package com.vmware.fraud.detection;

import com.vmware.fraud.detection.ai.FeaturesRequest;
import com.vmware.fraud.detection.ai.TransactionToFeaturesRequestConverter;
import com.vmware.fraud.detection.ai.tribuo.PredictionResponse;
import com.vmware.fraud.detection.ai.tribuo.TribuoModelClient;
import com.vmware.fraud.detection.functions.DetectFraudFunction;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.data.collections.QueueSupplier;
import nyla.solutions.core.io.IO;
import nyla.solutions.core.patterns.conversion.Converter;
import org.springframework.ai.model.ModelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tribuo.anomaly.Event;
import org.tribuo.common.libsvm.LibSVMModel;
import showcase.financial.banking.transactions.domain.Transaction;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    Converter<Transaction,FeaturesRequest> transactionToFeaturesRequestConverter()
    {
        return new TransactionToFeaturesRequestConverter();
    }


    @Bean
    Consumer<byte[]> modelConsumer(TribuoModelClient modelClient)
    {
        return new Consumer<byte[]>() {
            @Override
            public void accept(byte[] bytes) {

                try{
                    LibSVMModel<Event> model = IO.deserialize(bytes);
                    log.info("Received Model: {}",model);
                    modelClient.setModel(model);
                }
                catch(RuntimeException e)
                {
                    e.printStackTrace();
                    log.error("EXCEPTION: {}",e);
                }
            }
        };
    }

    @Bean
    ModelClient<FeaturesRequest, PredictionResponse> modelClient()
    {
        return new TribuoModelClient();
    }
}
