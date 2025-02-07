<template>
    <div class="menu-container">
      <nav class="menu">
        <ul>
          <li><router-link to="/cluster-config">集群配置</router-link></li>
          <li><router-link to="/topic-management">Topic管理</router-link></li>
          <li><router-link to="/consumer-group-management">消费组管理</router-link></li>
          <li><router-link to="/message-management">消息管理</router-link></li>
        </ul>
      </nav>
      <main class="content">
        <router-view></router-view>
      </main>
    </div>
  </template>
  
  <script>
  export default {
    name: 'MainMenu'
  }
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
  
  .menu ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
  }
  
  .menu li {
    margin-bottom: 10px;
    border-bottom: 1px solid #0056b3; /* 添加分隔线 */
    padding: 5px 0; /* 增加内边距 */
  }
  
  .menu li:last-child {
    border-bottom: none; /* 最后一个菜单项不需要分隔线 */
  }
  
  .menu a {
    display: block;
    padding: 10px;
    text-decoration: none;
    color: inherit; /* 继承父元素的颜色 */
    transition: background-color 0.3s;
    border: 1px solid #007bff; /* 添加蓝色边框 */
    border-radius: 4px; /* 圆角边框 */
  }
  
  .menu a:hover {
    background-color: #0056b3; /* 悬停时的背景颜色 */
  }
  </style>