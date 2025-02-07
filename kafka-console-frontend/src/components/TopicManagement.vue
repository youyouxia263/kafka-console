<template>
    <div>
      <h1>Topic管理</h1>
      <button @click="listTopics">列出Topic</button>
      <ul>
        <li v-for="topic in topics" :key="topic.name">{{ topic.name }}</li>
      </ul>
    </div>
  </template>
  
  <script>
  import { kafkaService } from '../api/kafkaService'
  
  export default {
    name: 'TopicManagement',
    data() {
      return {
        topics: []
      }
    },
    methods: {
      async listTopics() {
        try {
          const response = await kafkaService.listTopic(1, 10)
          this.topics = response.data.data
        } catch (error) {
          console.error('获取Topic列表失败', error)
        }
      }
    }
  }
  </script>
  
  <style scoped>
  </style>