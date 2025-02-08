<template>
  <div>
    <h1 class="title">集群broker配置</h1>
    <table class="styled-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Host</th>
          <th>Port</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="broker in brokers" :key="broker.id">
          <td>{{ broker.id }}</td>
          <td>{{ broker.name }}</td>
          <td>{{ broker.host }}</td>
          <td>{{ broker.port }}</td>
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
      brokers: []
    };
  },
  async created() {
    try {
      const response = await kafkaService.listBrokers(1, 10);
      this.brokers = response.data;
    } catch (error) {
      console.error('Failed to fetch brokers:', error);
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