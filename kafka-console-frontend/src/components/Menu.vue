<template>
  <div class="menu-container">
    <nav :class="['menu', { collapsed: isCollapsed }]">
      <button @click="toggleMenu" class="toggle-button">
        <i :class="isCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
      </button>
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
  width: 10%;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background-color: #7bbbbf2b; 
  padding-top: 20px;
  color: #ffffff; /* 文字颜色改为白色 */
  text-align: center;
  transition: width 0.3s;
}

.menu.collapsed {
  width: 50px;
}

.toggle-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: transparent;
  border: none;
  color: #7d5050;
  font-size: 20px;
  cursor: pointer;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin-bottom: 10px;
  line-height: 2; /* 确保文字在中间显示 */
}

router-link {
  color: #ffffff;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

router-link:hover {
  text-decoration: underline;
}

i {
  margin-right: 10px;
}
</style>