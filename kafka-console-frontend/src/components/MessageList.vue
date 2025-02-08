<template>
  <div>
    <h1 class="title">消息管理</h1>
    <table class="styled-table">
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
.title {
  display: block; /* 确保显示在单独一行 */
  font-size: 16px; /* 调整字体大小 */
  margin-bottom: 20px; /* 添加下边距 */
  color: #31875c;
}

.styled-table {
  width: 100%;
  border-collapse: collapse;
  margin: 25px 0;
  font-size: 18px;
  text-align: left;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
}

.styled-table thead tr {
  background-color: #009879;
  color: #ffffff;
  text-align: left;
}

.styled-table th,
.styled-table td {
  padding: 12px 15px;
  white-space: nowrap; /* 确保表头不换行 */
}

.styled-table tbody tr {
  border-bottom: 1px solid #dddddd;
}

.styled-table tbody tr:nth-of-type(even) {
  background-color: #f3f3f3;
}

.styled-table tbody tr:last-of-type {
  border-bottom: 2px solid #009879;
}

.styled-table tbody tr.active-row {
  font-weight: bold;
  color: #009879;
}

.styled-table tbody tr:hover {
  background-color: #f1f1f1;
  cursor: pointer;
}
</style>
