<template>
  <div>
    <h1>Consumer Group List</h1>
    <table>
      <thead>
        <tr>
          <th>Group ID</th>
          <th>State</th>
          <th>Members</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="group in consumerGroups" :key="group.groupId">
          <td>{{ group.groupId }}</td>
          <td>{{ group.state }}</td>
          <td>{{ group.members.length }}</td>
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
      consumerGroups: []
    };
  },
  async created() {
    try {
      const response = await kafkaService.listConsumerGroup(1, 10);
      this.consumerGroups = response.data;
    } catch (error) {
      console.error('Failed to fetch consumer groups:', error);
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