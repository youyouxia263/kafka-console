package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.KafkaOpConstants;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.OffsetResult;
import com.wubing.kafka.agent.domain.request.OffsetResetReq;
import com.wubing.kafka.agent.service.OffsetService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class OffsetServiceImpl implements OffsetService {

    @Override
    public ResultData<String> resetOffset(OffsetResetReq offsetResetReq) throws ExecutionException, InterruptedException {
        if(checkGroupIdHasMember(offsetResetReq.getGroupId())) {
            return ResultData.fail(ReturnCode.RC502.getCode(), ReturnCode.RC502.getMessage());
        }
        OffsetResult offsetResult = new OffsetResult();
        switch (offsetResetReq.getResetType()){
            case KafkaOpConstants.RESET_OFFSET_BY_TIMESTAMP:
                offsetResult = resetOffsetByTimestamp(offsetResetReq.getTopicName(), offsetResetReq.getPartition(), offsetResetReq.getGroupId(),
                        offsetResetReq.getResetTime());
                break;

            case  KafkaOpConstants.RESET_OFFSET_BY_OFFSET:
                offsetResult = resetOffsetByOffset(offsetResetReq.getTopicName(), offsetResetReq.getPartition(), offsetResetReq.getGroupId(),
                        offsetResetReq.getResetOffset());
                break;
            case KafkaOpConstants.RESET_OFFSET_TO_EARLY:
                offsetResult = resetToEndOrEarlyOffset(offsetResetReq.getTopicName(), offsetResetReq.getPartition(), offsetResetReq.getGroupId(),
                        KafkaOpConstants.RESET_OFFSET_TO_EARLY);
                break;
            case KafkaOpConstants.RESET_OFFSET_TO_END:
                offsetResult = resetToEndOrEarlyOffset(offsetResetReq.getTopicName(), offsetResetReq.getPartition(), offsetResetReq.getGroupId(),
                        KafkaOpConstants.RESET_OFFSET_TO_END);
                break;
            default:
                offsetResult.setOk(false);
                offsetResult.setErrMsg(ResponeMsg.NO_SUPPORT_RESET_TYPE);
                break;

        }

        if (offsetResult.isOk()){
            return ResultData.success(ReturnCode.RC200.getMessage());
        }else {
            return ResultData.fail(ReturnCode.RC501.getCode(), offsetResult.getErrMsg());
        }
    }

    private boolean checkGroupIdHasMember(String groupId) throws ExecutionException, InterruptedException {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        ConsumerGroupDescription groupDescription = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                .describedGroups().get(groupId).get();
        return groupDescription.state() != ConsumerGroupState.EMPTY;
    }

    private OffsetResult resetOffsetByTimestamp(String topicName, int partition, String groupId, long timestamp){
        OffsetResult offsetResult = new OffsetResult();
        KafkaConsumer<String, String> kafkaConsumer = null;
        try {
            kafkaConsumer = KafkaClientPool.createConsumerByGroupId(groupId);
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            // 获取最后位移的时间戳

            Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
            timestampsToSearch.put(new TopicPartition(topicName, partition), timestamp);
            OffsetAndTimestamp offsetAndTimestamp = kafkaConsumer.offsetsForTimes(timestampsToSearch).get(topicPartition);
            Map<TopicPartition, OffsetAndMetadata> offset = new HashMap<>();

            if (offsetAndTimestamp == null){
                log.warn("Groupid:{} topicName:{} partition:{} ,Reset timestamp{} is greater than the end offset time. Now reset to the end offset", groupId, topicName, partition, timestamp);
                long endOffset = kafkaConsumer.endOffsets(Collections.singletonList(topicPartition)).get(topicPartition);
                offsetResult.setErrMsg("Current timestamp is greater than the max offset timestamp, then reset end offset");
                offset.put(topicPartition, new OffsetAndMetadata(endOffset));
            }else {
                offset.put(topicPartition, new OffsetAndMetadata(offsetAndTimestamp.offset()));
            }

            kafkaConsumer.assign(Collections.singleton(topicPartition));
            kafkaConsumer.commitSync(offset);
        }finally {
            if (kafkaConsumer != null) {
                kafkaConsumer.close();
            }
        }


        offsetResult.setOk(true);
        return offsetResult;
    }

    private OffsetResult resetOffsetByOffset(String topicName, int partition, String groupId, long offset){
        OffsetResult offsetResult = new OffsetResult();
        KafkaConsumer<String, String> kafkaConsumer = null;
        try {

            kafkaConsumer = KafkaClientPool.createConsumerByGroupId(groupId);
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            long maxOffset = kafkaConsumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
            if (maxOffset < offset){
                log.info("maxOffset is {}, less than the offset: {}", maxOffset, offset);
                offsetResult.setOk(false);
                offsetResult.setErrMsg(ResponeMsg.NO_MESSAGE_BY_OFFSET + offset);
                return offsetResult;
            }
            long startOffset = kafkaConsumer.beginningOffsets(Collections.singletonList(topicPartition)).get(topicPartition);
            if (startOffset > offset){
                log.info("startOffset is {}, greater than the offset: {}", startOffset, offset);
                offsetResult.setOk(false);
                offsetResult.setErrMsg(ResponeMsg.START_OFFSET_GREATER_THAN_OFFSET.replace("startOffset", Long.toString(startOffset)).replace("setOffset", Long.toString(offset)));
                return offsetResult;
            }
            Map<TopicPartition, OffsetAndMetadata> tpOffset = new HashMap<>();
            tpOffset.put(topicPartition, new OffsetAndMetadata(offset));

            kafkaConsumer.assign(Collections.singleton(topicPartition));
            kafkaConsumer.commitSync(tpOffset);
        }finally {
            if (kafkaConsumer != null){
                kafkaConsumer.close();
            }
        }
        offsetResult.setOk(true);
        return offsetResult;
    }

    private OffsetResult resetToEndOrEarlyOffset(String topicName, int partition, String groupId, int endOrEarly){
        OffsetResult offsetResult = new OffsetResult();
        KafkaConsumer<String, String> kafkaConsumer = null;

        try {

            kafkaConsumer = KafkaClientPool.createConsumerByGroupId(groupId);
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            long endOrEarlyOffset;
            if (endOrEarly == KafkaOpConstants.RESET_OFFSET_TO_EARLY){
                // 重置最初位移
                endOrEarlyOffset = kafkaConsumer.beginningOffsets(Collections.singleton(topicPartition)).get(topicPartition);
            }else {
                // 重置最新位移
                endOrEarlyOffset = kafkaConsumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
            }

            Map<TopicPartition, OffsetAndMetadata> tpOffset = new HashMap<>();
            tpOffset.put(topicPartition, new OffsetAndMetadata(endOrEarlyOffset));

            kafkaConsumer.assign(Collections.singleton(topicPartition));
            kafkaConsumer.commitSync(tpOffset);
        }finally {
            if (kafkaConsumer != null){
                kafkaConsumer.close();
            }
        }
        offsetResult.setOk(true);
        return offsetResult;
    }
}
