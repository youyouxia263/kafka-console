<template>
  <div class="menu-container">
    <nav :class="['menu', { collapsed: isCollapsed }]">
      <div class="menu-header">
        <h1 class="menu-title" v-if="!isCollapsed">Kafka Console</h1>
        <button @click="toggleMenu" class="toggle-button">
          <i :class="isCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
        </button>
      </div>
      <ul>
        <li>
          <router-link to="/brokers">
            <i class="fas fa-server"></i>
            <span v-if="!isCollapsed">集群管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/topics">
            <i class="fas fa-list"></i>
            <span v-if="!isCollapsed">Topic 管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/consumer-groups">
            <i class="fas fa-users"></i>
            <span v-if="!isCollapsed">消费组</span>
          </router-link>
        </li>
        <li>
          <router-link to="/messages">
            <i class="fas fa-envelope"></i>
            <span v-if="!isCollapsed">消息</span>
          </router-link>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import { RouterLink } from 'vue-router';
import '@fortawesome/fontawesome-free/css/all.css';

export default {
  name: 'AppMenu',
  components: {
    RouterLink
  },
  data() {
    return {
      isCollapsed: false
    };
  },
  methods: {
    toggleMenu() {
      this.isCollapsed = !this.isCollapsed;
    }
  }
};
</script>

<style scoped>
.menu-container {
  display: flex;
}

.menu {
  width: 240px;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background-color: #2c3e50;
  color: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.menu.collapsed {
  width: 60px;
}

.menu-header {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #ffffff;
}

.toggle-button {
  background-color: transparent;
  border: none;
  color: #ffffff;
  font-size: 16px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.toggle-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

ul {
  list-style-type: none;
  padding: 16px 0;
  margin: 0;
}

li {
  margin: 4px 0;
}

li a {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.2s;
}

li a:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

li a.router-link-active {
  background-color: rgba(255, 255, 255, 0.15);
  border-left: 3px solid #42b983;
}

li i {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

li span {
  margin-left: 12px;
  font-size: 14px;
}
</style>