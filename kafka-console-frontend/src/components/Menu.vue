<template>
  <div class="menu-container">
    <nav class="menu">
      <ul>
        <li><router-link to="/brokers">Brokers</router-link></li>
        <li><router-link to="/topics">Topics</router-link></li>
        <li><router-link to="/consumer-groups">Consumer Groups</router-link></li>
        <li><router-link to="/messages">Messages</router-link></li>
      </ul>
    </nav>
  </div>
</template>

<script>
import { RouterLink } from 'vue-router';

export default {
  name: 'AppMenu',
  components: {
    RouterLink
  }
};
</script>

<style scoped>
.menu-container {
  display: flex;
}

.menu {
  width: 200px;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  background-color: #007bff; /* 修改为蓝色背景 */
  padding-top: 20px;
  color: #ffffff; /* 文字颜色改为白色 */
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin-bottom: 10px;
}

router-link {
  color: #ffffff;
  text-decoration: none;
}

router-link:hover {
  text-decoration: underline;
}
</style>