package com.wsm.canal.listener;

import com.alibaba.fastjson.JSONObject;
import com.wsm.canal.pojo.CanalMessage;
import com.wsm.canal.processor.RedisCommonProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CanalKafkaConsumerListener {

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @KafkaListener(topics = "canal-topic", groupId = "canal-group")
    public void listen(String message) {
        try {
            CanalMessage msg = JSONObject.parseObject(message, CanalMessage.class);
            if (msg.getTable().equals("user") && msg.getDatabase().equals("oauth")
                    && !msg.getType().equals("INSERT")) {
                List<Map<String, Object>> dataSet = msg.getData();
                for (Map data : dataSet) {
                    String id = String.valueOf(data.get("id"));
                    if (id != null) {
                        redisCommonProcessor.remove(Integer.valueOf(id) + 10000000 + "");
                    }
                }
            }
        } catch (NumberFormatException e) {
            // message 需要进一步的补偿机制 TODO
        }
    }
}
