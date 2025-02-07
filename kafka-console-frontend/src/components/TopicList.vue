<template>
  <div>
    <h1>Topic List</h1>
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Partitions</th>
          <th>Replication Factor</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="topic in topics" :key="topic.name">
          <td>{{ topic.name }}</td>
          <td>{{ topic.partitions }}</td>
          <td>{{ topic.replicationFactor }}</td>
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
      topics: []
    };
  },
  async created() {
    try {
      const response = await kafkaService.listTopics();
      this.topics = response.data;
    } catch (error) {
      console.error('Failed to fetch topics:', error);
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