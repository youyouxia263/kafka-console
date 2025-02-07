<template>
  <div>
    <h1>Broker List</h1>
    <table>
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