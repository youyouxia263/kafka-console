import axios from 'axios';

const baseURL = process.env.VUE_APP_BASE_URL;

export const kafkaService = {
  // Broker相关接口
  listBrokers(pageIndex, pageSize) {
    return axios.get(`${baseURL}/broker/list`, { params: { pageIndex, pageSize } });
  },
  describeBroker(brokerId) {
    return axios.get(`${baseURL}/broker/detail`, { params: { brokerId } });
  },
  updateBrokerConfig(brokerConfigReq) {
    return axios.post(`${baseURL}/broker/update_config`, brokerConfigReq);
  },
  
  // ConsumerGroup相关接口
  listConsumerGroup(pageIndex, pageSize) {
    return axios.get(`${baseURL}/listConsumerGroup`, { params: { pageIndex, pageSize } });
  },
  describeConsumerGroup(groupId) {
    return axios.get(`${baseURL}/describeConsumerGroup`, { params: { groupId } });
  },
  deleteConsumerGroup(groupId) {
    return axios.delete(`${baseURL}/deleteConsumerGroup`, { params: { groupId } });
  },

  // MessageService相关接口
  listMessagesByOffset(topicName, partition, offset, pageNo, pageSize) {
    return axios.get(`${baseURL}/listMessagesByOffset`, { params: { topicName, partition, offset, pageNo, pageSize } });
  },
  listMessagesByTime(topicName, partition, beginTime, endTime, pageNo, pageSize) {
    return axios.get(`${baseURL}/listMessagesByTime`, { params: { topicName, partition, beginTime, endTime, pageNo, pageSize } });
  },
  sendMessage(sendMessageReq) {
    return axios.post(`${baseURL}/sendMessage`, sendMessageReq);
  },

  // Topic相关接口
  listTopics() {
    return axios.get(`${baseURL}/topics`);
  },
  createTopic(topicReq) {
    return axios.post(`${baseURL}/topics`, topicReq);
  },
  deleteTopic(topicName) {
    return axios.delete(`${baseURL}/topics/${topicName}`);
  },
  describeTopic(topicName) {
    return axios.get(`${baseURL}/topics/${topicName}`);
  },
  updateTopicConfig(topicName, configReq) {
    return axios.put(`${baseURL}/topics/${topicName}/config`, configReq);
  }
};