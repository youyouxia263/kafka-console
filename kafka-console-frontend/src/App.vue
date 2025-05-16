<template>
  <div id="app">
    <AppMenu />
    <main>
      <div class="content-area">
        <router-view></router-view>
      </div>
    </main>
    <MessageNotification ref="notification" />
  </div>
</template>

<script>
import AppMenu from './components/Menu.vue';
import MessageNotification from './components/MessageNotification.vue';

export default {
  name: 'App',
  components: {
    AppMenu,
    MessageNotification
  },
  provide() {
    return {
      showNotification: this.showNotification
    };
  },
  methods: {
    showNotification(message, type = 'info', duration = 3000) {
      this.$refs.notification.showMessage(message, type, duration);
    }
  }
};
</script>

<style>
#app {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

main {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s ease;
  width: calc(100% - 240px);
  position: relative;
}

.menu.collapsed + main {
  margin-left: 60px;
  width: calc(100% - 60px);
}

.content-area {
  padding: 24px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  overflow-x: auto;
}
</style>