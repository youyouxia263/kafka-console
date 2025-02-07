package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.entity.ResourceInspectBean;
import com.wubing.kafka.agent.domain.request.TopicConfigReq;
import com.wubing.kafka.agent.domain.request.TopicPropertyReq;
import com.wubing.kafka.agent.domain.request.UpdatePartitionReq;
import com.wubing.kafka.agent.domain.request.UpdateReplicaReq;
import com.wubing.kafka.agent.domain.response.GroupsInfoForTopicResponse;
import com.wubing.kafka.agent.domain.response.TopicDescribeResponse;
import com.wubing.kafka.agent.domain.response.TopicListResponse;
import com.wubing.kafka.agent.domain.response.TopicPropertyResponse;
import com.wubing.kafka.agent.utils.ResultData;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface TopicOpService {
    ResultData<String> createTopic(TopicConfigReq topicConfigReq);
    ResultData<String> deleteTopic(String topicName);
    ResultData<TopicDescribeResponse> describeTopic(String topicName);
    ResultData<TopicListResponse> listTopic(int pageIndex, int pageSize);
    ResultData<List<GroupsInfoForTopicResponse>> consumerGroupsByTopic(String topicName);
    ResultData<TopicPropertyResponse> getTopicProperty(String topicName);
    ResultData<String> updateTopicProperty(TopicPropertyReq topicPropertyReq);
    ResultData<String> updateTopicPartitions(UpdatePartitionReq updatePartitionReq);
    ResultData<String> updateTopicReplica(UpdateReplicaReq updateReplica);
    ResultData<ResourceInspectBean> deleteTopicIfLegal(String topicName);
    ResultData<String> importTopics(String batchTopicJson);
    ResultData<String> exportTopics(List<String> exportedTopicList, boolean exportAll, HttpServletResponse response);

    boolean ifAnyConsumerGroupOnlineInTopic(String topicName);
    boolean ifAnyProducerOnlineInTopic(String topicName) throws Exception;
}
