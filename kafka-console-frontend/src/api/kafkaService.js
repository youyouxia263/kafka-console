import axios from 'axios'

const baseURL = 'http://localhost:8080' // 替换为你的后端服务地址

export const kafkaService = {
  // TopicOpService相关接口
  createTopic(topicConfigReq) {
    return axios.post(`${baseURL}/createTopic`, topicConfigReq)
  },
  deleteTopic(topicName) {
    return axios.delete(`${baseURL}/deleteTopic/${topicName}`)
  },
  describeTopic(topicName) {
    return axios.get(`${baseURL}/describeTopic/${topicName}`)
  },
  listTopic(pageIndex, pageSize) {
    return axios.get(`${baseURL}/listTopic`, { params: { pageIndex, pageSize } })
  },
  consumerGroupsByTopic(topicName) {
    return axios.get(`${baseURL}/consumerGroupsByTopic/${topicName}`)
  },
  getTopicProperty(topicName) {
    return axios.get(`${baseURL}/getTopicProperty/${topicName}`)
  },
  updateTopicProperty(topicPropertyReq) {
    return axios.post(`${baseURL}/updateTopicProperty`, topicPropertyReq)
  },
  updateTopicPartitions(updatePartitionReq) {
    return axios.post(`${baseURL}/updateTopicPartitions`, updatePartitionReq)
  },
  updateTopicReplica(updateReplica) {
    return axios.post(`${baseURL}/updateTopicReplica`, updateReplica)
  },
  deleteTopicIfLegal(topicName) {
    return axios.get(`${baseURL}/deleteTopicIfLegal/${topicName}`)
  },
  importTopics(batchTopicJson) {
    return axios.post(`${baseURL}/importTopics`, batchTopicJson)
  },
  exportTopics(exportedTopicList, exportAll) {
    return axios.post(`${baseURL}/exportTopics`, { exportedTopicList, exportAll })
  },
  ifAnyConsumerGroupOnlineInTopic(topicName) {
    return axios.get(`${baseURL}/ifAnyConsumerGroupOnlineInTopic/${topicName}`)
  },
  ifAnyProducerOnlineInTopic(topicName) {
    return axios.get(`${baseURL}/ifAnyProducerOnlineInTopic/${topicName}`)
  },

  // ConsumerGroupService相关接口
  deleteConsumerGroup(groupId) {
    return axios.delete(`${baseURL}/deleteConsumerGroup/${groupId}`)
  },
  describeConsumerGroup(groupId) {
    return axios.get(`${baseURL}/describeConsumerGroup/${groupId}`)
  },
  listConsumerGroup(pageIndex, pageSize) {
    return axios.get(`${baseURL}/listConsumerGroup`, { params: { pageIndex, pageSize } })
  },

  // MessageService相关接口
  listMessagesByOffset(topicName, partition, offset, pageNo, pageSize) {
    return axios.get(`${baseURL}/listMessagesByOffset`, { params: { topicName, partition, offset, pageNo, pageSize } })
  },
  listMessagesByTime(topicName, partition, beginTime, endTime, pageNo, pageSize) {
    return axios.get(`${baseURL}/listMessagesByTime`, { params: { topicName, partition, beginTime, endTime, pageNo, pageSize } })
  },
  sendMessage(sendMessageReq) {
    return axios.post(`${baseURL}/sendMessage`, sendMessageReq)
  }
}