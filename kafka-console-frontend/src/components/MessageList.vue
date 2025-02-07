<template>
  <div>
    <h1>Message List</h1>
    <table>
      <thead>
        <tr>
          <th>Offset</th>
          <th>Key</th>
          <th>Value</th>
          <th>Timestamp</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="message in messages" :key="message.offset">
          <td>{{ message.offset }}</td>
          <td>{{ message.key }}</td>
          <td>{{ message.value }}</td>
          <td>{{ message.timestamp }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { kafkaService } from '@/api/kafkaService';

export default {
  data() {
    return {
      messages: []
    };
  },
  async created() {
    try {
      const response = await kafkaService.listMessagesByOffset('topicName', 0, 0, 1, 10);
      this.messages = response.data;
    } catch (error) {
      console.error('Failed to fetch messages:', error);
    }
  }
};
</script>

<style scoped>
table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  border: 1px solid #ddd;
  padding: 8px;
}

th {
  background-color: #f2f2f2;
}
</style>
